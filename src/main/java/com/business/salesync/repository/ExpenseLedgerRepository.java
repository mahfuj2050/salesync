package com.business.salesync.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.business.salesync.models.ExpenseLedger;

@Repository
public interface ExpenseLedgerRepository extends JpaRepository<ExpenseLedger, Long> {

    Optional<ExpenseLedger> findByLedgerCode(String ledgerCode);
    
    Optional<ExpenseLedger> findByLedgerName(String ledgerName);
    
    List<ExpenseLedger> findByLedgerCategory(ExpenseLedger.LedgerCategory category);
    
    List<ExpenseLedger> findByIsActiveTrue();
    
    @Query("SELECT el FROM ExpenseLedger el WHERE el.deleted = false ORDER BY el.displayOrder ASC, el.ledgerName ASC")
    List<ExpenseLedger> findAllActive();
    
    boolean existsByLedgerCode(String ledgerCode);
    
    boolean existsByLedgerName(String ledgerName);
}