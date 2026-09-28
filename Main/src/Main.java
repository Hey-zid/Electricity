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

    private JButton buildBackButton() {
        JButton backButton = new JButton("Back to Home");
        backButton.addActionListener(e -> cardLayout.show(container, "home"));
        return backButton;
    }

    private JPanel buildPageFrame(String title, JComponent form, JTextArea output, JButton... actionButtons) {
        JPanel page = new JPanel(new BorderLayout(10, 10));
        page.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        page.add(titleLabel, BorderLayout.NORTH);

        JPanel centerArea = new JPanel(new BorderLayout(10, 10));
        centerArea.add(form, BorderLayout.NORTH);
        centerArea.add(new JScrollPane(output), BorderLayout.CENTER);
        page.add(centerArea, BorderLayout.CENTER);

        JPanel buttonRow = new JPanel();
        for (JButton button : actionButtons) {
            buttonRow.add(button);
        }
        buttonRow.add(buildBackButton());
        page.add(buttonRow, BorderLayout.SOUTH);

        return page;
    }

    private JPanel buildHomePanel() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        JLabel title = new JLabel("Electricity Management System", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        page.add(title, BorderLayout.NORTH);

        JPanel buttonGrid = new JPanel(new GridLayout(0, 2, 15, 15));
        buttonGrid.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        // each row here is {button label, name of the page to open}
        String[][] menuButtons = {
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

        for (String[] menuButton : menuButtons) {
            JButton button = new JButton(menuButton[0]);
            button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            button.addActionListener(e -> cardLayout.show(container, menuButton[1]));
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

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> {
            try {
                Customer newCustomer = new Customer(idField.getText(), nameField.getText(),
                        addressField.getText(), meterField.getText());
                fileManager.saveCustomer(newCustomer);
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
        JPanel form = new JPanel(new GridLayout(1, 2, 5, 5));
        JTextField idField = new JTextField();
        form.add(new JLabel("Customer ID:"));
        form.add(idField);

        JTextArea output = new JTextArea(6, 40);
        output.setEditable(false);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> {
            Customer customer = fileManager.findCustomerById(idField.getText());
            if (customer == null) {
                output.setText("Customer not found.");
            } else {
                output.setText("Customer ID: " + customer.getCustomerId() +
                        "\nName: " + customer.getName() +
                        "\nAddress: " + customer.getAddress() +
                        "\nMeter Number: " + customer.getMeterNumber());
            }
        });

        return buildPageFrame("Search Customer", form, output, searchButton);
    }

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

        JButton loadButton = new JButton("Load Current Info");
        JButton updateButton = new JButton("Update");

        loadButton.addActionListener(e -> {
            Customer customer = fileManager.findCustomerById(idField.getText());
            if (customer == null) {
                output.setText("Customer not found.");
            } else {
                nameField.setText(customer.getName());
                addressField.setText(customer.getAddress());
                meterField.setText(customer.getMeterNumber());
                output.setText("Current info loaded. Edit the fields above, then click Update.");
            }
        });

        updateButton.addActionListener(e -> {
            try {
                Customer updatedCustomer = new Customer(idField.getText(), nameField.getText(),
                        addressField.getText(), meterField.getText());
                fileManager.updateCustomer(updatedCustomer);
                output.setText("Customer updated successfully!");
            } catch (FileManager.CustomerNotFoundException ex) {
                output.setText(ex.getMessage());
            }
        });

        return buildPageFrame("Update Customer", form, output, loadButton, updateButton);
    }

    private JPanel buildDeletePanel() {
        JPanel form = new JPanel(new GridLayout(1, 2, 5, 5));
        JTextField idField = new JTextField();
        form.add(new JLabel("Customer ID:"));
        form.add(idField);

        JTextArea output = new JTextArea(6, 40);
        output.setEditable(false);

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> {
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

        return buildPageFrame("Delete Customer", form, output, deleteButton);
    }

    private JPanel buildMeterPanel() {
        JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField idField = new JTextField();
        JTextField currentReadingField = new JTextField();

        form.add(new JLabel("Customer ID:"));
        form.add(idField);
        form.add(new JLabel("Current Reading:"));
        form.add(currentReadingField);

        JTextArea output = new JTextArea(6, 40);
        output.setEditable(false);

        JButton addButton = new JButton("Add Meter Reading");
        addButton.addActionListener(e -> {
            Customer customer = fileManager.findCustomerById(idField.getText());
            if (customer == null) {
                output.setText("Error: Customer does not exist.");
                return;
            }

            try {
                int currentReading = Integer.parseInt(currentReadingField.getText());

                Meter lastMeter = fileManager.findMeterByCustomerId(idField.getText());
                int previousReading = (lastMeter == null) ? 0 : lastMeter.getCurrentReading();

                if (currentReading < previousReading) {
                    output.setText("Error: Current reading cannot be less than previous reading (" + previousReading + ").");
                    return;
                }

                Meter newMeter = new Meter(idField.getText(), customer.getMeterNumber(), previousReading, currentReading);
                fileManager.saveMeter(newMeter);

                output.setText("Reading saved!\nPrevious: " + previousReading +
                        "\nCurrent: " + currentReading +
                        "\nUnits Used: " + newMeter.getUnitsUsed());
                currentReadingField.setText("");
            } catch (NumberFormatException ex) {
                output.setText("Error: Please enter a valid number for the reading.");
            }
        });

        return buildPageFrame("Add Meter Reading", form, output, addButton);
    }

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

        JButton addButton = new JButton("Add Bill");
        addButton.addActionListener(e -> {
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

            Bill newBill = new Bill(idField.getText(), monthField.getText(),
                    meter.getUnitsUsed(), meter.getCurrentReading(), status);
            fileManager.saveBill(newBill);

            double cost = newBill.getUnitsUsed() * RATE_PER_UNIT;
            output.setText("Bill created!\nUnits: " + newBill.getUnitsUsed() +
                    "\nReading: " + newBill.getReading() +
                    "\nAmount: " + cost + " Taka" +
                    "\nStatus: " + newBill.getStatus());
        });

        return buildPageFrame("Add Monthly Bill", form, output, addButton);
    }
    
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

        JButton updateButton = new JButton("Update Status");
        updateButton.addActionListener(e -> {
            String status = statusField.getText();
            if (!status.equalsIgnoreCase("Paid") && !status.equalsIgnoreCase("Unpaid")) {
                output.setText("Error: Status must be 'Paid' or 'Unpaid'.");
                return;
            }
            boolean success = fileManager.updateBillStatus(idField.getText(), monthField.getText(), status);
            output.setText(success ? "Bill status updated." : "Error: Bill not found.");
        });

        return buildPageFrame("Update Bill Status", form, output, updateButton);
    }

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

        JButton deleteButton = new JButton("Delete Bill");
        deleteButton.addActionListener(e -> {
            boolean success = fileManager.deleteBill(idField.getText(), monthField.getText());
            output.setText(success ? "Bill deleted." : "Error: Bill not found.");
        });

        return buildPageFrame("Delete Bill", form, output, deleteButton);
    }

    private JPanel buildReportPanel() {
        JPanel form = new JPanel(new GridLayout(1, 2, 5, 5));
        JTextField idField = new JTextField();
        form.add(new JLabel("Customer ID:"));
        form.add(idField);

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

            StringBuilder report = new StringBuilder();
            report.append("Customer Name: ").append(customer.getName()).append("\n");
            report.append("Customer ID: ").append(customer.getCustomerId()).append("\n");
            report.append("Address: ").append(customer.getAddress()).append("\n\n");
            report.append(String.format("%-15s%-10s%-12s%-12s%-10s%n", "MONTH", "UNITS", "READING", "AMOUNT", "STATUS"));

            int totalUnits = 0;
            int paidCount = 0;
            int unpaidCount = 0;
            double totalBilled = 0;
            double totalPaid = 0;
            double totalDue = 0;

            for (Bill bill : bills) {
                double cost = bill.getUnitsUsed() * RATE_PER_UNIT;
                report.append(String.format("%-15s%-10d%-12d%-12.2f%-10s%n",
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

            report.append("\nTotal Units: ").append(totalUnits);
            report.append("\nPaid Bills: ").append(paidCount).append("   Unpaid Bills: ").append(unpaidCount);
            report.append("\nTotal Billed: ").append(totalBilled).append(" Taka");
            report.append("\nTotal Paid: ").append(totalPaid).append(" Taka");
            report.append("\nTotal Due: ").append(totalDue).append(" Taka");

            output.setText(report.toString());
        });

        return buildPageFrame("Annual Bill Report", form, output, generateButton);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
