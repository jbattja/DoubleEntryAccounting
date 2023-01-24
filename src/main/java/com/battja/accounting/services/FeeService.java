package com.battja.accounting.services;

import com.battja.accounting.entities.*;
import com.battja.accounting.events.EventType;
import com.battja.accounting.exceptions.DuplicateNameException;
import com.battja.accounting.repositories.ContractRepository;
import com.battja.accounting.repositories.FeeRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FeeService {

    private static final Logger log = LoggerFactory.getLogger(FeeService.class);

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

    public List<Contract> listAllContracts() {
        return contractRepository.findAll();
    }

    public Contract getContract(@NonNull Integer id) {
        return contractRepository.findById(id).orElse(null);
    }

    public Contract getContract(@NonNull String contractName) {
        List<Contract> contractList = contractRepository.findByContractName(contractName);
        if (contractList.isEmpty()) {
            return null;
        }
        if (contractList.size() > 1) {
            log.warn("More than 1 contract with name " + contractName);
        }
        return contractList.get(0);
    }

    @Transactional
    public Contract createContract(@NonNull Contract contract) throws DuplicateNameException {
        if (contract.getContractName() == null) {
            log.warn("Tried to create a contract without specifying contract name");
            return null;
        }
        if (getContract(contract.getContractName()) != null) {
            log.warn("Contract with name " + contract.getContractName() + " already exists");
            throw new DuplicateNameException("Contract with name " + contract.getContractName() + " already exists");
        }
        if (contract.getFees() != null) {
            for (Fee fee : contract.getFees()) {
                fee.setContract(contract);
                feeRepository.save(fee);
            }
        }
        contract = contractRepository.save(contract);
        log.info("Created contract:" + contract);
        return contract;
    }

    @Transactional
    public Contract updateContract(@NonNull Contract contract) throws DuplicateNameException {
        if (contract.getContractName() == null) {
            log.warn("Tried to update an contract without specifying contract name");
            return null;
        }
        Contract existingContract = getContract(contract.getId());
        if (existingContract == null) {
            log.warn("Cannot update contract: existing contract not found");
            return null;
        }
        if (existingContract.getContractName().equals(Contract.DEFAULT_MERCHANT_CONTRACT_NAME)) {
            log.warn("Cannot update default contract name");
            throw new DuplicateNameException("Cannot update default contract name");
        }
        if (!existingContract.getContractName().equals(contract.getContractName())) {
            if (getContract(contract.getContractName()) != null) {
                log.warn("Cannot update contract: contract with name " + contract.getContractName() + " already exists");
                throw new DuplicateNameException("Contract with name " + contract.getContractName() + " already exists");
            }
        }
        existingContract.setContractName(contract.getContractName());
        contractRepository.save(existingContract);
        log.info("Contract updated:" + existingContract);
        return existingContract;
    }

    public List<Fee> getFeeLines(@NonNull Contract contract) {
        return feeRepository.findFeeByContract(contract);
    }


    public List<Fee> getFeeLines(@NonNull Contract contract, @NonNull EventType eventType) {
        return feeRepository.findFeeByContractAndEventType(contract,eventType);
    }

    public Fee getFee(@NonNull Integer id) {
        return feeRepository.findById(id).orElse(null);
    }

    @Transactional
    public Fee createFeeLine(@NonNull Fee fee) {
        if (fee.getContract() == null || fee.getContract().getId() == null) {
            log.warn("Tried to create a fee line without specifying contract");
            return null;
        }
        if (fee.getEventType() == null) {
            log.warn("Tried to create a fee without specifying an event type");
            return null;
        }
        if (fee.getFixedAmount() == 0 && fee.getBasisPoints() == 0) {
            log.warn("Tried to create a fee without specifying either fixed amount or basis points");
            return null;
        }
        if (fee.getFixedAmount() != 0 && fee.getCurrency() == null) {
            log.warn("Tried to create a fee with fixed amount but no currency");
            return null;
        }
        if (getContract(fee.getContract().getId()) == null) {
            log.warn("Cannot create fee: invalid contract");
            return null;
        }
        if (feeRepository.findFeeByContractAndEventTypeAndPaymentMethod(fee.getContract(),fee.getEventType(),
                fee.getPaymentMethod()).size() > 0) {
            log.warn("Cannot create fee, conflicting fee exists");
            return null;
        }
        fee = feeRepository.save(fee);
        log.info("Created fee line:" + fee);
        return fee;
    }

    @Transactional
    public Fee updateFeeLine(@NonNull Fee fee)  {
        if (fee.getContract() == null || fee.getContract().getId() == null) {
            log.warn("Tried to update a fee line without specifying contract");
            return null;
        }
        if (fee.getEventType() == null) {
            log.warn("Tried to update a fee without specifying an event type");
            return null;
        }
        if (fee.getFixedAmount() == 0 && fee.getBasisPoints() == 0) {
            log.warn("Tried to update a fee without specifying either fixed amount or basis points");
            return null;
        }
        if (fee.getFixedAmount() != 0 && fee.getCurrency() == null) {
            log.warn("Tried to update a fee with fixed amount but no currency");
            return null;
        }
        Fee existingFee = getFee(fee.getId());
        if (existingFee == null) {
            log.warn("Cannot update fee: existing fee not found");
            return null;
        }
        if (existingFee.getContract() == null || !fee.getContract().getId().equals(existingFee.getContract().getId())) {
            log.warn("Cannot update fee: mismatch in contracts");
            return null;
        }
        List<Fee> conflictingFees = feeRepository.findFeeByContractAndEventTypeAndPaymentMethod(fee.getContract(),
                fee.getEventType(),fee.getPaymentMethod());
        if (conflictingFees.size() > 1 || (conflictingFees.size() == 1 && !conflictingFees.get(0).getId().equals(fee.getId()))) {
            log.warn("Cannot create fee, conflicting fee exists");
            return null;
        }
        feeRepository.save(fee);
        log.info("Fee updated:" + fee);
        return fee;
    }

    @Transactional
    public boolean removeFeeLine(@NonNull Fee fee) {
        if (fee.getId() == null) {
            log.warn("Cannot remove fee: invalid id");
            return false;
        }
        if (getFee(fee.getId()) == null) {
            log.warn("Cannot remove fee: invalid id");
            return false;
        }
        feeRepository.delete(fee);
        log.info("Fee removed:" + fee);
        return true;
    }

        public Contract getDefaultMerchantContract() {
        return getContract(Contract.DEFAULT_MERCHANT_CONTRACT_NAME);
    }


    @Autowired
    ContractRepository contractRepository;
    @Autowired
    FeeRepository feeRepository;

}
