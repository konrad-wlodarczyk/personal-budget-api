package com.softnet.budgetapi.model;

import com.softnet.budgetapi.exception.BusinessException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.softnet.budgetapi.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class AccountTest {

    private Account account;

    @BeforeEach
    void setUp(){
        account = new Account("Konto Oszczędnościowe");
    }

    @Test
    public void testSuccessfulDeposit(){
        account.deposit(new BigDecimal("1000"));
        assertEquals(new BigDecimal("1000"), account.getBalance());
    }

    @Test
    public void testSuccessfulWithdrawal(){
        account.deposit(new BigDecimal("1000"));
        account.withdraw(new BigDecimal("500"));
        assertEquals(new BigDecimal("500"), account.getBalance());
    }

    @Test
    public void testNegativeDeposit(){
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            account.deposit(new BigDecimal("-50"));
        });

        assertEquals("The deposit amount has to be a positive number", exception.getMessage());
        assertEquals(ErrorCode.BUSINESS_RULE_CONFLICT, exception.getErrorCode());
    }

    @Test
    public void testZeroDeposit() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            account.deposit(BigDecimal.ZERO);
        });

        assertEquals("The deposit amount has to be a positive number", exception.getMessage());
        assertEquals(ErrorCode.BUSINESS_RULE_CONFLICT, exception.getErrorCode());
    }

    @Test
    public void testNegativeWithdrawal(){
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            account.withdraw(new BigDecimal("-50"));
        });

        assertEquals("The withdrawal amount has to be a positive number", exception.getMessage());
        assertEquals(ErrorCode.BUSINESS_RULE_CONFLICT, exception.getErrorCode());
    }

    @Test
    public void testZeroWithdrawal(){
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            account.withdraw(BigDecimal.ZERO);
        });

        assertEquals("The withdrawal amount has to be a positive number", exception.getMessage());
        assertEquals(ErrorCode.BUSINESS_RULE_CONFLICT, exception.getErrorCode());
    }

    @Test
    public void testInsufficientBalance(){
        account.deposit(new BigDecimal("100"));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            account.withdraw(new BigDecimal("200"));
        });

        assertEquals("Insufficient balance for the withdrawal operation", exception.getMessage());
        assertEquals(ErrorCode.BUSINESS_RULE_CONFLICT, exception.getErrorCode());
    }
}
