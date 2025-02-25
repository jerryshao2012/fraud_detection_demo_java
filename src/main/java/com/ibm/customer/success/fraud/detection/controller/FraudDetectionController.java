package com.ibm.customer.success.fraud.detection.controller;

import com.ibm.customer.success.fraud.detection.model.FraudReport;
import com.ibm.customer.success.fraud.detection.model.Transaction;
import com.ibm.customer.success.fraud.detection.service.FraudDetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fraud")
public class FraudDetectionController {

    // ----------------------------------------------------- Instance Variables

    /**
     * Logger settings
     */
    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionController.class);

    private final FraudDetectionService service;

    // ----------------------------------------------------- Private Methods

    // ----------------------------------------------------- Protected Methods

    // ----------------------------------------------------- Public Methods

    public FraudDetectionController(FraudDetectionService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public FraudReport analyze(@RequestBody Transaction newTransaction) {
        logger.debug("newTransaction: {}", newTransaction);
        return service.analyzeTransaction(newTransaction);
    }

    @PostMapping("/historical")
    public List<Transaction> addHistorical(@RequestBody Transaction transaction) {
        logger.debug("transaction: {}", transaction);
        return service.addHistoricalTransaction(transaction);
    }

    @PostMapping("/historical/batch")
    public List<Transaction> addHistoricalBatch(@RequestBody List<Transaction> transactions) {
        logger.debug("transactions: {}", transactions);
        return service.addHistoricalTransactions(transactions);
    }
}
