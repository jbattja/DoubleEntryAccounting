package com.battja.accounting.repositories;

import com.battja.accounting.entities.BatchEntry;
import com.battja.accounting.entities.ReportLine;
import com.battja.accounting.entities.Transaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface BatchEntryRepository extends JpaRepository<BatchEntry,Integer> {

    @EntityGraph(attributePaths = { "journals", "batch","transaction" })
    List<BatchEntry> findByTransaction(Transaction transaction);
    @EntityGraph(attributePaths = { "journals", "batch","transaction"})
    List<BatchEntry> findByReportLine(ReportLine reportLine);
    @EntityGraph(attributePaths = { "batch","batch.account","transaction","transaction.partnerAccount","transaction.partnerAccount.parent" })
    List<BatchEntry> findByTransactionIn(Collection<Transaction> transactions);
    @EntityGraph(attributePaths = { "journals", "batch","transaction" })
    List<BatchEntry> findByBatchId(Integer batchId);

}
