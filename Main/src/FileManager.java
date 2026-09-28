import java.io.*;
import java.util.*;

/**
 * The only class that touches the text files. Everything else asks
 * FileManager to save, find, update or delete a record — nobody else
 * opens customers.txt, meters.txt or bills.txt directly.
 */
public class FileManager {

    private static final String CUSTOMER_FILE = "customers.txt";
    private static final String METER_FILE = "meters.txt";
    private static final String BILL_FILE = "bills.txt";

    /** Thrown when a customer ID is registered twice. */
    public static class DuplicateCustomerException extends Exception {
        public DuplicateCustomerException(String id) {
            super("Error: Customer ID " + id + " already exists.");
        }
    }

    /** Thrown when a customer ID can't be found. */
    public static class CustomerNotFoundException extends Exception {
        public CustomerNotFoundException(String id) {
            super("Error: Customer with ID " + id + " was not found.");
        }
    }

    // ================= LOW-LEVEL FILE I/O =================
    // These four methods are the only code that reads or writes a file.
    // Everything above them works with lines and objects instead.

    private List<String> readLines(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading " + fileName + ": " + e.getMessage());
        }
        return lines;
    }

    private void writeLines(String fileName, List<String> lines) {
        try (FileWriter writer = new FileWriter(fileName)) {
            for (String line : lines) {
                writer.write(line + System.lineSeparator());
            }
        } catch (IOException e) {
            System.out.println("Error writing " + fileName + ": " + e.getMessage());
        }
    }

    private void appendLine(String fileName, String line) {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write(line + System.lineSeparator());
        } catch (IOException e) {
            System.out.println("Error saving to " + fileName + ": " + e.getMessage());
        }
    }

    /** Every row in fileName whose first column matches id. */
    private List<String[]> readRowsForCustomer(String fileName, int columnCount, String id) {
        List<String[]> rows = new ArrayList<>();
        for (String line : readLines(fileName)) {
            String[] data = line.split(",");
            if (data.length == columnCount && data[0].equals(id)) {
                rows.add(data);
            }
        }
        return rows;
    }

    // ================= SHARED UPDATE/DELETE LOGIC =================
    // Both "update a customer" and "delete a customer" rewrite the
    // customer file with one line changed or removed, so they share
    // this helper. Passing newLine = null means "delete this line".

    private boolean replaceOrDeleteCustomerLine(String id, String newLine) {
        List<String> oldLines = readLines(CUSTOMER_FILE);
        List<String> updatedLines = new ArrayList<>();
        boolean found = false;

        for (String line : oldLines) {
            String[] data = line.split(",");
            boolean isMatch = data.length > 0 && data[0].equals(id);

            if (isMatch) {
                found = true;
                if (newLine != null) {
                    updatedLines.add(newLine); // update: keep the new version
                }
                // delete: newLine is null, so the line is simply dropped
            } else {
                updatedLines.add(line);
            }
        }

        if (found) {
            writeLines(CUSTOMER_FILE, updatedLines);
        }
        return found;
    }

    /** Removes every meter or bill line belonging to one customer. */
    private void removeAllForCustomer(String fileName, String id) {
        List<String> keptLines = new ArrayList<>();
        for (String line : readLines(fileName)) {
            String[] data = line.split(",");
            if (data.length > 0 && !data[0].equals(id)) {
                keptLines.add(line);
            }
        }
        writeLines(fileName, keptLines);
    }

    /** Both "update a bill's status" and "delete a bill" share this helper too. */
    private boolean changeBill(String id, String month, String newStatus) {
        List<String> oldLines = readLines(BILL_FILE);
        List<String> updatedLines = new ArrayList<>();
        boolean found = false;

        for (String line : oldLines) {
            String[] data = line.split(",");
            boolean isMatch = data.length == 5
                    && data[0].equals(id)
                    && data[1].equalsIgnoreCase(month);

            if (!isMatch) {
                updatedLines.add(line);
                continue;
            }

            found = true;
            if (newStatus != null) {
                Bill bill = new Bill(data[0], data[1],
                        Integer.parseInt(data[2]), Integer.parseInt(data[3]), newStatus);
                updatedLines.add(bill.toFileString()); // update: same bill, new status
            }
            // delete: newStatus is null, so the line is simply dropped
        }

        if (found) {
            writeLines(BILL_FILE, updatedLines);
        }
        return found;
    }

    // ================= CONVERTING FILE ROWS TO OBJECTS =================

    private Meter toMeter(String[] data) {
        return new Meter(data[0], data[1], Integer.parseInt(data[2]), Integer.parseInt(data[3]));
    }

    private Bill toBill(String[] data) {
        return new Bill(data[0], data[1], Integer.parseInt(data[2]), Integer.parseInt(data[3]), data[4]);
    }

    // ================= CUSTOMER =================

    public void saveCustomer(Customer customer) throws DuplicateCustomerException {
        String id = customer.getCustomerId();
        if (isCustomerIdTaken(id)) {
            throw new DuplicateCustomerException(id);
        }
        appendLine(CUSTOMER_FILE, customer.toFileString());
    }

    public boolean isCustomerIdTaken(String id) {
        return findCustomerById(id) != null;
    }

    public Customer findCustomerById(String id) {
        List<String[]> rows = readRowsForCustomer(CUSTOMER_FILE, 4, id);
        if (rows.isEmpty()) {
            return null;
        }
        String[] data = rows.get(0);
        return new Customer(data[0], data[1], data[2], data[3]);
    }

    public void updateCustomer(Customer customer) throws CustomerNotFoundException {
        String id = customer.getCustomerId();
        boolean updated = replaceOrDeleteCustomerLine(id, customer.toFileString());
        if (!updated) {
            throw new CustomerNotFoundException(id);
        }
    }

    /** Deletes the customer, then cascades to their meters and bills. */
    public void deleteCustomer(String id) throws CustomerNotFoundException {
        boolean deleted = replaceOrDeleteCustomerLine(id, null);
        if (!deleted) {
            throw new CustomerNotFoundException(id);
        }
        removeAllForCustomer(METER_FILE, id);
        removeAllForCustomer(BILL_FILE, id);
    }

    // ================= METER =================

    public void saveMeter(Meter meter) {
        appendLine(METER_FILE, meter.toFileString());
    }

    public List<Meter> findAllMetersByCustomerId(String id) {
        List<Meter> meters = new ArrayList<>();
        for (String[] data : readRowsForCustomer(METER_FILE, 4, id)) {
            meters.add(toMeter(data));
        }
        return meters;
    }

    /** The customer's most recent reading, or null if they have none yet. */
    public Meter findMeterByCustomerId(String id) {
        List<Meter> meters = findAllMetersByCustomerId(id);
        return meters.isEmpty() ? null : meters.get(meters.size() - 1);
    }

    /** The first reading that has no matching bill yet. */
    public Meter findNextUnbilledMeter(String id) {
        List<Bill> bills = findBillsByCustomerId(id);
        for (Meter meter : findAllMetersByCustomerId(id)) {
            if (!isBilled(meter, bills)) {
                return meter;
            }
        }
        return null;
    }

    /** A reading is billed once some bill records that exact reading value. */
    private boolean isBilled(Meter meter, List<Bill> bills) {
        for (Bill bill : bills) {
            if (bill.getReading() == meter.getCurrentReading()) {
                return true;
            }
        }
        return false;
    }

    // ================= BILL =================

    public void saveBill(Bill bill) {
        appendLine(BILL_FILE, bill.toFileString());
    }

    public List<Bill> findBillsByCustomerId(String id) {
        List<Bill> bills = new ArrayList<>();
        for (String[] data : readRowsForCustomer(BILL_FILE, 5, id)) {
            bills.add(toBill(data));
        }
        return bills;
    }

    public boolean updateBillStatus(String id, String month, String newStatus) {
        return changeBill(id, month, newStatus);
    }

    public boolean deleteBill(String id, String month) {
        return changeBill(id, month, null);
    }
}