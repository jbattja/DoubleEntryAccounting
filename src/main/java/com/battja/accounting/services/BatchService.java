package com.battja.accounting.services;

import com.battja.accounting.entities.*;
import com.battja.accounting.exceptions.BatchClosedException;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.repositories.BatchEntryRepository;
import com.battja.accounting.repositories.BatchRepository;
import com.battja.accounting.repositories.BookingRepository;
import com.battja.accounting.repositories.JournalRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.*;

@Service
public class BatchService {

    private static final Logger log = LoggerFactory.getLogger(BatchService.class);

    public Batch getBatch(Integer id) {
        return batchRepository.findById(id).orElse(null);
    }

    public List<Booking> getBookings(Integer batchId) {
        return bookingRepository.findByBatchId(batchId);
    }

    public List<BatchEntry> getEntries(Integer batchId) {
        return batchEntryRepository.findByBatchId(batchId);
    }

    @Transactional
    public void updateBatchEntries(Booking booking) throws BatchClosedException {
        BatchEntry entry = null;
        if (booking.getRegister().requiresEntryReconciliation()) {
            entry = findExistingBatchEntry(booking);
        }
        if (entry != null) {
            Long openAmount = entry.getOpenAmount();
            openAmount = openAmount + booking.getAmount();
            entry.setOpenAmount(openAmount);
            Set<Journal> journals = entry.getJournals();
            journals.add(booking.getJournal());
            entry.setJournals(journals);
            batchEntryRepository.save(entry);
        } else {
            Set<Journal> journals = new HashSet<>();
            journals.add(booking.getJournal());
            BatchEntry batchEntry = new BatchEntry(booking.getBatch(), journals, booking.getTransaction(), new Amount(booking.getCurrency(), booking.getAmount()));
            batchEntryRepository.save(batchEntry);
        }
    }

    private BatchEntry findExistingBatchEntry(Booking booking) throws BatchClosedException {
        List<BatchEntry> existingBatchEntries = batchEntryRepository.findByTransaction(booking.getTransaction());
        for (BatchEntry entry : existingBatchEntries) {
            if (entry.getBatch().getRegister().equals(booking.getRegister()) && entry.getBatch().getAccount().getId().equals(booking.getAccount().getId())) {
                if (entry.getBatch().getStatus().equals(Batch.BatchStatus.CLOSED)) {
                    throw new BatchClosedException("Batch already closed: " + entry.getBatch());
                }
                return entry;
            }
        }
        return null;
    }

    @Transactional
    public Batch findOrCreateAvailableBatch(Booking booking) throws BatchClosedException {
        // First check if the transaction already in a batch, this to close off an amount.
        if (booking.getRegister().requiresEntryReconciliation()) {
            BatchEntry existingBatchEntry = findExistingBatchEntry(booking);
            if (existingBatchEntry != null) {
                return existingBatchEntry.getBatch();
            }
        }
        // Find available batches
        return findOrCreateAvailableBatch(booking.getAccount(), booking.getRegister());
    }

    private Batch findOrCreateAvailableBatch(@NonNull Account account, @NonNull RegisterType registerType) {
        List<Batch> batches = batchRepository.findByAccountAndRegisterAndStatus(account, registerType, Batch.BatchStatus.AVAILABLE);
        if (!batches.isEmpty()) {
            return batches.get(0);
        }
        // No available batch, we'll create a new one
        Integer batchNumber = 0;
        Batch lastBatch = batchRepository.findFirstByAccountAndRegisterOrderByBatchNumberDesc(account, registerType).orElse(null);
        if (lastBatch != null) {
            batchNumber = lastBatch.getBatchNumber();
        }
        batchNumber++;
        Batch batch = new Batch(account, registerType,batchNumber);
        return batchRepository.save(batch);
    }

    @Transactional
    public boolean endBatchPeriod(@NonNull Integer batchId) {
        Batch batch = batchRepository.findById(batchId).orElse(null);
        if (batch == null) {
            log.warn("No batch found with id " + batchId);
            return false;
        }
        if (batch.getEndDate() == null) {
            batch.setEndDate(new Date());
        }
        batch.setStatus(Batch.BatchStatus.ENDED);
        batchRepository.save(batch);
        return true;
    }

