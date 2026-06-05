package com.softnet.budgetapi.service;

import com.softnet.budgetapi.exception.BusinessException;
import com.softnet.budgetapi.exception.ResourceNotFoundException;
import com.softnet.budgetapi.model.Account;
import com.softnet.budgetapi.repository.AccountRepository;
import com.softnet.budgetapi.repository.TransactionRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository){
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Account createAccount(Account account){
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public List<Account> getAllAccounts(String name){
        Specification<Account> spec = Specification.where(null);

        if(name != null && !name.isBlank()){
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }

        return accountRepository.findAll(spec);
    }

    @Transactional(readOnly = true)
    public Account getAccountById(Long id){
        return accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account with ID: " +
                id + " does not exist"));

    }

    @Transactional
    public void deleteAccount(Long id){
        if(!accountRepository.existsById(id)){
            throw new ResourceNotFoundException("Account with ID: " + id + " does not exist");
        }

        boolean hasTransactions = transactionRepository.existsByAccountId(id);

        if(hasTransactions){
            throw new BusinessException("Cannot delete account with ID: " + id + " because it contains transactions");
        }

        accountRepository.deleteById(id);
    }

}
