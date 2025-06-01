package com.mybank.tui;

import jexer.TAction;
import jexer.TApplication;
import jexer.TField;
import jexer.TText;
import jexer.TWindow;
import jexer.event.TMenuEvent;
import jexer.menu.TMenu;

import com.mybank.domain.*;

import java.io.*;

public class TUIdemo extends TApplication {

    private static final int ABOUT_APP = 2000;
    private static final int CUST_INFO = 2010;

    public static void main(String[] args) throws Exception {
        TUIdemo tdemo = new TUIdemo();
        (new Thread(tdemo)).start();
    }

    public TUIdemo() throws Exception {
        super(BackendType.SWING);

        addToolMenu();

        TMenu fileMenu = addMenu("&File");
        fileMenu.addItem(CUST_INFO, "&Customer Info");
        fileMenu.addDefaultItem(TMenu.MID_SHELL);
        fileMenu.addSeparator();
        fileMenu.addDefaultItem(TMenu.MID_EXIT);

        addWindowMenu();

        TMenu helpMenu = addMenu("&Help");
        helpMenu.addItem(ABOUT_APP, "&About...");

        setFocusFollowsMouse(true);

        ShowCustomerDetails();
    }

    @Override
    protected boolean onMenu(TMenuEvent menu) {
        if (menu.getId() == ABOUT_APP) {
            messageBox("About", "\t\tJust a simple Jexer demo.\n\nCopyright \u00A9 2019 Alexander 'Taurus' Babich").show();
            return true;
        }
        if (menu.getId() == CUST_INFO) {
            ShowCustomerDetails();
            return true;
        }
        return super.onMenu(menu);
    }

    private void ShowCustomerDetails() {
        TWindow custWin = addWindow("Customer Window", 2, 1, 60, 12, TWindow.NOZOOMBOX);
        custWin.newStatusBar("Enter valid customer number and press Show...");

        custWin.addLabel("Enter customer number: ", 2, 2);
        TField custNo = custWin.addField(28, 2, 3, false);
        TText details = custWin.addText("", 2, 4, 56, 6);

        String[] possiblePaths = {
            "test/test.dat",                                                    
            "./test/test.dat",                                                  
            "src/test/resources/test.dat",                                      
            "test/test.dat",                                                    
            System.getProperty("user.dir") + File.separator + "test" + File.separator + "test.dat", 
            "build/test/classes/test.dat",                                      
            getClass().getClassLoader().getResource("test.dat") != null ? 
                getClass().getClassLoader().getResource("test.dat").getPath() : null 
        };
        
        boolean fileLoaded = false;
        
        try {
            java.net.URL resourceUrl = getClass().getClassLoader().getResource("test.dat");
            if (resourceUrl != null) {
                System.out.println("Знайдено файл як ресурс: " + resourceUrl.getPath());
                loadDataFromResource("test.dat");
                fileLoaded = true;
            }
        } catch (Exception e) {
            System.out.println("Не вдалося завантажити як ресурс: " + e.getMessage());
        }
        
        // Якщо не знайшли як ресурс, пробуємо файлові шляхи
        if (!fileLoaded) {
            for (String path : possiblePaths) {
                if (path == null) continue;
                File testFile = new File(path);
                if (testFile.exists()) {
                    System.out.println("Знайдено файл: " + testFile.getAbsolutePath());
                    loadDataFromFile(path);
                    fileLoaded = true;
                    break;
                } else {
                    System.out.println("Файл не знайдено: " + testFile.getAbsolutePath());
                }
            }
        }
        
        if (!fileLoaded) {
            System.out.println("Поточна робоча директорія: " + System.getProperty("user.dir"));
            System.out.println("Файл test.dat не знайдено в жодному з можливих місць");
            createTestCustomers();
        }

        custWin.addButton("&Show", 34, 2, new TAction() {
            @Override
            public void DO() {
                try {
                    int custNum = Integer.parseInt(custNo.getText());
                    if (custNum < 0 || custNum >= Bank.getNumOfCustomers()) {
                        throw new IndexOutOfBoundsException("Invalid customer number: " + custNum);
                    }

                    Customer c = Bank.getCustomer(custNum);
                    if (c.getNumOfAccounts() == 0) {
                        details.setText("Customer has no accounts");
                        return;
                    }
                    
                    Account a = c.getAccount(0); 

                    StringBuilder sb = new StringBuilder();
                    sb.append("Owner Name: ")
                      .append(c.getFirstName()).append(" ").append(c.getLastName()).append("\n");
                    sb.append("Account Type: ").append(a.getAccountType()).append("\n");
                    sb.append(String.format("Account Balance: $%.2f", a.getBalance()));

                    details.setText(sb.toString());
                } catch (NumberFormatException e) {
                    messageBox("Error", "Please enter a valid number!").show();
                } catch (Exception e) {
                    messageBox("Error", "Customer not found or invalid customer number!\nTotal customers: " + Bank.getNumOfCustomers()).show();
                }
            }
        });
    }

