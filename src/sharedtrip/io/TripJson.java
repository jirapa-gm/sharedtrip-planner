package sharedtrip.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import sharedtrip.composite.Activity;
import sharedtrip.composite.Day;
import sharedtrip.composite.Expense;
import sharedtrip.composite.Trip;

// แปลง Trip <-> JSON ใช้ทั้งบันทึกไฟล์และเก็บ Memento
public final class TripJson {
    private TripJson() {}

    public static String toJson(Trip t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("name", t.getName());
        m.put("budget", t.getBudget());
        m.put("members", new ArrayList<Object>(t.getMembers()));
        List<Object> days = new ArrayList<>();
        for (Day d : t.getDays()) {
            Map<String, Object> dm = new LinkedHashMap<>();
            dm.put("id", d.getId());
            dm.put("date", d.getDate());
            List<Object> acts = new ArrayList<>();
            for (Activity a : d.getChildren()) {
                Map<String, Object> am = new LinkedHashMap<>();
                am.put("id", a.getId());
                am.put("name", a.getName());
                am.put("start", a.getStart());
                am.put("end", a.getEnd());
                am.put("cost", a.getCost());
                am.put("place", a.getPlace());
                acts.add(am);
            }
            dm.put("activities", acts);
            days.add(dm);
        }
        m.put("days", days);
        List<Object> exps = new ArrayList<>();
        for (Expense e : t.getExpenses()) {
            Map<String, Object> em = new LinkedHashMap<>();
            em.put("id", e.getId());
            em.put("title", e.getTitle());
            em.put("amount", e.getAmount());
            em.put("paidBy", e.getPaidBy());
            em.put("participants", new ArrayList<Object>(e.getParticipants()));
            Map<String, Object> split = new LinkedHashMap<>();
            split.put("type", e.getSplitType());
            split.put("values", new LinkedHashMap<String, Object>(e.getValues()));
            em.put("split", split);
            exps.add(em);
        }
        m.put("expenses", exps);
        return Json.stringify(m);
    }

    @SuppressWarnings("unchecked")
    public static Trip fromJson(String text) {
        Map<String, Object> m = (Map<String, Object>) Json.parse(text);
        Trip t = new Trip(str(m.get("id")), str(m.get("name")), num(m.get("budget")), strList(m.get("members")));
        for (Object o : list(m.get("days"))) {
            Map<String, Object> dm = (Map<String, Object>) o;
            Day d = new Day(str(dm.get("id")), str(dm.get("date")));
            for (Object ao : list(dm.get("activities"))) {
                Map<String, Object> am = (Map<String, Object>) ao;
                d.add(new Activity(str(am.get("id")), str(am.get("name")), str(am.get("start")),
                        str(am.get("end")), num(am.get("cost")), str(am.get("place"))));
            }
            t.add(d);
        }
        for (Object o : list(m.get("expenses"))) {
            Map<String, Object> em = (Map<String, Object>) o;
            Map<String, Object> split = em.get("split") instanceof Map ? (Map<String, Object>) em.get("split") : new LinkedHashMap<>();
            Map<String, Double> values = new LinkedHashMap<>();
            if (split.get("values") instanceof Map)
                for (Map.Entry<String, Object> v : ((Map<String, Object>) split.get("values")).entrySet())
                    values.put(v.getKey(), num(v.getValue()));
            t.addExpense(new Expense(str(em.get("id")), str(em.get("title")), num(em.get("amount")),
                    str(em.get("paidBy")), strList(em.get("participants")),
                    split.get("type") == null ? "equal" : str(split.get("type")), values));
        }
        return t;
    }

    public static void save(Trip t, Path file) throws IOException {
        Files.writeString(file, toJson(t), StandardCharsets.UTF_8);
    }

    public static Trip load(Path file) throws IOException {
        return fromJson(Files.readString(file, StandardCharsets.UTF_8));
    }

    private static String str(Object o) { return o == null ? null : o.toString(); }

    private static double num(Object o) {
        if (o instanceof Number) return ((Number) o).doubleValue();
        if (o instanceof String && !((String) o).isEmpty()) return Double.parseDouble((String) o);
        return 0;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> list(Object o) { return o instanceof List ? (List<Object>) o : new ArrayList<>(); }

    private static List<String> strList(Object o) {
        List<String> out = new ArrayList<>();
        for (Object x : list(o)) out.add(String.valueOf(x));
        return out;
    }
}
