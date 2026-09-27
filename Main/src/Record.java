public abstract class Record {

    protected String id; // the customer id this record belongs to

    public Record(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    // Every child class has to say how it prints itself...
    public abstract void displayInfo();

    // ...and how it turns into one line of text to save in a file.
    public abstract String toFileString();
}
