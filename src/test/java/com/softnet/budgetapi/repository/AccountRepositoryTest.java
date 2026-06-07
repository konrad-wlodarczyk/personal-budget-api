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
    public void testGetAllAccounts(){
        Specification<Account> spec1 = (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%konto%");
        List<Account> result1 = accountRepository.findAll(spec1);

        assertEquals(3, result1.size());

        Specification<Account> spec2 = (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%test%");
        List<Account> result2 = accountRepository.findAll(spec2);

        assertEquals(0, result2.size());

        Specification<Account> spec3 = (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%oszczędnościowe%");
        List<Account> result3 = accountRepository.findAll(spec3);

        assertEquals(1, result3.size());
    }
}
