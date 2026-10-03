import java.io.*;
import java.util.*;

public class FileManager {
    private static final String CUSTOMER_FILE = "customers.txt";
    private static final String METER_FILE = "meters.txt";
    private static final String BILL_FILE = "bills.txt";
    private static final String SEP = ",";

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

    // ================= HELPERS =================

    /** Removes commas (which would break the file format) and trims spaces. */
    private String clean(String s) {
        return s == null ? "" : s.replace(",", " ").trim();
    }

    /** Splits a line keeping empty fields (so blanks don't change the field count). */
    private String[] split(String line) {
        return line.split(SEP, -1);
    }

    /** Returns the number, or null if the text is not a valid integer. */
    private Integer parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<String> readLines(String path) {
        List<String> lines = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return lines;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) lines.add(line);
            }
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

    /** Replaces (or deletes when newLine == null) the first line whose field idIndex equals id. */
    private boolean replaceOrDelete(String path, int idIndex, String id, String newLine) {
        List<String> result = new ArrayList<>();
        boolean found = false;

        for (String line : readLines(path)) {
            String[] parts = split(line);
            if (!found && parts.length > idIndex && parts[idIndex].trim().equals(id)) {
                found = true;
                if (newLine != null) result.add(newLine);
            } else {
                result.add(line);
            }
        }

        if (found) writeLines(path, result);
        return found;
    }

    private void removeAllMatching(String path, int idIndex, String id) {
        List<String> result = new ArrayList<>();
        for (String line : readLines(path)) {
            String[] parts = split(line);
            if (!(parts.length > idIndex && parts[idIndex].trim().equals(id))) {
                result.add(line);
            }
        }
        writeLines(path, result);
    }

    private Customer cleanCustomer(Customer c) {
        return new Customer(clean(c.getCustomerId()), clean(c.getName()),
                clean(c.getAddress()), clean(c.getMeterNumber()));
    }

    // ================= CUSTOMER =================

    public void saveCustomer(Customer c) throws DuplicateCustomerException {
        Customer safe = cleanCustomer(c);
        if (isCustomerIdTaken(safe.getCustomerId())) {
            throw new DuplicateCustomerException(safe.getCustomerId());
        }
        appendLine(CUSTOMER_FILE, safe.toFileString());
    }

    public boolean isCustomerIdTaken(String id) {
        return findCustomerById(id) != null;
    }

    public Customer findCustomerById(String id) {
        String key = clean(id);
        for (String line : readLines(CUSTOMER_FILE)) {
            String[] p = split(line);
            if (p.length == 4 && p[0].trim().equals(key)) {
                return new Customer(p[0].trim(), p[1], p[2], p[3]);
            }
        }
        return null;
    }

    public void updateCustomer(Customer c) throws CustomerNotFoundException {
        Customer safe = cleanCustomer(c);
        if (!replaceOrDelete(CUSTOMER_FILE, 0, safe.getCustomerId(), safe.toFileString())) {
            throw new CustomerNotFoundException(safe.getCustomerId());
        }
    }

    public void deleteCustomer(String id) throws CustomerNotFoundException {
        String key = clean(id);
        if (!replaceOrDelete(CUSTOMER_FILE, 0, key, null)) {
            throw new CustomerNotFoundException(key);
        }
        removeAllMatching(METER_FILE, 0, key);
        removeAllMatching(BILL_FILE, 0, key);
    }

    // ================= METER =================

    public void saveMeter(Meter m) {
        Meter safe = new Meter(clean(m.getCustomerId()), clean(m.getMeterNumber()),
                m.getPreviousReading(), m.getCurrentReading());
        appendLine(METER_FILE, safe.toFileString());
    }

    public Meter findMeterByCustomerId(String id) {
        List<Meter> all = findAllMetersByCustomerId(id);
        return all.isEmpty() ? null : all.get(all.size() - 1); // latest reading
    }

    public List<Meter> findAllMetersByCustomerId(String id) {
        String key = clean(id);
        List<Meter> meters = new ArrayList<>();
        for (String line : readLines(METER_FILE)) {
            String[] p = split(line);
            if (p.length == 4 && p[0].trim().equals(key)) {
                Integer prev = parseIntSafe(p[2]);
                Integer curr = parseIntSafe(p[3]);
                if (prev != null && curr != null) {
                    meters.add(new Meter(p[0].trim(), p[1], prev, curr));
                }
            }
        }
        return meters;
    }

    /**
     * First meter reading that has no bill yet. Each bill can "use up" only one
     * meter reading, so two readings with the same value are handled correctly.
     */
    public Meter findNextUnbilledMeter(String id) {
        List<Bill> bills = findBillsByCustomerId(id);
        boolean[] used = new boolean[bills.size()];

        for (Meter m : findAllMetersByCustomerId(id)) {
            boolean billed = false;
            for (int i = 0; i < bills.size(); i++) {
                if (!used[i] && bills.get(i).getReading() == m.getCurrentReading()) {
                    used[i] = true;
                    billed = true;
                    break;
                }
            }
            if (!billed) return m;
        }
        return null;
    }

    // ================= BILL =================

    public void saveBill(Bill b) {
        Bill safe = new Bill(clean(b.getCustomerId()), clean(b.getMonth()),
                b.getUnitsUsed(), b.getReading(), clean(b.getStatus()));
        appendLine(BILL_FILE, safe.toFileString());
    }

    public List<Bill> findBillsByCustomerId(String id) {
        String key = clean(id);
        List<Bill> bills = new ArrayList<>();
        for (String line : readLines(BILL_FILE)) {
            String[] p = split(line);
            if (p.length == 5 && p[0].trim().equals(key)) {
                Integer units = parseIntSafe(p[2]);
                Integer reading = parseIntSafe(p[3]);
                if (units != null && reading != null) {
                    bills.add(new Bill(p[0].trim(), p[1], units, reading, p[4]));
                }
            }
        }
        return bills;
    }

    public boolean updateBillStatus(String id, String month, String newStatus) {
        String key = clean(id);
        String m = clean(month);
        List<String> result = new ArrayList<>();
        boolean found = false;

        for (String line : readLines(BILL_FILE)) {
            String[] p = split(line);
            Integer units = (p.length == 5) ? parseIntSafe(p[2]) : null;
            Integer reading = (p.length == 5) ? parseIntSafe(p[3]) : null;

            if (units != null && reading != null
                    && p[0].trim().equals(key) && p[1].trim().equalsIgnoreCase(m)) {
                Bill updated = new Bill(p[0].trim(), p[1], units, reading, clean(newStatus));
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
        String key = clean(id);
        String m = clean(month);
        List<String> result = new ArrayList<>();
        boolean found = false;

        for (String line : readLines(BILL_FILE)) {
            String[] p = split(line);
            if (p.length == 5 && p[0].trim().equals(key) && p[1].trim().equalsIgnoreCase(m)) {
                found = true;
            } else {
                result.add(line);
            }
        }

        if (found) writeLines(BILL_FILE, result);
        return found;
    }
}