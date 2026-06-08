package com.softnet.budgetapi.service;

import com.softnet.budgetapi.domain.TransactionSummary;
import com.softnet.budgetapi.exception.BusinessException;
import com.softnet.budgetapi.exception.ErrorCode;
import com.softnet.budgetapi.exception.ResourceNotFoundException;
import com.softnet.budgetapi.model.Account;
import com.softnet.budgetapi.model.Transaction;
import com.softnet.budgetapi.model.TransactionType;
import com.softnet.budgetapi.repository.AccountRepository;
import com.softnet.budgetapi.repository.CategoryExpense;
import com.softnet.budgetapi.repository.TransactionRepository;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
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

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction;
    private Account account;

    @BeforeEach
    void setUp(){ account = new Account("Konto Testowe"); }

    @Test
    public void testCreateIncomeTransaction(){
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        transaction = transactionService.createTransaction(new BigDecimal("1000"),
                TransactionType.INCOME,
                "Test",
                "Description",
                1L);

        assertNotNull(transaction);
        assertEquals(transaction.getAmount(), account.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void testCreateExpenseTransaction(){
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        account.deposit(new BigDecimal("1000"));

        transaction = transactionService.createTransaction(new BigDecimal("500"),
                TransactionType.EXPENSE,
                "Test",
                "Description",
                1L);

        assertNotNull(transaction);
        assertEquals(new BigDecimal("500"), account.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void testCreateTransaction_ShouldThrowResourceException(){
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.createTransaction(new BigDecimal("500"),
                    TransactionType.EXPENSE,
                    "Test",
                    "Description",
                    99L);
        });

        assertEquals("Account with ID: 99 does not exist", exception.getMessage());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    public void testCreateNegativeExpenseTransaction(){
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BusinessException exception = assertThrows(BusinessException.class, () ->{
            transactionService.createTransaction(new BigDecimal("-50"),
                    TransactionType.EXPENSE,
                    "Test",
                    "Description",
                    1L);
        });

        assertEquals("The withdrawal amount has to be a positive number", exception.getMessage());
        assertEquals(ErrorCode.BUSINESS_RULE_CONFLICT, exception.getErrorCode());
    }

    @Test
    public void testCreateNegativeIncomeTransaction(){
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            transactionService.createTransaction(new BigDecimal("-50"),
                    TransactionType.INCOME,
                    "Test",
                    "Description",
                    1L);
        });

        assertEquals("The deposit amount has to be a positive number", exception.getMessage());
        assertEquals(ErrorCode.BUSINESS_RULE_CONFLICT, exception.getErrorCode());
    }

    @Test
    public void testCreateTransaction_TypeNull() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            transactionService.createTransaction(new BigDecimal("100"),
                    null,
                    "Test",
                    "Description",
                    1L);

        });

        assertEquals("Transaction type cannot be null", exception.getMessage());
        assertEquals(ErrorCode.BUSINESS_RULE_CONFLICT, exception.getErrorCode());
    }

    @Test
    public void testGetSummary() {
        ZonedDateTime from = ZonedDateTime.now().minusDays(7);
        ZonedDateTime to = ZonedDateTime.now();
        String category = "Spożywcze";

        List<CategoryExpense> categoryExpenses = List.of(
                new CategoryExpense("Spożywcze", new BigDecimal("300"))
        );

        when(transactionRepository.sumAmountFiltered(TransactionType.INCOME, from, to, category))
                .thenReturn(new BigDecimal("1000"));
        when(transactionRepository.sumAmountFiltered(TransactionType.EXPENSE, from, to, category))
                .thenReturn(new BigDecimal("300"));
        when(transactionRepository.sumExpensesByCategoryFiltered(from, to, category))
                .thenReturn(categoryExpenses);


        TransactionSummary summary = transactionService.getSummary(from, to, category);

        assertNotNull(summary);
        assertEquals(new BigDecimal("1000"), summary.totalIncome());
        assertEquals(new BigDecimal("300"), summary.totalExpense());
        assertEquals(1, summary.expensesByCategory().size());
        assertEquals("Spożywcze", summary.expensesByCategory().get(0).category());

        verify(transactionRepository).sumAmountFiltered(TransactionType.INCOME, from, to, category);
        verify(transactionRepository).sumExpensesByCategoryFiltered(from, to, category);
    }

    @Test
    public void testDeleteIncomeTransaction(){
        transaction = new Transaction(new BigDecimal("1000"),
                TransactionType.INCOME,
                "Test",
                "Description",
                account);

        account.deposit(new BigDecimal("1000"));

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        transactionService.deleteTransaction(1L);

        assertEquals(BigDecimal.ZERO, account.getBalance());
        verify(transactionRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteExpenseTransaction(){
        transaction = new Transaction(new BigDecimal("1000"),
                TransactionType.EXPENSE,
                "Test",
                "Description",
                account);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        transactionService.deleteTransaction(1L);

        assertEquals(new BigDecimal("1000"), account.getBalance());
        verify(transactionRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteTransaction_ShouldThrowResourceException(){
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.deleteTransaction(99L);
        });

        assertEquals("Transaction with ID: 99 does not exist", exception.getMessage());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        verify(transactionRepository, never()).deleteById(99L);
    }

    @Test
    public void testDeleteIncomeTransaction_ShouldThrowBusinessException(){
        transaction = new Transaction(new BigDecimal("1000"),
                TransactionType.INCOME,
                "Test",
                "Description",
                account);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            transactionService.deleteTransaction(1L);
        });

        assertEquals("Insufficient balance for the withdrawal operation", exception.getMessage());
        assertEquals(ErrorCode.BUSINESS_RULE_CONFLICT, exception.getErrorCode());
    }

    @Test
    public void testDeleteTransaction_TypeNull(){
        transaction = new Transaction(new BigDecimal("1000"),
                null,
                "Test",
                "Description",
                account);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        BusinessException exception = assertThrows(BusinessException.class, () ->{
            transactionService.deleteTransaction(1L);
        });

        assertEquals("Transaction type cannot be null", exception.getMessage());
        assertEquals(ErrorCode.BUSINESS_RULE_CONFLICT, exception.getErrorCode());
    }

    @Test
    public void testGetAllTransactions_Unfiltered(){
        transaction = new Transaction(new BigDecimal("1000"),
                null,
                "Test",
                "Description",
                account);

        when(transactionRepository.findAll(any(Specification.class))).thenReturn(List.of(transaction));

        List<Transaction> result = transactionService.getAllTransactions(null, null, null);

        assertNotNull(result);
        verify(transactionRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    public void testGetAllTransactions_WithAllFilters() {
        transaction = new Transaction(new BigDecimal("1000"),
                null,
                "Test",
                "Description",
                account);

        ZonedDateTime from = ZonedDateTime.now().minusDays(1);
        ZonedDateTime to = ZonedDateTime.now();
        String category = "Jedzenie";

        when(transactionRepository.findAll(any(Specification.class))).thenReturn(List.of(transaction));

        List<Transaction> result = transactionService.getAllTransactions(from, to, category);

        assertNotNull(result);
        verify(transactionRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    public void testGetAllTransactions_WithOnlyCategoryFilter() {
        transaction = new Transaction(new BigDecimal("1000"),
                null,
                "Test",
                "Description",
                account);

        when(transactionRepository.findAll(any(Specification.class))).thenReturn(List.of(transaction));

        List<Transaction> result = transactionService.getAllTransactions(null, null, "Test");

        assertNotNull(result);
        verify(transactionRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    public void testGetAllTransactions_WithEmptyCategory() {
        transaction = new Transaction(new BigDecimal("1000"),
                null,
                "Test",
                "Description",
                account);

        when(transactionRepository.findAll(any(Specification.class))).thenReturn(List.of(transaction));

        List<Transaction> result = transactionService.getAllTransactions(null, null, "   ");

        assertNotNull(result);
        verify(transactionRepository, times(1)).findAll(any(Specification.class));
    }
}
