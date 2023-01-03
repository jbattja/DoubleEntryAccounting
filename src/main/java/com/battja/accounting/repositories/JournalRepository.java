package com.battja.accounting.repositories;

import com.battja.accounting.entities.Journal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalRepository extends JpaRepository<Journal,Integer> {


}
