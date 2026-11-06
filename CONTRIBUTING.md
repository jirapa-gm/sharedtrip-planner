# แนวทางการทำงานร่วมกัน

## สมาชิกและขอบเขตงาน

| สมาชิก | GitHub | รับผิดชอบ |
|---|---|---|
| จิราภา กองมูล | @jirapa-gm | หัวหน้าโครงการ, ตรวจและ merge งาน, โครงสร้างระบบ |
| ปทุมพร | @pathumpornjorrapong-ops | ระบบค่าใช้จ่าย, สรุปยอดโอน, UML |

### ไฟล์ที่แต่ละคนดูแล

**จิราภา**
- `src/patterns/composite.js` (Composite)
- `src/patterns/builder.js` (Builder)
- `src/patterns/memento.js` (Memento)
- `src/patterns/observer.js` (Observer)
- `src/App.jsx`, `src/styles.css`, `src/hooks/`
- `src/features/trips/`, `src/features/itinerary/`

**ปทุมพร**
- `src/patterns/splitStrategy.js` (Strategy)
- `src/features/expenses/`
- `src/features/settlement/`
- `docs/uml/`

ไฟล์ที่ใช้ร่วมกัน (`composite.js`, `observer.js`, `styles.css`) ถ้าต้องแก้ ให้แจ้งจิราภาก่อน

## ลำดับการทำงาน

1. จิราภาวางโครงสร้างระบบและ push ขึ้น `main`
2. ปทุมพร clone แล้วทำส่วนค่าใช้จ่ายใน branch ของตัวเอง
3. ปทุมพรเปิด Pull Request → จิราภาตรวจและ merge
4. ทั้งคู่ `git pull` แล้วทดสอบระบบรวมบน `main`
5. ทำ UML ให้ตรงกับโค้ดจริง แล้วเตรียมนำเสนอ

## กติกา Git

- ห้าม push เข้า `main` โดยตรง ทุกงานต้องผ่าน Pull Request
- ตั้งชื่อ branch: `feature/ชื่องาน` หรือ `fix/ชื่อบั๊ก`
- ข้อความ commit: `feat:` เพิ่มฟีเจอร์, `fix:` แก้บั๊ก, `docs:` เอกสาร, `style:` หน้าตา
- PR ต้องได้รับการ Approve 1 คนก่อน merge
- ก่อนเปิด PR ให้รัน `npm run dev` เช็กว่าแอปยังใช้งานได้

## ขั้นตอนเริ่มงาน

```bash
git clone https://github.com/jirapa-gm/sharedtrip-planner.git
cd sharedtrip-planner
npm install
git checkout -b feature/ชื่องาน
# ทำงาน แล้ว
git add .
git commit -m "feat: อธิบายสิ่งที่ทำ"
git push -u origin feature/ชื่องาน
```

จากนั้นเปิด Pull Request บน GitHub

## ก่อนเริ่มงานใหม่ทุกครั้ง

```bash
git checkout main
git pull
git checkout -b feature/งานใหม่
```
