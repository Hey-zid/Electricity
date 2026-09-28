/**
 * One meter reading for a customer. Once saved, a reading never changes.
 * Units used is simply the current reading minus the previous one.
 */
public class Meter extends Record {

    private final String meterNumber;
    private final int previousReading;
    private final int currentReading;

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

    @Override
    public void displayInfo() {
        System.out.println("Customer ID: " + id);
        System.out.println("Meter Number: " + meterNumber);
        System.out.println("Previous Reading: " + previousReading);
        System.out.println("Current Reading: " + currentReading);
        System.out.println("Units Used: " + getUnitsUsed());
    }

    /** Saved as: id,meterNumber,previousReading,currentReading */
    @Override
    public String toFileString() {
        return String.join(FIELD_SEPARATOR, id, meterNumber,
                String.valueOf(previousReading), String.valueOf(currentReading));
    }
}