# UML: SharedTrip Planner

แผนภาพทั้งหมดในโฟลเดอร์นี้ทำตามโค้ดจริงใน `src/`
- ไฟล์ `.mmd` คือต้นฉบับ (Mermaid) แก้แล้ว GitHub จะวาดใหม่ให้เอง
- ไฟล์ `.png` / `.svg` ใช้ใส่สไลด์นำเสนอ

## 1. Class Diagram

![Class diagram](class-diagram.png)

| Pattern | คลาส | บทบาท |
|---|---|---|
| Composite | `TripComponent`, `Group`, `Trip`, `Day`, `Activity` | `Trip` → `Day` → `Activity` ทุกชั้นมี `getCost()` / `getDuration()` เรียกจาก `Trip` ทีเดียวได้ยอดรวมทั้งทริป |
| Builder | `TripBuilder` | สร้าง `Trip` ทีละขั้น (`setName` → `setBudget` → `setDateRange` → `addMember`) แล้วตรวจข้อมูลใน `build()` |
| Memento | `TripMemento`, `History` | เก็บสำเนาทริปเป็น JSON ก่อนทุกการแก้ไข ใช้ทำ Undo / Redo (เก็บสูงสุด 50 ครั้ง) |
| Observer | `TripStore`, `useTrip` | `TripStore` เป็น Subject เมื่อข้อมูลเปลี่ยนจะ `notify()` ทุกหน้าจอที่ `subscribe` อยู่ |
| Strategy | `SplitStrategy`, `EqualSplit`, `PercentSplit`, `ExactSplit`, `ExpenseSplitter` | วิธีหารเงินสลับได้ตอนรัน `ExpenseSplitter` (Context) เรียก `split()` โดยไม่ต้องรู้ว่าเป็นแบบไหน |

หมายเหตุ: JavaScript ไม่มี `interface` จริง `TripComponent` จึงเป็น interface เชิงแนวคิด ทั้ง `Activity` และ `Group` มีเมธอดชุดเดียวกัน (duck typing)

```mermaid
classDiagram
  direction TB

  namespace Composite {
    class TripComponent {
      <<interface>>
      +getCost() number
      +getDuration() number
    }
    class Activity {
      +id : string
      +name : string
      +start : string
      +end : string
      +cost : number
      +place : string
      +getCost() number
      +getDuration() number
      +overlaps(other: Activity) boolean
      +toJSON() object
    }
    class Group {
      <<abstract>>
      #children : TripComponent[]
      +add(child) Group
      +remove(id) Group
      +getCost() number
      +getDuration() number
    }
    class Day {
      +id : string
      +date : string
      +getConflicts() Activity[][]
      +toJSON() object
    }
    class Trip {
      +id : string
      +name : string
      +budget : number
      +members : string[]
      +expenses : Expense[]
      +days : Day[]
      +isOverBudget() boolean
      +getTotalSpent() number
      +toJSON() object
      +fromJSON(data)$ Trip
    }
    class Expense {
      <<data>>
      +id : string
      +title : string
      +amount : number
      +paidBy : string
      +participants : string[]
      +split : type, values
    }
  }

  namespace Builder {
    class TripBuilder {
      -data : object
      +setName(name) TripBuilder
      +setBudget(budget) TripBuilder
      +addMember(name) TripBuilder
      +setDateRange(start, end) TripBuilder
      +build() Trip
    }
  }

  namespace Memento {
    class TripMemento {
      -state : string
      +getState() object
    }
    class History {
      -undoStack : TripMemento[]
      -redoStack : TripMemento[]
      -limit : number
      +save(memento)
      +undo(current) TripMemento
      +redo(current) TripMemento
      +canUndo() boolean
      +canRedo() boolean
    }
  }

  namespace Observer {
    class TripStore {
      -observers : Set~Function~
      -version : number
      +trip : Trip
      +history : History
      +subscribe(fn) Function
      +getSnapshot() number
      +notify()
      +update(mutator)
      +setTrip(trip)
      +undo()
      +redo()
      +reset()
    }
    class useTrip {
      <<React hook>>
      +useTrip() Trip
    }
  }

  namespace Strategy {
    class SplitStrategy {
      <<abstract>>
      +type : string
      +label : string
      +validate(amount, participants, values) string
      +split(amount, participants, values) Map
    }
    class EqualSplit {
      +split(amount, participants) Map
    }
    class PercentSplit {
      +validate(amount, participants, values) string
      +split(amount, participants, values) Map
    }
    class ExactSplit {
      +validate(amount, participants, values) string
      +split(amount, participants, values) Map
    }
    class ExpenseSplitter {
      -strategy : SplitStrategy
      +setStrategy(strategy) ExpenseSplitter
      +validate(amount, participants, values) string
      +split(amount, participants, values) Map
      +sharesOf(expense)$ Map
    }
  }

  namespace Settlement {
    class Settle {
      <<module>>
      +getBalances(trip) Balance[]
      +getTransfers(balances) Transfer[]
    }
  }

  TripComponent <|.. Activity
  TripComponent <|.. Group
  Group <|-- Day
  Group <|-- Trip
  Group o-- TripComponent : children
  Trip *-- "1..*" Day
  Day *-- "0..*" Activity
  Trip *-- "0..*" Expense

  TripBuilder ..> Trip : «create»
  TripBuilder ..> Day : «create»

  History o-- "0..50" TripMemento
  TripStore *-- History
  TripStore --> "0..1" Trip
  TripStore ..> TripMemento : «create»
  useTrip ..> TripStore : subscribe

  SplitStrategy <|-- EqualSplit
  SplitStrategy <|-- PercentSplit
  SplitStrategy <|-- ExactSplit
  ExpenseSplitter o-- SplitStrategy : strategy
  ExpenseSplitter ..> Expense : uses
  Settle ..> ExpenseSplitter : sharesOf
  Settle ..> Trip : reads
```

