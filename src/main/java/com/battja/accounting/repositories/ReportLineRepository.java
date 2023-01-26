package com.battja.accounting.repositories;

import com.battja.accounting.entities.ReportLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportLineRepository extends JpaRepository<ReportLine,Integer> {
}
