package bankingsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

class Transaction {
    String type;
    double amount;
    double previousBalance;
    Date date;

    public Transaction(String type, double amount, double previousBalance) {
        this.type = type;
        this.amount = amount;
        this.previousBalance = previousBalance;
        this.date = new Date(); // current timestamp
    }

    @Override
    public String toString() {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return type + " of $" + amount + " (Previous Balance: $" + previousBalance + ") on " + df.format(date);
    }
}

class Customer {
    int id;
    String name;
    String password;
    double balance;
    LinkedList<Transaction> transactions = new LinkedList<>();
    Stack<Transaction> undoStack = new Stack<>();

    public Customer(int id, String name, String password, double balance) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.balance = balance;
    }

    public void deposit(double amount) {
        double prevBalance = balance;
        balance += amount;
        Transaction deposit = new Transaction("Deposit", amount, prevBalance);
        transactions.add(deposit);
        undoStack.push(deposit);
    }

    public void withdraw(double amount) {
        if (amount <= balance) {
            double prevBalance = balance;
            balance -= amount;
            Transaction withdrawal = new Transaction("Withdraw", amount, prevBalance);
            transactions.add(withdrawal);
            undoStack.push(withdrawal);
        } else {
            JOptionPane.showMessageDialog(null, "Insufficient balance!");
        }
    }

    public void undoTransaction() {
        if (!undoStack.isEmpty()) {
            Transaction lastTransaction = undoStack.pop();
            if (lastTransaction.type.equals("Deposit")) {
                balance -= lastTransaction.amount;
            } else if (lastTransaction.type.equals("Withdraw")) {
                balance += lastTransaction.amount;
            }
            transactions.removeLast();
        } else {
            JOptionPane.showMessageDialog(null, "No transaction to undo.");
        }
    }

    public void printTransactions(JTextArea textArea) {
        textArea.append("Transaction history for " + name + ":\n");
        for (Transaction t : transactions) {
            textArea.append(t + "\n");
        }
    }

    public boolean authenticate(String enteredPassword) {
        return password.equals(enteredPassword);
    }
}

class BankingSystem {
    private Customer[] customers;
    private int customerCount;

    public BankingSystem(int maxCustomers) {
        customers = new Customer[maxCustomers];
        customerCount = 0;
    }

    public void addCustomer(Customer customer) {
        if (customerCount < customers.length) {
            customers[customerCount++] = customer;
        } else {
            JOptionPane.showMessageDialog(null, "Customer list is full.");
        }
    }

    public Customer findCustomerById(int id) {
        for (int i = 0; i < customerCount; i++) {
            if (customers[i].id == id) {
                return customers[i];
            }
        }
        return null;
    }

    public void sortCustomersByBalance() {
        Arrays.sort(customers, 0, customerCount, Comparator.comparingDouble(c -> c.balance));
    }

    public void printAllCustomers(JTextArea textArea) {
        for (int i = 0; i < customerCount; i++) {
            textArea.append("ID: " + customers[i].id + ", Name: " + customers[i].name + ", Balance: $" + customers[i].balance + "\n");
        }
    }
}

public class BankingSystemGUI {
    private static BankingSystem bankingSystem;
    private static JTextArea textArea; // Declare textArea as a class-level variable

    public static void main(String[] args) {
        bankingSystem = new BankingSystem(5);

        // Create customers and add them to the banking system
        Customer customer1 = new Customer(2310598, "Kaine", "kaine123", 1000);
        Customer customer2 = new Customer(2, "Cyriel", "cyriel123", 500);
        Customer customer3 = new Customer(2311868, "Lord", "lord123", 1500);

        bankingSystem.addCustomer(customer1);
        bankingSystem.addCustomer(customer2);
        bankingSystem.addCustomer(customer3);

        // Initialize the JFrame (GUI window)
        JFrame frame = new JFrame("Banking System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Top panel for logo and company name
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(34, 139, 34)); // Green background
        
        // Load the image from the file
        ImageIcon originalIcon = new ImageIcon("images/logo.png");
        Image image = originalIcon.getImage(); // Get the Image from ImageIcon

        // Resize the image
        Image resizedImage = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH); // Adjust width and height as needed

        // Set the resized image as the new icon for the JLabel
        ImageIcon resizedIcon = new ImageIcon(resizedImage);
        JLabel logoLabel = new JLabel(resizedIcon);

        JLabel companyLabel = new JLabel("Marilaque Banking");
        companyLabel.setFont(new Font("Arial", Font.BOLD, 24));
        companyLabel.setForeground(Color.WHITE);

        topPanel.add(logoLabel);
        topPanel.add(companyLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        // Login screen
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(3, 2));
        loginPanel.setBackground(new Color(255, 255, 255)); // White background

