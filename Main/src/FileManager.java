import java.io.*;
import java.util.*;

public class FileManager {
    private static final String CUSTOMER_FILE = "customers.txt";
    private static final String METER_FILE = "meters.txt";
    private static final String BILL_FILE = "bills.txt";

    public static class DuplicateCustomerException extends Exception {
        public DuplicateCustomerException(String id) {
            super("Error: Customer ID " + id + " already exists.");
        }
    }

    public static class CustomerNotFoundException extends Exception {
        public CustomerNotFoundException(String id) {
            super("Error: Customer with ID " + id + " was not found.");
        }
    }

    // ---------- Generic file helpers (used by all update/delete methods) ----------

    private List<String> readLines(String path) {
        List<String> lines = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return lines;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) lines.add(line);
        } catch (IOException e) {
            System.out.println("Error reading " + path + ": " + e.getMessage());
        }
        return lines;
    }

    private void writeLines(String path, List<String> lines) {
        try (FileWriter writer = new FileWriter(path, false)) {
            for (String l : lines) writer.write(l + System.lineSeparator());
        } catch (IOException e) {
            System.out.println("Error writing " + path + ": " + e.getMessage());
        }
    }

    private void appendLine(String path, String line) {
        try (FileWriter writer = new FileWriter(path, true)) {
            writer.write(line + System.lineSeparator());
        } catch (IOException e) {
            System.out.println("Error saving to " + path + ": " + e.getMessage());
        }
    }

    // Replaces the first line whose column[idIndex] matches id with newLine.
    // If newLine is null, the matching line is removed instead. Returns true if a match was found.
    private boolean replaceOrDelete(String path, int idIndex, String id, String newLine) {
        List<String> result = new ArrayList<>();
        boolean found = false;

        for (String line : readLines(path)) {
            String[] parts = line.split(",");
            if (parts.length > idIndex && parts[idIndex].equals(id)) {
                found = true;
                if (newLine != null) result.add(newLine);
            } else {
                result.add(line);
            }
        }

        if (found) writeLines(path, result);
        return found;
    }

    // Removes every line whose column[idIndex] matches id (used when deleting a customer entirely)
    private void removeAllMatching(String path, int idIndex, String id) {
        List<String> result = new ArrayList<>();
        for (String line : readLines(path)) {
            String[] parts = line.split(",");
            if (!(parts.length > idIndex && parts[idIndex].equals(id))) {
                result.add(line);
            }
        }
        writeLines(path, result);
    }

    // ---------- CUSTOMER ----------

    public void saveCustomer(Customer c) throws DuplicateCustomerException {
        if (isCustomerIdTaken(c.getCustomerId())) throw new DuplicateCustomerException(c.getCustomerId());
        appendLine(CUSTOMER_FILE, c.toFileString());
    }

    public boolean isCustomerIdTaken(String id) {
        return findCustomerById(id) != null;
    }

    public Customer findCustomerById(String id) {
        for (String line : readLines(CUSTOMER_FILE)) {
            String[] p = line.split(",");
            if (p.length == 4 && p[0].equals(id)) return new Customer(p[0], p[1], p[2], p[3]);
        }
        return null;
    }

    public void updateCustomer(Customer c) throws CustomerNotFoundException {
        if (!replaceOrDelete(CUSTOMER_FILE, 0, c.getCustomerId(), c.toFileString())) {
            throw new CustomerNotFoundException(c.getCustomerId());
        }
    }

    public void deleteCustomer(String id) throws CustomerNotFoundException {
        if (!replaceOrDelete(CUSTOMER_FILE, 0, id, null)) {
            throw new CustomerNotFoundException(id);
        }
        removeAllMatching(METER_FILE, 0, id);
        removeAllMatching(BILL_FILE, 0, id);
    }

    // ---------- METER ----------

    public void saveMeter(Meter m) {
        appendLine(METER_FILE, m.toFileString());
    }

    // Most recent reading for a customer (last matching line in the file)
    public Meter findMeterByCustomerId(String id) {
        Meter result = null;
        for (String line : readLines(METER_FILE)) {
            String[] p = line.split(",");
            if (p.length == 4 && p[0].equals(id)) {
                result = new Meter(p[0], p[1], Integer.parseInt(p[2]), Integer.parseInt(p[3]));
            }
        }
        return result;
    }

    public List<Meter> findAllMetersByCustomerId(String id) {
        List<Meter> meters = new ArrayList<>();
        for (String line : readLines(METER_FILE)) {
            String[] p = line.split(",");
            if (p.length == 4 && p[0].equals(id)) {
                meters.add(new Meter(p[0], p[1], Integer.parseInt(p[2]), Integer.parseInt(p[3])));
            }
        }
        return meters;
    }

    // Oldest reading that has no matching bill yet
    public Meter findNextUnbilledMeter(String id) {
        List<Bill> bills = findBillsByCustomerId(id);
        for (Meter m : findAllMetersByCustomerId(id)) {
            boolean billed = false;
            for (Bill b : bills) {
                if (b.getReading() == m.getCurrentReading()) { billed = true; break; }
            }
            if (!billed) return m;
        }
        return null;
    }

    // ---------- BILL ----------

    public void saveBill(Bill b) {
        appendLine(BILL_FILE, b.toFileString());
    }

    public List<Bill> findBillsByCustomerId(String id) {
        List<Bill> bills = new ArrayList<>();
        for (String line : readLines(BILL_FILE)) {
            String[] p = line.split(",");
            if (p.length == 5 && p[0].equals(id)) {
                bills.add(new Bill(p[0], p[1], Integer.parseInt(p[2]), Integer.parseInt(p[3]), p[4]));
            }
        }
        return bills;
    }

    // Bills are matched by customerId + month together, so this stays a manual loop
    public boolean updateBillStatus(String id, String month, String newStatus) {
        List<String> result = new ArrayList<>();
        boolean found = false;

        for (String line : readLines(BILL_FILE)) {
            String[] p = line.split(",");
            if (p.length == 5 && p[0].equals(id) && p[1].equalsIgnoreCase(month)) {
                Bill updated = new Bill(p[0], p[1], Integer.parseInt(p[2]), Integer.parseInt(p[3]), newStatus);
                result.add(updated.toFileString());
                found = true;
            } else {
                result.add(line);
            }
        }

        if (found) writeLines(BILL_FILE, result);
        return found;
    }

    public boolean deleteBill(String id, String month) {
        List<String> result = new ArrayList<>();
        boolean found = false;

        for (String line : readLines(BILL_FILE)) {
            String[] p = line.split(",");
            if (p.length == 5 && p[0].equals(id) && p[1].equalsIgnoreCase(month)) {
                found = true;
            } else {
                result.add(line);
            }
        }

        if (found) writeLines(BILL_FILE, result);
        return found;
    }
}