package com.softnet.budgetapi.service;

import com.softnet.budgetapi.dto.request.TransactionCreateRequest;
import com.softnet.budgetapi.dto.response.SummaryResponse;
import com.softnet.budgetapi.dto.response.TransactionResponse;
import com.softnet.budgetapi.exception.BusinessException;
import com.softnet.budgetapi.exception.ResourceNotFoundException;
import com.softnet.budgetapi.mapper.SummaryMapper;
import com.softnet.budgetapi.mapper.TransactionMapper;
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

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;
    private final SummaryMapper summaryMapper;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository, TransactionMapper transactionMapper, SummaryMapper summaryMapper){
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.summaryMapper = summaryMapper;
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionCreateRequest request) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account with ID: " + request.accountId() + " does not exist"));

        if (request.type() == TransactionType.INCOME){
            account.deposit(request.amount());
        }
        else if (request.type() == TransactionType.EXPENSE){
            account.withdraw(request.amount());
        }
        else{
            throw new BusinessException("Transaction type invalid");
        }

        Transaction saved = transactionRepository.save(new Transaction(
                request.amount(), request.type(), request.category(), request.description(), account));

        return transactionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions(ZonedDateTime from, ZonedDateTime to, String category){
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

        return transactionRepository.findAll(spec).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SummaryResponse getSummary(ZonedDateTime from, ZonedDateTime to, String category) {
        BigDecimal totalIncome = transactionRepository.sumAmountFiltered(TransactionType.INCOME, from, to, category);
        BigDecimal totalExpense = transactionRepository.sumAmountFiltered(TransactionType.EXPENSE, from, to, category);
        List<CategoryExpense> categoryExpenses = transactionRepository.sumExpensesByCategoryFiltered(from, to, category);

        return summaryMapper.toResponse(new TransactionSummary(totalIncome, totalExpense, categoryExpenses));
    }

    @Transactional
    public void deleteTransaction(Long id){
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Transaction with ID: " + id + " does not exist"));

        TransactionType transactionType = transaction.getType();
        Account account = transaction.getAccount();

        if(transactionType == TransactionType.EXPENSE){
            account.deposit(transaction.getAmount());
        } else if(transactionType == TransactionType.INCOME){
            account.withdraw(transaction.getAmount());
        } else {
            throw new BusinessException("Transaction type cannot be null");
        }

        transactionRepository.deleteById(id);
    }
}