        JLabel idLabel = new JLabel("Customer ID:");
        JTextField idField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        loginPanel.add(idLabel);
        loginPanel.add(idField);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordField);
        loginPanel.add(new JLabel()); // Empty cell
        loginPanel.add(loginButton);

        frame.add(loginPanel, BorderLayout.CENTER);

        // Display area for transactions, etc.
        textArea = new JTextArea(10, 40); // Initialize textArea here
        textArea.setEditable(false);
        textArea.setBackground(new Color(240, 240, 240)); // Light gray background
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.SOUTH);

        frame.setVisible(true);

        // Login button action listener
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int customerId = Integer.parseInt(idField.getText());
                String password = new String(passwordField.getPassword());

                Customer customer = bankingSystem.findCustomerById(customerId);
                if (customer != null && customer.authenticate(password)) {
                    // Hide login screen and show the main menu
                    frame.remove(loginPanel);
                    showMainMenu(customer, textArea, frame);
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid credentials. Please try again.");
                }
            }
        });
    }

    private static void showMainMenu(Customer customer, JTextArea textArea, JFrame frame) {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(5, 1)); // Increased rows for logout button
        menuPanel.setBackground(new Color(34, 139, 34)); // Green background
    
        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");
        JButton undoButton = new JButton("Undo Transaction");
        JButton transactionHistoryButton = new JButton("View Transactions");
        JButton logoutButton = new JButton("Logout");
    
        // Customize button size and color
        depositButton.setPreferredSize(new Dimension(200, 40));
        withdrawButton.setPreferredSize(new Dimension(200, 40));
        undoButton.setPreferredSize(new Dimension(200, 40));
        transactionHistoryButton.setPreferredSize(new Dimension(200, 40));
        logoutButton.setPreferredSize(new Dimension(200, 40)); // Added logout button size
    
        depositButton.setBackground(new Color(85, 107, 47)); // Olive green button
        withdrawButton.setBackground(new Color(85, 107, 47)); // Olive green button
        undoButton.setBackground(new Color(85, 107, 47)); // Olive green button
        transactionHistoryButton.setBackground(new Color(85, 107, 47)); // Olive green button
        logoutButton.setBackground(new Color(85, 107, 47)); // Olive green button
    
        depositButton.setForeground(Color.WHITE);
        withdrawButton.setForeground(Color.WHITE);
        undoButton.setForeground(Color.WHITE);
        transactionHistoryButton.setForeground(Color.WHITE);
        logoutButton.setForeground(Color.WHITE); // White text on logout button
    
        menuPanel.add(depositButton);
        menuPanel.add(withdrawButton);
        menuPanel.add(undoButton);
        menuPanel.add(transactionHistoryButton);
        menuPanel.add(logoutButton); // Added logout button to the menu panel
    
        frame.add(menuPanel, BorderLayout.NORTH);
    
        // Deposit button action listener
        depositButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String amountStr = JOptionPane.showInputDialog(frame, "Enter deposit amount:");
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount > 0) {
                        customer.deposit(amount);
                        JOptionPane.showMessageDialog(frame, "Deposited $" + amount);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Amount must be positive!");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid amount.");
                }
            }
        });
    
        // Withdraw button action listener
        withdrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String amountStr = JOptionPane.showInputDialog(frame, "Your current balance is $" + customer.balance + ". Enter amount to withdraw:");
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount > 0) {
                        customer.withdraw(amount);
                        JOptionPane.showMessageDialog(frame, "Withdrew $" + amount);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Amount must be positive!");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid amount.");
                }
            }
        });
    
        // Undo button action listener
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customer.undoTransaction();
                JOptionPane.showMessageDialog(frame, "Last transaction undone.");
            }
        });
    
        // Transaction history button action listener
        transactionHistoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");  // Clear the text area before displaying new transactions
                customer.printTransactions(textArea);
            }
        });
    
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Remove the current main menu and show the login screen again
                frame.getContentPane().removeAll(); // Remove all existing components
                frame.setLayout(new BorderLayout()); // Reset the layout
                showLoginScreen(frame); // Show the login screen
                
                // Refresh the frame after removing and adding components
                frame.revalidate();
                frame.repaint();
            }
        });
        
    
        frame.revalidate();
        frame.repaint();
    }
    
    private static void showLoginScreen(JFrame frame) {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(3, 2));
        loginPanel.setBackground(new Color(255, 255, 255)); // White background
    
        JLabel idLabel = new JLabel("Customer ID:");
        JTextField idField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");
    
        loginPanel.add(idLabel);
        loginPanel.add(idField);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordField);
        loginPanel.add(new JLabel()); // Empty cell
        loginPanel.add(loginButton);
    
        frame.add(loginPanel, BorderLayout.CENTER);
    
        // Login button action listener
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int customerId = Integer.parseInt(idField.getText());
                String password = new String(passwordField.getPassword());
    
                Customer customer = bankingSystem.findCustomerById(customerId);
                if (customer != null && customer.authenticate(password)) {
                    // Hide login screen and show the main menu
                    frame.getContentPane().removeAll();
                    showMainMenu(customer, textArea, frame);
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid credentials. Please try again.");
                }
            }
        });
    }    
}