    private void loadDataFromResource(String resourceName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8))) {
            
            if (inputStream == null) {
                System.err.println("Resource not found: " + resourceName);
                createTestCustomers();
                return;
            }
            
            Bank.clear();
            String line;
            int customerCount = 0;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] customerData = line.split("\\t");
                if (customerData.length < 3) {
                    System.err.println("Invalid customer data: " + line);
                    continue;
                }

                Customer customer = new Customer(customerData[0], customerData[1]);
                int numAccounts;
                try {
                    numAccounts = Integer.parseInt(customerData[2]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid number of accounts: " + customerData[2]);
                    continue;
                }

                for (int i = 0; i < numAccounts; i++) {
                    String accLine = reader.readLine();
                    if (accLine == null || accLine.trim().isEmpty()) {
                        System.err.println("Missing account data for customer: " + customer.getFirstName());
                        continue;
                    }

                    String[] accData = accLine.split("\\t");
                    if (accData.length < 3) {
                        System.err.println("Invalid account data: " + accLine);
                        continue;
                    }

                    try {
                        String type = accData[0];
                        double balance = Double.parseDouble(accData[1]);
                        double overdraftOrInterest = Double.parseDouble(accData[2]);

                        if (type.equals("S")) {
                            SavingsAccount sa = new SavingsAccount(balance, overdraftOrInterest);
                            customer.addAccount(sa);
                        } else if (type.equals("C")) {
                            CheckingAccount ca = new CheckingAccount(balance, overdraftOrInterest);
                            customer.addAccount(ca);
                        } else {
                            System.err.println("Unknown account type: " + type);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number format in: " + accLine);
                    }
                }

                Bank.addCustomer(customer);
                customerCount++;
            }
            
            System.out.println("Successfully loaded " + customerCount + " customers from resource: " + resourceName);
            
        } catch (IOException e) {
            System.err.println("Error reading resource: " + resourceName);
            e.printStackTrace();
            createTestCustomers();
        }
    }

    private void loadDataFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename, java.nio.charset.StandardCharsets.UTF_8))) {
            Bank.clear();
            String line;
            int customerCount = 0;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                if (firstLine) {
                    firstLine = false;
                    try {
                        Integer.parseInt(line.trim());
                        
                        continue;
                    } catch (NumberFormatException e) {
                        
                    }
                }

                String[] customerData = line.split("\\t");
                if (customerData.length < 3) {
                    System.err.println("Invalid customer data: " + line);
                    continue;
                }

                Customer customer = new Customer(customerData[0], customerData[1]);
                int numAccounts;
                try {
                    numAccounts = Integer.parseInt(customerData[2]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid number of accounts: " + customerData[2]);
                    continue;
                }

                for (int i = 0; i < numAccounts; i++) {
                    String accLine = reader.readLine();
                    if (accLine == null || accLine.trim().isEmpty()) {
                        System.err.println("Missing account data for customer: " + customer.getFirstName());
                        continue;
                    }

                    String[] accData = accLine.split("\\t");
                    if (accData.length < 3) {
                        System.err.println("Invalid account data: " + accLine);
                        continue;
                    }

                    try {
                        String type = accData[0];
                        double balance = Double.parseDouble(accData[1]);
                        double overdraftOrInterest = Double.parseDouble(accData[2]);

                        if (type.equals("S")) {
                            SavingsAccount sa = new SavingsAccount(balance, overdraftOrInterest);
                            customer.addAccount(sa);
                        } else if (type.equals("C")) {
                            CheckingAccount ca = new CheckingAccount(balance, overdraftOrInterest);
                            customer.addAccount(ca);
                        } else {
                            System.err.println("Unknown account type: " + type);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number format in: " + accLine);
                    }
                }

                Bank.addCustomer(customer);
                customerCount++;
            }
            
            System.out.println("Successfully loaded " + customerCount + " customers from file: " + filename);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + filename);
            e.printStackTrace();
           
            createTestCustomers();
        }
    }

    private void createTestCustomers() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
  
   
}