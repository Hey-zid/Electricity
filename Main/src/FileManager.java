import java.io.*;
import java.util.*;

public class FileManager {

    private static final String CUSTOMER_FILE = "customers.txt";
    private static final String METER_FILE = "meters.txt";
    private static final String BILL_FILE = "bills.txt";

    // Thrown when someone tries to register a customer ID that's already used
    public static class DuplicateCustomerException extends Exception {
        public DuplicateCustomerException(String id) {
            super("Error: Customer ID " + id + " already exists.");
        }
    }

    // Thrown when we look for a customer id and can't find it
    public static class CustomerNotFoundException extends Exception {
        public CustomerNotFoundException(String id) {
            super("Error: Customer with ID " + id + " was not found.");
        }
    }

    // ---------------------------------------------------------
    //  Small helper methods used by everything below.
    //  Keeping the file reading/writing in one place means we
    //  don't repeat the same try/catch code over and over.
    // ---------------------------------------------------------

    // Reads every line of a file into a list. Returns an empty
    // list if the file doesn't exist yet (first time running the app).
    private List<String> readLines(String path) {
        List<String> lines = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) {
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading " + path + ": " + e.getMessage());
        }
        return lines;
    }

    // Wipes the file and writes the given lines back in, in order.
    // We use this whenever a line was updated or removed.
    private void writeLines(String path, List<String> lines) {
        try (FileWriter writer = new FileWriter(path, false)) {
            for (String line : lines) {
                writer.write(line + System.lineSeparator());
            }
        } catch (IOException e) {
            System.out.println("Error writing " + path + ": " + e.getMessage());
        }
    }

    // Adds one new line to the end of a file without touching what's already there.
    private void appendLine(String path, String line) {
        try (FileWriter writer = new FileWriter(path, true)) {
            writer.write(line + System.lineSeparator());
        } catch (IOException e) {
            System.out.println("Error saving to " + path + ": " + e.getMessage());
        }
    }

    // Looks through a file for the first line whose id column matches "id".
    // - If newLine is not null, that line gets replaced with newLine.
    // - If newLine is null, that line just gets removed.
    // Returns true if a matching line was actually found.
    private boolean replaceOrDelete(String path, int idColumn, String id, String newLine) {
        List<String> updatedLines = new ArrayList<>();
        boolean found = false;

        for (String line : readLines(path)) {
            String[] columns = line.split(",");
            boolean isMatch = columns.length > idColumn && columns[idColumn].equals(id);

            if (isMatch) {
                found = true;
                if (newLine != null) {
                    updatedLines.add(newLine);
                }
                // if newLine is null we simply don't add anything = deleted
            } else {
                updatedLines.add(line);
            }
        }

        if (found) {
            writeLines(path, updatedLines);
        }
        return found;
    }


    private void removeAllMatching(String path, int idColumn, String id) {
        List<String> keptLines = new ArrayList<>();
        for (String line : readLines(path)) {
            String[] columns = line.split(",");
            boolean isMatch = columns.length > idColumn && columns[idColumn].equals(id);
            if (!isMatch) {
                keptLines.add(line);
            }
        }
        writeLines(path, keptLines);
    }

    // ---------------------------------------------------------
    //  CUSTOMER
    // ---------------------------------------------------------

    public void saveCustomer(Customer customer) throws DuplicateCustomerException {
        if (isCustomerIdTaken(customer.getCustomerId())) {
            throw new DuplicateCustomerException(customer.getCustomerId());
        }
        appendLine(CUSTOMER_FILE, customer.toFileString());
    }

    public boolean isCustomerIdTaken(String id) {
        return findCustomerById(id) != null;
    }

    public Customer findCustomerById(String id) {
        for (String line : readLines(CUSTOMER_FILE)) {
            String[] columns = line.split(",");
            if (columns.length == 4 && columns[0].equals(id)) {
                return new Customer(columns[0], columns[1], columns[2], columns[3]);
            }
        }
        return null;
    }

    public void updateCustomer(Customer customer) throws CustomerNotFoundException {
        boolean found = replaceOrDelete(CUSTOMER_FILE, 0, customer.getCustomerId(), customer.toFileString());
        if (!found) {
            throw new CustomerNotFoundException(customer.getCustomerId());
        }
    }

    public void deleteCustomer(String id) throws CustomerNotFoundException {
        boolean found = replaceOrDelete(CUSTOMER_FILE, 0, id, null);
        if (!found) {
            throw new CustomerNotFoundException(id);
        }
        // also clean up anything else tied to this customer
        removeAllMatching(METER_FILE, 0, id);
        removeAllMatching(BILL_FILE, 0, id);
    }

    // ---------------------------------------------------------
    //  METER
    // ---------------------------------------------------------

    public void saveMeter(Meter meter) {
        appendLine(METER_FILE, meter.toFileString());
    }

    // Gets the most recent reading saved for a customer.
    // Since readings are appended in order, the last matching
    // line in the file is simply the newest one.
    public Meter findMeterByCustomerId(String id) {
        Meter latest = null;
        for (String line : readLines(METER_FILE)) {
            String[] columns = line.split(",");
            if (columns.length == 4 && columns[0].equals(id)) {
                latest = new Meter(columns[0], columns[1],
                        Integer.parseInt(columns[2]), Integer.parseInt(columns[3]));
            }
        }
        return latest;
    }

    public List<Meter> findAllMetersByCustomerId(String id) {
        List<Meter> meters = new ArrayList<>();
        for (String line : readLines(METER_FILE)) {
            String[] columns = line.split(",");
            if (columns.length == 4 && columns[0].equals(id)) {
                meters.add(new Meter(columns[0], columns[1],
                        Integer.parseInt(columns[2]), Integer.parseInt(columns[3])));
            }
        }
        return meters;
    }

    // Finds the oldest meter reading that hasn't been turned into a bill yet.
    // We check each reading against the existing bills to see if one already covers it.
    public Meter findNextUnbilledMeter(String id) {
        List<Bill> existingBills = findBillsByCustomerId(id);

        for (Meter meter : findAllMetersByCustomerId(id)) {
            boolean alreadyBilled = false;
            for (Bill bill : existingBills) {
                if (bill.getReading() == meter.getCurrentReading()) {
                    alreadyBilled = true;
                    break;
                }
            }
            if (!alreadyBilled) {
                return meter;
            }
        }
        return null;
    }

    // ---------------------------------------------------------
    //  BILL
    // ---------------------------------------------------------

    public void saveBill(Bill bill) {
        appendLine(BILL_FILE, bill.toFileString());
    }

    public List<Bill> findBillsByCustomerId(String id) {
        List<Bill> bills = new ArrayList<>();
        for (String line : readLines(BILL_FILE)) {
            String[] columns = line.split(",");
            if (columns.length == 5 && columns[0].equals(id)) {
                bills.add(new Bill(columns[0], columns[1],
                        Integer.parseInt(columns[2]), Integer.parseInt(columns[3]), columns[4]));
            }
        }
        return bills;
    }

    // A bill is identified by customer id + month together, so this one
    // needs its own loop instead of reusing replaceOrDelete.
    public boolean updateBillStatus(String id, String month, String newStatus) {
        List<String> updatedLines = new ArrayList<>();
        boolean found = false;

        for (String line : readLines(BILL_FILE)) {
            String[] columns = line.split(",");
            boolean isMatch = columns.length == 5 && columns[0].equals(id) && columns[1].equalsIgnoreCase(month);

            if (isMatch) {
                Bill updatedBill = new Bill(columns[0], columns[1],
                        Integer.parseInt(columns[2]), Integer.parseInt(columns[3]), newStatus);
                updatedLines.add(updatedBill.toFileString());
                found = true;
            } else {
                updatedLines.add(line);
            }
        }

        if (found) {
            writeLines(BILL_FILE, updatedLines);
        }
        return found;
    }

    public boolean deleteBill(String id, String month) {
        List<String> updatedLines = new ArrayList<>();
        boolean found = false;

        for (String line : readLines(BILL_FILE)) {
            String[] columns = line.split(",");
            boolean isMatch = columns.length == 5 && columns[0].equals(id) && columns[1].equalsIgnoreCase(month);

            if (isMatch) {
                found = true;
                // don't add it back to updatedLines = it's deleted
            } else {
                updatedLines.add(line);
            }
        }

        if (found) {
            writeLines(BILL_FILE, updatedLines);
        }
        return found;
    }
}
