package sharedtrip.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import sharedtrip.composite.Trip;
import sharedtrip.io.TripJson;
import sharedtrip.memento.History;
import sharedtrip.memento.TripMemento;

// Observer Pattern: TripStore เป็น Subject ทุกการแก้ไขผ่าน update()
// เพื่อบันทึก Memento ก่อนเปลี่ยน แล้ว notify ทุกหน้าจอ
public class TripStore {
    private static final TripStore INSTANCE = new TripStore();

    private final List<TripObserver> observers = new ArrayList<>();
    private History history = new History();
    private Trip trip;

    private TripStore() {}

    public static TripStore getInstance() {
        return INSTANCE;
    }

    public void subscribe(TripObserver o) { observers.add(o); }
    public void unsubscribe(TripObserver o) { observers.remove(o); }

    public void notifyObservers() {
        for (TripObserver o : new ArrayList<>(observers)) o.onTripChanged(trip);
    }

    public Trip getTrip() { return trip; }

    public void update(Consumer<Trip> mutator) {
        if (trip == null) return;
        history.save(new TripMemento(trip));
        Trip draft = TripJson.fromJson(TripJson.toJson(trip)); // ทำงานกับสำเนา
        mutator.accept(draft);
        trip = draft;
        notifyObservers();
    }

    public void setTrip(Trip newTrip) {
        if (trip != null) history.save(new TripMemento(trip));
        trip = newTrip;
        notifyObservers();
    }

    public void undo() {
        if (trip == null) return;
        TripMemento m = history.undo(new TripMemento(trip));
        if (m != null) { trip = m.getState(); notifyObservers(); }
    }

    public void redo() {
        if (trip == null) return;
        TripMemento m = history.redo(new TripMemento(trip));
        if (m != null) { trip = m.getState(); notifyObservers(); }
    }

    public boolean canUndo() { return history.canUndo(); }
    public boolean canRedo() { return history.canRedo(); }

    public void reset() {
        history = new History();
        trip = null;
        notifyObservers();
    }
}
