package com.battja.accounting.repositories;

import com.battja.accounting.entities.PartnerReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface PartnerReportRepository extends JpaRepository<PartnerReport,Integer> {

    @EntityGraph(attributePaths = { "partner", "reportLines" })
    @Override
    @NonNull
    Optional<PartnerReport> findById(@NonNull Integer id);
}
