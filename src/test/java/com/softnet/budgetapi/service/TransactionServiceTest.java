package com.softnet.budgetapi.service;

import com.softnet.budgetapi.dto.request.TransactionCreateRequest;
import com.softnet.budgetapi.dto.response.SummaryResponse;
import com.softnet.budgetapi.dto.response.TransactionResponse;
import com.softnet.budgetapi.exception.BusinessException;
import com.softnet.budgetapi.exception.ErrorCode;
import com.softnet.budgetapi.exception.ResourceNotFoundException;
import com.softnet.budgetapi.mapper.SummaryMapper;
import com.softnet.budgetapi.mapper.TransactionMapper;
import com.softnet.budgetapi.model.Account;
import com.softnet.budgetapi.model.Transaction;
import com.softnet.budgetapi.model.TransactionType;
import com.softnet.budgetapi.repository.AccountRepository;
import com.softnet.budgetapi.domain.CategoryExpense;
import com.softnet.budgetapi.repository.TransactionRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private SummaryMapper summaryMapper;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction;
    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account("Konto Testowe");
    }

    private TransactionCreateRequest buildRequest(BigDecimal amount, TransactionType type) {
        return new TransactionCreateRequest(amount, type, "Test", "Description", 1L);
    }

    private TransactionResponse stubMapper(Transaction t) {
        TransactionResponse response = mock(TransactionResponse.class);
        when(transactionMapper.toResponse(t)).thenReturn(response);
        return response;
    }

    @Test
    public void testCreateIncomeTransaction() {
        TransactionCreateRequest request = buildRequest(new BigDecimal("1000"), TransactionType.INCOME);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionMapper.toEntity(any(TransactionCreateRequest.class), any(Account.class)))
                .thenReturn(new Transaction(request.amount(), request.type(), request.category(), request.description(), account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(mock(TransactionResponse.class));

        TransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("1000"), account.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void testCreateExpenseTransaction() {
        account.deposit(new BigDecimal("1000"));
        TransactionCreateRequest request = buildRequest(new BigDecimal("500"), TransactionType.EXPENSE);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionMapper.toEntity(any(TransactionCreateRequest.class), any(Account.class)))
                .thenReturn(new Transaction(request.amount(), request.type(), request.category(), request.description(), account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(mock(TransactionResponse.class));

        TransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("500"), account.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void testCreateTransaction_ShouldThrowResourceException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        TransactionCreateRequest request = new TransactionCreateRequest(
                new BigDecimal("500"), TransactionType.EXPENSE, "Test", "Description", 99L);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                transactionService.createTransaction(request));

        assertEquals("Account with ID: 99 does not exist", exception.getMessage());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    public void testCreateNegativeExpenseTransaction() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                transactionService.createTransaction(buildRequest(new BigDecimal("-50"), TransactionType.EXPENSE)));

        assertEquals("The withdrawal amount has to be a positive number", exception.getMessage());
        assertEquals(ErrorCode.BUSINESS_RULE_CONFLICT, exception.getErrorCode());
    }

    @Test
    public void testCreateNegativeIncomeTransaction() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                transactionService.createTransaction(buildRequest(new BigDecimal("-50"), TransactionType.INCOME)));

        assertEquals("The deposit amount has to be a positive number", exception.getMessage());
        assertEquals(ErrorCode.BUSINESS_RULE_CONFLICT, exception.getErrorCode());
    }

    @Test
    public void testCreateTransaction_TypeNull() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                transactionService.createTransaction(buildRequest(new BigDecimal("100"), null)));

        assertEquals("Transaction type invalid", exception.getMessage());
        assertEquals(ErrorCode.BUSINESS_RULE_CONFLICT, exception.getErrorCode());
    }

    @Test
    public void testGetSummary() {
        ZonedDateTime from = ZonedDateTime.now().minusDays(7);
        ZonedDateTime to = ZonedDateTime.now();
        String category = "Spożywcze";

        List<CategoryExpense> categoryExpenses = List.of(
                new CategoryExpense("Spożywcze", new BigDecimal("300")));

        when(transactionRepository.sumAmountFiltered(TransactionType.INCOME, from, to, category))
                .thenReturn(new BigDecimal("1000"));
        when(transactionRepository.sumAmountFiltered(TransactionType.EXPENSE, from, to, category))
                .thenReturn(new BigDecimal("300"));
        when(transactionRepository.sumExpensesByCategoryFiltered(from, to, category))
                .thenReturn(categoryExpenses);

        SummaryResponse expectedResponse = new SummaryResponse(new BigDecimal("1000"), new BigDecimal("300"), List.of());

        when(summaryMapper.toResponse(any())).thenReturn(expectedResponse);

        SummaryResponse result = transactionService.getSummary(from, to, category);

        assertNotNull(result);
        assertEquals(expectedResponse.totalIncome(), result.totalIncome());
        assertEquals(expectedResponse.totalExpense(), result.totalExpense());

        verify(transactionRepository).sumAmountFiltered(TransactionType.INCOME, from, to, category);
        verify(transactionRepository).sumExpensesByCategoryFiltered(from, to, category);
    }

    @Test
    public void testDeleteIncomeTransaction() {
        transaction = new Transaction(new BigDecimal("1000"), TransactionType.INCOME, "Test", "Description", account);
        account.deposit(new BigDecimal("1000"));

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        transactionService.deleteTransaction(1L);

        assertEquals(BigDecimal.ZERO, account.getBalance());
        verify(transactionRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteExpenseTransaction() {
        transaction = new Transaction(new BigDecimal("1000"), TransactionType.EXPENSE, "Test", "Description", account);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        transactionService.deleteTransaction(1L);

        assertEquals(new BigDecimal("1000"), account.getBalance());
        verify(transactionRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteTransaction_ShouldThrowResourceException() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                transactionService.deleteTransaction(99L));

        assertEquals("Transaction with ID: 99 does not exist", exception.getMessage());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        verify(transactionRepository, never()).deleteById(99L);
    }

    @Test
    public void testDeleteIncomeTransaction_ShouldThrowBusinessException() {
        transaction = new Transaction(new BigDecimal("1000"), TransactionType.INCOME, "Test", "Description", account);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                transactionService.deleteTransaction(1L));

        assertEquals("Insufficient balance for the withdrawal operation", exception.getMessage());
        assertEquals(ErrorCode.BUSINESS_RULE_CONFLICT, exception.getErrorCode());
    }

    @Test
    public void testDeleteTransaction_TypeNull() {
        transaction = new Transaction(new BigDecimal("1000"), null, "Test", "Description", account);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                transactionService.deleteTransaction(1L));

        assertEquals("Transaction type cannot be null", exception.getMessage());
        assertEquals(ErrorCode.BUSINESS_RULE_CONFLICT, exception.getErrorCode());
    }

    @Test
    public void testGetTransactionsByAccountId() {
        transaction = new Transaction(new BigDecimal("1000"), TransactionType.INCOME, "Test", "Description", account);

        Long accountId = 1L;
        when(accountRepository.existsById(accountId)).thenReturn(true);

        when(transactionRepository.findByAccountId(accountId)).thenReturn(List.of(transaction));
        when(transactionMapper.toResponse(transaction)).thenReturn(mock(TransactionResponse.class));

        List<TransactionResponse> response = transactionService.getTransactionsByAccountId(accountId);

        assertNotNull(response);
        assertEquals(1, response.size());
        verify(transactionRepository, times(1)).findByAccountId(accountId);
    }

    @Test
    public void testGetTransactionsByAccountId_NotFound(){
        when(accountRepository.existsById(99L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.getTransactionsByAccountId(99L);
        });

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        assertEquals("Account with ID: 99 does not exist", exception.getMessage());
        verify(accountRepository, times(1)).existsById(99L);
    }

    @Test
    public void testGetAllTransactions_Unfiltered() {
        transaction = new Transaction(new BigDecimal("1000"), TransactionType.INCOME, "Test", "Description", account);

        when(transactionRepository.findAll(any(Specification.class))).thenReturn(List.of(transaction));
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(mock(TransactionResponse.class));

        List<TransactionResponse> result = transactionService.getAllTransactions(null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    public void testGetAllTransactions_WithAllFilters() {
        transaction = new Transaction(new BigDecimal("1000"), TransactionType.INCOME, "Test", "Description", account);

        ZonedDateTime from = ZonedDateTime.now().minusDays(1);
        ZonedDateTime to = ZonedDateTime.now();

        when(transactionRepository.findAll(any(Specification.class))).thenReturn(List.of(transaction));
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(mock(TransactionResponse.class));

        List<TransactionResponse> result = transactionService.getAllTransactions(from, to, "Jedzenie");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    public void testGetAllTransactions_WithOnlyCategoryFilter() {
        transaction = new Transaction(new BigDecimal("1000"), TransactionType.INCOME, "Test", "Description", account);

        when(transactionRepository.findAll(any(Specification.class))).thenReturn(List.of(transaction));
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(mock(TransactionResponse.class));

        List<TransactionResponse> result = transactionService.getAllTransactions(null, null, "Test");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    public void testGetAllTransactions_WithEmptyCategory() {
        transaction = new Transaction(new BigDecimal("1000"), TransactionType.INCOME, "Test", "Description", account);

        when(transactionRepository.findAll(any(Specification.class))).thenReturn(List.of(transaction));
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(mock(TransactionResponse.class));

        List<TransactionResponse> result = transactionService.getAllTransactions(null, null, "   ");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository, times(1)).findAll(any(Specification.class));
    }
}