/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Aditi
 */


import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class TiffinBilling extends JFrame {
    // 1. Define UI Components
    JComboBox<String> cmbCustomer, cmbPlan;
    JTextField txtDays, txtAmount;
    JButton btnBill;

    public TiffinBilling() {
        // Window Settings
        setTitle("Tiffin Billing System");
        setSize(400, 400);
        setLayout(new GridLayout(6, 2, 10, 10));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // 2. Initialize Components
        add(new JLabel(" Select Customer:"));
        cmbCustomer = new JComboBox<>();
        add(cmbCustomer);

        add(new JLabel(" Select Plan:"));
        cmbPlan = new JComboBox<>();
        add(cmbPlan);

        add(new JLabel(" Number of Days:"));
        txtDays = new JTextField();
        add(txtDays);

        add(new JLabel(" Total Amount:"));
        txtAmount = new JTextField();
        txtAmount.setEditable(false);
        add(txtAmount);

        btnBill = new JButton("Generate & Save Bill");
        add(btnBill);

        // 3. Load Data from DB immediately
        loadDataFromDatabase();

        // 4. Button Action
        btnBill.addActionListener(e -> calculateAndSave());

        setVisible(true);
    }

    // THE BRIDGE: Database Connection Method
    private Connection connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/tiffin_db", "root", "aditi");
        } catch (Exception e) {
            System.out.println("Connection Failed: " + e.getMessage());
            return null;
        }
    }

    private void loadDataFromDatabase() {
        try (Connection con = connect()) {
            if (con == null) return;

            // Load Customers
            ResultSet rs1 = con.createStatement().executeQuery("SELECT name FROM customers");
            while (rs1.next()) cmbCustomer.addItem(rs1.getString("name"));

            // Load Plans
            ResultSet rs2 = con.createStatement().executeQuery("SELECT plan_name FROM plans");
            while (rs2.next()) cmbPlan.addItem(rs2.getString("plan_name"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void calculateAndSave() {
    // 1. Validation: Check if days is empty
    if (txtDays.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter the number of days!");
        return;
    }

    try (Connection con = connect()) {
        String customer = cmbCustomer.getSelectedItem().toString();
        String plan = cmbPlan.getSelectedItem().toString();
        int days = Integer.parseInt(txtDays.getText());

        // 2. Get Price from plans table
        double price = 0;
        PreparedStatement pst1 = con.prepareStatement("SELECT price FROM plans WHERE plan_name=?");
        pst1.setString(1, plan);
        ResultSet rs = pst1.executeQuery();
        
        if (rs.next()) {
            price = rs.getDouble("price");
        } else {
            JOptionPane.showMessageDialog(this, "Price for this plan not found!");
            return;
        }

        // 3. Calculation
        double total = price * days;
        txtAmount.setText(String.valueOf(total));

        // 4. Save to 'billing' table
        PreparedStatement pst2 = con.prepareStatement("INSERT INTO billing(customer, plan, days, amount) VALUES(?,?,?,?)");
        pst2.setString(1, customer);
        pst2.setString(2, plan);
        pst2.setInt(3, days);
        pst2.setDouble(4, total);
        pst2.executeUpdate();

        JOptionPane.showMessageDialog(this, "Bill Saved Successfully!\nTotal: Rs. " + total);

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Please enter a valid number for days.");
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        e.printStackTrace();
    }
}

    public static void main(String[] args) {
        new TiffinBilling();
    }
}
