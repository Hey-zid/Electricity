public class Customer extends Record {

    private String name;
    private String address;
    private String meterNumber;

    public Customer(String customerId, String name, String address, String meterNumber) {
        super(customerId); // customerId gets stored as "id" in Record
        this.name = name;
        this.address = address;
        this.meterNumber = meterNumber;
    }

    public String getCustomerId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getMeterNumber() {
        return meterNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setMeterNumber(String meterNumber) {
        this.meterNumber = meterNumber;
    }

    @Override
    public void displayInfo() {
        System.out.println("Customer ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("Address: " + address);
        System.out.println("Meter Number: " + meterNumber);
    }

    // Turns the customer into one comma separated line, e.g:
    // C001,John Doe,123 Main St,MTR-99
    @Override
    public String toFileString() {
        return id + "," + name + "," + address + "," + meterNumber;
    }
}
