package com.battja.accounting.repositories;

import com.battja.accounting.entities.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract,Integer> {
    
    Optional<Contract> findByContractName(String contractName);
}
