package com.ibm.customer.success.fraud.detection.model;

public class Transaction {

    // ----------------------------------------------------- Instance Variables

    private String transactionId;
    private double amount;
    private String location;
    private String recipient;
    private String device;
    private String transactionType;

    // ----------------------------------------------------- Private Methods

    // ----------------------------------------------------- Protected Methods

    // ----------------------------------------------------- Public Methods


    public Transaction(String transactionId, double amount, String location, String recipient, String device, String transactionType) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.location = location;
        this.recipient = recipient;
        this.device = device;
        this.transactionType = transactionType;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", amount=" + amount +
                ", location='" + location + '\'' +
                ", recipient='" + recipient + '\'' +
                ", device='" + device + '\'' +
                ", transactionType='" + transactionType + '\'' +
                '}';
    }
}
