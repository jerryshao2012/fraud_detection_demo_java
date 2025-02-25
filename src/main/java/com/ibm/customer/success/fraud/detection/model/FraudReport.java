package com.ibm.customer.success.fraud.detection.model;

import java.util.List;

public class FraudReport {

    // ----------------------------------------------------- Instance Variables

    private Transaction newTransaction;
    private List<Transaction> historicalCases;
    private String recommendation;

    // ----------------------------------------------------- Private Methods

    // ----------------------------------------------------- Protected Methods

    // ----------------------------------------------------- Public Methods

    public FraudReport(Transaction newTransaction, List<Transaction> similar, String recommendation) {
        this.newTransaction = newTransaction;
        this.historicalCases = similar;
        this.recommendation = recommendation;
    }

    public Transaction getNewTransaction() {
        return newTransaction;
    }

    public void setNewTransaction(Transaction newTransaction) {
        this.newTransaction = newTransaction;
    }

    public List<Transaction> getHistoricalCases() {
        return historicalCases;
    }

    public void setHistoricalCases(List<Transaction> historicalCases) {
        this.historicalCases = historicalCases;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}
