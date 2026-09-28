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

    private List<String> readLines(String fileName) {
        List<String> lines = new ArrayList<>();
        File file = new File(fileName);

        if (!file.exists()) {
            return lines;
        }

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

    private boolean replaceOrDeleteCustomerLine(String id, String newLine) {
        List<String> newLines = new ArrayList<>();
        boolean found = false;

        for (String line : readLines(CUSTOMER_FILE)) {
            String[] data = line.split(",");

            if (data.length > 0 && data[0].equals(id)) {
                found = true;
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

    private Meter toMeter(String[] data) {
        return new Meter(data[0], data[1], Integer.parseInt(data[2]), Integer.parseInt(data[3]));
    }

    private Bill toBill(String[] data) {
        return new Bill(data[0], data[1], Integer.parseInt(data[2]), Integer.parseInt(data[3]), data[4]);
    }

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

        if (!replaceOrDeleteCustomerLine(id, customer.toFileString())) {
            throw new CustomerNotFoundException(id);
        }
    }

    public void deleteCustomer(String id) throws CustomerNotFoundException {
        if (!replaceOrDeleteCustomerLine(id, null)) {
            throw new CustomerNotFoundException(id);
        }

        removeAllForCustomer(METER_FILE, id);
        removeAllForCustomer(BILL_FILE, id);
    }

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

    public Meter findMeterByCustomerId(String id) {
        List<Meter> meters = findAllMetersByCustomerId(id);

        if (meters.isEmpty()) {
            return null;
        }

        return meters.get(meters.size() - 1);
    }

    public Meter findNextUnbilledMeter(String id) {
        List<Bill> bills = findBillsByCustomerId(id);

        for (Meter meter : findAllMetersByCustomerId(id)) {
            if (!isBilled(meter, bills)) {
                return meter;
            }
        }

        return null;
    }

    private boolean isBilled(Meter meter, List<Bill> bills) {
        for (Bill bill : bills) {
            if (bill.getReading() == meter.getCurrentReading()) {
                return true;
            }
        }
        return false;
    }

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

    private boolean changeBill(String id, String month, String newStatus) {
        List<String> newLines = new ArrayList<>();
        boolean found = false;

        for (String line : readLines(BILL_FILE)) {
            String[] data = line.split(",");

            boolean isMatch = data.length == 5
                    && data[0].equals(id)
                    && data[1].equalsIgnoreCase(month);

            if (!isMatch) {
                newLines.add(line);
                continue;
            }

            found = true;

            if (newStatus != null) {
                Bill updatedBill = new Bill(data[0], data[1],
                        Integer.parseInt(data[2]), Integer.parseInt(data[3]), newStatus);
                newLines.add(updatedBill.toFileString());
            }
        }

        if (found) {
            writeLines(BILL_FILE, newLines);
        }

        return found;
    }
}
