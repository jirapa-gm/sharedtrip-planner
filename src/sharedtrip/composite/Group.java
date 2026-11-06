package sharedtrip.composite;

import java.util.ArrayList;
import java.util.List;

// Composite: กลุ่มที่มีลูกเป็น TripComponent ยอดรวมคือผลรวมของลูกทุกตัว
public abstract class Group<T extends TripComponent> implements TripComponent {
    protected final List<T> children = new ArrayList<>();

    public Group<T> add(T child) {
        children.add(child);
        return this;
    }

    public abstract Group<T> remove(String id);

    public List<T> getChildren() {
        return children;
    }

    @Override
    public double getCost() {
        double sum = 0;
        for (T c : children) sum += c.getCost();
        return sum;
    }

    @Override
    public int getDuration() {
        int sum = 0;
        for (T c : children) sum += c.getDuration();
        return sum;
    }
}
