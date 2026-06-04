package com.softnet.budgetapi.mapper;

import com.softnet.budgetapi.dto.request.AccountCreateRequest;
import com.softnet.budgetapi.dto.response.AccountResponse;
import com.softnet.budgetapi.model.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public Account toEntity(AccountCreateRequest request){
        return new Account(request.name());
    }

    public AccountResponse toResponse(Account account){
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getBalance()
        );
    }
}
