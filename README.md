# SharedTrip Planner

ระบบวางแผนการเดินทางและจัดการค่าใช้จ่ายร่วม (Mini Project OOSE)

## ความสามารถ
- สร้างทริป กำหนดวัน งบประมาณ และสมาชิก
- เพิ่มกิจกรรมรายวัน คำนวณเวลาและค่าใช้จ่ายรวม
- แจ้งเตือนเมื่อเวลากิจกรรมชนกัน หรือใช้เงินเกินงบ
- บันทึกค่าใช้จ่าย หารเท่ากัน / ตามเปอร์เซ็นต์ / ระบุเอง
- สรุปว่าใครต้องโอนให้ใครเท่าไร
- Undo / Redo และบันทึก/เปิดทริปเป็นไฟล์ JSON

## Design Patterns
| Pattern | ไฟล์ | ใช้ทำอะไร |
|---|---|---|
| Composite | `src/patterns/composite.js` | Trip → Day → Activity คำนวณเวลาและเงินรวมแบบลำดับชั้น |
| Builder | `src/patterns/builder.js` | สร้างทริปทีละขั้นพร้อมตรวจข้อมูล |
| Memento | `src/patterns/memento.js` | เก็บสถานะสำหรับ Undo / Redo |
| Strategy | `src/patterns/splitStrategy.js` | วิธีหารค่าใช้จ่ายที่สลับได้ |
| Observer | `src/patterns/observer.js` | แจ้งทุกหน้าจอเมื่อข้อมูลทริปเปลี่ยน |

## วิธีรัน
```bash
npm install
npm run dev
```

## UML
ดูในโฟลเดอร์ `docs/uml/`

## ผู้จัดทำ
- จิราภา กองมูล: โครงสร้างระบบ, Composite, Builder, Memento, Observer, หน้าวางแผนทริป
- ปทุมพร: Strategy, หน้าค่าใช้จ่าย, สรุปยอดโอน, UML
