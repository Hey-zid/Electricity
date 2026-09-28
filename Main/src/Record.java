public abstract class Record {

    protected String id;

    public Record(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract void displayInfo()
    public abstract String toFileString();
    
}
