package com.softnet.budgetapi.repository;

import com.softnet.budgetapi.model.Account;
import com.softnet.budgetapi.model.Transaction;
import com.softnet.budgetapi.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    private Account account;

    @BeforeEach
    void setUp(){
        account = new Account("Konto Testowe");
        account.deposit(new BigDecimal("1000"));
        account = entityManager.persistAndFlush(account);
    }

    @Test
    void testGetAllTransactions_FilteredByCategory(){
        Transaction transaction1 = new Transaction(new BigDecimal("100"), TransactionType.EXPENSE, "Jedzenie", "Biedronka", account);
        Transaction transaction2 = new Transaction(new BigDecimal("50"), TransactionType.EXPENSE, "Jedzenie", "Kebab", account);
        Transaction transaction3 = new Transaction(new BigDecimal("200"), TransactionType.EXPENSE, "Transport", "Paliwo", account);

        entityManager.persist(transaction1);
        entityManager.persist(transaction2);
        entityManager.persist(transaction3);

        Specification<Transaction> spec = (root, query, cb) -> cb.like(cb.lower(root.get("category")), "%jedzenie%");
        List<Transaction> result = transactionRepository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Jedzenie", result.getFirst().getCategory());
        assertEquals("Jedzenie", result.get(1).getCategory());
    }

    @Test
    void testGetAllTransactions_FilteredByDay(){
        Transaction transaction1 = new Transaction(new BigDecimal("100"), TransactionType.EXPENSE, "Jedzenie", "Biedronka", account);
        Transaction transaction2 = new Transaction(new BigDecimal("50"), TransactionType.EXPENSE, "Jedzenie", "Kebab", account);

        entityManager.persist(transaction1);
        entityManager.persist(transaction2);

        ZonedDateTime from = ZonedDateTime.now().minusDays(1);
        ZonedDateTime to = ZonedDateTime.now().plusDays(1);

        Specification<Transaction> spec = (root, query, cb) -> cb.between(root.get("date"), from, to);
        List<Transaction> result = transactionRepository.findAll(spec);

        assertEquals(2, result.size());

        ZonedDateTime fromEmpty = ZonedDateTime.now().minusDays(3);
        ZonedDateTime toEmpty = ZonedDateTime.now().minusDays(2);

        Specification<Transaction> specEmpty = (root, query, cb) -> cb.between(root.get("date"), fromEmpty, toEmpty);
        List<Transaction> resultEmpty = transactionRepository.findAll(specEmpty);

        assertEquals(0, resultEmpty.size());
    }
}
