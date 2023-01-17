package com.battja.accounting.services;

import com.battja.accounting.entities.*;
import com.battja.accounting.events.EventType;
import com.battja.accounting.repositories.ContractRepository;
import com.battja.accounting.repositories.FeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FeeService {

    public static List<Amount> calculateFee(Fee fee, Amount amount) {
        List<Amount> amounts = new ArrayList<>();
        if (fee.getFixedAmount() != 0 && fee.getCurrency() != null) {
            amounts.add(new Amount(fee.getCurrency(),fee.getFixedAmount()));
        }
        if (fee.getBasisPoints() != 0) {
            amounts.add(new Amount(amount.getCurrency(),amount.getValue()*fee.getBasisPoints()/10000));
        }
        return amounts;
    }

    public Contract storeContract(Contract contract) {
        for (Fee fee : contract.getFees()) {
            fee.setContract(contract);
            feeRepository.save(fee);
        }
        return contractRepository.save(contract);
    }

    public List<Fee> getFeeLines(Contract contract, EventType eventType) {
        if (contract == null || eventType == null) {
            return new ArrayList<>();
        }
        return feeRepository.findFeeByContractAndEventType(contract,eventType);
    }

    public Contract getDefaultMerchantContract() {
        return contractRepository.findByContractName(Contract.DEFAULT_MERCHANT_CONTRACT_NAME).orElse(null);
    }


    @Autowired
    ContractRepository contractRepository;
    @Autowired
    FeeRepository feeRepository;

}
