package com.battja.accounting.repositories;

import com.battja.accounting.entities.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractRepository extends JpaRepository<Contract,Integer> {
    
    List<Contract> findByContractName(String contractName);
}
