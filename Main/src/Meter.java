public class Meter extends Record {
    private String meterNumber;
    private int previousReading;
    private int currentReading;

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

    public int getUnitsUsed() {
        return currentReading - previousReading;
    }

    // Polymorphism: Meter's own version of displayInfo
    @Override
    public void displayInfo() {
        System.out.println("Customer ID: " + id);
        System.out.println("Meter Number: " + meterNumber);
        System.out.println("Previous Reading: " + previousReading);
        System.out.println("Current Reading: " + currentReading);
        System.out.println("Units Used: " + getUnitsUsed());
    }

    // Polymorphism: Meter's own version of toFileString
    @Override
    public String toFileString() {
        return id + "," + meterNumber + "," + previousReading + "," + currentReading;
    }
}