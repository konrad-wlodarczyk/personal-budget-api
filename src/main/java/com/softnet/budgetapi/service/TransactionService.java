package com.softnet.budgetapi.service;

import com.softnet.budgetapi.model.Account;
import com.softnet.budgetapi.model.Transaction;
import com.softnet.budgetapi.domain.TransactionSummary;
import com.softnet.budgetapi.model.TransactionType;
import com.softnet.budgetapi.repository.AccountRepository;
import com.softnet.budgetapi.repository.CategoryExpense;
import com.softnet.budgetapi.repository.TransactionRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository){
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction createTransaction(BigDecimal amount, TransactionType type, String category,
                                         String description, Long accountId) {

        Account account = accountRepository.findById(accountId).orElseThrow(() -> new IllegalArgumentException("Account with ID: "
        + accountId + " does not exist"));

        if(type == TransactionType.INCOME){
            account.deposit(amount);
        } else if(type == TransactionType.EXPENSE){
            account.withdraw(amount);
        }

        return transactionRepository.save(new Transaction(amount, type, category, description, account));
    }

    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions(ZonedDateTime from, ZonedDateTime to, String category){
        Specification<Transaction> spec = Specification.where(null);

        if(from != null){
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), from));
        }
        if(to != null){
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), to));
        }
        if(category != null && !category.isBlank()){
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("category")), "%" + category.toLowerCase() + "%"));
        }

        return transactionRepository.findAll(spec);
    }

    @Transactional(readOnly = true)
    public TransactionSummary getSummary(ZonedDateTime from, ZonedDateTime to, String category) {
        BigDecimal totalIncome = transactionRepository.sumAmountFiltered(TransactionType.INCOME, from, to, category);
        BigDecimal totalExpense = transactionRepository.sumAmountFiltered(TransactionType.EXPENSE, from, to, category);
        List<CategoryExpense> categoryExpenses = transactionRepository.sumExpensesByCategoryFiltered(from, to, category);

        return new TransactionSummary(totalIncome, totalExpense, categoryExpenses);
    }

    @Transactional
    public void deleteTransaction(Long id){
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Transaction with ID " + id + " does not exist"));

        TransactionType transactionType = transaction.getType();
        Account account = transaction.getAccount();

        if(transactionType == TransactionType.EXPENSE){
            account.deposit(transaction.getAmount());
        } else if(transactionType == TransactionType.INCOME){
            account.withdraw(transaction.getAmount());
        }

        transactionRepository.deleteById(id);
    }
}