## 2. Sequence Diagram: เพิ่มค่าใช้จ่ายแบบหารตามเปอร์เซ็นต์

แสดง Strategy + Memento + Observer ทำงานร่วมกัน

![Sequence add expense](sequence-add-expense.png)

```mermaid
sequenceDiagram
  autonumber
  actor U as ผู้ใช้
  participant F as ExpenseForm
  participant S as ExpenseSplitter
  participant P as PercentSplit
  participant St as TripStore
  participant H as History
  participant UI as หน้าจอที่ subscribe

  U->>F: กรอกรายการ เลือก "ตามเปอร์เซ็นต์"
  F->>S: new ExpenseSplitter(getStrategy("percent"))
  S-->>F: splitter (ถือ PercentSplit)
  F->>S: validate(amount, participants, values)
  S->>P: validate(...)
  P-->>S: null (ผ่าน)
  F->>S: split(amount, participants, values)
  S->>P: split(...)
  P-->>S: { คน: ยอด }
  S-->>F: แสดงยอดของแต่ละคนให้ดูก่อน
  U->>F: กด "บันทึกค่าใช้จ่าย"
  F->>St: update(trip => trip.expenses.push(expense))
  St->>H: save(new TripMemento(trip))
  St->>St: สร้าง draft แล้วเรียก mutator
  St->>St: notify()
  St-->>UI: เรียก observer ทุกตัว
  UI->>UI: render ใหม่ (สรุปยอด / ยอดโอนอัปเดต)
```

## 3. Sequence Diagram: กด Undo

![Sequence undo](sequence-undo.png)

```mermaid
sequenceDiagram
  autonumber
  actor U as ผู้ใช้
  participant T as TripToolbar
  participant St as TripStore
  participant H as History
  participant M as TripMemento
  participant UI as หน้าจอที่ subscribe

  U->>T: กด Undo
  T->>St: undo()
  St->>M: new TripMemento(trip ปัจจุบัน)
  St->>H: undo(current)
  H->>H: เก็บ current ลง redoStack
  H-->>St: memento ก่อนหน้า (pop จาก undoStack)
  St->>M: getState()
  M-->>St: ข้อมูลทริปเดิม
  St->>St: trip = Trip.fromJSON(state)
  St->>St: notify()
  St-->>UI: เรียก observer ทุกตัว
  UI->>UI: render ทริปสถานะก่อนหน้า
```
