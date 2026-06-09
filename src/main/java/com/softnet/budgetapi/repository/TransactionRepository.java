package com.softnet.budgetapi.repository;

import com.softnet.budgetapi.domain.CategoryExpense;
import com.softnet.budgetapi.model.Transaction;
import com.softnet.budgetapi.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    boolean existsByAccountId(long accountId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE " +
            "t.type = :type AND " +
            "(CAST(:from as String) IS NULL OR t.date >= :from) AND " +
            "(CAST(:to as String) IS NULL OR t.date <= :to) AND " +
            "(:category IS NULL OR t.category = :category)")
    BigDecimal sumAmountFiltered(
            @Param("type") TransactionType type,
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to,
            @Param("category") String category
    );

    @Query("""
            SELECT new com.softnet.budgetapi.domain.CategoryExpense(
                t.category,
                SUM(t.amount)
            )
            FROM Transaction t
            WHERE t.type = com.softnet.budgetapi.model.TransactionType.EXPENSE
            AND (CAST(:from as String) IS NULL OR t.date >= :from)
            AND (CAST(:to as String) IS NULL OR t.date <= :to)
            AND (:category IS NULL OR t.category = :category)
            GROUP BY t.category
        """)
    List<CategoryExpense> sumExpensesByCategoryFiltered(
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to,
            @Param("category") String category
    );

    List<Transaction> findByAccountId(Long accountId);
}