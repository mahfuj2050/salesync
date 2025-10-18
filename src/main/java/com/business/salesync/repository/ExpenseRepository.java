package com.business.salesync.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.business.salesync.models.Expense;


@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    // Additional query methods can be added if needed
}