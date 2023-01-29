package com.battja.accounting.repositories;

import com.battja.accounting.entities.PartnerReport;
import com.battja.accounting.entities.ReportLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportLineRepository extends JpaRepository<ReportLine,Integer> {

    List<ReportLine> findByPartnerReport(PartnerReport partnerReport);
}
