import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Main extends JFrame {

    private static final double RATE_PER_UNIT = 5.0;

    private final FileManager fileManager = new FileManager();
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
        showPage("home");
    }

    private void showPage(String name) {
        cardLayout.show(container, name);
    }

    private JPanel createForm(int rows) {
        return new JPanel(new GridLayout(rows, 2, 5, 5));
    }

    private JTextField addField(JPanel form, String label) {
        JTextField field = new JTextField();
        form.add(new JLabel(label));
        form.add(field);
        return field;
    }

    private JTextArea createOutput() {
        JTextArea output = new JTextArea(6, 40);
        output.setEditable(false);
        return output;
    }

    private boolean isValidStatus(String status) {
        return status.equalsIgnoreCase("Paid") || status.equalsIgnoreCase("Unpaid");
    }

    private JPanel buildPageFrame(String title, JPanel form, JTextArea output, JButton... actionButtons) {
        JPanel page = new JPanel(new BorderLayout(10, 10));
        page.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        page.add(titleLabel, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.add(form, BorderLayout.NORTH);
        center.add(new JScrollPane(output), BorderLayout.CENTER);
        page.add(center, BorderLayout.CENTER);

        JPanel buttonRow = new JPanel();
        for (JButton button : actionButtons) {
            buttonRow.add(button);
        }

        JButton backButton = new JButton("Back to Home");
        backButton.addActionListener(e -> showPage("home"));
        buttonRow.add(backButton);
        page.add(buttonRow, BorderLayout.SOUTH);

        return page;
    }

    private JPanel buildHomePanel() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        JLabel title = new JLabel("Electricity Management System", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        page.add(title, BorderLayout.NORTH);

        String[][] menuItems = {
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

        JPanel buttonGrid = new JPanel(new GridLayout(0, 2, 15, 15));
        buttonGrid.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        for (String[] item : menuItems) {
            JButton button = new JButton(item[0]);
            button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            button.addActionListener(e -> showPage(item[1]));
            buttonGrid.add(button);
        }
        page.add(buttonGrid, BorderLayout.CENTER);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));
        JPanel bottomRow = new JPanel();
        bottomRow.add(exitButton);
        page.add(bottomRow, BorderLayout.SOUTH);

        return page;
    }

    private JPanel buildRegisterPanel() {
        JPanel form = createForm(4);
        JTextField idField = addField(form, "Customer ID:");
        JTextField nameField = addField(form, "Name:");
        JTextField addressField = addField(form, "Address:");
        JTextField meterField = addField(form, "Meter Number:");
        JTextArea output = createOutput();

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> {
            try {
                Customer customer = new Customer(idField.getText(), nameField.getText(),
                        addressField.getText(), meterField.getText());
                fileManager.saveCustomer(customer);
                output.setText("Customer registered successfully!");

                idField.setText("");
                nameField.setText("");
                addressField.setText("");
                meterField.setText("");
            } catch (FileManager.DuplicateCustomerException ex) {
                output.setText(ex.getMessage());
            }
        });

        return buildPageFrame("Register New Customer", form, output, registerButton);
    }

    private JPanel buildSearchPanel() {
        JPanel form = createForm(1);
        JTextField idField = addField(form, "Customer ID:");
        JTextArea output = createOutput();

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> {
            Customer customer = fileManager.findCustomerById(idField.getText());
            if (customer == null) {
                output.setText("Customer not found.");
                return;
            }
            output.setText("Customer ID: " + customer.getCustomerId() +
                    "\nName: " + customer.getName() +
                    "\nAddress: " + customer.getAddress() +
                    "\nMeter Number: " + customer.getMeterNumber());
        });

        return buildPageFrame("Search Customer", form, output, searchButton);
    }

    private JPanel buildUpdatePanel() {
        JPanel form = createForm(4);
        JTextField idField = addField(form, "Customer ID:");
        JTextField nameField = addField(form, "New Name:");
        JTextField addressField = addField(form, "New Address:");
        JTextField meterField = addField(form, "New Meter Number:");
        JTextArea output = createOutput();

        JButton loadButton = new JButton("Load Current Info");
        loadButton.addActionListener(e -> {
            Customer customer = fileManager.findCustomerById(idField.getText());
            if (customer == null) {
                output.setText("Customer not found.");
                return;
            }
            nameField.setText(customer.getName());
            addressField.setText(customer.getAddress());
            meterField.setText(customer.getMeterNumber());
            output.setText("Current info loaded. Edit the fields above, then click Update.");
        });

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> {
            try {
                Customer customer = new Customer(idField.getText(), nameField.getText(),
                        addressField.getText(), meterField.getText());
                fileManager.updateCustomer(customer);
                output.setText("Customer updated successfully!");
            } catch (FileManager.CustomerNotFoundException ex) {
                output.setText(ex.getMessage());
            }
        });

        return buildPageFrame("Update Customer", form, output, loadButton, updateButton);
    }

    private JPanel buildDeletePanel() {
        JPanel form = createForm(1);
        JTextField idField = addField(form, "Customer ID:");
        JTextArea output = createOutput();

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Delete customer " + idField.getText() + " and all their records?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (choice != JOptionPane.YES_OPTION) {
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

        return buildPageFrame("Delete Customer", form, output, deleteButton);
    }

    private JPanel buildMeterPanel() {
        JPanel form = createForm(2);
        JTextField idField = addField(form, "Customer ID:");
        JTextField readingField = addField(form, "Current Reading:");
        JTextArea output = createOutput();

        JButton addButton = new JButton("Add Meter Reading");
        addButton.addActionListener(e -> {
            Customer customer = fileManager.findCustomerById(idField.getText());
            if (customer == null) {
                output.setText("Error: Customer does not exist.");
                return;
            }

            try {
                int currentReading = Integer.parseInt(readingField.getText());

                Meter lastMeter = fileManager.findMeterByCustomerId(idField.getText());
                int previousReading = (lastMeter == null) ? 0 : lastMeter.getCurrentReading();

                if (currentReading < previousReading) {
                    output.setText("Error: Current reading cannot be less than previous reading ("
                            + previousReading + ").");
                    return;
                }

                Meter meter = new Meter(idField.getText(), customer.getMeterNumber(),
                        previousReading, currentReading);
                fileManager.saveMeter(meter);

                output.setText("Reading saved!\nPrevious: " + previousReading +
                        "\nCurrent: " + currentReading +
                        "\nUnits Used: " + meter.getUnitsUsed());
                readingField.setText("");
            } catch (NumberFormatException ex) {
                output.setText("Error: Please enter a valid number for the reading.");
            }
        });

        return buildPageFrame("Add Meter Reading", form, output, addButton);
    }

    private JPanel buildBillAddPanel() {
        JPanel form = createForm(3);
        JTextField idField = addField(form, "Customer ID:");
        JTextField monthField = addField(form, "Month:");
        JTextField statusField = addField(form, "Status (Paid/Unpaid):");
        JTextArea output = createOutput();

        JButton addButton = new JButton("Add Bill");
        addButton.addActionListener(e -> {
            if (fileManager.findCustomerById(idField.getText()) == null) {
                output.setText("Error: Customer does not exist.");
                return;
            }

            Meter meter = fileManager.findNextUnbilledMeter(idField.getText());
            if (meter == null) {
                output.setText("Error: No unbilled meter reading found.");
                return;
            }

            if (!isValidStatus(statusField.getText())) {
                output.setText("Error: Status must be 'Paid' or 'Unpaid'.");
                return;
            }

            Bill bill = new Bill(idField.getText(), monthField.getText(),
                    meter.getUnitsUsed(), meter.getCurrentReading(), statusField.getText());
            fileManager.saveBill(bill);

            double amount = bill.getUnitsUsed() * RATE_PER_UNIT;
            output.setText("Bill created!\nUnits: " + bill.getUnitsUsed() +
                    "\nReading: " + bill.getReading() +
                    "\nAmount: " + amount + " Taka" +
                    "\nStatus: " + bill.getStatus());
        });

        return buildPageFrame("Add Monthly Bill", form, output, addButton);
    }

    private JPanel buildBillUpdatePanel() {
        JPanel form = createForm(3);
        JTextField idField = addField(form, "Customer ID:");
        JTextField monthField = addField(form, "Month:");
        JTextField statusField = addField(form, "New Status (Paid/Unpaid):");
        JTextArea output = createOutput();

        JButton updateButton = new JButton("Update Status");
        updateButton.addActionListener(e -> {
            if (!isValidStatus(statusField.getText())) {
                output.setText("Error: Status must be 'Paid' or 'Unpaid'.");
                return;
            }
            boolean updated = fileManager.updateBillStatus(
                    idField.getText(), monthField.getText(), statusField.getText());
            output.setText(updated ? "Bill status updated." : "Error: Bill not found.");
        });

        return buildPageFrame("Update Bill Status", form, output, updateButton);
    }

    private JPanel buildBillDeletePanel() {
        JPanel form = createForm(2);
        JTextField idField = addField(form, "Customer ID:");
        JTextField monthField = addField(form, "Month:");
        JTextArea output = createOutput();

        JButton deleteButton = new JButton("Delete Bill");
        deleteButton.addActionListener(e -> {
            boolean deleted = fileManager.deleteBill(idField.getText(), monthField.getText());
            output.setText(deleted ? "Bill deleted." : "Error: Bill not found.");
        });

        return buildPageFrame("Delete Bill", form, output, deleteButton);
    }

    private JPanel buildReportPanel() {
        JPanel form = createForm(1);
        JTextField idField = addField(form, "Customer ID:");

        JTextArea output = new JTextArea(14, 50);
        output.setEditable(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JButton generateButton = new JButton("Generate Report");
        generateButton.addActionListener(e -> {
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

            output.setText(createReport(customer, bills));
        });

        return buildPageFrame("Annual Bill Report", form, output, generateButton);
    }

    private String createReport(Customer customer, List<Bill> bills) {
        StringBuilder report = new StringBuilder();
        report.append("Customer Name: ").append(customer.getName()).append("\n");
        report.append("Customer ID: ").append(customer.getCustomerId()).append("\n");
        report.append("Address: ").append(customer.getAddress()).append("\n\n");
        report.append(String.format("%-15s%-10s%-12s%-12s%-10s%n",
                "MONTH", "UNITS", "READING", "AMOUNT", "STATUS"));

        int totalUnits = 0;
        int paidCount = 0;
        int unpaidCount = 0;
        double totalBilled = 0;
        double totalPaid = 0;
        double totalDue = 0;

        for (Bill bill : bills) {
            double amount = bill.getUnitsUsed() * RATE_PER_UNIT;
            report.append(String.format("%-15s%-10d%-12d%-12.2f%-10s%n",
                    bill.getMonth(), bill.getUnitsUsed(), bill.getReading(), amount, bill.getStatus()));

            totalUnits += bill.getUnitsUsed();
            totalBilled += amount;

            if (bill.getStatus().equalsIgnoreCase("Paid")) {
                paidCount++;
                totalPaid += amount;
            } else {
                unpaidCount++;
                totalDue += amount;
            }
        }

        report.append("\nTotal Units: ").append(totalUnits);
        report.append("\nPaid Bills: ").append(paidCount).append("   Unpaid Bills: ").append(unpaidCount);
        report.append("\nTotal Billed: ").append(totalBilled).append(" Taka");
        report.append("\nTotal Paid: ").append(totalPaid).append(" Taka");
        report.append("\nTotal Due: ").append(totalDue).append(" Taka");

        return report.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
