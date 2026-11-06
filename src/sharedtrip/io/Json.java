package sharedtrip.io;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// ตัวอ่าน/เขียน JSON ขนาดเล็ก (ไม่ต้องใช้ไลบรารีภายนอก)
// object -> Map, array -> List, number -> Double, string -> String, true/false -> Boolean, null -> null
public final class Json {
    private final String s;
    private int i = 0;

    private Json(String s) { this.s = s; }

    public static Object parse(String text) {
        Json p = new Json(text);
        p.ws();
        Object v = p.value();
        p.ws();
        if (p.i != p.s.length()) throw p.err("มีข้อความเกินมา");
        return v;
    }

    private RuntimeException err(String msg) {
        return new IllegalArgumentException("JSON ไม่ถูกต้อง (" + msg + ") ที่ตำแหน่ง " + i);
    }

    private void ws() {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
    }

    private Object value() {
        if (i >= s.length()) throw err("จบไฟล์ก่อนกำหนด");
        char c = s.charAt(i);
        if (c == '{') return object();
        if (c == '[') return array();
        if (c == '"') return string();
        if (s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
        if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
        if (s.startsWith("null", i)) { i += 4; return null; }
        return number();
    }

    private Map<String, Object> object() {
        Map<String, Object> m = new LinkedHashMap<>();
        i++; ws();
        if (s.charAt(i) == '}') { i++; return m; }
        while (true) {
            ws();
            String k = string();
            ws();
            if (s.charAt(i++) != ':') throw err("ต้องมี :");
            ws();
            m.put(k, value());
            ws();
            char c = s.charAt(i++);
            if (c == '}') return m;
            if (c != ',') throw err("ต้องมี , หรือ }");
        }
    }

    private List<Object> array() {
        List<Object> a = new ArrayList<>();
        i++; ws();
        if (s.charAt(i) == ']') { i++; return a; }
        while (true) {
            ws();
            a.add(value());
            ws();
            char c = s.charAt(i++);
            if (c == ']') return a;
            if (c != ',') throw err("ต้องมี , หรือ ]");
        }
    }

    private String string() {
        if (s.charAt(i) != '"') throw err("ต้องเป็นข้อความ");
        StringBuilder b = new StringBuilder();
        i++;
        while (true) {
            char c = s.charAt(i++);
            if (c == '"') return b.toString();
            if (c == '\\') {
                char e = s.charAt(i++);
                switch (e) {
                    case 'n': b.append('\n'); break;
                    case 't': b.append('\t'); break;
                    case 'r': b.append('\r'); break;
                    case 'b': b.append('\b'); break;
                    case 'f': b.append('\f'); break;
                    case 'u': b.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; break;
                    default: b.append(e);
                }
            } else b.append(c);
        }
    }

    private Double number() {
        int st = i;
        while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
        if (st == i) throw err("ค่าที่อ่านไม่ได้");
        return Double.parseDouble(s.substring(st, i));
    }

    // ---------- เขียน ----------
    public static String stringify(Object v) {
        StringBuilder b = new StringBuilder();
        write(b, v, 0);
        return b.toString();
    }

    private static void indent(StringBuilder b, int n) {
        b.append('\n');
        for (int k = 0; k < n; k++) b.append("  ");
    }

    @SuppressWarnings("unchecked")
    private static void write(StringBuilder b, Object v, int lvl) {
        if (v == null) b.append("null");
        else if (v instanceof String) quote(b, (String) v);
        else if (v instanceof Number) {
            double d = ((Number) v).doubleValue();
            if (d == Math.rint(d) && Math.abs(d) < 1e15) b.append((long) d); else b.append(d);
        } else if (v instanceof Boolean) b.append(v);
        else if (v instanceof Map) {
            Map<String, Object> m = (Map<String, Object>) v;
            if (m.isEmpty()) { b.append("{}"); return; }
            b.append('{');
            boolean first = true;
            for (Map.Entry<String, Object> e : m.entrySet()) {
                if (!first) b.append(',');
                first = false;
                indent(b, lvl + 1);
                quote(b, e.getKey());
                b.append(": ");
                write(b, e.getValue(), lvl + 1);
            }
            indent(b, lvl);
            b.append('}');
        } else if (v instanceof List) {
            List<Object> a = (List<Object>) v;
            if (a.isEmpty()) { b.append("[]"); return; }
            b.append('[');
            for (int k = 0; k < a.size(); k++) {
                if (k > 0) b.append(',');
                indent(b, lvl + 1);
                write(b, a.get(k), lvl + 1);
            }
            indent(b, lvl);
            b.append(']');
        } else quote(b, v.toString());
    }

    private static void quote(StringBuilder b, String str) {
        b.append('"');
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"': b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n"); break;
                case '\r': b.append("\\r"); break;
                case '\t': b.append("\\t"); break;
                default:
                    if (c < 0x20) b.append(String.format("\\u%04x", (int) c)); else b.append(c);
            }
        }
        b.append('"');
    }
}
