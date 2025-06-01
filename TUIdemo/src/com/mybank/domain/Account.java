package com.mybank.domain;

public abstract class Account {
    protected double balance;

    public Account(double initBalance) {
        balance = initBalance;
    }

    public double getBalance() {
        return balance;
    }

    public abstract String getAccountType();
}
