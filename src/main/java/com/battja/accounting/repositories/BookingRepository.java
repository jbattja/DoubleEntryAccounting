package com.battja.accounting.repositories;

import com.battja.accounting.entities.Booking;
import com.battja.accounting.entities.Journal;
import com.battja.accounting.entities.ReportLine;
import com.battja.accounting.entities.Transaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking,Integer> {

    List<Booking> findByBatchId(Integer batchId);
    List<Booking> findByTransaction(Transaction transaction);
    List<Booking> findByTransactionIn(Collection<Transaction> transactions);
    @EntityGraph(attributePaths = { "journal","batch","account","transaction","reportLine"})
    List<Booking> findByJournal(Journal journal);
    List<Booking> findByReportLine(ReportLine reportLine);

}
