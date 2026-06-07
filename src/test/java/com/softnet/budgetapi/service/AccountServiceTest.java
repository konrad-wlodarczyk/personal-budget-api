package com.softnet.budgetapi.service;

import com.softnet.budgetapi.exception.BusinessException;
import com.softnet.budgetapi.exception.ErrorCode;
import com.softnet.budgetapi.exception.ResourceNotFoundException;
import com.softnet.budgetapi.model.Account;
import com.softnet.budgetapi.repository.AccountRepository;
import com.softnet.budgetapi.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;

    @BeforeEach
    void setUp(){
        account = new Account("Konto Testowe");
    }

    @Test
    public void testCreateAccount(){
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        Account result = accountService.createAccount(account);

        assertEquals("Konto Testowe", result.getName());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    public void testGetAccountById_ShouldReturnAccount(){
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        Account result = accountService.getAccountById(1L);
        assertEquals("Konto Testowe", result.getName());

        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetAccountById_ShouldThrowException(){
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            accountService.getAccountById(99L);
        });

        assertEquals("Account with ID: 99 does not exist", exception.getMessage());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    public void testDeleteAccount(){
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.existsByAccountId(1L)).thenReturn(false);

        accountService.deleteAccount(1L);

        verify(accountRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteAccount_ShouldThrowResourceException(){
        when(accountRepository.existsById(99L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            accountService.deleteAccount(99L);
        });

        assertEquals("Account with ID: 99 does not exist", exception.getMessage());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        verify(accountRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteAccount_ShouldThrowBusinessException(){
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.existsByAccountId(1L)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            accountService.deleteAccount(1L);
        });

        assertEquals("Cannot delete account with ID: 1 because it contains transactions", exception.getMessage());
        assertEquals(ErrorCode.BUSINESS_RULE_CONFLICT, exception.getErrorCode());

        verify(accountRepository, never()).deleteById(anyLong());
    }
}
