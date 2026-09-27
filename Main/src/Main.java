import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Main extends JFrame {
    private final FileManager fileManager = new FileManager();
    private static final double RATE_PER_UNIT = 5.0;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);

    public Main() {
        setTitle("Electricity Management System");
        setSize(650, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        container.add(buildHomePanel(), "home");
        container.add(buildRegisterPanel(), "register");
        container.add(buildSearchPanel(), "search");
        container.add(buildUpdatePanel(), "update");
        container.add(buildDeletePanel(), "delete");
        container.add(buildMeterPanel(), "meter");
        container.add(buildBillAddPanel(), "billAdd");
        container.add(buildBillUpdatePanel(), "billUpdate");
        container.add(buildBillDeletePanel(), "billDelete");
        container.add(buildReportPanel(), "report");

        add(container);
        cardLayout.show(container, "home");
    }

    // ================= REUSABLE HELPERS =================

    // Every feature page ends with this button, which always returns to Home
    private JButton buildBackButton() {
        JButton back = new JButton("Back to Home");
        back.addActionListener(e -> cardLayout.show(container, "home"));
        return back;
    }

    // Standard page skeleton: title at top, form/output in the middle, action+back buttons at bottom
    private JPanel buildPageFrame(String title, JComponent formArea, JTextArea output, JButton... actionButtons) {
        JPanel page = new JPanel(new BorderLayout(10, 10));
        page.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        page.add(titleLabel, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.add(formArea, BorderLayout.NORTH);
        center.add(new JScrollPane(output), BorderLayout.CENTER);
        page.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        for (JButton b : actionButtons) bottom.add(b);
        bottom.add(buildBackButton());
        page.add(bottom, BorderLayout.SOUTH);

        return page;
    }

    // ================= HOME PAGE =================

    private JPanel buildHomePanel() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        JLabel title = new JLabel("Electricity Management System", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        page.add(title, BorderLayout.NORTH);

        JPanel buttonGrid = new JPanel(new GridLayout(0, 2, 15, 15));
        buttonGrid.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        String[][] buttons = {
                {"Register Customer", "register"},
                {"Search Customer", "search"},
                {"Update Customer", "update"},
                {"Delete Customer", "delete"},
                {"Add Meter Reading", "meter"},
                {"Add Monthly Bill", "billAdd"},
                {"Update Bill Status", "billUpdate"},
                {"Delete Bill", "billDelete"},
                {"Annual Bill Report", "report"}
        };

        for (String[] b : buttons) {
            JButton btn = new JButton(b[0]);
            btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            btn.addActionListener(e -> cardLayout.show(container, b[1]));
            buttonGrid.add(btn);
        }

        page.add(buttonGrid, BorderLayout.CENTER);

        JButton exitBtn = new JButton("Exit");
        exitBtn.addActionListener(e -> System.exit(0));
        JPanel bottom = new JPanel();
        bottom.add(exitBtn);
        page.add(bottom, BorderLayout.SOUTH);

        return page;
    }

    // ================= CUSTOMER: REGISTER =================

    private JPanel buildRegisterPanel() {
        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField meterField = new JTextField();

        form.add(new JLabel("Customer ID:"));
        form.add(idField);
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Address:"));
        form.add(addressField);
        form.add(new JLabel("Meter Number:"));
        form.add(meterField);

        JTextArea output = new JTextArea(6, 40);
        output.setEditable(false);

        JButton registerBtn = new JButton("Register");
        registerBtn.addActionListener(e -> {
            try {
                Customer c = new Customer(idField.getText(), nameField.getText(),
                        addressField.getText(), meterField.getText());
                fileManager.saveCustomer(c);
                output.setText("Customer registered successfully!");
                idField.setText(""); nameField.setText(""); addressField.setText(""); meterField.setText("");
            } catch (FileManager.DuplicateCustomerException ex) {
                output.setText(ex.getMessage());
            }
        });

        return buildPageFrame("Register New Customer", form, output, registerBtn);
    }

    // ================= CUSTOMER: SEARCH =================

    private JPanel buildSearchPanel() {
        JPanel form = new JPanel(new GridLayout(1, 2, 5, 5));
        JTextField idField = new JTextField();
        form.add(new JLabel("Customer ID:"));
        form.add(idField);

        JTextArea output = new JTextArea(6, 40);
        output.setEditable(false);

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> {
            Customer c = fileManager.findCustomerById(idField.getText());
            if (c == null) {
                output.setText("Customer not found.");
            } else {
                output.setText("Customer ID: " + c.getCustomerId() +
                        "\nName: " + c.getName() +
                        "\nAddress: " + c.getAddress() +
                        "\nMeter Number: " + c.getMeterNumber());
            }
        });

        return buildPageFrame("Search Customer", form, output, searchBtn);
    }

    // ================= CUSTOMER: UPDATE =================

    private JPanel buildUpdatePanel() {
        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField meterField = new JTextField();

        form.add(new JLabel("Customer ID:"));
        form.add(idField);
        form.add(new JLabel("New Name:"));
        form.add(nameField);
        form.add(new JLabel("New Address:"));
        form.add(addressField);
        form.add(new JLabel("New Meter Number:"));
        form.add(meterField);

        JTextArea output = new JTextArea(6, 40);
        output.setEditable(false);

        JButton loadBtn = new JButton("Load Current Info");
        JButton updateBtn = new JButton("Update");

        loadBtn.addActionListener(e -> {
            Customer c = fileManager.findCustomerById(idField.getText());
            if (c == null) {
                output.setText("Customer not found.");
            } else {
                nameField.setText(c.getName());
                addressField.setText(c.getAddress());
                meterField.setText(c.getMeterNumber());
                output.setText("Current info loaded. Edit the fields above, then click Update.");
            }
        });

        updateBtn.addActionListener(e -> {
            try {
                Customer c = new Customer(idField.getText(), nameField.getText(),
                        addressField.getText(), meterField.getText());
                fileManager.updateCustomer(c);
                output.setText("Customer updated successfully!");
            } catch (FileManager.CustomerNotFoundException ex) {
                output.setText(ex.getMessage());
            }
        });

        return buildPageFrame("Update Customer", form, output, loadBtn, updateBtn);
    }

    // ================= CUSTOMER: DELETE =================

    private JPanel buildDeletePanel() {
        JPanel form = new JPanel(new GridLayout(1, 2, 5, 5));
        JTextField idField = new JTextField();
        form.add(new JLabel("Customer ID:"));
        form.add(idField);

        JTextArea output = new JTextArea(6, 40);
        output.setEditable(false);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete customer " + idField.getText() + " and all their records?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                output.setText("Delete cancelled.");
                return;
            }

            try {
                fileManager.deleteCustomer(idField.getText());
                output.setText("Customer and their records deleted successfully.");
                idField.setText("");
            } catch (FileManager.CustomerNotFoundException ex) {
                output.setText(ex.getMessage());
            }
        });

        return buildPageFrame("Delete Customer", form, output, deleteBtn);
    }

    // ================= METER: ADD READING =================

    private JPanel buildMeterPanel() {
        JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField idField = new JTextField();
        JTextField currentField = new JTextField();

        form.add(new JLabel("Customer ID:"));
        form.add(idField);
        form.add(new JLabel("Current Reading:"));
        form.add(currentField);

        JTextArea output = new JTextArea(6, 40);
        output.setEditable(false);

        JButton addBtn = new JButton("Add Meter Reading");
        addBtn.addActionListener(e -> {
            Customer customer = fileManager.findCustomerById(idField.getText());
            if (customer == null) {
                output.setText("Error: Customer does not exist.");
                return;
            }

            try {
                int currentReading = Integer.parseInt(currentField.getText());
                Meter lastMeter = fileManager.findMeterByCustomerId(idField.getText());
                int previousReading = (lastMeter == null) ? 0 : lastMeter.getCurrentReading();

                if (currentReading < previousReading) {
                    output.setText("Error: Current reading cannot be less than previous reading (" + previousReading + ").");
                    return;
                }

                Meter meter = new Meter(idField.getText(), customer.getMeterNumber(), previousReading, currentReading);
                fileManager.saveMeter(meter);
                output.setText("Reading saved!\nPrevious: " + previousReading +
                        "\nCurrent: " + currentReading +
                        "\nUnits Used: " + meter.getUnitsUsed());
                currentField.setText("");
            } catch (NumberFormatException ex) {
                output.setText("Error: Please enter a valid number for the reading.");
            }
        });

        return buildPageFrame("Add Meter Reading", form, output, addBtn);
    }

    // ================= BILL: ADD =================

    private JPanel buildBillAddPanel() {
        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField idField = new JTextField();
        JTextField monthField = new JTextField();
        JTextField statusField = new JTextField();

        form.add(new JLabel("Customer ID:"));
        form.add(idField);
        form.add(new JLabel("Month:"));
        form.add(monthField);
        form.add(new JLabel("Status (Paid/Unpaid):"));
        form.add(statusField);

        JTextArea output = new JTextArea(6, 40);
        output.setEditable(false);

        JButton addBtn = new JButton("Add Bill");
        addBtn.addActionListener(e -> {
            Customer customer = fileManager.findCustomerById(idField.getText());
            if (customer == null) {
                output.setText("Error: Customer does not exist.");
                return;
            }

            Meter meter = fileManager.findNextUnbilledMeter(idField.getText());
            if (meter == null) {
                output.setText("Error: No unbilled meter reading found.");
                return;
            }

            String status = statusField.getText();
            if (!status.equalsIgnoreCase("Paid") && !status.equalsIgnoreCase("Unpaid")) {
                output.setText("Error: Status must be 'Paid' or 'Unpaid'.");
                return;
            }

            Bill bill = new Bill(idField.getText(), monthField.getText(),
                    meter.getUnitsUsed(), meter.getCurrentReading(), status);
            fileManager.saveBill(bill);

            double cost = bill.getUnitsUsed() * RATE_PER_UNIT;
            output.setText("Bill created!\nUnits: " + bill.getUnitsUsed() +
                    "\nReading: " + bill.getReading() +
                    "\nAmount: " + cost + " Taka" +
                    "\nStatus: " + bill.getStatus());
        });

        return buildPageFrame("Add Monthly Bill", form, output, addBtn);
    }

    // ================= BILL: UPDATE STATUS =================

    private JPanel buildBillUpdatePanel() {
        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField idField = new JTextField();
        JTextField monthField = new JTextField();
        JTextField statusField = new JTextField();

        form.add(new JLabel("Customer ID:"));
        form.add(idField);
        form.add(new JLabel("Month:"));
        form.add(monthField);
        form.add(new JLabel("New Status (Paid/Unpaid):"));
        form.add(statusField);

        JTextArea output = new JTextArea(6, 40);
        output.setEditable(false);

        JButton updateBtn = new JButton("Update Status");
        updateBtn.addActionListener(e -> {
            String status = statusField.getText();
            if (!status.equalsIgnoreCase("Paid") && !status.equalsIgnoreCase("Unpaid")) {
                output.setText("Error: Status must be 'Paid' or 'Unpaid'.");
                return;
            }
            boolean success = fileManager.updateBillStatus(idField.getText(), monthField.getText(), status);
            output.setText(success ? "Bill status updated." : "Error: Bill not found.");
        });

        return buildPageFrame("Update Bill Status", form, output, updateBtn);
    }

    // ================= BILL: DELETE =================

    private JPanel buildBillDeletePanel() {
        JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField idField = new JTextField();
        JTextField monthField = new JTextField();

        form.add(new JLabel("Customer ID:"));
        form.add(idField);
        form.add(new JLabel("Month:"));
        form.add(monthField);

        JTextArea output = new JTextArea(6, 40);
        output.setEditable(false);

        JButton deleteBtn = new JButton("Delete Bill");
        deleteBtn.addActionListener(e -> {
            boolean success = fileManager.deleteBill(idField.getText(), monthField.getText());
            output.setText(success ? "Bill deleted." : "Error: Bill not found.");
        });

        return buildPageFrame("Delete Bill", form, output, deleteBtn);
    }

    // ================= ANNUAL REPORT =================

    private JPanel buildReportPanel() {
        JPanel form = new JPanel(new GridLayout(1, 2, 5, 5));
        JTextField idField = new JTextField();
        form.add(new JLabel("Customer ID:"));
        form.add(idField);

        JTextArea output = new JTextArea(14, 50);
        output.setEditable(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JButton generateBtn = new JButton("Generate Report");
        generateBtn.addActionListener(e -> {
            Customer customer = fileManager.findCustomerById(idField.getText());
            if (customer == null) {
                output.setText("Error: Customer does not exist.");
                return;
            }

            List<Bill> bills = fileManager.findBillsByCustomerId(idField.getText());
            if (bills.isEmpty()) {
                output.setText("No bills found for this customer yet.");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Customer Name: ").append(customer.getName()).append("\n");
            sb.append("Customer ID: ").append(customer.getCustomerId()).append("\n");
            sb.append("Address: ").append(customer.getAddress()).append("\n\n");
            sb.append(String.format("%-15s%-10s%-12s%-12s%-10s%n", "MONTH", "UNITS", "READING", "AMOUNT", "STATUS"));

            int totalUnits = 0, paidCount = 0, unpaidCount = 0;
            double totalBilled = 0, totalPaid = 0, totalDue = 0;

            for (Bill bill : bills) {
                double cost = bill.getUnitsUsed() * RATE_PER_UNIT;
                sb.append(String.format("%-15s%-10d%-12d%-12.2f%-10s%n",
                        bill.getMonth(), bill.getUnitsUsed(), bill.getReading(), cost, bill.getStatus()));

                totalUnits += bill.getUnitsUsed();
                totalBilled += cost;
                if (bill.getStatus().equalsIgnoreCase("Paid")) {
                    paidCount++;
                    totalPaid += cost;
                } else {
                    unpaidCount++;
                    totalDue += cost;
                }
            }

            sb.append("\nTotal Units: ").append(totalUnits);
            sb.append("\nPaid Bills: ").append(paidCount).append("   Unpaid Bills: ").append(unpaidCount);
            sb.append("\nTotal Billed: ").append(totalBilled).append(" Taka");
            sb.append("\nTotal Paid: ").append(totalPaid).append(" Taka");
            sb.append("\nTotal Due: ").append(totalDue).append(" Taka");

            output.setText(sb.toString());
        });

        return buildPageFrame("Annual Bill Report", form, output, generateBtn);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}