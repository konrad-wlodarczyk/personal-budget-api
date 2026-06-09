package com.softnet.budgetapi.controller;

import com.softnet.budgetapi.dto.request.AccountCreateRequest;
import com.softnet.budgetapi.dto.response.AccountResponse;
import com.softnet.budgetapi.dto.response.TransactionResponse;
import com.softnet.budgetapi.service.AccountService;
import com.softnet.budgetapi.service.CsvExportService;
import com.softnet.budgetapi.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Validated
public class AccountController {
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final CsvExportService csvExportService;

    public AccountController(AccountService accountService, TransactionService transactionService, CsvExportService csvExportService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.csvExportService = csvExportService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(request));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts(
            @RequestParam(required = false) String name
    ){
        return ResponseEntity.ok(accountService.getAllAccounts(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id){
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id){
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{id}/transactions/export")
    public ResponseEntity<String> exportAccountTransactions(@PathVariable Long id){
        List<TransactionResponse> transactions = transactionService.getTransactionsByAccountId(id);
        String csvData = csvExportService.exportToCsv(transactions);
        String filename = "account_" + id + "_transactions.csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(csvData);
    }
}
