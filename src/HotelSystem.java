import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.Scanner;

/*
 * HOTEL AND RESERVATION MANAGEMENT SYSTEM
 * ---------------------------------------
 * - Console-based, written in Java.
 * - The "database" is a hand-made singly linked list (MyLinkedList).
 * - No java.util.LinkedList, ArrayList, Map, HashMap, arrays, etc. are used.
 *
 * Compile:  javac HotelSystem.java
 * Run:      java HotelSystem
 */

// ============================================================
//  1. LINKED LIST (our own implementation)
// ============================================================

interface Matcher<T> {
    boolean matches(T item);
}

class Node<T> {
    T data;
    Node<T> next;

    Node(T data) {
        this.data = data;
    }
}

class MyLinkedList<T> implements Iterable<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    // ADD at the end - O(1) thanks to the tail pointer
    public void addLast(T data) {
        Node<T> node = new Node<>(data);
        if (head == null) {
            head = tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    // ADD at the front
    public void addFirst(T data) {
        Node<T> node = new Node<>(data);
        if (head == null) {
            head = tail = node;
        } else {
            node.next = head;
            head = node;
        }
        size++;
    }

    // SEARCH - returns first item that matches, or null
    public T find(Matcher<T> matcher) {
        Node<T> cur = head;
        while (cur != null) {
            if (matcher.matches(cur.data)) return cur.data;
            cur = cur.next;
        }
        return null;
    }

    // UPDATE - replaces the first matching item's data
    public boolean update(Matcher<T> matcher, T newData) {
        Node<T> cur = head;
        while (cur != null) {
            if (matcher.matches(cur.data)) {
                cur.data = newData;
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    // DELETE - removes the first matching node
    public boolean remove(Matcher<T> matcher) {
        Node<T> prev = null;
        Node<T> cur = head;
        while (cur != null) {
            if (matcher.matches(cur.data)) {
                if (prev == null) {
                    head = cur.next;           // deleting the head
                } else {
                    prev.next = cur.next;      // bypass the node
                }
                if (cur == tail) tail = prev;  // deleting the tail
                size--;
                return true;
            }
            prev = cur;
            cur = cur.next;
        }
        return false;
    }

    // COUNT items that match
    public int count(Matcher<T> matcher) {
        int n = 0;
        Node<T> cur = head;
        while (cur != null) {
            if (matcher.matches(cur.data)) n++;
            cur = cur.next;
        }
        return n;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> cur = head;

            public boolean hasNext() {
                return cur != null;
            }

            public T next() {
                T d = cur.data;
                cur = cur.next;
                return d;
            }
        };
    }
}

// ============================================================
//  2. MODEL CLASSES
// ============================================================

class Room {
    enum Status { AVAILABLE, RESERVED, OCCUPIED }

    private final int number;
    private String type;
    private double pricePerNight;
    private Status status;

    Room(int number, String type, double pricePerNight) {
        this.number = number;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.status = Status.AVAILABLE;
    }

    int getNumber() { return number; }
    String getType() { return type; }
    double getPricePerNight() { return pricePerNight; }
    Status getStatus() { return status; }

    void setType(String type) { this.type = type; }
    void setPricePerNight(double p) { this.pricePerNight = p; }
    void setStatus(Status s) { this.status = s; }
}

class Reservation {
    enum Status { BOOKED, CHECKED_IN, CHECKED_OUT, CANCELLED }

    private final int id;
    private final String guestName;
    private final String contact;
    private final int roomNumber;
    private final LocalDate checkInDate;
    private final int nights;
    private Status status;

    Reservation(int id, String guestName, String contact, int roomNumber,
                LocalDate checkInDate, int nights) {
        this.id = id;
        this.guestName = guestName;
        this.contact = contact;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.nights = nights;
        this.status = Status.BOOKED;
    }

    int getId() { return id; }
    String getGuestName() { return guestName; }
    String getContact() { return contact; }
    int getRoomNumber() { return roomNumber; }
    LocalDate getCheckInDate() { return checkInDate; }
    LocalDate getCheckOutDate() { return checkInDate.plusDays(nights); }
    int getNights() { return nights; }
    Status getStatus() { return status; }
    void setStatus(Status s) { this.status = s; }
}

class LineItem {
    private final String description;
    private final int quantity;
    private final double unitPrice;

    LineItem(String description, int quantity, double unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    String getDescription() { return description; }
    int getQuantity() { return quantity; }
    double getUnitPrice() { return unitPrice; }
    double getAmount() { return quantity * unitPrice; }
}

class Receipt {
    private final String receiptNo;
    private final int reservationId;
    private final String guestName;
    private final int roomNumber;
    private final String roomType;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final MyLinkedList<LineItem> items;   // item breakdown (linked list)
    private final double subtotal;
    private final double tax;
    private final double total;
    private final double amountPaid;
    private final LocalDateTime timestamp;

    Receipt(String receiptNo, Reservation r, Room room, MyLinkedList<LineItem> items,
            double subtotal, double tax, double total, double amountPaid) {
        this.receiptNo = receiptNo;
        this.reservationId = r.getId();
        this.guestName = r.getGuestName();
        this.roomNumber = room.getNumber();
        this.roomType = room.getType();
        this.checkIn = r.getCheckInDate();
        this.checkOut = r.getCheckOutDate();
        this.items = items;
        this.subtotal = subtotal;
        this.tax = tax;
        this.total = total;
        this.amountPaid = amountPaid;
        this.timestamp = LocalDateTime.now();
    }

    String getReceiptNo() { return receiptNo; }
    int getReservationId() { return reservationId; }
    String getGuestName() { return guestName; }
    int getRoomNumber() { return roomNumber; }
    String getRoomType() { return roomType; }
    double getTotal() { return total; }
    LocalDateTime getTimestamp() { return timestamp; }

    void print(String hotelName) {
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
        String line = "=".repeat(56);
        String dash = "-".repeat(56);
        System.out.println();
        System.out.println(line);
        System.out.println(center(hotelName.toUpperCase(), 56));
        System.out.println(center("OFFICIAL RECEIPT", 56));
        System.out.println(line);
        System.out.printf("Receipt No.  : %s%n", receiptNo);
        System.out.printf("Date/Time    : %s%n", timestamp.format(dt));
        System.out.printf("Reservation  : #%d%n", reservationId);
        System.out.printf("Guest        : %s%n", guestName);
        System.out.printf("Room         : %d (%s)%n", roomNumber, roomType);
        System.out.printf("Check-in     : %s%n", checkIn);
        System.out.printf("Check-out    : %s%n", checkOut);
        System.out.println(dash);
        System.out.printf("%-24s %4s %12s %12s%n", "Description", "Qty", "Unit Price", "Amount");
        System.out.println(dash);
        for (LineItem it : items) {
            System.out.printf("%-24s %4d %12.2f %12.2f%n",
                    cut(it.getDescription(), 24), it.getQuantity(), it.getUnitPrice(), it.getAmount());
        }
        System.out.println(dash);
        System.out.printf("%-42s %13.2f%n", "Subtotal", subtotal);
        System.out.printf("%-42s %13.2f%n", "VAT (12%)", tax);
        System.out.printf("%-42s %13.2f%n", "TOTAL", total);
        System.out.printf("%-42s %13.2f%n", "Amount Paid", amountPaid);
        System.out.printf("%-42s %13.2f%n", "Change", amountPaid - total);
        System.out.println(line);
        System.out.println(center("Thank you for staying with us!", 56));
        System.out.println(line);
    }

    private static String center(String s, int width) {
        int pad = Math.max(0, (width - s.length()) / 2);
        return " ".repeat(pad) + s;
    }

    private static String cut(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }
}

// Used by the sales report (aggregated per room type, stored in a linked list)
class TypeSales {
    final String type;
    int transactions;
    double revenue;

    TypeSales(String type) {
        this.type = type;
    }
}

// ============================================================
//  3. MAIN SYSTEM
// ============================================================

public class HotelSystem {

    static final String HOTEL_NAME = "Grand Lumina Hotel";
    static final double TAX_RATE = 0.12;

    // The "database" - all linked lists
    static MyLinkedList<Room> rooms = new MyLinkedList<>();
    static MyLinkedList<Reservation> reservations = new MyLinkedList<>();
    static MyLinkedList<Receipt> receipts = new MyLinkedList<>();

    static int nextReservationId = 1;
    static int nextReceiptNo = 1;

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        seedData();
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("==========================================");
            System.out.println("   " + HOTEL_NAME.toUpperCase());
            System.out.println("   Hotel and Reservation Management");
            System.out.println("==========================================");
            System.out.println("  [1] Room Management");
            System.out.println("  [2] Reservations");
            System.out.println("  [3] Check-out and Billing");
            System.out.println("  [4] Reports (Sales / Transactions)");
            System.out.println("  [0] Exit");
            System.out.println("------------------------------------------");
            int choice = readInt("Choose an option: ", 0, 4);
            switch (choice) {
                case 1: roomMenu(); break;
                case 2: reservationMenu(); break;
                case 3: checkOut(); break;
                case 4: reportMenu(); break;
                case 0:
                    running = false;
                    System.out.println("\nThank you for using the system. Goodbye!");
                    break;
            }
        }
    }

    static void seedData() {
        rooms.addLast(new Room(101, "Single", 1500));
        rooms.addLast(new Room(102, "Single", 1500));
        rooms.addLast(new Room(201, "Double", 2500));
        rooms.addLast(new Room(202, "Double", 2500));
        rooms.addLast(new Room(301, "Suite", 5000));
    }

    // ------------------------------------------------------------
    //  ROOM MANAGEMENT
    // ------------------------------------------------------------
    static void roomMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- ROOM MANAGEMENT ---");
            System.out.println("  [1] Add Room");
            System.out.println("  [2] View All Rooms");
            System.out.println("  [3] View Available Rooms");
            System.out.println("  [4] Update Room (type / price)");
            System.out.println("  [5] Remove Room");
            System.out.println("  [6] Search Room");
            System.out.println("  [0] Back");
            int c = readInt("Choose an option: ", 0, 6);
            switch (c) {
                case 1: addRoom(); break;
                case 2: displayRooms(false); break;
                case 3: displayRooms(true); break;
                case 4: updateRoom(); break;
                case 5: removeRoom(); break;
                case 6: searchRoom(); break;
                case 0: back = true; break;
            }
        }
    }

    static Room findRoom(final int number) {
        return rooms.find(r -> r.getNumber() == number);
    }

    static void addRoom() {
        System.out.println("\n--- Add Room ---");
        int number = readInt("Room number: ", 1, 99999);
        if (findRoom(number) != null) {
            System.out.println("  ! Room " + number + " already exists.");
            return;
        }
        String type = readString("Room type (e.g., Single, Double, Suite): ");
        double price = readDouble("Price per night: ");
        rooms.addLast(new Room(number, type, price));
        System.out.println("  Room " + number + " added successfully.");
    }

    static void displayRooms(boolean availableOnly) {
        System.out.println("\n" + (availableOnly ? "--- AVAILABLE ROOMS ---" : "--- ALL ROOMS ---"));
        System.out.printf("%-8s %-12s %14s  %-10s%n", "Room #", "Type", "Price/Night", "Status");
        System.out.println("-".repeat(50));
        int shown = 0;
        for (Room r : rooms) {
            if (availableOnly && r.getStatus() != Room.Status.AVAILABLE) continue;
            System.out.printf("%-8d %-12s %14.2f  %-10s%n",
                    r.getNumber(), r.getType(), r.getPricePerNight(), r.getStatus());
            shown++;
        }
        if (shown == 0) System.out.println("  (no rooms to show)");
    }

    static void updateRoom() {
        System.out.println("\n--- Update Room ---");
        if (rooms.isEmpty()) {
            System.out.println("  ! There are no rooms yet.");
            return;
        }
        int number = readInt("Room number to update: ", 1, 99999);
        Room r = findRoom(number);
        if (r == null) {
            System.out.println("  ! Room not found.");
            return;
        }
        System.out.printf("Current: %s, %.2f per night%n", r.getType(), r.getPricePerNight());
        System.out.println("  [1] Change type");
        System.out.println("  [2] Change price");
        System.out.println("  [3] Change both");
        int c = readInt("Choose: ", 1, 3);
        if (c == 1 || c == 3) r.setType(readString("New type: "));
        if (c == 2 || c == 3) r.setPricePerNight(readDouble("New price per night: "));
        System.out.println("  Room " + number + " updated.");
    }

    static void removeRoom() {
        System.out.println("\n--- Remove Room ---");
        int number = readInt("Room number to remove: ", 1, 99999);
        Room r = findRoom(number);
        if (r == null) {
            System.out.println("  ! Room not found.");
            return;
        }
        if (r.getStatus() != Room.Status.AVAILABLE) {
            System.out.println("  ! Cannot remove a room that is " + r.getStatus() + ".");
            return;
        }
        rooms.remove(x -> x.getNumber() == number);
        System.out.println("  Room " + number + " removed.");
    }

    static void searchRoom() {
        System.out.println("\n--- Search Room ---");
        int number = readInt("Room number: ", 1, 99999);
        Room r = findRoom(number);
        if (r == null) {
            System.out.println("  ! Room not found.");
            return;
        }
        System.out.println("  Room #" + r.getNumber() + " | " + r.getType() + " | "
                + String.format("%.2f", r.getPricePerNight()) + " per night | " + r.getStatus());
    }

    // ------------------------------------------------------------
    //  RESERVATIONS
    // ------------------------------------------------------------
    static void reservationMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- RESERVATIONS ---");
            System.out.println("  [1] Make Reservation");
            System.out.println("  [2] View All Reservations");
            System.out.println("  [3] Search Reservation (by guest name)");
            System.out.println("  [4] Check-in Guest");
            System.out.println("  [5] Cancel Reservation");
            System.out.println("  [0] Back");
            int c = readInt("Choose an option: ", 0, 5);
            switch (c) {
                case 1: makeReservation(); break;
                case 2: displayReservations(null); break;
                case 3: searchReservation(); break;
                case 4: checkIn(); break;
                case 5: cancelReservation(); break;
                case 0: back = true; break;
            }
        }
    }

    static Reservation findReservation(final int id) {
        return reservations.find(r -> r.getId() == id);
    }

    static void makeReservation() {
        System.out.println("\n--- Make Reservation ---");
        if (rooms.count(r -> r.getStatus() == Room.Status.AVAILABLE) == 0) {
            System.out.println("  ! No rooms are available right now.");
            return;
        }
        displayRooms(true);
        int roomNo = readInt("\nRoom number to reserve: ", 1, 99999);
        Room room = findRoom(roomNo);
        if (room == null) {
            System.out.println("  ! Room not found.");
            return;
        }
        if (room.getStatus() != Room.Status.AVAILABLE) {
            System.out.println("  ! Room " + roomNo + " is not available.");
            return;
        }
        String guest = readString("Guest name: ");
        String contact = readString("Contact number: ");
        LocalDate checkIn = readDate("Check-in date (yyyy-MM-dd): ");
        if (checkIn.isBefore(LocalDate.now())) {
            System.out.println("  ! Check-in date cannot be in the past.");
            return;
        }
        int nights = readInt("Number of nights (1-365): ", 1, 365);

        Reservation res = new Reservation(nextReservationId++, guest, contact, roomNo, checkIn, nights);
        reservations.addLast(res);
        room.setStatus(Room.Status.RESERVED);
        System.out.println("\n  Reservation #" + res.getId() + " created for " + guest
                + " (Room " + roomNo + ", " + nights + " night/s, until " + res.getCheckOutDate() + ").");
    }

    // filter: null = show all
    static void displayReservations(Reservation.Status filter) {
        System.out.println("\n--- RESERVATIONS ---");
        System.out.printf("%-5s %-18s %-6s %-12s %-12s %-6s %-12s%n",
                "ID", "Guest", "Room", "Check-in", "Check-out", "Nights", "Status");
        System.out.println("-".repeat(80));
        int shown = 0;
        for (Reservation r : reservations) {
            if (filter != null && r.getStatus() != filter) continue;
            printReservationRow(r);
            shown++;
        }
        if (shown == 0) System.out.println("  (nothing to show)");
    }

    static void printReservationRow(Reservation r) {
        System.out.printf("%-5d %-18s %-6d %-12s %-12s %-6d %-12s%n",
                r.getId(), cut(r.getGuestName(), 18), r.getRoomNumber(),
                r.getCheckInDate(), r.getCheckOutDate(), r.getNights(), r.getStatus());
    }

    static void searchReservation() {
        System.out.println("\n--- Search Reservation ---");
        final String key = readString("Guest name (or part of it): ").toLowerCase();
        int found = 0;
        for (Reservation r : reservations) {
            if (r.getGuestName().toLowerCase().contains(key)) {
                printReservationRow(r);
                found++;
            }
        }
        if (found == 0) System.out.println("  No reservation matches \"" + key + "\".");
    }

    static void checkIn() {
        System.out.println("\n--- Check-in Guest ---");
        if (reservations.count(r -> r.getStatus() == Reservation.Status.BOOKED) == 0) {
            System.out.println("  ! No booked reservations to check in.");
            return;
        }
        displayReservations(Reservation.Status.BOOKED);
        int id = readInt("\nReservation ID to check in: ", 1, Integer.MAX_VALUE);
        Reservation r = findReservation(id);
        if (r == null) {
            System.out.println("  ! Reservation not found.");
            return;
        }
        if (r.getStatus() != Reservation.Status.BOOKED) {
            System.out.println("  ! Reservation #" + id + " is " + r.getStatus() + " and cannot be checked in.");
            return;
        }
        Room room = findRoom(r.getRoomNumber());
        if (room != null) room.setStatus(Room.Status.OCCUPIED);
        r.setStatus(Reservation.Status.CHECKED_IN);
        System.out.println("  " + r.getGuestName() + " is now checked in to Room " + r.getRoomNumber() + ".");
    }

    static void cancelReservation() {
        System.out.println("\n--- Cancel Reservation ---");
        if (reservations.count(r -> r.getStatus() == Reservation.Status.BOOKED) == 0) {
            System.out.println("  ! No booked reservations to cancel.");
            return;
        }
        displayReservations(Reservation.Status.BOOKED);
        int id = readInt("\nReservation ID to cancel: ", 1, Integer.MAX_VALUE);
        Reservation r = findReservation(id);
        if (r == null) {
            System.out.println("  ! Reservation not found.");
            return;
        }
        if (r.getStatus() != Reservation.Status.BOOKED) {
            System.out.println("  ! Only BOOKED reservations can be cancelled (this one is " + r.getStatus() + ").");
            return;
        }
        if (!confirm("Cancel reservation #" + id + "? (y/n): ")) {
            System.out.println("  Cancelled the cancellation. Nothing changed.");
            return;
        }
        r.setStatus(Reservation.Status.CANCELLED);
        Room room = findRoom(r.getRoomNumber());
        if (room != null) room.setStatus(Room.Status.AVAILABLE);
        System.out.println("  Reservation #" + id + " cancelled. Room " + r.getRoomNumber() + " is available again.");
    }

    // ------------------------------------------------------------
    //  CHECK-OUT AND BILLING  (generates the receipt)
    // ------------------------------------------------------------
    static void checkOut() {
        System.out.println("\n--- Check-out and Billing ---");
        if (reservations.count(r -> r.getStatus() == Reservation.Status.CHECKED_IN) == 0) {
            System.out.println("  ! No checked-in guests to check out.");
            return;
        }
        displayReservations(Reservation.Status.CHECKED_IN);
        int id = readInt("\nReservation ID to check out: ", 1, Integer.MAX_VALUE);
        Reservation res = findReservation(id);
        if (res == null) {
            System.out.println("  ! Reservation not found.");
            return;
        }
        if (res.getStatus() != Reservation.Status.CHECKED_IN) {
            System.out.println("  ! Reservation #" + id + " is " + res.getStatus() + ", not checked in.");
            return;
        }
        Room room = findRoom(res.getRoomNumber());
        if (room == null) {
            System.out.println("  ! Room record is missing. Cannot bill.");
            return;
        }

        // Build the item breakdown (linked list)
        MyLinkedList<LineItem> items = new MyLinkedList<>();
        items.addLast(new LineItem("Room " + room.getNumber() + " (" + room.getType() + ")",
                res.getNights(), room.getPricePerNight()));

        System.out.println("\nAdd extra charges (e.g., Breakfast, Laundry). Leave the description blank to finish.");
        while (true) {
            System.out.print("Extra charge description: ");
            String desc = sc.nextLine().trim();
            if (desc.isEmpty()) break;
            int qty = readInt("  Quantity: ", 1, 1000);
            double price = readDouble("  Unit price: ");
            items.addLast(new LineItem(desc, qty, price));
            System.out.println("  Added.");
        }

        double subtotal = 0;
        for (LineItem it : items) subtotal += it.getAmount();
        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;

        System.out.printf("%nSubtotal: %.2f | VAT: %.2f | TOTAL DUE: %.2f%n", subtotal, tax, total);

        double paid;
        while (true) {
            paid = readDouble("Amount paid: ");
            if (paid + 0.0001 >= total) break;
            System.out.printf("  ! Payment is not enough. Total due is %.2f.%n", total);
        }

        String receiptNo = String.format("RCPT-%04d", nextReceiptNo++);
        Receipt receipt = new Receipt(receiptNo, res, room, items, subtotal, tax, total, paid);
        receipts.addLast(receipt);

        res.setStatus(Reservation.Status.CHECKED_OUT);
        room.setStatus(Room.Status.AVAILABLE);

        receipt.print(HOTEL_NAME);
    }

    // ------------------------------------------------------------
    //  REPORTS
    // ------------------------------------------------------------
    static void reportMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- REPORTS ---");
            System.out.println("  [1] Transaction History");
            System.out.println("  [2] View / Reprint a Receipt");
            System.out.println("  [3] Sales Report");
            System.out.println("  [4] Occupancy Summary");
            System.out.println("  [0] Back");
            int c = readInt("Choose an option: ", 0, 4);
            switch (c) {
                case 1: transactionHistory(); break;
                case 2: reprintReceipt(); break;
                case 3: salesReport(); break;
                case 4: occupancySummary(); break;
                case 0: back = true; break;
            }
        }
    }

    static void transactionHistory() {
        System.out.println("\n--- TRANSACTION HISTORY ---");
        if (receipts.isEmpty()) {
            System.out.println("  (no transactions yet)");
            return;
        }
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        System.out.printf("%-10s %-18s %-6s %-12s %12s  %-16s%n",
                "Receipt", "Guest", "Room", "Type", "Total", "Date/Time");
        System.out.println("-".repeat(82));
        for (Receipt r : receipts) {
            System.out.printf("%-10s %-18s %-6d %-12s %12.2f  %-16s%n",
                    r.getReceiptNo(), cut(r.getGuestName(), 18), r.getRoomNumber(),
                    r.getRoomType(), r.getTotal(), r.getTimestamp().format(dt));
        }
    }

    static void reprintReceipt() {
        System.out.println("\n--- View / Reprint Receipt ---");
        if (receipts.isEmpty()) {
            System.out.println("  (no receipts yet)");
            return;
        }
        final String no = readString("Receipt number (e.g., RCPT-0001): ").toUpperCase();
        Receipt r = receipts.find(x -> x.getReceiptNo().equals(no));
        if (r == null) {
            System.out.println("  ! Receipt not found.");
            return;
        }
        r.print(HOTEL_NAME);
    }

    static void salesReport() {
        System.out.println("\n--- SALES REPORT ---");
        if (receipts.isEmpty()) {
            System.out.println("  (no sales yet)");
            return;
        }
        MyLinkedList<TypeSales> byType = new MyLinkedList<>();
        double grand = 0;
        for (Receipt r : receipts) {
            final String type = r.getRoomType();
            TypeSales ts = byType.find(t -> t.type.equalsIgnoreCase(type));
            if (ts == null) {
                ts = new TypeSales(type);
                byType.addLast(ts);
            }
            ts.transactions++;
            ts.revenue += r.getTotal();
            grand += r.getTotal();
        }
        System.out.printf("%-14s %14s %16s%n", "Room Type", "Transactions", "Revenue");
        System.out.println("-".repeat(46));
        for (TypeSales ts : byType) {
            System.out.printf("%-14s %14d %16.2f%n", ts.type, ts.transactions, ts.revenue);
        }
        System.out.println("-".repeat(46));
        System.out.printf("%-14s %14d %16.2f%n", "TOTAL", receipts.size(), grand);
        System.out.printf("Average per transaction: %.2f%n", grand / receipts.size());
    }

    static void occupancySummary() {
        System.out.println("\n--- OCCUPANCY SUMMARY ---");
        int total = rooms.size();
        int avail = rooms.count(r -> r.getStatus() == Room.Status.AVAILABLE);
        int resv = rooms.count(r -> r.getStatus() == Room.Status.RESERVED);
        int occ = rooms.count(r -> r.getStatus() == Room.Status.OCCUPIED);
        System.out.println("  Total rooms : " + total);
        System.out.println("  Available   : " + avail);
        System.out.println("  Reserved    : " + resv);
        System.out.println("  Occupied    : " + occ);
        System.out.println("  Reservations: " + reservations.size() + " total ("
                + reservations.count(r -> r.getStatus() == Reservation.Status.CANCELLED) + " cancelled)");
    }

    // ------------------------------------------------------------
    //  INPUT HELPERS (error handling)
    // ------------------------------------------------------------
    static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v < min || v > max) {
                    System.out.println("  ! Please enter a number from " + min + " to " + max + ".");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("  ! Invalid input. Please enter a whole number.");
            }
        }
    }

    static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                double v = Double.parseDouble(s);
                if (Double.isNaN(v) || Double.isInfinite(v) || v <= 0) {
                    System.out.println("  ! Amount must be greater than 0.");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("  ! Invalid input. Please enter a number (e.g., 1500 or 1500.50).");
            }
        }
    }

    static String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("  ! This field cannot be empty.");
        }
    }

    static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                return LocalDate.parse(s);
            } catch (DateTimeParseException e) {
                System.out.println("  ! Invalid date. Use the format yyyy-MM-dd (e.g., 2026-10-20).");
            }
        }
    }

    static boolean confirm(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim().toLowerCase();
            if (s.equals("y") || s.equals("yes")) return true;
            if (s.equals("n") || s.equals("no")) return false;
            System.out.println("  ! Please answer y or n.");
        }
    }

    static String cut(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }
}