package sharedtrip.memento;

import sharedtrip.composite.Trip;
import sharedtrip.io.TripJson;

// Memento Pattern: เก็บสถานะทริปเป็นข้อความ JSON (คนอื่นแก้ข้างในไม่ได้)
public class TripMemento {
    private final String state;

    public TripMemento(Trip trip) {
        this.state = TripJson.toJson(trip);
    }

    public Trip getState() {
        return TripJson.fromJson(state);
    }
}
