/**
 * Base class for everything saved in the text files (Customer, Meter, Bill).
 * Holds the customer ID that every record belongs to, and forces each
 * subclass to say how it is displayed and how it is written to a file.
 */
public abstract class Record {

    /** Separator between fields in every saved line. */
    protected static final String FIELD_SEPARATOR = ",";

    /** The customer ID this record belongs to. */
    protected final String id;

    protected Record(String id) {
        this.id = id;
    }

    /** Prints the record's details to the console. */
    public abstract void displayInfo();

    /** Returns the record as one line of text, ready to save to a file. */
    public abstract String toFileString();
}