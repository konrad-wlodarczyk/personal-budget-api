package com.softnet.budgetapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softnet.budgetapi.dto.request.AccountCreateRequest;
import com.softnet.budgetapi.dto.response.AccountResponse;
import com.softnet.budgetapi.exception.BusinessException;
import com.softnet.budgetapi.exception.ErrorCode;
import com.softnet.budgetapi.exception.ResourceNotFoundException;
import com.softnet.budgetapi.mapper.AccountMapper;
import com.softnet.budgetapi.model.Account;
import com.softnet.budgetapi.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private AccountMapper accountMapper;

    @Test
    public void testCreateAccount() throws Exception {
        AccountCreateRequest request = new AccountCreateRequest("Konto Oszczędnościowe");
        Account account = new Account("Konto Oszczędnościowe");

        AccountResponse response = new AccountResponse(1L, "Konto Oszczędnościowe", BigDecimal.ZERO);

        when(accountMapper.toEntity(any(AccountCreateRequest.class))).thenReturn(new Account("Konto Oszczędnościowe"));
        when(accountService.createAccount(any(Account.class))).thenReturn(account);
        when(accountMapper.toResponse(any(Account.class))).thenReturn(response);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Konto Oszczędnościowe"))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.balance").value("0"));
    }

    @Test
    public void testCreateAccount_ShouldThrowValidationException() throws Exception {
        AccountCreateRequest request = new AccountCreateRequest("");

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAccountById() throws Exception {
        Account account = new Account("Konto Oszczędnościowe");
        account.deposit(new BigDecimal("1000"));
        AccountResponse response = new AccountResponse(1L, "Konto Oszczędnościowe", new BigDecimal("1000"));

        when(accountService.getAccountById(1L)).thenReturn(account);
        when(accountMapper.toResponse(any(Account.class))).thenReturn(response);

        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Konto Oszczędnościowe"))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.balance").value("1000"));
    }

    @Test
    public void testGetAccountById_NotFound() throws Exception {
        when(accountService.getAccountById(99L)).thenThrow(new ResourceNotFoundException("Account with ID: 99 does not exist"));

        mockMvc.perform(get("/api/accounts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("Account with ID: 99 does not exist"));
    }

    @Test
    public void testGetAllAccounts() throws Exception {
        Account account1 = new Account("Konto Oszczędnościowe");
        Account account2 = new Account("Konto Główne");
        List<Account> accounts = List.of(account1, account2);

        AccountResponse response1 = new AccountResponse(1L, "Konto Oszczędnościowe", BigDecimal.ZERO);
        AccountResponse response2 = new AccountResponse(2L, "Konto Główne", BigDecimal.ZERO);

        when(accountService.getAllAccounts(null)).thenReturn(accounts);
        when(accountMapper.toResponse(account1)).thenReturn(response1);
        when(accountMapper.toResponse(account2)).thenReturn(response2);

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Konto Oszczędnościowe"))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].balance").value("0"))
                .andExpect(jsonPath("$[1].name").value("Konto Główne"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].balance").value("0"));
    }

    @Test
    public void testGetAllAccount_Filtered() throws Exception {
        Account account1 = new Account("Konto Oszczędnościowe");
        List<Account> accounts = List.of(account1);

        AccountResponse response1 = new AccountResponse(1L, "Konto Oszczędnościowe", BigDecimal.ZERO);

        when(accountService.getAllAccounts("Oszczędnościowe")).thenReturn(accounts);
        when(accountMapper.toResponse(account1)).thenReturn(response1);

        mockMvc.perform(get("/api/accounts").param("name", "Oszczędnościowe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Konto Oszczędnościowe"))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].balance").value("0"));
    }

    @Test
    public void testDeleteAccount() throws Exception {
        mockMvc.perform(delete("/api/accounts/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteAccount_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Account with ID: 99 does not exist"))
                .when(accountService).deleteAccount(99L);

        mockMvc.perform(delete("/api/accounts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("Account with ID: 99 does not exist"));
    }

    @Test
    public void testDeleteAccount_HasTransactions() throws Exception {
        doThrow(new BusinessException("Cannot delete account with ID: 99 because it contains transactions"))
                .when(accountService).deleteAccount(99L);

        mockMvc.perform(delete("/api/accounts/99"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_CONFLICT"))
                .andExpect(jsonPath("$.detail").value("Cannot delete account with ID: 99 because it contains transactions"));
    }

    @Test
    public void shouldHandleInternalServerError() throws Exception {
        when(accountService.getAccountById(anyLong())).thenThrow(new RuntimeException("Something Wrong"));

        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("An unexpected error occured. Please try again"))
                .andExpect(jsonPath("$.code").value(ErrorCode.INTERNAL_SERVER_ERROR.name()));
    }
}
