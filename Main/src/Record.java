/**
 * The common base for everything we store: Customer, Meter and Bill.
 * Every record belongs to one customer, so the customer's ID lives here
 * and each subclass adds only what is special about itself.
 */
public abstract class Record {

    // Used to join and split the fields of one line in the text files
    protected static final String FIELD_SEPARATOR = ",";

    protected final String id;

    public Record(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /** Prints the record in a readable form. */
    public abstract void displayInfo();

    /** Turns the record into one line of text for saving to a file. */
    public abstract String toFileString();
}