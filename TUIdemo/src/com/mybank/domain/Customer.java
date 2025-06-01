package com.mybank.domain;
import java.util.ArrayList;

public class Customer {
    private String firstName;
    private String lastName;
    private ArrayList<Account> accounts = new ArrayList<>();
    
    public Customer(String f, String l) {
        firstName = f;
        lastName = l;
    }
    
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    
    public void addAccount(Account acc) {
        accounts.add(acc);
    }
    
    public Account getAccount(int index) {
        return accounts.get(index);
    }
    
    public int getNumOfAccounts() {
        return accounts.size();
    }
    
}