package sharedtrip.composite;

// Leaf ของ Composite: กิจกรรม 1 รายการ
public class Activity implements TripComponent {
    private final String id;
    private String name;
    private String start; // HH:mm
    private String end;   // HH:mm
    private double cost;
    private String place;

    public Activity(String id, String name, String start, String end, double cost, String place) {
        this.id = id == null ? Ids.next() : id;
        this.name = name;
        this.start = start;
        this.end = end;
        this.cost = cost;
        this.place = place == null ? "" : place;
    }

    public Activity(String name, String start, String end, double cost, String place) {
        this(null, name, start, end, cost, place);
    }

    static int toMinutes(String hhmm) {
        String[] p = hhmm.split(":");
        return Integer.parseInt(p[0].trim()) * 60 + Integer.parseInt(p[1].trim());
    }

    @Override
    public double getCost() {
        return cost;
    }

    @Override
    public int getDuration() {
        return Math.max(0, toMinutes(end) - toMinutes(start));
    }

    // เวลาทับกันหรือไม่
    public boolean overlaps(Activity other) {
        return toMinutes(start) < toMinutes(other.end) && toMinutes(other.start) < toMinutes(end);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getStart() { return start; }
    public String getEnd() { return end; }
    public String getPlace() { return place; }
}
