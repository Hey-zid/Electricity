/**
 * A monthly bill for one customer. Only the payment status can change
 * after the bill is created (for example, from "Unpaid" to "Paid").
 */
public class Bill extends Record {

    private final String month;
    private final int unitsUsed;
    private final int reading;   // the meter's current reading this bill was made from
    private String status;       // "Paid" or "Unpaid"

    public Bill(String customerId, String month, int unitsUsed, int reading, String status) {
        super(customerId);
        this.month = month;
        this.unitsUsed = unitsUsed;
        this.reading = reading;
        this.status = status;
    }

    public String getCustomerId() {
        return id;
    }

    public String getMonth() {
        return month;
    }

    public int getUnitsUsed() {
        return unitsUsed;
    }

    public int getReading() {
        return reading;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void displayInfo() {
        System.out.println("Customer ID: " + id);
        System.out.println("Month: " + month);
        System.out.println("Units Used: " + unitsUsed);
        System.out.println("Reading: " + reading);
        System.out.println("Status: " + status);
    }

    /** Saved as: id,month,unitsUsed,reading,status */
    @Override
    public String toFileString() {
        return String.join(FIELD_SEPARATOR, id, month,
                String.valueOf(unitsUsed), String.valueOf(reading), status);
    }
}