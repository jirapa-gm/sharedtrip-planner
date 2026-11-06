package sharedtrip.composite;

import java.util.ArrayList;
import java.util.List;

// Composite: root ของทริป มีหลายวัน + รายการค่าใช้จ่าย
public class Trip extends Group<Day> {
    private final String id;
    private final String name;
    private final double budget;
    private final List<String> members;
    private final List<Expense> expenses = new ArrayList<>();

    public Trip(String id, String name, double budget, List<String> members) {
        this.id = id == null ? Ids.next() : id;
        this.name = name;
        this.budget = budget;
        this.members = new ArrayList<>(members);
    }

    @Override
    public Trip remove(String dayId) {
        children.removeIf(d -> d.getId().equals(dayId));
        return this;
    }

    public Day findDay(String dayId) {
        for (Day d : children) if (d.getId().equals(dayId)) return d;
        return null;
    }

    public double getExpenseTotal() {
        double sum = 0;
        for (Expense e : expenses) sum += e.getAmount();
        return sum;
    }

    // ยอดใช้จ่ายทั้งหมด = ค่ากิจกรรม (Composite) + ค่าใช้จ่ายที่จด
    public double getTotalSpent() {
        return getCost() + getExpenseTotal();
    }

    public boolean isOverBudget() {
        return budget > 0 && getTotalSpent() > budget;
    }

    public void addExpense(Expense e) { expenses.add(e); }
    public void removeExpense(String expenseId) { expenses.removeIf(e -> e.getId().equals(expenseId)); }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getBudget() { return budget; }
    public List<String> getMembers() { return members; }
    public List<Day> getDays() { return children; }
    public List<Expense> getExpenses() { return expenses; }
}