    public boolean canBookBalanceTransfer(@NonNull Batch batch) {
        if (batch.getRegister().requiresEntryReconciliation()) {
            return false;
        }
        if (batch.getCloseDate() != null) {
            return false;
        }
        return hasOpenAmounts(batch);
    }


        public boolean canBatchClose(@NonNull Batch batch) {
        if (batch.getCloseDate() != null) {
            return false;
        }
        if (batch.getRegister().requiresEntryReconciliation()) {
            List<BatchEntry> entries = batchEntryRepository.findByBatchId(batch.getId());
            for (BatchEntry entry : entries) {
                if (entry.getOpenAmount() != 0) {
                    return false;
                }
            }
        }
        return !hasOpenAmounts(batch);
    }

    private List<Amount> getOpenAmounts(@NonNull Batch batch) {
        List<Booking> bookings = bookingRepository.findByBatchId(batch.getId());
        Map<String, Long> counterPerCurrency = new HashMap<>();
        for (Booking booking : bookings) {
            Long counter = counterPerCurrency.get(booking.getCurrency());
            if (counter == null) {
                counter = 0L;
            }
            counter = counter + booking.getAmount();
            counterPerCurrency.put(booking.getCurrency(), counter);
        }
        List<Amount> openAmounts = new ArrayList<>();
        for (Map.Entry<String,Long> mapEntry : counterPerCurrency.entrySet()) {
            if (mapEntry.getValue() != 0) {
                openAmounts.add(new Amount(mapEntry.getKey(), mapEntry.getValue()));
            }
        }
        return openAmounts;
    }

    private boolean hasOpenAmounts(@NonNull Batch batch) {
        return getOpenAmounts(batch).size() != 0;
    }

    @Transactional
    public boolean closeBatch(@NonNull Integer batchId) {
        Batch batch = batchRepository.findById(batchId).orElse(null);
        if (batch == null) {
            log.warn("No batch found with id " + batchId);
            return false;
        }
        if (canBatchClose(batch)) {
            if (batch.getEndDate() == null) {
                batch.setEndDate(new Date());
            }
            batch.setCloseDate(new Date());
            batch.setStatus(Batch.BatchStatus.CLOSED);
            batchRepository.save(batch);
            return true;
        } else {
            log.warn("Cannot close batch " + batch.getId());
            return false;
        }
    }

    @Transactional
    public boolean bookBalanceTransfer(@NonNull Integer batchId) {
        Batch batch = batchRepository.findById(batchId).orElse(null);
        if (batch == null) {
            log.warn("No batch found with id " + batchId);
            return false;
        }
        List<Amount> openAmounts = getOpenAmounts(batch);
        if (openAmounts.size() > 0) {
            if (batch.getStatus().equals(Batch.BatchStatus.AVAILABLE)) {
                endBatchPeriod(batch.getId());
            }
            try {
                Batch newBatch = findOrCreateAvailableBatch(batch.getAccount(), batch.getRegister());
                Journal journal = bookBalanceTransfer(batch, newBatch, openAmounts);
                for (Booking booking : journal.getBookings()) {
                    booking.setJournal(journal);
                    bookingRepository.save(booking);
                    log.info("Created booking " + booking);
                    updateBatchEntries(booking);
                }
                return true;
            } catch (BookingException e) {
                log.warn(e.getMessage());
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            }
        } else {
            log.warn("Cannot book balance transfer: " + batch.getId());
            return false;
        }
    }

    @Transactional
    private Journal bookBalanceTransfer(@NonNull Batch batchFrom, @NonNull Batch batchTo, @NonNull List<Amount> amounts) {
        List<Booking> bookings = new ArrayList<>();
        for (Amount amount : amounts) {
            log.info("Booking Balance transfer between " + batchFrom.getId() + " and " + batchTo.getId() + ". Amount: " + amount);
            bookings.add(new Booking(batchFrom.getAccount(),batchFrom.getRegister(),amount.getValue()*-1,amount.getCurrency(),batchFrom,null));
            bookings.add(new Booking(batchTo.getAccount(),batchTo.getRegister(),amount.getValue(),amount.getCurrency(),batchTo,null));
        }
        Journal journal = journalRepository.save(new Journal(bookings, "BalanceTransfer"));
        log.info("Created journal " + journal);
        return journal;
    }

    @Autowired
    BatchRepository batchRepository;
    @Autowired
    BatchEntryRepository batchEntryRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    JournalRepository journalRepository;

}
