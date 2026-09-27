public abstract class Record {
 
    protected String id; // the customer id this record belongs to
 
    public Record(String id) {
        this.id = id;
    }
 
    public String getId() {
        return id;
    }
 
    // Every child class has to say how it prints itself for the reason
    public abstract void displayInfo();
 
    // How it turns into one line of text to save in a file must be at any cost
    public abstract String toFileString();
}
