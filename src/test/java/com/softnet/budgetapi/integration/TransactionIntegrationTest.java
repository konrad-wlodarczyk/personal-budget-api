package com.softnet.budgetapi.integration;

import com.softnet.budgetapi.dto.request.AccountCreateRequest;
import com.softnet.budgetapi.dto.request.TransactionCreateRequest;
import com.softnet.budgetapi.dto.response.AccountResponse;
import com.softnet.budgetapi.dto.response.SummaryResponse;
import com.softnet.budgetapi.dto.response.TransactionResponse;
import com.softnet.budgetapi.model.TransactionType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    private Long setupAccount() {
        AccountCreateRequest request = new AccountCreateRequest("Konto Testowe");
        return testRestTemplate.postForEntity("/api/accounts", request, AccountResponse.class).getBody().id();
    }

    @Test
    public void testTransactionLifecycle() {
        Long accId = setupAccount();
        TransactionCreateRequest tRequest = new TransactionCreateRequest(
                new BigDecimal("200"), TransactionType.INCOME, "Wypłata", "Czerwiec", accId);
        ResponseEntity<TransactionResponse> postRes = testRestTemplate.postForEntity("/api/transactions", tRequest, TransactionResponse.class);
        assertEquals(HttpStatus.CREATED, postRes.getStatusCode());
        Long tId = postRes.getBody().id();

        ResponseEntity<TransactionResponse[]> getRes = testRestTemplate.getForEntity("/api/transactions", TransactionResponse[].class);
        assertEquals(1, getRes.getBody().length);

        ResponseEntity<SummaryResponse> sumRes = testRestTemplate.getForEntity("/api/transactions/summary", SummaryResponse.class);
        assertEquals(new BigDecimal("200.00"), sumRes.getBody().totalIncome());

        testRestTemplate.delete("/api/transactions/" + tId);

        ResponseEntity<TransactionResponse[]> finalRes = testRestTemplate.getForEntity("/api/transactions", TransactionResponse[].class);
        assertEquals(0, finalRes.getBody().length);
    }
}