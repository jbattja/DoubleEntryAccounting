package com.battja.accounting.repositories;

import com.battja.accounting.entities.Account;
import com.battja.accounting.entities.Batch;
import com.battja.accounting.entities.Booking;
import com.battja.accounting.journals.RegisterType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch,Integer> {

    List<Batch> findByAccountAndRegisterAndStatus(Account account, RegisterType register, Batch.BatchStatus status);
    Optional<Batch> findFirstByAccountAndRegisterOrderByBatchNumberDesc(Account account, RegisterType register);

}
