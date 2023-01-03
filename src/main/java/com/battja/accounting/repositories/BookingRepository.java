package com.battja.accounting.repositories;

import com.battja.accounting.entities.Booking;
import com.battja.accounting.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking,Integer> {

    List<Booking> findByBatchId(Integer batchId);
    List<Booking> findByTransaction(Transaction transaction);
}
