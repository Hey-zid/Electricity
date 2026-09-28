# Electricity Management System

A desktop customer, meter and billing platform for a utility, built with **Java Swing** and **flat text files** — no database server and no login required.

Register customers, log meter readings, generate monthly bills at a fixed rate, track payment status, and produce an annual report — all from a single-window GUI.

---

## Features

| Screen | What it does |
|---|---|
| **Register Customer** | Adds a new customer, blocking duplicate IDs |
| **Search Customer** | Looks up a customer by ID |
| **Update Customer** | Loads a customer's current info, then saves your edits |
| **Delete Customer** | Deletes a customer and cascades to their meters and bills |
| **Add Meter Reading** | Records a new reading, rejecting any value below the previous one |
| **Add Monthly Bill** | Turns the oldest unbilled reading into a bill at a fixed rate |
| **Update Bill Status** | Marks a bill Paid or Unpaid |
| **Delete Bill** | Removes a single bill |
| **Annual Bill Report** | Totals units, amount billed, amount paid and amount due |

## Getting Started

### Requirements
- Java 17 or later (JDK)

### Run it

```bash
javac *.java
java Main
```

The app opens a 650×500 window. All screens are reachable from the home menu and every screen has a **Back to Home** button.

No setup is required — `customers.txt`, `meters.txt` and `bills.txt` are created automatically in the working directory the first time they're needed.

## Project Structure

```
.
├── Main.java            # GUI: one JFrame, ten CardLayout panels
├── FileManager.java      # The only class that reads or writes the data files
├── Record.java            # Abstract base class shared by every entity
├── Customer.java          # Customer entity (fully editable)
├── Meter.java              # Meter reading entity (immutable once saved)
├── Bill.java                # Bill entity (only its status can change)
├── customers.txt            # Generated — one row per customer
├── meters.txt                # Generated — one row per meter reading
└── bills.txt                   # Generated — one row per bill
```

## Architecture

The system is organized into four layers, and every request flows in one direction — top to bottom:

```
┌─────────────────────────────────────────┐
│  1. Presentation — Main (JFrame)         │  10 panels, CardLayout, never touches files
├─────────────────────────────────────────┤
│  2. Data Access — FileManager            │  Single gateway for every read and write
├─────────────────────────────────────────┤
│  3. Domain Model — Record → Customer /   │  Business rules and entity state
│     Meter / Bill                         │
├─────────────────────────────────────────┤
│  4. Storage — customers.txt, meters.txt, │  Human-readable CSV rows
│     bills.txt                            │
└─────────────────────────────────────────┘
```

### Domain model

`Record` is an abstract base class holding the shared `id` field. Each subclass decides how much of its own state can change after creation:

- **`Customer`** — fully editable (name, address, meter number)
- **`Meter`** — immutable once saved; `getUnitsUsed()` is simply `current − previous`
- **`Bill`** — only its `status` field can change

### File format

Each entity has a matching CSV row, written by its own `toFileString()`:

| File | Row format |
|---|---|
| `customers.txt` | `id,name,address,meterNumber` |
| `meters.txt` | `id,meterNumber,previousReading,currentReading` |
| `bills.txt` | `id,month,unitsUsed,reading,status` |

## Billing Logic

Adding a bill runs four checks in order:

1. **Customer exists** — `findCustomerById(id) != null`
2. **An unbilled reading exists** — `findNextUnbilledMeter(id) != null`. A reading counts as billed once some bill records that exact reading value.
3. **Status is valid** — must be `Paid` or `Unpaid`, case-insensitive
4. **Amount is computed** — `unitsUsed × RATE_PER_UNIT` (5.0 Taka per unit)

A meter reading is rejected if it's below the previous one; the very first reading for a customer is compared against 0.

### Worked example — customer C1042

| Month | Previous | Current | Units | Amount | Status |
|---|---|---|---|---|---|
| Jan | 1000 | 1120 | 120 | 600.0 | Paid |
| Feb | 1120 | 1254 | 134 | 670.0 | Paid |
| Mar | 1254 | 1352 | 98 | 490.0 | Unpaid |

**Totals:** 352 units billed · 1,760.0 Taka billed · 1,270.0 Taka paid · 490.0 Taka due

## Error Handling

Every guard clause produces a specific, human-readable message rather than a stack trace:

| Trigger | Message |
|---|---|
| Duplicate customer ID | `Error: Customer ID … already exists.` |
| Unknown customer | `Error: Customer does not exist.` |
| Reading below previous | `Error: Current reading cannot be less than previous reading (n).` |
| Non-numeric reading | `Error: Please enter a valid number for the reading.` |
| Everything already billed | `Error: No unbilled meter reading found.` |
| Invalid status | `Error: Status must be 'Paid' or 'Unpaid'.` |
| Bill month not found | `Error: Bill not found.` |
| Delete declined | `Delete cancelled.` |

`DuplicateCustomerException` and `CustomerNotFoundException` are checked exceptions nested inside `FileManager`.

## Design Patterns

| Pattern | Where |
|---|---|
| **Facade** | `FileManager` hides all file I/O behind a small set of intent-named methods |
| **Page Template** | `buildPageFrame()` gives every screen the same title, form, output and button layout |
| **Data Mapper** | `toFileString()` writes an entity to a row; `toMeter()` / `toBill()` read it back |
| **Navigator** | One `CardLayout` swaps panels by name; every panel returns to `home` |

## Testing

Seven scenarios exercise the riskiest logic. Each can be run by hand in the GUI and verified by opening the three text files.

| Scenario | Input | Expected result |
|---|---|---|
| Duplicate ID | Register the same customer twice | Second attempt shows the already-exists error |
| Backwards reading | Previous 1120, enter 1100 | Rejected, previous reading (1120) shown in the message |
| Bad number | Enter `abc` as a reading | "Please enter a valid number" message |
| Nothing to bill | Add a bill when all readings are billed | "No unbilled meter reading found" |
| Invalid status | Enter `Pending` | "Status must be Paid or Unpaid" |
| Cascade delete | Delete a customer, then open their report | Customer, meters and bills all gone |
| Report totals | 600 Paid, 670 Paid, 490 Unpaid | Billed 1760.0 · Paid 1270.0 · Due 490.0 |

## Known Limitations & Roadmap

This is a working prototype, not a hardened production system:

- [ ] **No database** — move to SQLite or MySQL via JDBC transactions for concurrent, durable storage
- [ ] **Unescaped delimiters** — a comma inside an address currently breaks `split(",")`
- [ ] **Flat rate only** — replace `RATE_PER_UNIT` with slab-based tariffs
- [ ] **Fragile bill–meter link** — bills are matched to meters by reading value; link by `id` instead

## License

Add your license here.