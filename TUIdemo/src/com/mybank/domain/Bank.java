package com.mybank.domain;

import java.util.ArrayList;

public class Bank {
    private static ArrayList<Customer> customers = new ArrayList<>();

    public static void addCustomer(Customer c) {
        customers.add(c);
    }

    public static Customer getCustomer(int index) {
        return customers.get(index);
    }

    public static int getNumOfCustomers() {
        return customers.size();
    }

    public static void clear() {
        customers.clear();
    }
}
