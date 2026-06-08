package com.softnet.budgetapi.service;

import com.softnet.budgetapi.dto.request.AccountCreateRequest;
import com.softnet.budgetapi.dto.response.AccountResponse;
import com.softnet.budgetapi.exception.BusinessException;
import com.softnet.budgetapi.exception.ErrorCode;
import com.softnet.budgetapi.exception.ResourceNotFoundException;
import com.softnet.budgetapi.mapper.AccountMapper;
import com.softnet.budgetapi.model.Account;
import com.softnet.budgetapi.repository.AccountRepository;
import com.softnet.budgetapi.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private AccountCreateRequest request;
    private AccountResponse accountResponse;

    @BeforeEach
    void setUp() {
        account = new Account("Konto Testowe");
        request = new AccountCreateRequest("Konto Testowe");
        accountResponse = mock(AccountResponse.class);
    }

    @Test
    void testCreateAccount() {
        when(accountMapper.toEntity(request)).thenReturn(account);
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        AccountResponse result = accountService.createAccount(request);

        assertEquals(accountResponse, result);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testGetAccountById_ShouldReturnAccount() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        AccountResponse result = accountService.getAccountById(1L);

        assertEquals(accountResponse, result);
        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAccountById_ShouldThrowException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                accountService.getAccountById(99L));

        assertEquals("Account with ID: 99 does not exist", exception.getMessage());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testDeleteAccount() {
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.existsByAccountId(1L)).thenReturn(false);

        accountService.deleteAccount(1L);

        verify(accountRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteAccount_ShouldThrowResourceException() {
        when(accountRepository.existsById(99L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                accountService.deleteAccount(99L));

        assertEquals("Account with ID: 99 does not exist", exception.getMessage());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        verify(accountRepository, never()).deleteById(anyLong());
    }

    @Test
    void testDeleteAccount_ShouldThrowBusinessException() {
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.existsByAccountId(1L)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                accountService.deleteAccount(1L));

        assertEquals("Cannot delete account with ID: 1 because it contains transactions", exception.getMessage());
        assertEquals(ErrorCode.BUSINESS_RULE_CONFLICT, exception.getErrorCode());
        verify(accountRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGetAllAccounts_Filtered() {
        when(accountRepository.findAll(any(Specification.class))).thenReturn(List.of(account));
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        List<AccountResponse> result = accountService.getAllAccounts("konto");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(accountRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void testGetAllAccounts_Unfiltered() {
        when(accountRepository.findAll(any(Specification.class))).thenReturn(List.of(account));
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        List<AccountResponse> result = accountService.getAllAccounts(null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(accountRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void testGetAllAccounts_FilteredBlank() {
        when(accountRepository.findAll(any(Specification.class))).thenReturn(List.of(account));
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        List<AccountResponse> result = accountService.getAllAccounts("");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(accountRepository, times(1)).findAll(any(Specification.class));
    }
}