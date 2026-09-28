public class Meter extends Record {

    private String meterNumber;
    private int previousReading;
    private int currentReading;

    // Creates a meter reading for one customer.
    // The customer ID is stored in the parent Record class.
    public Meter(String customerId, String meterNumber, int previousReading, int currentReading) {
        super(customerId);
        this.meterNumber = meterNumber;
        this.previousReading = previousReading;
        this.currentReading = currentReading;
    }

    public String getCustomerId() {
        return id;
    }

    public String getMeterNumber() {
        return meterNumber;
    }

    public int getPreviousReading() {
        return previousReading;
    }

    public int getCurrentReading() {
        return currentReading;
    }

    // Units used is worked out on demand instead of being stored,
    // so it can never go out of sync with the two readings.
    public int getUnitsUsed() {
        return currentReading - previousReading;
    }

    // Prints this reading to the console.
    public void displayInfo() {
        System.out.println("Customer ID: " + id);
        System.out.println("Meter Number: " + meterNumber);
        System.out.println("Previous Reading: " + previousReading);
        System.out.println("Current Reading: " + currentReading);
        System.out.println("Units Used: " + getUnitsUsed());
    }

    // One comma-separated line, in the same order FileManager reads it back:
    // customerId, meterNumber, previousReading, currentReading
    public String toFileString() {
        return id + "," + meterNumber + "," + previousReading + "," + currentReading;
    }
}
