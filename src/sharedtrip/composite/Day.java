package sharedtrip.composite;

import java.util.ArrayList;
import java.util.List;

// Composite: 1 วัน มีหลายกิจกรรม
public class Day extends Group<Activity> {
    private final String id;
    private final String date; // yyyy-MM-dd

    public Day(String id, String date) {
        this.id = id == null ? Ids.next() : id;
        this.date = date;
    }

    public Day(String date) {
        this(null, date);
    }

    @Override
    public Day remove(String activityId) {
        children.removeIf(a -> a.getId().equals(activityId));
        return this;
    }

    // คู่กิจกรรมที่เวลาชนกัน
    public List<Activity[]> getConflicts() {
        List<Activity[]> out = new ArrayList<>();
        for (int i = 0; i < children.size(); i++)
            for (int j = i + 1; j < children.size(); j++)
                if (children.get(i).overlaps(children.get(j)))
                    out.add(new Activity[] { children.get(i), children.get(j) });
        return out;
    }

    public String getId() { return id; }
    public String getDate() { return date; }
}
