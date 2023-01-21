package com.battja.accounting.repositories;

import com.battja.accounting.entities.BatchEntry;
import com.battja.accounting.entities.Transaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchEntryRepository extends JpaRepository<BatchEntry,Integer> {

    @EntityGraph(attributePaths = { "journals", "batch","transaction" })
    List<BatchEntry> findByTransaction(Transaction transaction);
    @EntityGraph(attributePaths = { "journals", "batch","transaction" })
    List<BatchEntry> findByBatchId(Integer batchId);

}
