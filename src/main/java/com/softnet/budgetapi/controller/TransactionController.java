package com.softnet.budgetapi.controller;

import com.softnet.budgetapi.domain.TransactionSummary;
import com.softnet.budgetapi.dto.request.TransactionCreateRequest;
import com.softnet.budgetapi.dto.response.SummaryResponse;
import com.softnet.budgetapi.dto.response.TransactionResponse;
import com.softnet.budgetapi.mapper.SummaryMapper;
import com.softnet.budgetapi.mapper.TransactionMapper;
import com.softnet.budgetapi.model.Transaction;
import com.softnet.budgetapi.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;
    private final SummaryMapper summaryMapper;

    public TransactionController(TransactionService transactionService, TransactionMapper transactionMapper, SummaryMapper summaryMapper){
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
        this.summaryMapper = summaryMapper;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionCreateRequest request){
        Transaction transaction = transactionService.createTransaction(
                request.amount(),
                request.type(),
                request.category(),
                request.description(),
                request.accountId()
        );

        TransactionResponse response = transactionMapper.toResponse(transaction);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @RequestParam(required = false) ZonedDateTime from,
            @RequestParam(required = false) ZonedDateTime to,
            @RequestParam(required = false) String category
            ) {

        List<Transaction> transactions = transactionService.getAllTransactions(from, to, category);
        List<TransactionResponse> responseList = transactions.stream()
                .map(transactionMapper::toResponse)
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/summary")
    public ResponseEntity<SummaryResponse> getSummary(
            @RequestParam(required = false) ZonedDateTime from,
            @RequestParam(required = false) ZonedDateTime to,
            @RequestParam(required = false) String category
    ) {
        TransactionSummary summary = transactionService.getSummary(from, to, category);

        SummaryResponse response = summaryMapper.toResponse(summary);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id){
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
