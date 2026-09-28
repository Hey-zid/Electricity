import java.io.*;
import java.util.*;

public class FileManager {

    private static final String CUSTOMER_FILE = "customers.txt";
    private static final String METER_FILE = "meters.txt";
    private static final String BILL_FILE = "bills.txt";

    // Exception for duplicate customer
    public static class DuplicateCustomerException extends Exception {
        public DuplicateCustomerException(String id) {
            super("Error: Customer ID " + id + " already exists.");
        }
    }

    // Exception for customer not found
    public static class CustomerNotFoundException extends Exception {
        public CustomerNotFoundException(String id) {
            super("Error: Customer with ID " + id + " was not found.");
        }
    }

    // Read all lines from a file
    private List<String> readLines(String fileName) {
        List<String> lines = new ArrayList<>();

        File file = new File(fileName);

        if (!file.exists()) {
            return lines;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

            reader.close();

        } catch (IOException e) {
            System.out.println("Error reading " + fileName + ": " + e.getMessage());
        }

        return lines;
    }

    // Write all lines to a file
    private void writeLines(String fileName, List<String> lines) {
        try {
            FileWriter writer = new FileWriter(fileName);

            for (String line : lines) {
                writer.write(line + System.lineSeparator());
            }

            writer.close();

        } catch (IOException e) {
            System.out.println("Error writing " + fileName + ": " + e.getMessage());
        }
    }

    // Add one line at the end of a file
    private void appendLine(String fileName, String line) {
        try {
            FileWriter writer = new FileWriter(fileName, true);

            writer.write(line + System.lineSeparator());

            writer.close();

        } catch (IOException e) {
            System.out.println("Error saving to " + fileName + ": " + e.getMessage());
        }
    }

    // Find rows that belong to a customer
    private List<String[]> readRowsForCustomer(
            String fileName, int columnCount, String id) {

        List<String[]> rows = new ArrayList<>();

        List<String> lines = readLines(fileName);

        for (String line : lines) {
            String[] data = line.split(",");

            if (data.length == columnCount && data[0].equals(id)) {
                rows.add(data);
            }
        }

        return rows;
    }

    // Update or delete a customer
    private boolean replaceOrDeleteCustomerLine(String id, String newLine) {

        List<String> oldLines = readLines(CUSTOMER_FILE);
        List<String> newLines = new ArrayList<>();

        boolean found = false;

        for (String line : oldLines) {

            String[] data = line.split(",");

            if (data.length > 0 && data[0].equals(id)) {

                found = true;

                // If newLine is not null, update the customer
                if (newLine != null) {
                    newLines.add(newLine);
                }

            } else {
                newLines.add(line);
            }
        }

        if (found) {
            writeLines(CUSTOMER_FILE, newLines);
        }

        return found;
    }

    // Delete all meter/bill records of a customer
    private void removeAllForCustomer(String fileName, String id) {

        List<String> newLines = new ArrayList<>();

        for (String line : readLines(fileName)) {

            String[] data = line.split(",");

            if (data.length > 0 && !data[0].equals(id)) {
                newLines.add(line);
            }
        }

        writeLines(fileName, newLines);
    }

    // Convert file data into Meter object
    private Meter toMeter(String[] data) {
        return new Meter(
                data[0],
                data[1],
                Integer.parseInt(data[2]),
                Integer.parseInt(data[3])
        );
    }

    // Convert file data into Bill object
    private Bill toBill(String[] data) {
        return new Bill(
                data[0],
                data[1],
                Integer.parseInt(data[2]),
                Integer.parseInt(data[3]),
                data[4]
        );
    }

    // Save a new customer
    public void saveCustomer(Customer customer)
            throws DuplicateCustomerException {

        String id = customer.getCustomerId();

        if (isCustomerIdTaken(id)) {
            throw new DuplicateCustomerException(id);
        }

        appendLine(CUSTOMER_FILE, customer.toFileString());
    }

    // Check whether customer ID already exists
    public boolean isCustomerIdTaken(String id) {
        return findCustomerById(id) != null;
    }

