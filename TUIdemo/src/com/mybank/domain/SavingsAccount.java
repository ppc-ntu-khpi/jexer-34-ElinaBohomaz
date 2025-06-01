package com.mybank.domain;

public class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount(double balance, double rate) {
        super(balance);
        this.interestRate = rate;
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }

    public double getInterestRate() {
        return interestRate;
    }
}
