public class Bill extends Record {
    private String month;
    private int unitsUsed;
    private int reading;
    private String status;

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

    // Polymorphism: Bill's own version of displayInfo
    @Override
    public void displayInfo() {
        System.out.println("Customer ID: " + id);
        System.out.println("Month: " + month);
        System.out.println("Units Used: " + unitsUsed);
        System.out.println("Reading: " + reading);
        System.out.println("Status: " + status);
    }

    // Polymorphism: Bill's own version of toFileString
    @Override
    public String toFileString() {
        return id + "," + month + "," + unitsUsed + "," + reading + "," + status;
    }
}