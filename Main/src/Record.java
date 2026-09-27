public abstract class Record {
    protected String id;

    public Record(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    // Abstraction: subclasses must define how they display themselves
    public abstract void displayInfo();

    // Abstraction: subclasses must define how they turn into a file line
    public abstract String toFileString();
}