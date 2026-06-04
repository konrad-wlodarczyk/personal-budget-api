package com.softnet.budgetapi.mapper;

import com.softnet.budgetapi.dto.request.TransactionCreateRequest;
import com.softnet.budgetapi.dto.response.TransactionResponse;
import com.softnet.budgetapi.model.Account;
import com.softnet.budgetapi.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionCreateRequest request, Account account){
        return new Transaction(
                request.amount(),
                request.type(),
                request.category(),
                request.description(),
                account
        );
    }

    public TransactionResponse toResponse(Transaction transaction){
        return new TransactionResponse(
            transaction.getId(),
            transaction.getAmount(),
            transaction.getType(),
            transaction.getCategory(),
            transaction.getDescription(),
            transaction.getDate(),
            transaction.getAccount().getId(),
            transaction.getAccount().getName()
        );
    }


}
