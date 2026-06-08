
package com.softnet.budgetapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softnet.budgetapi.dto.request.TransactionCreateRequest;
import com.softnet.budgetapi.dto.response.SummaryResponse;
import com.softnet.budgetapi.dto.response.TransactionResponse;
import com.softnet.budgetapi.exception.ResourceNotFoundException;
import com.softnet.budgetapi.model.TransactionType;
import com.softnet.budgetapi.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    public void testCreateTransaction() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest(
                new BigDecimal("1000"),
                TransactionType.INCOME,
                "Test",
                "Description",
                1L
        );

        TransactionResponse response = new TransactionResponse(
                1L,
                new BigDecimal("1000"),
                TransactionType.INCOME,
                "Test",
                "Description",
                ZonedDateTime.now(),
                1L,
                "Konto Oszczędnościowe"
        );

        when(transactionService.createTransaction(any(TransactionCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.category").value("Test"))
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.accountName").value("Konto Oszczędnościowe"));
    }

    @Test
    public void testGetAllTransactions() throws Exception {
        TransactionResponse response1 = new TransactionResponse(
                1L,
                new BigDecimal("1000"),
                TransactionType.INCOME,
                "Test",
                "Description",
                ZonedDateTime.now(),
                1L,
                "Konto Oszczędnościowe"
        );

        TransactionResponse response2 = new TransactionResponse(
                2L,
                new BigDecimal("300"),
                TransactionType.EXPENSE,
                "Test",
                "Description",
                ZonedDateTime.now(),
                2L,
                "Konto Główne"
        );

        when(transactionService.getAllTransactions(null, null, null))
                .thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].id").value(1))
                .andExpect(jsonPath("[1].id").value(2))
                .andExpect(jsonPath("[0].amount").value(1000))
                .andExpect(jsonPath("[1].amount").value(300))
                .andExpect(jsonPath("[0].type").value("INCOME"))
                .andExpect(jsonPath("[1].type").value("EXPENSE"))
                .andExpect(jsonPath("[0].category").value("Test"))
                .andExpect(jsonPath("[1].category").value("Test"))
                .andExpect(jsonPath("[0].description").value("Description"))
                .andExpect(jsonPath("[1].description").value("Description"))
                .andExpect(jsonPath("[0].date").exists())
                .andExpect(jsonPath("[1].date").exists())
                .andExpect(jsonPath("[0].accountId").value(1))
                .andExpect(jsonPath("[1].accountId").value(2))
                .andExpect(jsonPath("[0].accountName").value("Konto Oszczędnościowe"))
                .andExpect(jsonPath("[1].accountName").value("Konto Główne"));
    }

    @Test
    public void testGetAllTransactions_FilteredCategory() throws Exception {
        TransactionResponse response1 = new TransactionResponse(
                1L,
                new BigDecimal("1000"),
                TransactionType.INCOME,
                "Wypłata Czerwiec",
                "Description",
                ZonedDateTime.now(),
                1L,
                "Konto Oszczędnościowe"
        );

        when(transactionService.getAllTransactions(null, null, "Czerwiec"))
                .thenReturn(List.of(response1));

        mockMvc.perform(get("/api/transactions").param("category", "Czerwiec"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("[0].id").value(1))
                .andExpect(jsonPath("[0].amount").value(1000))
                .andExpect(jsonPath("[0].type").value("INCOME"))
                .andExpect(jsonPath("[0].category").value("Wypłata Czerwiec"))
                .andExpect(jsonPath("[0].description").value("Description"))
                .andExpect(jsonPath("[0].date").exists())
                .andExpect(jsonPath("[0].accountId").value(1))
                .andExpect(jsonPath("[0].accountName").value("Konto Oszczędnościowe"));
    }

    @Test
    public void testGetAllTransactions_FilteredDate() throws Exception {
        TransactionResponse response1 = new TransactionResponse(
                1L,
                new BigDecimal("1000"),
                TransactionType.INCOME,
                "Wypłata Czerwiec",
                "Description",
                ZonedDateTime.now(),
                1L,
                "Konto Oszczędnościowe"
        );

        ZonedDateTime from = ZonedDateTime.now().minusDays(1);
        ZonedDateTime to = ZonedDateTime.now().plusDays(1);

        when(transactionService.getAllTransactions(
                any(ZonedDateTime.class),
                any(ZonedDateTime.class),
                any()))
                .thenReturn(List.of(response1));

        mockMvc.perform(get("/api/transactions")
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("[0].id").value(1))
                .andExpect(jsonPath("[0].amount").value(1000))
                .andExpect(jsonPath("[0].type").value("INCOME"))
                .andExpect(jsonPath("[0].category").value("Wypłata Czerwiec"))
                .andExpect(jsonPath("[0].description").value("Description"))
                .andExpect(jsonPath("[0].date").exists())
                .andExpect(jsonPath("[0].accountId").value(1))
                .andExpect(jsonPath("[0].accountName").value("Konto Oszczędnościowe"));
    }

    @Test
    public void testGetAllTransactions_FilteredDateEmpty() throws Exception {
        ZonedDateTime from = ZonedDateTime.now().minusDays(10);
        ZonedDateTime to = ZonedDateTime.now().plusDays(5);

        when(transactionService.getAllTransactions(
                any(ZonedDateTime.class),
                any(ZonedDateTime.class),
                any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/transactions")
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void testGetSummary() throws Exception {
        SummaryResponse response = new SummaryResponse(
                new BigDecimal("1000"),
                new BigDecimal("300"),
                Collections.emptyList()
        );

        when(transactionService.getSummary(any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/transactions/summary")
                        .param("category", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(1000))
                .andExpect(jsonPath("$.totalExpense").value(300))
                .andExpect(jsonPath("$.expensesByCategory").exists());
    }

    @Test
    public void testDeleteTransaction() throws Exception {
        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteTransaction_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Transaction with ID: 99 does not exist"))
                .when(transactionService).deleteTransaction(99L);

        mockMvc.perform(delete("/api/transactions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("Transaction with ID: 99 does not exist"));
    }
}