    // Find customer using ID
    public Customer findCustomerById(String id) {

        List<String[]> rows =
                readRowsForCustomer(CUSTOMER_FILE, 4, id);

        if (rows.isEmpty()) {
            return null;
        }

        String[] data = rows.get(0);

        return new Customer(
                data[0],
                data[1],
                data[2],
                data[3]
        );
    }

    // Update customer information
    public void updateCustomer(Customer customer)
            throws CustomerNotFoundException {

        String id = customer.getCustomerId();

        boolean updated =
                replaceOrDeleteCustomerLine(id, customer.toFileString());

        if (!updated) {
            throw new CustomerNotFoundException(id);
        }
    }

    // Delete customer and all related records
    public void deleteCustomer(String id)
            throws CustomerNotFoundException {

        boolean deleted =
                replaceOrDeleteCustomerLine(id, null);

        if (!deleted) {
            throw new CustomerNotFoundException(id);
        }

        removeAllForCustomer(METER_FILE, id);
        removeAllForCustomer(BILL_FILE, id);
    }

    // Save meter information
    public void saveMeter(Meter meter) {
        appendLine(METER_FILE, meter.toFileString());
    }

    // Find all meters of a customer
    public List<Meter> findAllMetersByCustomerId(String id) {

        List<Meter> meters = new ArrayList<>();

        List<String[]> rows =
                readRowsForCustomer(METER_FILE, 4, id);

        for (String[] data : rows) {
            meters.add(toMeter(data));
        }

        return meters;
    }

    // Find the latest meter of a customer
    public Meter findMeterByCustomerId(String id) {

        List<Meter> meters =
                findAllMetersByCustomerId(id);

        if (meters.isEmpty()) {
            return null;
        }

        return meters.get(meters.size() - 1);
    }

    // Find the first meter that has not been billed
    public Meter findNextUnbilledMeter(String id) {

        List<Bill> bills = findBillsByCustomerId(id);
        List<Meter> meters = findAllMetersByCustomerId(id);

        for (Meter meter : meters) {

            if (!isBilled(meter, bills)) {
                return meter;
            }
        }

        return null;
    }

    // Check whether a meter reading is already billed
    private boolean isBilled(Meter meter, List<Bill> bills) {

        for (Bill bill : bills) {

            if (bill.getReading() == meter.getCurrentReading()) {
                return true;
            }
        }

        return false;
    }

    // Save a bill
    public void saveBill(Bill bill) {
        appendLine(BILL_FILE, bill.toFileString());
    }

    // Find all bills of a customer
    public List<Bill> findBillsByCustomerId(String id) {

        List<Bill> bills = new ArrayList<>();

        List<String[]> rows =
                readRowsForCustomer(BILL_FILE, 5, id);

        for (String[] data : rows) {
            bills.add(toBill(data));
        }

        return bills;
    }

    // Update bill status
    public boolean updateBillStatus(
            String id, String month, String newStatus) {

        return changeBill(id, month, newStatus);
    }

    // Delete a bill
    public boolean deleteBill(String id, String month) {

        return changeBill(id, month, null);
    }

    // Update or delete a bill
    private boolean changeBill(
            String id, String month, String newStatus) {

        List<String> oldLines = readLines(BILL_FILE);
        List<String> newLines = new ArrayList<>();

        boolean found = false;

        for (String line : oldLines) {

            String[] data = line.split(",");

            boolean match = data.length == 5
                    && data[0].equals(id)
                    && data[1].equalsIgnoreCase(month);

            // Keep the line if it is not the required bill
            if (!match) {
                newLines.add(line);
                continue;
            }

            found = true;

            // If newStatus is not null, update the bill
            if (newStatus != null) {

                Bill bill = new Bill(
                        data[0],
                        data[1],
                        Integer.parseInt(data[2]),
                        Integer.parseInt(data[3]),
                        newStatus
                );

                newLines.add(bill.toFileString());
            }
        }

        if (found) {
            writeLines(BILL_FILE, newLines);
        }

        return found;
    }
}
```

This version keeps the **same behavior and output messages** while making the structure and comments easier to follow. I also kept the existing method names so it should remain compatible with your `Customer`, `Meter`, and `Bill` classes.
