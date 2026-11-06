package sharedtrip.composite;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// ข้อมูลค่าใช้จ่าย 1 รายการ เก็บแค่ชนิดการหาร (splitType) เพื่อบันทึกเป็น JSON ได้
public class Expense {
    private final String id;
    private final String title;
    private final double amount;
    private final String paidBy;
    private final List<String> participants;
    private final String splitType; // equal | percent | exact
    private final Map<String, Double> values;

    public Expense(String id, String title, double amount, String paidBy, List<String> participants,
                   String splitType, Map<String, Double> values) {
        this.id = id == null ? Ids.next() : id;
        this.title = title;
        this.amount = amount;
        this.paidBy = paidBy;
        this.participants = new ArrayList<>(participants);
        this.splitType = splitType == null ? "equal" : splitType;
        this.values = values == null ? new LinkedHashMap<>() : new LinkedHashMap<>(values);
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public String getPaidBy() { return paidBy; }
    public List<String> getParticipants() { return participants; }
    public String getSplitType() { return splitType; }
    public Map<String, Double> getValues() { return values; }
}
