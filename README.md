# SharedTrip Planner

ระบบวางแผนการเดินทางและจัดการค่าใช้จ่ายร่วม เขียนด้วย Java (Swing) มินิโปรเจกต์วิชา Object Oriented Software Engineering

## ความสามารถ

- สร้างทริป กำหนดงบ ช่วงวันเดินทาง และรายชื่อเพื่อนร่วมทริป
- เพิ่มกิจกรรมรายวัน ระบบรวมเวลาและค่าใช้จ่ายให้ เตือนเมื่อเวลาชนกันหรืองบเกิน
- จดค่าใช้จ่าย หารได้ 3 แบบ: หารเท่ากัน, ตามเปอร์เซ็นต์, ระบุเอง
- สรุปว่าใครต้องโอนให้ใครเท่าไร
- ย้อนกลับ / ทำซ้ำ (Undo / Redo)
- บันทึกและเปิดทริปเป็นไฟล์ JSON

## วิธีรัน

ต้องมี JDK 17 ขึ้นไป (เช็กด้วย `java -version`)

**Windows:** ดับเบิลคลิก `run.bat`

**คำสั่งเอง (ทุกระบบ):**

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out sharedtrip.Main
```

บน Windows PowerShell:

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out sharedtrip.Main
```

**VS Code:** ติดตั้ง Extension Pack for Java แล้วเปิด `src/sharedtrip/Main.java` กด Run

## Design Pattern

| Pattern | ไฟล์ | หน้าที่ |
|---|---|---|
| Composite | `composite/TripComponent.java`, `Group.java`, `Trip.java`, `Day.java`, `Activity.java` | Trip → Day → Activity ทุกชั้นมี `getCost()` / `getDuration()` เรียกจาก Trip ครั้งเดียวได้ยอดรวม |
| Builder | `builder/TripBuilder.java` | สร้าง Trip ทีละขั้น แล้วตรวจข้อมูลใน `build()` |
| Memento | `memento/TripMemento.java`, `History.java` | เก็บสถานะทริปก่อนทุกการแก้ไข ใช้ทำ Undo / Redo (สูงสุด 50 ครั้ง) |
| Observer | `observer/TripObserver.java`, `TripStore.java` | TripStore เป็น Subject เมื่อข้อมูลเปลี่ยนจะแจ้งทุกหน้าจอที่ subscribe |
| Strategy | `strategy/SplitStrategy.java`, `EqualSplit.java`, `PercentSplit.java`, `ExactSplit.java`, `ExpenseSplitter.java` | วิธีหารเงินสลับได้ตอนรัน ExpenseSplitter (Context) ไม่ต้องรู้ว่าเป็นแบบไหน |

## โครงสร้างโฟลเดอร์

```
src/sharedtrip/
  Main.java            จุดเริ่มโปรแกรม
  composite/           Trip, Day, Activity, Expense
  builder/             TripBuilder
  memento/             TripMemento, History
  observer/            TripObserver, TripStore
  strategy/            วิธีหารค่าใช้จ่าย
  settlement/          คำนวณยอดคงเหลือและยอดโอน
  io/                  อ่าน/เขียนไฟล์ JSON
  ui/                  หน้าจอ Swing
```
