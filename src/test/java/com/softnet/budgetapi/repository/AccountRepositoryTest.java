package com.softnet.budgetapi.repository;

import com.softnet.budgetapi.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        Account account1 = new Account("Konto Oszczędnościowe");
        Account account2 = new Account("Konto Główne");
        Account account3 = new Account("Konto Walutowe");
        account1 = entityManager.persist(account1);
        account2 = entityManager.persist(account2);
        account3 = entityManager.persist(account3);
    }

    @Test
    public void shouldReturnAllAccountsWhenMatchingKeyword() {
        Specification<Account> spec = (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%konto%");
        List<Account> result = accountRepository.findAll(spec);
        assertEquals(3, result.size());
    }

    @Test
    public void shouldReturnEmptyListWhenNoAccountMatches() {
        Specification<Account> spec = (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%test%");
        List<Account> result = accountRepository.findAll(spec);
        assertEquals(0, result.size());
    }

    @Test
    public void shouldReturnSingleAccountWhenNameIsSpecific() {
        Specification<Account> spec = (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%oszczędnościowe%");
        List<Account> result = accountRepository.findAll(spec);
        assertEquals(1, result.size());
    }
}
