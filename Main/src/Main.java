import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * SmartElectricity Management System - GUI entry point.
 * IMPORTANT: this file must be saved exactly as "Main.java".
 */
public class Main extends JFrame {

    private final FileManager fileManager = new FileManager();
    private static final double RATE_PER_UNIT = 5.0;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);

    // ================= THEME CONSTANTS =================
    private static final Color BG_DARK     = new Color(0x13, 0x13, 0x1B);
    private static final Color BG_PANEL    = new Color(0x1B, 0x1B, 0x26);
    private static final Color BG_PANEL_2  = new Color(0x0C, 0x0C, 0x12);
    private static final Color GOLD        = new Color(0xC9, 0xA0, 0x50);
    private static final Color GOLD_LIGHT  = new Color(0xE8, 0xC7, 0x7D);
    private static final Color CREAM       = new Color(0xF4, 0xEF, 0xE6);
    private static final Color MUTED       = new Color(0xA2, 0x9D, 0x92);

    private static final Font FONT_TITLE  = new Font("Cambria", Font.BOLD, 26);
    private static final Font FONT_HEADER = new Font("Cambria", Font.BOLD, 20);
    private static final Font FONT_LABEL  = new Font("Calibri", Font.PLAIN, 14);
    private static final Font FONT_BTN    = new Font("Calibri", Font.BOLD, 14);
    private static final Font FONT_MONO   = new Font("Courier New", Font.PLAIN, 13);

    public Main() {
        setTitle("SmartElectricity Management System");
        setSize(760, 560);
        setMinimumSize(new Dimension(680, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        container.setBackground(BG_DARK);
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

    // ================= REUSABLE UI BUILDING BLOCKS =================

    private JButton goldButton(String text) {
        JButton b = new JButton(text);
        styleButtonBase(b, GOLD, BG_DARK, GOLD_LIGHT);
        return b;
    }

    private JButton ghostButton(String text) {
        JButton b = new JButton(text);
        styleButtonBase(b, BG_PANEL, CREAM, BG_PANEL);
        b.setBorder(new CompoundBorder(new LineBorder(GOLD, 1, true),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setForeground(GOLD_LIGHT); }
            public void mouseExited(java.awt.event.MouseEvent e)  { b.setForeground(CREAM); }
        });
        return b;
    }

    private void styleButtonBase(JButton b, Color bg, Color fg, Color hoverBg) {
        b.setFont(FONT_BTN);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        final Color base = bg;
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(hoverBg); }
            public void mouseExited(java.awt.event.MouseEvent e)  { b.setBackground(base); }
        });
    }

    private JButton buildBackButton() {
        JButton back = ghostButton("Back to Home");
        back.addActionListener(e -> cardLayout.show(container, "home"));
        return back;
    }

    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(CREAM);
        return l;
    }

    private JTextField styledField() {
        JTextField f = new JTextField();
        f.setFont(FONT_LABEL);
        f.setBackground(BG_PANEL);
        f.setForeground(CREAM);
        f.setCaretColor(GOLD_LIGHT);
        f.setBorder(new CompoundBorder(new LineBorder(MUTED, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return f;
    }

    private JTextArea styledOutput(int rows, int cols) {
        JTextArea area = new JTextArea(rows, cols);
        area.setEditable(false);
        area.setFont(FONT_MONO);
        area.setBackground(BG_PANEL_2);
        area.setForeground(GOLD_LIGHT);
        area.setCaretColor(GOLD_LIGHT);
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return area;
    }

    private JPanel formGrid(int rows) {
        JPanel form = new JPanel(new GridLayout(rows, 2, 10, 10));
        form.setBackground(BG_DARK);
        form.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        return form;
    }

    private void addRow(JPanel form, String labelText, JTextField field) {
        form.add(styledLabel(labelText));
        form.add(field);
    }

    /** True if any of the given fields is empty or only spaces. */
    private boolean anyBlank(JTextField... fields) {
        for (JTextField f : fields) {
            if (f.getText().trim().isEmpty()) return true;
        }
        return false;
    }

    private JPanel buildPageFrame(String title, JComponent formArea, JTextArea output, JButton... actionButtons) {
        JPanel page = new JPanel(new BorderLayout(10, 15));
        page.setBackground(BG_DARK);
        page.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        page.add(pageHeader(title), BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(BG_DARK);
        card.add(formArea, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(output);
        scroll.setBorder(new LineBorder(MUTED, 1));
        card.add(scroll, BorderLayout.CENTER);
        page.add(card, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        bottom.setBackground(BG_DARK);
        for (JButton b : actionButtons) bottom.add(b);
        bottom.add(buildBackButton());
        page.add(bottom, BorderLayout.SOUTH);

        return page;
    }

    private JPanel pageHeader(String title) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(CREAM);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        header.add(titleLabel, BorderLayout.CENTER);

        JPanel divider = new JPanel();
        divider.setBackground(GOLD);
        divider.setPreferredSize(new Dimension(10, 2));
        header.add(divider, BorderLayout.SOUTH);

        return header;
    }

    // ================= HOME PAGE =================

    private JPanel buildHomePanel() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BG_DARK);
        page.setBorder(BorderFactory.createEmptyBorder(35, 60, 30, 60));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setBackground(BG_DARK);

        JLabel title = new JLabel("SmartElectricity Management System");
        title.setFont(FONT_TITLE);
        title.setForeground(CREAM);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Customer, Meter & Billing Console");
        subtitle.setFont(FONT_LABEL);
        subtitle.setForeground(GOLD_LIGHT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        titleBlock.add(title);
        titleBlock.add(subtitle);
        page.add(titleBlock, BorderLayout.NORTH);

        JPanel buttonGrid = new JPanel(new GridLayout(0, 3, 18, 18));
        buttonGrid.setBackground(BG_DARK);
        buttonGrid.setBorder(BorderFactory.createEmptyBorder(35, 0, 35, 0));

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
            JButton btn = ghostButton(b[0]);
            btn.addActionListener(e -> cardLayout.show(container, b[1]));
            buttonGrid.add(btn);
        }

        page.add(buttonGrid, BorderLayout.CENTER);

        JButton exitBtn = goldButton("Exit");
        exitBtn.addActionListener(e -> System.exit(0));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setBackground(BG_DARK);
        bottom.add(exitBtn);
        page.add(bottom, BorderLayout.SOUTH);

        return page;
    }

    // ================= CUSTOMER: REGISTER =================

    private JPanel buildRegisterPanel() {
        JPanel form = formGrid(4);
        JTextField idField = styledField();
        JTextField nameField = styledField();
        JTextField addressField = styledField();
        JTextField meterField = styledField();

        addRow(form, "Customer ID:", idField);
        addRow(form, "Name:", nameField);
        addRow(form, "Address:", addressField);
        addRow(form, "Meter Number:", meterField);

        JTextArea output = styledOutput(6, 40);

        JButton registerBtn = goldButton("Register");
        registerBtn.addActionListener(e -> {
            if (anyBlank(idField, nameField, addressField, meterField)) {
                output.setText("Error: Please fill in all fields.");
                return;
            }
            try {
                Customer c = new Customer(idField.getText().trim(), nameField.getText().trim(),
                        addressField.getText().trim(), meterField.getText().trim());
                fileManager.saveCustomer(c);
                output.setText("Customer registered successfully!");
                idField.setText(""); nameField.setText("");
                addressField.setText(""); meterField.setText("");
            } catch (FileManager.DuplicateCustomerException ex) {
                output.setText(ex.getMessage());
            }
        });

        return buildPageFrame("Register New Customer", form, output, registerBtn);
    }

    // ================= CUSTOMER: SEARCH =================

    private JPanel buildSearchPanel() {
        JPanel form = formGrid(1);
        JTextField idField = styledField();
        addRow(form, "Customer ID:", idField);

        JTextArea output = styledOutput(6, 40);

        JButton searchBtn = goldButton("Search");
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
        JPanel form = formGrid(4);
        JTextField idField = styledField();
        JTextField nameField = styledField();
        JTextField addressField = styledField();
        JTextField meterField = styledField();

        addRow(form, "Customer ID:", idField);
        addRow(form, "New Name:", nameField);
        addRow(form, "New Address:", addressField);
        addRow(form, "New Meter Number:", meterField);

        JTextArea output = styledOutput(6, 40);

        JButton loadBtn = ghostButton("Load Current Info");
        JButton updateBtn = goldButton("Update");

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
            if (anyBlank(idField, nameField, addressField, meterField)) {
                output.setText("Error: Please fill in all fields.");
                return;
            }
            try {
                Customer c = new Customer(idField.getText().trim(), nameField.getText().trim(),
                        addressField.getText().trim(), meterField.getText().trim());
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
        JPanel form = formGrid(1);
        JTextField idField = styledField();
        addRow(form, "Customer ID:", idField);

        JTextArea output = styledOutput(6, 40);

        JButton deleteBtn = goldButton("Delete");
        deleteBtn.addActionListener(e -> {
            if (anyBlank(idField)) {
                output.setText("Error: Please enter a Customer ID.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete customer " + idField.getText().trim() + " and all their records?",
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
        JPanel form = formGrid(2);
        JTextField idField = styledField();
        JTextField currentField = styledField();

        addRow(form, "Customer ID:", idField);
        addRow(form, "Current Reading:", currentField);

        JTextArea output = styledOutput(6, 40);

        JButton addBtn = goldButton("Add Meter Reading");
        addBtn.addActionListener(e -> {
            Customer customer = fileManager.findCustomerById(idField.getText());
            if (customer == null) {
                output.setText("Error: Customer does not exist.");
                return;
            }

            try {
                int currentReading = Integer.parseInt(currentField.getText().trim());
                Meter lastMeter = fileManager.findMeterByCustomerId(idField.getText());
                int previousReading = (lastMeter == null) ? 0 : lastMeter.getCurrentReading();

                if (currentReading < previousReading) {
                    output.setText("Error: Current reading cannot be less than previous reading ("
                            + previousReading + ").");
                    return;
                }

                Meter meter = new Meter(customer.getCustomerId(), customer.getMeterNumber(),
                        previousReading, currentReading);
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
        JPanel form = formGrid(3);
        JTextField idField = styledField();
        JTextField monthField = styledField();
        JTextField statusField = styledField();

        addRow(form, "Customer ID:", idField);
        addRow(form, "Month:", monthField);
        addRow(form, "Status (Paid/Unpaid):", statusField);

        JTextArea output = styledOutput(6, 40);

        JButton addBtn = goldButton("Add Bill");
        addBtn.addActionListener(e -> {
            Customer customer = fileManager.findCustomerById(idField.getText());
            if (customer == null) {
                output.setText("Error: Customer does not exist.");
                return;
            }

            if (anyBlank(monthField)) {
                output.setText("Error: Please enter the month.");
                return;
            }

            String status = statusField.getText().trim();
            if (!status.equalsIgnoreCase("Paid") && !status.equalsIgnoreCase("Unpaid")) {
                output.setText("Error: Status must be 'Paid' or 'Unpaid'.");
                return;
            }

            Meter meter = fileManager.findNextUnbilledMeter(idField.getText());
            if (meter == null) {
                output.setText("Error: No unbilled meter reading found.");
                return;
            }

            Bill bill = new Bill(customer.getCustomerId(), monthField.getText().trim(),
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
        JPanel form = formGrid(3);
        JTextField idField = styledField();
        JTextField monthField = styledField();
        JTextField statusField = styledField();

        addRow(form, "Customer ID:", idField);
        addRow(form, "Month:", monthField);
        addRow(form, "New Status (Paid/Unpaid):", statusField);

        JTextArea output = styledOutput(6, 40);

        JButton updateBtn = goldButton("Update Status");
        updateBtn.addActionListener(e -> {
            String status = statusField.getText().trim();
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
        JPanel form = formGrid(2);
        JTextField idField = styledField();
        JTextField monthField = styledField();

        addRow(form, "Customer ID:", idField);
        addRow(form, "Month:", monthField);

        JTextArea output = styledOutput(6, 40);

        JButton deleteBtn = goldButton("Delete Bill");
        deleteBtn.addActionListener(e -> {
            boolean success = fileManager.deleteBill(idField.getText(), monthField.getText());
            output.setText(success ? "Bill deleted." : "Error: Bill not found.");
        });

        return buildPageFrame("Delete Bill", form, output, deleteBtn);
    }

    // ================= ANNUAL REPORT =================

    private JPanel buildReportPanel() {
        JPanel form = formGrid(1);
        JTextField idField = styledField();
        addRow(form, "Customer ID:", idField);

        JTextArea output = styledOutput(14, 50);

        JButton generateBtn = goldButton("Generate Report");
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
            sb.append(String.format("%-15s%-10s%-12s%-12s%-10s%n",
                    "MONTH", "UNITS", "READING", "AMOUNT", "STATUS"));

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
            sb.append("\nPaid Bills: ").append(paidCount)
                    .append("   Unpaid Bills: ").append(unpaidCount);
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