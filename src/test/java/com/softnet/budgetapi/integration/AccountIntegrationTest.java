package com.softnet.budgetapi.integration;

import com.softnet.budgetapi.dto.request.AccountCreateRequest;
import com.softnet.budgetapi.dto.response.AccountResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void testAccountLifecycle() {
        AccountCreateRequest request = new AccountCreateRequest("Konto Testowe");
        ResponseEntity<AccountResponse> postResponse = testRestTemplate.postForEntity("/api/accounts", request, AccountResponse.class);
        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
        Long id = postResponse.getBody().id();
        assertNotNull(id);

        ResponseEntity<AccountResponse> getResponse = testRestTemplate.getForEntity("/api/accounts/" + id, AccountResponse.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals("Konto Testowe", getResponse.getBody().name());

        ResponseEntity<AccountResponse[]> listResponse = testRestTemplate.getForEntity("/api/accounts", AccountResponse[].class);
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertTrue(listResponse.getBody().length > 0);

        ResponseEntity<Void> deleteResponse = testRestTemplate.exchange("/api/accounts/" + id, HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<AccountResponse> getAfterDelete = testRestTemplate.getForEntity("/api/accounts/" + id, AccountResponse.class);
        assertEquals(HttpStatus.NOT_FOUND, getAfterDelete.getStatusCode());
    }
}