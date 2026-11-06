package sharedtrip.strategy;

import java.util.List;
import java.util.Map;

// Strategy Pattern: interface ของวิธีหารค่าใช้จ่าย
// split() คืน { ชื่อ -> ยอดที่ต้องจ่าย } validate() คืนข้อความผิดพลาด หรือ null ถ้าผ่าน
public interface SplitStrategy {
    String getType();

    String getLabel();

    String validate(double amount, List<String> participants, Map<String, Double> values);

    Map<String, Double> split(double amount, List<String> participants, Map<String, Double> values);
}
