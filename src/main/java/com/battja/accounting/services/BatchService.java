package com.battja.accounting.services;

import com.battja.accounting.entities.Batch;
import com.battja.accounting.entities.BatchEntry;
import com.battja.accounting.entities.Journal;
import com.battja.accounting.exceptions.BatchClosedException;
import com.battja.accounting.journals.Amount;
import com.battja.accounting.repositories.BatchEntryRepository;
import com.battja.accounting.repositories.BatchRepository;
import com.battja.accounting.entities.Booking;
import com.battja.accounting.repositories.BookingRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

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
        List<Batch> batches = batchRepository.findByAccountAndRegisterAndStatus(booking.getAccount(), booking.getRegister(), Batch.BatchStatus.AVAILABLE);
        if (batches.isEmpty()) {
            // Create a new batch if none available
            Integer batchNumber = 0;
            Batch lastBatch = batchRepository.findFirstByAccountAndRegisterOrderByBatchNumberDesc(booking.getAccount(), booking.getRegister()).orElse(null);
            if (lastBatch != null) {
                batchNumber = lastBatch.getBatchNumber();
            }
            batchNumber++;
            Batch batch = new Batch(booking.getAccount(), booking.getRegister(),batchNumber);
            return batchRepository.save(batch);
        } else {
            return batches.get(0);
        }
    }

    @Transactional
    public boolean endBatchPeriod(@NonNull Integer batchId) {
        Batch batch = batchRepository.findById(batchId).orElse(null);
        if (batch == null) {
            log.warn("No batch found with id " + batchId);
            return false;
        }
        if (batch.getEndDate() != null) {
            log.warn("Cannot end batch as it was already ended: " + batch);
            return false;
        }
        batch.setStatus(Batch.BatchStatus.ENDED);
        batch.setEndDate(new Date());
        batchRepository.save(batch);
        return true;
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
        for (Map.Entry<String,Long> mapEntry : counterPerCurrency.entrySet()) {
            if (mapEntry.getValue() != 0) {
                return false;
            }
        }
        return true;
    }

    @Transactional
    public boolean closeBatch(@NonNull Integer batchId) {
        Batch batch = batchRepository.findById(batchId).orElse(null);
        if (batch == null) {
            log.warn("No batch found with id " + batchId);
            return false;
        }
        if (!canBatchClose(batch)) {
            log.warn("Cannot close batch " + batch.getId());
            return false;
        }
        if (batch.getEndDate() == null) {
            batch.setEndDate(new Date());
        }
        batch.setCloseDate(new Date());
        batch.setStatus(Batch.BatchStatus.CLOSED);
        batchRepository.save(batch);
        return true;
    }

    @Autowired
    BatchRepository batchRepository;
    @Autowired
    BatchEntryRepository batchEntryRepository;
    @Autowired
    BookingRepository bookingRepository;

}
