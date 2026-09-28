import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class Main extends JFrame {
    private final FileManager fileManager = new FileManager();
    private static final double RATE_PER_UNIT = 5.0;

    // ================= THEME (matches the presentation) =================
    private static final Color BG         = new Color(0x0C0C12);
    private static final Color PANEL      = new Color(0x13131B);
    private static final Color CARD       = new Color(0x1B1B26);
    private static final Color CARD_HOVER = new Color(0x2A2418);
    private static final Color BORDER_DIM = new Color(0x3A3122);
    private static final Color GOLD       = new Color(0xC9A050);
    private static final Color GOLD_LIGHT = new Color(0xE8C77D);
    private static final Color CREAM      = new Color(0xF4EFE6);
    private static final Color MUTED      = new Color(0xA29D92);
    private static final Color ERROR_RED  = new Color(0xE27D6A);

    private static final String SERIF = pickFont("Cambria", "Georgia", Font.SERIF);
    private static final String SANS  = pickFont("Calibri", "Segoe UI", Font.SANS_SERIF);

    private static String pickFont(String... names) {
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String wanted : names) {
            for (String have : available) {
                if (have.equalsIgnoreCase(wanted)) return have;
            }
        }
        return names[names.length - 1];
    }

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);

    public Main() {
        setTitle("Electricity Management System");
        setSize(650, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        container.setBackground(BG);

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

    // ================= STYLING HELPERS =================

    // Dark page with a soft gold glow in the top-right corner (like the slide backgrounds)
    private static class GlowPanel extends JPanel {
        GlowPanel(LayoutManager layout) {
            super(layout);
            setBackground(BG);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            float radius = Math.max(getWidth(), getHeight()) * 0.9f;
            g2.setPaint(new RadialGradientPaint(
                    new Point(getWidth(), 0), radius,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0xC9, 0xA0, 0x50, 70), new Color(0xC9, 0xA0, 0x50, 0)}));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    // Rounded button: "card" style (dark, gold outline) or "primary" style (solid gold)
    private static class GoldButton extends JButton {
        private final boolean primary;
        private boolean hover = false;

        GoldButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(new Font(SANS, primary ? Font.BOLD : Font.PLAIN, 14));
            setForeground(primary ? BG : CREAM);
            setMargin(new Insets(8, 18, 8, 18));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(java.awt.event.MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            RoundRectangle2D shape = new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
            if (primary) {
                g2.setColor(hover ? GOLD_LIGHT : GOLD);
                g2.fill(shape);
            } else {
                g2.setColor(hover ? CARD_HOVER : CARD);
                g2.fill(shape);
                g2.setColor(hover ? GOLD : BORDER_DIM);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(shape);
            }
            g2.dispose();
            super.paintComponent(g); // draws the text on top
        }
    }

    private JButton primaryButton(String text) {
        return new GoldButton(text, true);
    }

    private JButton cardButton(String text) {
        return new GoldButton(text, false);
    }

    private Border fieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_DIM, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8));
    }

    private JTextField styledField() {
        JTextField f = new JTextField();
        f.setBackground(CARD);
        f.setForeground(CREAM);
        f.setCaretColor(GOLD_LIGHT);
        f.setSelectionColor(GOLD);
        f.setSelectedTextColor(BG);
        f.setFont(new Font(SANS, Font.PLAIN, 14));
        f.setBorder(fieldBorder());
        return f;
    }

    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(GOLD_LIGHT);
        l.setFont(new Font(SANS, Font.BOLD, 13));
        return l;
    }

    private JTextArea styledOutput(int rows, int cols, boolean monospaced) {
        JTextArea area = new JTextArea(rows, cols);
        area.setEditable(false);
        area.setBackground(PANEL);
        area.setForeground(CREAM);
        area.setCaretColor(GOLD_LIGHT);
        area.setMargin(new Insets(10, 12, 10, 12));
        area.setFont(new Font(monospaced ? Font.MONOSPACED : SANS, Font.PLAIN, monospaced ? 12 : 14));
        return area;
    }

    private JPanel styledForm(int rows) {
        JPanel form = new JPanel(new GridLayout(rows, 2, 10, 10));
        form.setOpaque(false);
        return form;
    }

    // Shows a message in the output area; errors appear in soft red, everything else in cream
    private void show(JTextArea output, String message) {
        boolean isError = message.startsWith("Error") || message.contains("not found");
        output.setForeground(isError ? ERROR_RED : CREAM);
        output.setText(message);
        output.setCaretPosition(0);
    }

    // ================= REUSABLE HELPERS =================

    // Every feature page ends with this button, which always returns to Home
    private JButton buildBackButton() {
        JButton back = cardButton("Back to Home");
        back.addActionListener(e -> cardLayout.show(container, "home"));
        return back;
    }

    // Standard page skeleton: title at top, form/output in the middle, action+back buttons at bottom
    private JPanel buildPageFrame(String title, JComponent formArea, JTextArea output, JButton... actionButtons) {
        JPanel page = new GlowPanel(new BorderLayout(10, 10));
        page.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel eyebrow = new JLabel("ELECTRICITY MANAGEMENT SYSTEM", SwingConstants.CENTER);
        eyebrow.setFont(new Font(SANS, Font.BOLD, 11));
        eyebrow.setForeground(GOLD);
        eyebrow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font(SERIF, Font.BOLD, 22));
        titleLabel.setForeground(CREAM);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(eyebrow);
        header.add(Box.createVerticalStrut(4));
        header.add(titleLabel);
        page.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 14));
        center.setOpaque(false);
        center.add(formArea, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(output);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_DIM, 1));
        scroll.getViewport().setBackground(PANEL);
        center.add(scroll, BorderLayout.CENTER);
        page.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        bottom.setOpaque(false);
        for (JButton b : actionButtons) bottom.add(b);
        bottom.add(buildBackButton());
        page.add(bottom, BorderLayout.SOUTH);

        return page;
    }

    // ================= HOME PAGE =================

    private JPanel buildHomePanel() {
        JPanel page = new GlowPanel(new BorderLayout());
        page.setBorder(BorderFactory.createEmptyBorder(24, 60, 20, 60));

        JLabel eyebrow = new JLabel("JAVA SWING  \u00B7  OBJECT-ORIENTED  \u00B7  FILE-BASED", SwingConstants.CENTER);
        eyebrow.setFont(new Font(SANS, Font.BOLD, 11));
        eyebrow.setForeground(GOLD);
        eyebrow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel(
                "<html><span style='color:#F4EFE6'>Electricity </span>"
                        + "<i><span style='color:#E8C77D'>Management</span></i>"
                        + "<span style='color:#F4EFE6'> System</span></html>",
                SwingConstants.CENTER);
        title.setFont(new Font(SERIF, Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(eyebrow);
        header.add(Box.createVerticalStrut(6));
        header.add(title);
        page.add(header, BorderLayout.NORTH);

        JPanel buttonGrid = new JPanel(new GridLayout(0, 2, 15, 15));
        buttonGrid.setOpaque(false);
        buttonGrid.setBorder(BorderFactory.createEmptyBorder(22, 0, 18, 0));

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
            JButton btn = cardButton(b[0]);
            btn.addActionListener(e -> cardLayout.show(container, b[1]));
            buttonGrid.add(btn);
        }

        page.add(buttonGrid, BorderLayout.CENTER);

        JButton exitBtn = primaryButton("Exit");
        exitBtn.addActionListener(e -> System.exit(0));
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(exitBtn);
        page.add(bottom, BorderLayout.SOUTH);

        return page;
    }

    // ================= CUSTOMER: REGISTER =================

    private JPanel buildRegisterPanel() {
        JPanel form = styledForm(4);
        JTextField idField = styledField();
        JTextField nameField = styledField();
        JTextField addressField = styledField();
        JTextField meterField = styledField();

        form.add(styledLabel("Customer ID:"));
        form.add(idField);
        form.add(styledLabel("Name:"));
        form.add(nameField);
        form.add(styledLabel("Address:"));
        form.add(addressField);
        form.add(styledLabel("Meter Number:"));
        form.add(meterField);

        JTextArea output = styledOutput(6, 40, false);

        JButton registerBtn = primaryButton("Register");
        registerBtn.addActionListener(e -> {
            try {
                Customer c = new Customer(idField.getText(), nameField.getText(),
                        addressField.getText(), meterField.getText());
                fileManager.saveCustomer(c);
                show(output, "Customer registered successfully!");
                idField.setText(""); nameField.setText(""); addressField.setText(""); meterField.setText("");
            } catch (FileManager.DuplicateCustomerException ex) {
                show(output, ex.getMessage());
            }
        });

        return buildPageFrame("Register New Customer", form, output, registerBtn);
    }

    // ================= CUSTOMER: SEARCH =================

    private JPanel buildSearchPanel() {
        JPanel form = styledForm(1);
        JTextField idField = styledField();
        form.add(styledLabel("Customer ID:"));
        form.add(idField);

        JTextArea output = styledOutput(6, 40, false);

        JButton searchBtn = primaryButton("Search");
        searchBtn.addActionListener(e -> {
            Customer c = fileManager.findCustomerById(idField.getText());
            if (c == null) {
                show(output, "Customer not found.");
            } else {
                show(output, "Customer ID: " + c.getCustomerId() +
                        "\nName: " + c.getName() +
                        "\nAddress: " + c.getAddress() +
                        "\nMeter Number: " + c.getMeterNumber());
            }
        });

        return buildPageFrame("Search Customer", form, output, searchBtn);
    }

    // ================= CUSTOMER: UPDATE =================

    private JPanel buildUpdatePanel() {
        JPanel form = styledForm(4);
        JTextField idField = styledField();
        JTextField nameField = styledField();
        JTextField addressField = styledField();
        JTextField meterField = styledField();

        form.add(styledLabel("Customer ID:"));
        form.add(idField);
        form.add(styledLabel("New Name:"));
        form.add(nameField);
        form.add(styledLabel("New Address:"));
        form.add(addressField);
        form.add(styledLabel("New Meter Number:"));
        form.add(meterField);

        JTextArea output = styledOutput(6, 40, false);

        JButton loadBtn = cardButton("Load Current Info");
        JButton updateBtn = primaryButton("Update");

        loadBtn.addActionListener(e -> {
            Customer c = fileManager.findCustomerById(idField.getText());
            if (c == null) {
                show(output, "Customer not found.");
            } else {
                nameField.setText(c.getName());
                addressField.setText(c.getAddress());
                meterField.setText(c.getMeterNumber());
                show(output, "Current info loaded. Edit the fields above, then click Update.");
            }
        });

        updateBtn.addActionListener(e -> {
            try {
                Customer c = new Customer(idField.getText(), nameField.getText(),
                        addressField.getText(), meterField.getText());
                fileManager.updateCustomer(c);
                show(output, "Customer updated successfully!");
            } catch (FileManager.CustomerNotFoundException ex) {
                show(output, ex.getMessage());
            }
        });

        return buildPageFrame("Update Customer", form, output, loadBtn, updateBtn);
    }

    // ================= CUSTOMER: DELETE =================

    private JPanel buildDeletePanel() {
        JPanel form = styledForm(1);
        JTextField idField = styledField();
        form.add(styledLabel("Customer ID:"));
        form.add(idField);

        JTextArea output = styledOutput(6, 40, false);

        JButton deleteBtn = primaryButton("Delete");
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete customer " + idField.getText() + " and all their records?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                show(output, "Delete cancelled.");
                return;
            }

            try {
                fileManager.deleteCustomer(idField.getText());
                show(output, "Customer and their records deleted successfully.");
                idField.setText("");
            } catch (FileManager.CustomerNotFoundException ex) {
                show(output, ex.getMessage());
            }
        });

        return buildPageFrame("Delete Customer", form, output, deleteBtn);
    }

    // ================= METER: ADD READING =================

    private JPanel buildMeterPanel() {
        JPanel form = styledForm(2);
        JTextField idField = styledField();
        JTextField currentField = styledField();

        form.add(styledLabel("Customer ID:"));
        form.add(idField);
        form.add(styledLabel("Current Reading:"));
        form.add(currentField);

        JTextArea output = styledOutput(6, 40, false);

        JButton addBtn = primaryButton("Add Meter Reading");
        addBtn.addActionListener(e -> {
            Customer customer = fileManager.findCustomerById(idField.getText());
            if (customer == null) {
                show(output, "Error: Customer does not exist.");
                return;
            }

            try {
                int currentReading = Integer.parseInt(currentField.getText());
                Meter lastMeter = fileManager.findMeterByCustomerId(idField.getText());
                int previousReading = (lastMeter == null) ? 0 : lastMeter.getCurrentReading();

                if (currentReading < previousReading) {
                    show(output, "Error: Current reading cannot be less than previous reading (" + previousReading + ").");
                    return;
                }

                Meter meter = new Meter(idField.getText(), customer.getMeterNumber(), previousReading, currentReading);
                fileManager.saveMeter(meter);
                show(output, "Reading saved!\nPrevious: " + previousReading +
                        "\nCurrent: " + currentReading +
                        "\nUnits Used: " + meter.getUnitsUsed());
                currentField.setText("");
            } catch (NumberFormatException ex) {
                show(output, "Error: Please enter a valid number for the reading.");
            }
        });

        return buildPageFrame("Add Meter Reading", form, output, addBtn);
    }

    // ================= BILL: ADD =================

    private JPanel buildBillAddPanel() {
        JPanel form = styledForm(3);
        JTextField idField = styledField();
        JTextField monthField = styledField();
        JTextField statusField = styledField();

        form.add(styledLabel("Customer ID:"));
        form.add(idField);
        form.add(styledLabel("Month:"));
        form.add(monthField);
        form.add(styledLabel("Status (Paid/Unpaid):"));
        form.add(statusField);

        JTextArea output = styledOutput(6, 40, false);

        JButton addBtn = primaryButton("Add Bill");
        addBtn.addActionListener(e -> {
            Customer customer = fileManager.findCustomerById(idField.getText());
            if (customer == null) {
                show(output, "Error: Customer does not exist.");
                return;
            }

            Meter meter = fileManager.findNextUnbilledMeter(idField.getText());
            if (meter == null) {
                show(output, "Error: No unbilled meter reading found.");
                return;
            }

            String status = statusField.getText();
            if (!status.equalsIgnoreCase("Paid") && !status.equalsIgnoreCase("Unpaid")) {
                show(output, "Error: Status must be 'Paid' or 'Unpaid'.");
                return;
            }

            Bill bill = new Bill(idField.getText(), monthField.getText(),
                    meter.getUnitsUsed(), meter.getCurrentReading(), status);
            fileManager.saveBill(bill);

            double cost = bill.getUnitsUsed() * RATE_PER_UNIT;
            show(output, "Bill created!\nUnits: " + bill.getUnitsUsed() +
                    "\nReading: " + bill.getReading() +
                    "\nAmount: " + cost + " Taka" +
                    "\nStatus: " + bill.getStatus());
        });

        return buildPageFrame("Add Monthly Bill", form, output, addBtn);
    }

    // ================= BILL: UPDATE STATUS =================

    private JPanel buildBillUpdatePanel() {
        JPanel form = styledForm(3);
        JTextField idField = styledField();
        JTextField monthField = styledField();
        JTextField statusField = styledField();

        form.add(styledLabel("Customer ID:"));
        form.add(idField);
        form.add(styledLabel("Month:"));
        form.add(monthField);
        form.add(styledLabel("New Status (Paid/Unpaid):"));
        form.add(statusField);

        JTextArea output = styledOutput(6, 40, false);

        JButton updateBtn = primaryButton("Update Status");
        updateBtn.addActionListener(e -> {
            String status = statusField.getText();
            if (!status.equalsIgnoreCase("Paid") && !status.equalsIgnoreCase("Unpaid")) {
                show(output, "Error: Status must be 'Paid' or 'Unpaid'.");
                return;
            }
            boolean success = fileManager.updateBillStatus(idField.getText(), monthField.getText(), status);
            show(output, success ? "Bill status updated." : "Error: Bill not found.");
        });

        return buildPageFrame("Update Bill Status", form, output, updateBtn);
    }

    // ================= BILL: DELETE =================

    private JPanel buildBillDeletePanel() {
        JPanel form = styledForm(2);
        JTextField idField = styledField();
        JTextField monthField = styledField();

        form.add(styledLabel("Customer ID:"));
        form.add(idField);
        form.add(styledLabel("Month:"));
        form.add(monthField);

        JTextArea output = styledOutput(6, 40, false);

        JButton deleteBtn = primaryButton("Delete Bill");
        deleteBtn.addActionListener(e -> {
            boolean success = fileManager.deleteBill(idField.getText(), monthField.getText());
            show(output, success ? "Bill deleted." : "Error: Bill not found.");
        });

        return buildPageFrame("Delete Bill", form, output, deleteBtn);
    }

    // ================= ANNUAL REPORT =================

    private JPanel buildReportPanel() {
        JPanel form = styledForm(1);
        JTextField idField = styledField();
        form.add(styledLabel("Customer ID:"));
        form.add(idField);

        JTextArea output = styledOutput(14, 50, true);

        JButton generateBtn = primaryButton("Generate Report");
        generateBtn.addActionListener(e -> {
            Customer customer = fileManager.findCustomerById(idField.getText());
            if (customer == null) {
                show(output, "Error: Customer does not exist.");
                return;
            }

            List<Bill> bills = fileManager.findBillsByCustomerId(idField.getText());
            if (bills.isEmpty()) {
                show(output, "No bills found for this customer yet.");
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

            show(output, sb.toString());
        });

        return buildPageFrame("Annual Bill Report", form, output, generateBtn);
    }

    // Dark theme for the JOptionPane confirmation dialog on the Delete screen
    private static void applyDialogTheme() {
        UIManager.put("OptionPane.background", PANEL);
        UIManager.put("OptionPane.messageForeground", CREAM);
        UIManager.put("OptionPane.messageFont", new Font(SANS, Font.PLAIN, 14));
        UIManager.put("Panel.background", PANEL);
        UIManager.put("Button.background", CARD);
        UIManager.put("Button.foreground", CREAM);
        UIManager.put("Button.font", new Font(SANS, Font.PLAIN, 13));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // falls back to the default look and feel
            }
            applyDialogTheme();
            new Main().setVisible(true);
        });
    }
}