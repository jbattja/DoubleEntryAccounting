package com.battja.accounting.repositories;

import com.battja.accounting.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    List<Transaction> findByTransactionReference(String transactionReference);
    List<Transaction> findByOriginalReference(String originalReference);
    List<Transaction> findByType(Transaction.TransactionType type);
    List<Transaction> findByTransactionReferenceAndType(String transactionReference, Transaction.TransactionType type);

}
