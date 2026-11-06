package sharedtrip.builder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import sharedtrip.composite.Day;
import sharedtrip.composite.Trip;

// Builder Pattern: สร้างทริปทีละขั้น แล้วตรวจข้อมูลใน build()
public class TripBuilder {
    private String name = "";
    private double budget = 0;
    private final List<String> members = new ArrayList<>();
    private final List<Day> days = new ArrayList<>();

    public TripBuilder setName(String name) {
        this.name = name == null ? "" : name.trim();
        return this;
    }

    public TripBuilder setBudget(double budget) {
        this.budget = Math.max(0, budget);
        return this;
    }

    public TripBuilder addMember(String member) {
        if (member != null && !member.trim().isEmpty() && !members.contains(member.trim()))
            members.add(member.trim());
        return this;
    }

    // สร้าง Day ให้ทุกวันในช่วงวันที่
    public TripBuilder setDateRange(LocalDate start, LocalDate end) {
        days.clear();
        if (start == null) return this;
        if (end == null || end.isBefore(start)) end = start;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1))
            days.add(new Day(d.toString()));
        return this;
    }

    public Trip build() {
        if (name.isEmpty()) throw new IllegalStateException("กรุณาตั้งชื่อทริป");
        if (members.isEmpty()) throw new IllegalStateException("ต้องมีสมาชิกอย่างน้อย 1 คน");
        if (days.isEmpty()) throw new IllegalStateException("กรุณาเลือกวันเดินทาง");
        Trip trip = new Trip(null, name, budget, members);
        for (Day d : days) trip.add(d);
        return trip;
    }
}
