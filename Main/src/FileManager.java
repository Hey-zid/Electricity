import java.io.*;
import java.util.*;

public class FileManager {

    private static final String CUSTOMER_FILE = "customers.txt";
    private static final String METER_FILE = "meters.txt";
    private static final String BILL_FILE = "bills.txt";

    // Exception for duplicate customer ID
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

    // Add one line to a file
    private void appendLine(String fileName, String line) {
        try {
            FileWriter writer = new FileWriter(fileName, true);

            writer.write(line + System.lineSeparator());

            writer.close();

        } catch (IOException e) {
            System.out.println("Error saving to " + fileName + ": " + e.getMessage());
        }
    }

    // Replace or delete a line using an ID
    private boolean replaceOrDelete(
            String fileName,
            int idColumn,
            String id,
            String newLine) {

        List<String> lines = readLines(fileName);
        List<String> newLines = new ArrayList<>();

        boolean found = false;

        for (String line : lines) {

            String[] data = line.split(",");

            if (data.length > idColumn && data[idColumn].equals(id)) {

                found = true;

                // If newLine is not null, replace the old line
                if (newLine != null) {
                    newLines.add(newLine);
                }

                // If newLine is null, the line is deleted

            } else {
                newLines.add(line);
            }
        }

        if (found) {
            writeLines(fileName, newLines);
        }

        return found;
    }

    // Delete all lines with a matching ID
    private void removeAllMatching(String fileName, int idColumn, String id) {

        List<String> lines = readLines(fileName);
        List<String> newLines = new ArrayList<>();

        for (String line : lines) {

            String[] data = line.split(",");

            if (data.length > idColumn && !data[idColumn].equals(id)) {
                newLines.add(line);
            }
        }

        writeLines(fileName, newLines);
    }

    // ---------------------------------------------------------
    // CUSTOMER
    // ---------------------------------------------------------

    public void saveCustomer(Customer customer)
            throws DuplicateCustomerException {

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

        for (String line : readLines(CUSTOMER_FILE)) {

            String[] data = line.split(",");

            if (data.length == 4 && data[0].equals(id)) {
                return new Customer(
                        data[0],
                        data[1],
                        data[2],
                        data[3]
                );
            }
        }

        return null;
    }

    public void updateCustomer(Customer customer)
            throws CustomerNotFoundException {

        String id = customer.getCustomerId();

        boolean found = replaceOrDelete(
                CUSTOMER_FILE,
                0,
                id,
                customer.toFileString()
        );

        if (!found) {
            throw new CustomerNotFoundException(id);
        }
    }

    public void deleteCustomer(String id)
            throws CustomerNotFoundException {

        boolean found = replaceOrDelete(
                CUSTOMER_FILE,
                0,
                id,
                null
        );

        if (!found) {
            throw new CustomerNotFoundException(id);
        }

        // Delete related meter and bill records
        removeAllMatching(METER_FILE, 0, id);
        removeAllMatching(BILL_FILE, 0, id);
    }

    // ---------------------------------------------------------
    // METER
    // ---------------------------------------------------------

    public void saveMeter(Meter meter) {
        appendLine(METER_FILE, meter.toFileString());
    }

    public Meter findMeterByCustomerId(String id) {

        Meter latest = null;

        for (String line : readLines(METER_FILE)) {

            String[] data = line.split(",");

            if (data.length == 4 && data[0].equals(id)) {

                latest = new Meter(
                        data[0],
                        data[1],
                        Integer.parseInt(data[2]),
                        Integer.parseInt(data[3])
                );
            }
        }

        return latest;
    }

    public List<Meter> findAllMetersByCustomerId(String id) {

        List<Meter> meters = new ArrayList<>();

        for (String line : readLines(METER_FILE)) {

            String[] data = line.split(",");

            if (data.length == 4 && data[0].equals(id)) {

                Meter meter = new Meter(
                        data[0],
                        data[1],
                        Integer.parseInt(data[2]),
                        Integer.parseInt(data[3])
                );

                meters.add(meter);
            }
        }

        return meters;
    }

    public Meter findNextUnbilledMeter(String id) {

        List<Bill> bills = findBillsByCustomerId(id);
        List<Meter> meters = findAllMetersByCustomerId(id);

        for (Meter meter : meters) {

            boolean billed = false;

            for (Bill bill : bills) {

                if (bill.getReading() == meter.getCurrentReading()) {
                    billed = true;
                    break;
                }
            }

            if (!billed) {
                return meter;
            }
        }

        return null;
    }

    // ---------------------------------------------------------
    // BILL
    // ---------------------------------------------------------

    public void saveBill(Bill bill) {
        appendLine(BILL_FILE, bill.toFileString());
    }

    public List<Bill> findBillsByCustomerId(String id) {

        List<Bill> bills = new ArrayList<>();

        for (String line : readLines(BILL_FILE)) {

            String[] data = line.split(",");

            if (data.length == 5 && data[0].equals(id)) {

                Bill bill = new Bill(
                        data[0],
                        data[1],
                        Integer.parseInt(data[2]),
                        Integer.parseInt(data[3]),
                        data[4]
                );

                bills.add(bill);
            }
        }

        return bills;
    }

    public boolean updateBillStatus(
            String id,
            String month,
            String newStatus) {

        List<String> lines = readLines(BILL_FILE);
        List<String> newLines = new ArrayList<>();

        boolean found = false;

        for (String line : lines) {

            String[] data = line.split(",");

            boolean match =
                    data.length == 5
                    && data[0].equals(id)
                    && data[1].equalsIgnoreCase(month);

            if (match) {

                Bill bill = new Bill(
                        data[0],
                        data[1],
                        Integer.parseInt(data[2]),
                        Integer.parseInt(data[3]),
                        newStatus
                );

                newLines.add(bill.toFileString());
                found = true;

            } else {
                newLines.add(line);
            }
        }

        if (found) {
            writeLines(BILL_FILE, newLines);
        }

        return found;
    }

    public boolean deleteBill(String id, String month) {

        List<String> lines = readLines(BILL_FILE);
        List<String> newLines = new ArrayList<>();

        boolean found = false;

        for (String line : lines) {

            String[] data = line.split(",");

            boolean match =
                    data.length == 5
                    && data[0].equals(id)
                    && data[1].equalsIgnoreCase(month);

            if (match) {
                found = true;
            } else {
                newLines.add(line);
            }
        }

        if (found) {
            writeLines(BILL_FILE, newLines);
        }

        return found;
    }
}
