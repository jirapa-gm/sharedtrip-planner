package sharedtrip.memento;

import java.util.ArrayDeque;
import java.util.Deque;

// Caretaker ของ Memento: เก็บประวัติไว้ทำ Undo / Redo
public class History {
    private final Deque<TripMemento> undoStack = new ArrayDeque<>();
    private final Deque<TripMemento> redoStack = new ArrayDeque<>();
    private final int limit;

    public History(int limit) {
        this.limit = limit;
    }

    public History() {
        this(50);
    }

    public void save(TripMemento memento) {
        undoStack.push(memento);
        if (undoStack.size() > limit) undoStack.removeLast();
        redoStack.clear();
    }

    public TripMemento undo(TripMemento current) {
        if (undoStack.isEmpty()) return null;
        redoStack.push(current);
        return undoStack.pop();
    }

    public TripMemento redo(TripMemento current) {
        if (redoStack.isEmpty()) return null;
        undoStack.push(current);
        return redoStack.pop();
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
}
