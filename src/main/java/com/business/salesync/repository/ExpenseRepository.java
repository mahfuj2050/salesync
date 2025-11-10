package com.business.salesync.repository;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.business.salesync.models.Expense;
import com.business.salesync.models.Expense.ExpenseCategory;
import com.business.salesync.models.Expense.PaymentStatus;

/**
 * 🧾 Expense Repository
 * Handles all database operations for Expense entity
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // ========================================
    // 🔍 BASIC QUERIES
    // ========================================

    /**
     * Find expense by reference number
     */
    Optional<Expense> findByExpenseRefNo(String expenseRefNo);

    /**
     * Find last expense (for generating next ref number)
     */
    Optional<Expense> findTopByOrderByIdDesc();

    /**
     * Find expenses by vendor
     */
    List<Expense> findByVendorNameContainingIgnoreCase(String vendorName);

    /**
     * Find expenses by vendor ID
     */
    List<Expense> findByVendorId(Long vendorId);

    /**
     * Check if expense reference exists
     */
    boolean existsByExpenseRefNo(String expenseRefNo);

    // ========================================
    // 📅 DATE-BASED QUERIES
    // ========================================

    /**
     * Find expenses by date range
     */
    @Query("SELECT e FROM Expense e WHERE e.expenseDate BETWEEN :fromDate AND :toDate " +
           "AND e.deleted = false ORDER BY e.expenseDate DESC")
    List<Expense> findByExpenseDateBetween(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    /**
     * Find expenses for a specific date
     */
    List<Expense> findByExpenseDateAndDeletedFalse(LocalDate expenseDate);

    /**
     * Find expenses by month and year
     */
    @Query("SELECT e FROM Expense e WHERE " +
           "YEAR(e.expenseDate) = :year AND " +
           "MONTH(e.expenseDate) = :month AND " +
           "e.deleted = false " +
           "ORDER BY e.expenseDate DESC")
    List<Expense> findByMonthAndYear(
            @Param("year") int year,
            @Param("month") int month
    );

    /**
     * Find expenses by financial year
     */
    List<Expense> findByFinancialYearAndDeletedFalse(String financialYear);

    // ========================================
    // 📊 CATEGORY & TYPE QUERIES
    // ========================================

    /**
     * Find expenses by category
     */
    List<Expense> findByExpenseCategoryAndDeletedFalseOrderByExpenseDateDesc(
            ExpenseCategory category
    );

    /**
     * Find expenses by multiple categories
     */
    @Query("SELECT e FROM Expense e WHERE e.expenseCategory IN :categories " +
           "AND e.deleted = false ORDER BY e.expenseDate DESC")
    List<Expense> findByCategories(@Param("categories") List<ExpenseCategory> categories);

    /**
     * Find expenses by department
     */
    List<Expense> findByDepartmentAndDeletedFalseOrderByExpenseDateDesc(String department);

    /**
     * Find expenses by project
     */
    List<Expense> findByProjectIdAndDeletedFalse(Long projectId);

    // ========================================
    // 💰 PAYMENT STATUS QUERIES
    // ========================================

    /**
     * Find expenses by payment status
     */
    List<Expense> findByPaymentStatusAndDeletedFalseOrderByExpenseDateDesc(
            PaymentStatus paymentStatus
    );

    /**
     * Find pending expenses
     */
    @Query("SELECT e FROM Expense e WHERE e.paymentStatus = 'PENDING' " +
           "AND e.deleted = false ORDER BY e.expenseDate DESC")
    List<Expense> findPendingExpenses();

    /**
     * Find partially paid expenses
     */
    @Query("SELECT e FROM Expense e WHERE e.paymentStatus = 'PARTIALLY_PAID' " +
           "AND e.deleted = false ORDER BY e.expenseDate DESC")
    List<Expense> findPartiallyPaidExpenses();

    /**
     * Find overdue expenses
     */
    @Query("SELECT e FROM Expense e WHERE e.paymentStatus = 'OVERDUE' " +
           "AND e.deleted = false ORDER BY e.dueDate ASC")
    List<Expense> findOverdueExpenses();

    /**
     * Find expenses due within date range
     */
    @Query("SELECT e FROM Expense e WHERE e.dueDate BETWEEN :fromDate AND :toDate " +
           "AND e.paymentStatus != 'PAID' AND e.deleted = false " +
           "ORDER BY e.dueDate ASC")
    List<Expense> findExpensesDueWithinRange(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    // ========================================
    // 🔢 FINANCIAL CALCULATIONS
    // ========================================

    /**
     * Calculate total expenses for date range
     */
    @Query("SELECT COALESCE(SUM(e.totalAmount), 0) FROM Expense e " +
           "WHERE e.expenseDate BETWEEN :fromDate AND :toDate " +
           "AND e.deleted = false")
    BigDecimal getTotalExpensesByDateRange(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    /**
     * Calculate total expenses by category
     */
    @Query("SELECT COALESCE(SUM(e.totalAmount), 0) FROM Expense e " +
           "WHERE e.expenseCategory = :category AND e.deleted = false")
    BigDecimal getTotalExpensesByCategory(@Param("category") ExpenseCategory category);

    /**
     * Calculate total expenses by vendor
     */
    @Query("SELECT COALESCE(SUM(e.totalAmount), 0) FROM Expense e " +
           "WHERE e.vendorName = :vendorName AND e.deleted = false")
    BigDecimal getTotalExpensesByVendor(@Param("vendorName") String vendorName);

    /**
     * Calculate total pending payments
     */
    @Query("SELECT COALESCE(SUM(e.amountDue), 0) FROM Expense e " +
           "WHERE e.paymentStatus IN ('PENDING', 'PARTIALLY_PAID', 'OVERDUE') " +
           "AND e.deleted = false")
    BigDecimal getTotalPendingPayments();

    /**
     * Calculate total paid expenses
     */
    @Query("SELECT COALESCE(SUM(e.amountPaid), 0) FROM Expense e " +
           "WHERE e.deleted = false")
    BigDecimal getTotalPaidAmount();

    /**
     * Get expense summary by category
     */
    @Query("SELECT e.expenseCategory, COUNT(e), COALESCE(SUM(e.totalAmount), 0) " +
           "FROM Expense e WHERE e.deleted = false " +
           "GROUP BY e.expenseCategory")
    List<Object[]> getExpenseSummaryByCategory();

    /**
     * Get monthly expense totals for a year
     */
    @Query("SELECT MONTH(e.expenseDate), COALESCE(SUM(e.totalAmount), 0) " +
           "FROM Expense e WHERE YEAR(e.expenseDate) = :year " +
           "AND e.deleted = false " +
           "GROUP BY MONTH(e.expenseDate) " +
           "ORDER BY MONTH(e.expenseDate)")
    List<Object[]> getMonthlyExpenseTotals(@Param("year") int year);

    // ========================================
    // ✅ APPROVAL QUERIES
    // ========================================

    /**
     * Find unapproved expenses
     */
    @Query("SELECT e FROM Expense e WHERE e.isApproved = false " +
           "AND e.deleted = false ORDER BY e.expenseDate DESC")
    List<Expense> findUnapprovedExpenses();

    /**
     * Find approved expenses
     */
    @Query("SELECT e FROM Expense e WHERE e.isApproved = true " +
           "AND e.deleted = false ORDER BY e.expenseDate DESC")
    List<Expense> findApprovedExpenses();

    /**
     * Find expenses approved by user
     */
    List<Expense> findByApprovedByAndDeletedFalseOrderByApprovedAtDesc(String approvedBy);

    // ========================================
    // 🔄 RECURRING EXPENSES
    // ========================================


     //Find recurring expenses

    @Query("SELECT e FROM Expense e WHERE e.isRecurring = true " +
           "AND e.deleted = false ORDER BY e.nextOccurrenceDate ASC")
    List<Expense> findRecurringExpenses();


     //Find recurring expenses due for processing
    @Query("SELECT e FROM Expense e WHERE e.isRecurring = true " +
           "AND e.nextOccurrenceDate <= :date " +
           "AND e.deleted = false")
    List<Expense> findRecurringExpensesDueByDate(@Param("date") LocalDate date);

    // ========================================
    // 🔍 SEARCH & FILTER
    // ========================================


    //Search expenses by multiple criteria

    @Query("SELECT e FROM Expense e WHERE " +
           "(:vendorName IS NULL OR LOWER(e.vendorName) LIKE LOWER(CONCAT('%', :vendorName, '%'))) AND " +
           "(:category IS NULL OR e.expenseCategory = :category) AND " +
           "(:paymentStatus IS NULL OR e.paymentStatus = :paymentStatus) AND " +
           "(:fromDate IS NULL OR e.expenseDate >= :fromDate) AND " +
           "(:toDate IS NULL OR e.expenseDate <= :toDate) AND " +
           "e.deleted = false " +
           "ORDER BY e.expenseDate DESC")
    List<Expense> searchExpenses(
            @Param("vendorName") String vendorName,
            @Param("category") ExpenseCategory category,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );


     //Search by description or reference
    @Query("SELECT e FROM Expense e WHERE " +
           "(LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.referenceNo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.expenseRefNo) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "e.deleted = false " +
           "ORDER BY e.expenseDate DESC")
    List<Expense> searchByKeyword(@Param("keyword") String keyword);

    // ========================================
    // 📈 ANALYTICS & REPORTS
    // ========================================


     //Get top vendors by expense amount

    @Query("SELECT e.vendorName, COALESCE(SUM(e.totalAmount), 0) " +
           "FROM Expense e WHERE e.deleted = false " +
           "GROUP BY e.vendorName " +
           "ORDER BY SUM(e.totalAmount) DESC")
    List<Object[]> getTopVendorsByAmount();


     //Get expense count by category
    @Query("SELECT e.expenseCategory, COUNT(e) FROM Expense e " +
           "WHERE e.deleted = false " +
           "GROUP BY e.expenseCategory " +
           "ORDER BY COUNT(e) DESC")
    List<Object[]> getExpenseCountByCategory();


     //Get department-wise expenses
    @Query("SELECT e.department, COALESCE(SUM(e.totalAmount), 0) " +
           "FROM Expense e WHERE e.deleted = false " +
           "GROUP BY e.department " +
           "ORDER BY SUM(e.totalAmount) DESC")
    List<Object[]> getDepartmentWiseExpenses();


     //Get payment method statistics
    @Query("SELECT e.paymentMethod, COUNT(e), COALESCE(SUM(e.totalAmount), 0) " +
           "FROM Expense e WHERE e.deleted = false " +
           "GROUP BY e.paymentMethod")
    List<Object[]> getPaymentMethodStatistics();

    // ========================================
    // 🗑️ SOFT DELETE
    // ========================================


     //Soft delete expense

    @Query("UPDATE Expense e SET e.deleted = true, e.updatedAt = :now, " +
           "e.updatedBy = :updatedBy WHERE e.id = :id")
    void softDelete(
            @Param("id") Long id,
            @Param("now") LocalDateTime now,
            @Param("updatedBy") String updatedBy
    );


     //Find deleted expenses

    @Query("SELECT e FROM Expense e WHERE e.deleted = true ORDER BY e.expenseDate DESC")
    List<Expense> findDeletedExpenses();

    // ========================================
    // 📊 DASHBOARD QUERIES
    // ========================================


     //Get today's expenses
    @Query("SELECT e FROM Expense e WHERE e.expenseDate = :today " +
           "AND e.deleted = false ORDER BY e.createdAt DESC")
    List<Expense> getTodayExpenses(@Param("today") LocalDate today);


    //Get this week's expenses
    @Query("SELECT e FROM Expense e WHERE e.expenseDate BETWEEN :startOfWeek AND :endOfWeek " +
           "AND e.deleted = false ORDER BY e.expenseDate DESC")
    List<Expense> getWeekExpenses(
            @Param("startOfWeek") LocalDate startOfWeek,
            @Param("endOfWeek") LocalDate endOfWeek
    );


     //Get this month's expenses
    @Query("SELECT e FROM Expense e WHERE " +
           "YEAR(e.expenseDate) = :year AND MONTH(e.expenseDate) = :month " +
           "AND e.deleted = false ORDER BY e.expenseDate DESC")
    List<Expense> getMonthExpenses(
            @Param("year") int year,
            @Param("month") int month
    );


     //Count expenses by status
    @Query("SELECT e.paymentStatus, COUNT(e) FROM Expense e " +
           "WHERE e.deleted = false GROUP BY e.paymentStatus")
    List<Object[]> countExpensesByStatus();
    
    //Add this to your ExpenseRepository
    @Query("SELECT e FROM Expense e WHERE e.ledger.id = :ledgerId " +
           "AND e.expenseDate BETWEEN :fromDate AND :toDate " +
           "AND e.deleted = false")
    List<Expense> findByLedgerAndDateRange(
        @Param("ledgerId") Long ledgerId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );
}