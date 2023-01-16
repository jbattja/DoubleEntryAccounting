package com.battja.accounting.services;

import com.battja.accounting.entities.AdditionalInfo;
import com.battja.accounting.entities.Booking;
import com.battja.accounting.entities.Journal;
import com.battja.accounting.entities.Transaction;
import com.battja.accounting.events.BookingEvent;
import com.battja.accounting.events.EventType;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.repositories.BookingRepository;
import com.battja.accounting.repositories.JournalRepository;
import com.battja.accounting.repositories.TransactionRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.HashSet;
import java.util.Set;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    @Transactional
    public Journal book(@NonNull Transaction transaction, @NonNull EventType event) {
        Set<Transaction> transactions = new HashSet<>();
        transactions.add(transaction);
        return book(transactions,event,null);
    }

    @Transactional
    public Journal book(@NonNull Set<Transaction> transactions, @NonNull EventType event) {
        return book(transactions,event,null);
    }

    @Transactional
    public Journal book(@NonNull Set<Transaction> transactions, @NonNull EventType event, AdditionalInfo additionalInfo) {
        BookingEvent bookingEvent;
        try {
            bookingEvent = event.getEventClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.warn(e.getMessage());
            return null;
        }
        try {
            return bookInternal(transactions,bookingEvent, additionalInfo);
        } catch (BookingException e) {
            log.error(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
    }

    private Journal bookInternal(@NonNull Set<Transaction> transactions, @NonNull BookingEvent event, AdditionalInfo additionalInfo) throws BookingException {
        event.book(transactions,additionalInfo);
        for (Booking booking : event.getBookings()) { // set batches
            booking.setBatch(batchService.findOrCreateAvailableBatch(booking));
        }
        Journal journal = journalRepository.save(new Journal(event.getBookings(), event.getEventTypeName()));
        log.info("Created journal " + journal);
        for (Booking booking : event.getBookings()) {
            booking.setJournal(journal);
            bookingRepository.save(booking);
            log.info("Created booking " + booking);
            batchService.updateBatchEntries(booking);
        }
        for (Transaction t : transactions) {
            transactionRepository.save(t);
        }
        return journal;
    }

    @Autowired
    BatchService batchService;
    @Autowired
    JournalRepository journalRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    TransactionRepository transactionRepository;


}
