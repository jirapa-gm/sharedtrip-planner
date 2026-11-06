// Strategy Pattern: วิธีหารค่าใช้จ่ายที่สลับกันได้
// ทุก Strategy มี interface เดียวกันคือ split(amount, participants, values) -> { ชื่อ: ยอดที่ต้องจ่าย }
// ExpenseSplitter (Context) ไม่ต้องรู้ว่าหารแบบไหน แค่เรียก strategy ที่ถูกตั้งไว้
//
// คำนวณเป็น "สตางค์" (จำนวนเต็ม) เพื่อไม่ให้เกิดเศษทศนิยมแบบ 33.333333
// เศษที่เหลือจากการปัดจะกระจายให้คนแรก ๆ ทีละ 1 สตางค์ ยอดรวมจึงเท่ากับยอดจริงเสมอ

const toSatang = (baht) => Math.round(Number(baht) * 100)
const toBaht = (satang) => satang / 100

// แจกยอดตามสัดส่วน weights แล้วกระจายเศษให้ยอดรวมตรงพอดี
function distribute(totalSatang, people, weights) {
  const sumW = weights.reduce((s, w) => s + w, 0)
  const raw = weights.map((w) => (totalSatang * w) / sumW)
  const floored = raw.map(Math.floor)
  let remainder = totalSatang - floored.reduce((s, v) => s + v, 0)
  // ให้เศษกับคนที่มีส่วนทศนิยมมากที่สุดก่อน
  const order = raw.map((v, i) => [v - Math.floor(v), i]).sort((a, b) => b[0] - a[0])
  for (const [, i] of order) { if (remainder <= 0) break; floored[i]++; remainder-- }
  return Object.fromEntries(people.map((p, i) => [p, toBaht(floored[i])]))
}

export class SplitStrategy {
  get type() { throw new Error('ต้องกำหนด type') }
  get label() { throw new Error('ต้องกำหนด label') }
  // คืนข้อความ error ถ้าข้อมูลไม่ถูกต้อง, คืน null ถ้าผ่าน
  validate(amount, participants) {
    if (!(Number(amount) > 0)) return 'จำนวนเงินต้องมากกว่า 0'
    if (participants.length === 0) return 'เลือกคนที่ร่วมจ่ายอย่างน้อย 1 คน'
    return null
  }
  split() { throw new Error('ต้อง implement split()') }
}

export class EqualSplit extends SplitStrategy {
  get type() { return 'equal' }
  get label() { return 'หารเท่ากัน' }
  split(amount, participants) {
    return distribute(toSatang(amount), participants, participants.map(() => 1))
  }
}

export class PercentSplit extends SplitStrategy {
  get type() { return 'percent' }
  get label() { return 'ตามเปอร์เซ็นต์' }
  validate(amount, participants, values = {}) {
    const base = super.validate(amount, participants)
    if (base) return base
    if (participants.some((p) => Number(values[p] ?? 0) < 0)) return 'เปอร์เซ็นต์ต้องไม่ติดลบ'
    const total = participants.reduce((s, p) => s + Number(values[p] ?? 0), 0)
    if (Math.abs(total - 100) > 0.01) return `เปอร์เซ็นต์รวมต้องเท่ากับ 100 (ตอนนี้ ${Math.round(total * 100) / 100})`
    return null
  }
  split(amount, participants, values = {}) {
    return distribute(toSatang(amount), participants, participants.map((p) => Number(values[p] ?? 0)))
  }
}

export class ExactSplit extends SplitStrategy {
  get type() { return 'exact' }
  get label() { return 'ระบุเอง' }
  validate(amount, participants, values = {}) {
    const base = super.validate(amount, participants)
    if (base) return base
    if (participants.some((p) => Number(values[p] ?? 0) < 0)) return 'จำนวนเงินต้องไม่ติดลบ'
    const total = participants.reduce((s, p) => s + toSatang(values[p] ?? 0), 0)
    if (total !== toSatang(amount)) return `ยอดที่ระบุรวม ฿${toBaht(total).toLocaleString()} ไม่เท่ากับ ฿${Number(amount).toLocaleString()}`
    return null
  }
  split(amount, participants, values = {}) {
    return Object.fromEntries(participants.map((p) => [p, toBaht(toSatang(values[p] ?? 0))]))
  }
}

// รายการ strategy ทั้งหมด ใช้ทำ dropdown และใช้หา strategy จาก type ที่เก็บใน JSON
export const STRATEGIES = [new EqualSplit(), new PercentSplit(), new ExactSplit()]
export const getStrategy = (type) => STRATEGIES.find((s) => s.type === type) ?? STRATEGIES[0]

// Context: ถือ strategy ไว้หนึ่งตัว เปลี่ยนได้ตอนรันด้วย setStrategy()
export class ExpenseSplitter {
  constructor(strategy = new EqualSplit()) { this.strategy = strategy }
  setStrategy(strategy) { this.strategy = strategy; return this }
  validate(amount, participants, values) { return this.strategy.validate(amount, participants, values) }
  split(amount, participants, values) { return this.strategy.split(amount, participants, values) }

  // ส่วนแบ่งของ expense ที่บันทึกไว้แล้ว (ใน JSON เก็บแค่ split.type เพราะ object strategy เก็บเป็น JSON ไม่ได้)
  static sharesOf(expense) {
    const { type = 'equal', values = {} } = expense.split ?? {}
    return new ExpenseSplitter(getStrategy(type)).split(expense.amount, expense.participants, values)
  }
}
