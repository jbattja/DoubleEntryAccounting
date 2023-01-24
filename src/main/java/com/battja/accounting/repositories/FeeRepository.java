package com.battja.accounting.repositories;

import com.battja.accounting.entities.Contract;
import com.battja.accounting.entities.Fee;
import com.battja.accounting.entities.PaymentMethod;
import com.battja.accounting.events.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeeRepository extends JpaRepository<Fee,Integer> {

    List<Fee> findFeeByContractAndEventType(Contract contract, EventType eventType);
    List<Fee> findFeeByContractAndEventTypeAndPaymentMethod(Contract contract, EventType eventType, PaymentMethod paymentMethod);
    List<Fee> findFeeByContract(Contract contract);
}
