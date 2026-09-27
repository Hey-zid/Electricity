public class Customer extends Record {
    private String name;
    private String address;
    private String meterNumber;

    public Customer(String customerId, String name, String address, String meterNumber) {
        super(customerId); // sends the ID up to Record
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

    // Polymorphism: Customer's own version of displayInfo
    @Override
    public void displayInfo() {
        System.out.println("Customer ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("Address: " + address);
        System.out.println("Meter Number: " + meterNumber);
    }

    // Polymorphism: Customer's own version of toFileString
    @Override
    public String toFileString() {
        return id + "," + name + "," + address + "," + meterNumber;
    }
}