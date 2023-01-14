package com.battja.accounting.services;

import com.battja.accounting.entities.Booking;
import com.battja.accounting.entities.Journal;
import com.battja.accounting.repositories.BookingRepository;
import com.battja.accounting.repositories.JournalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JournalService {

    private static final Logger log = LoggerFactory.getLogger(JournalService.class);

    public Journal getJournalWithBookings(@NonNull Integer journalId) {
        Journal journal = journalRepository.findById(journalId).orElse(null);
        List<Booking> bookings = bookingRepository.findByJournal(journal);
        journal.setBookings(bookings);
        return journal;
    }

    @Autowired
    JournalRepository journalRepository;

    @Autowired
    BookingRepository bookingRepository;

}
