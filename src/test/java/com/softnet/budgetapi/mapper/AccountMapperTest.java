package com.softnet.budgetapi.mapper;

import com.softnet.budgetapi.dto.request.AccountCreateRequest;
import com.softnet.budgetapi.dto.response.AccountResponse;
import com.softnet.budgetapi.model.Account;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountMapperTest {

    private final AccountMapper accountMapper = new AccountMapper();

    @Test
    public void testRequestToEntity() {
        AccountCreateRequest request = new AccountCreateRequest("Konto Oszczędnościowe");
        Account account = new Account("Konto Oszczędnościowe");

        Account test = accountMapper.toEntity(request);

        assertEquals(account.getName(), test.getName());
        assertEquals(account.getId(), test.getId());
    }

    @Test
    public void testEntityToResponse() {
        AccountResponse response = new AccountResponse(1L, "Konto Oszczędnościowe", BigDecimal.ZERO);
        Account account = new Account("Konto Oszczędnościowe");
        ReflectionTestUtils.setField(account, "id", 1L);
        AccountResponse test = accountMapper.toResponse(account);

        assertEquals(response.id(), test.id());
        assertEquals(response.name(), test.name());
        assertEquals(response.balance(), test.balance());
    }

}
