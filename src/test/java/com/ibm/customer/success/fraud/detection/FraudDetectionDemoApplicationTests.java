package com.ibm.customer.success.fraud.detection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.customer.success.fraud.detection.FraudDetectionDemoApplication;
import com.ibm.customer.success.fraud.detection.model.Transaction;
import com.ibm.customer.success.fraud.detection.service.FraudDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FraudDetectionDemoApplication.class)
@AutoConfigureMockMvc
public class FraudDetectionDemoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    FraudDetectionService service;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        service.resetHistoricalTransactionsForTesting();
    }

    @Test
    void testAddHistoricalSingle() throws Exception {
        Transaction transaction = new Transaction("TX-100", 15000, "Mumbai", "Unknown Entity", "New Device", "Transfer");
        mockMvc.perform(post("/api/v1/fraud/historical")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isOk());

        List<Transaction> history = service.getHistoricalTransactions();
        assert history.size() == 1;
        assert history.get(0).getTransactionId().equals("TX-100");
    }

    @Test
    void testAddHistoricalBatch() throws Exception {
        List<Transaction> transactions = List.of(
                new Transaction("TX-101", 8000, "Paris", "Vendor A", "Laptop", "Transfer"),
                new Transaction("TX-102", 12000, "Berlin", "Vendor B", "Terminal", "Withdrawal")
        );
        mockMvc.perform(post("/api/v1/fraud/historical/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactions)))
                .andExpect(status().isOk());

        List<Transaction> history = service.getHistoricalTransactions();
        assert history.size() == 2;
        assert history.stream().anyMatch(tx -> tx.getTransactionId().equals("TX-101"));
        assert history.stream().anyMatch(tx -> tx.getTransactionId().equals("TX-102"));
    }

    @Test
    void testAnalyzeTransaction() throws Exception {
        // Add historical data
        Transaction historical = new Transaction("TX-100", 8000, "Sydney", "New Vendor Co.", "Unrecognized Device", "Transfer");
        mockMvc.perform(post("/api/v1/fraud/historical")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(historical)))
                .andExpect(status().isOk());

        // Analyze new transaction
        Transaction newTx = new Transaction("TX-NEW", 8000, "Sydney", "New Vendor Co.", "Unrecognized Device", "Transfer");
        mockMvc.perform(post("/api/v1/fraud/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTx)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newTransaction.transactionId").value("TX-NEW"))
                .andExpect(jsonPath("$.historicalCases[0].transactionId").value("TX-100"))
                .andExpect(jsonPath("$.recommendation").exists());
    }

}
