// Builder Pattern: สร้างทริปทีละขั้น อ่านง่ายกว่าส่ง object ก้อนใหญ่
import { Trip, Day } from './composite.js'

export class TripBuilder {
  constructor() { this.data = { name: '', budget: 0, members: [], days: [] } }
  setName(name) { this.data.name = name.trim(); return this }
  setBudget(budget) { this.data.budget = Number(budget) || 0; return this }
  addMember(name) { if (name.trim()) this.data.members.push(name.trim()); return this }
  setDateRange(startDate, endDate) {
    const start = new Date(startDate)
    const end = new Date(endDate || startDate)
    for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1))
      this.data.days.push(new Day({ date: d.toISOString().slice(0, 10) }))
    return this
  }
  build() {
    if (!this.data.name) throw new Error('กรุณาตั้งชื่อทริป')
    if (this.data.members.length === 0) throw new Error('ต้องมีสมาชิกอย่างน้อย 1 คน')
    if (this.data.days.length === 0) throw new Error('กรุณาเลือกวันเดินทาง')
    return new Trip(this.data)
  }
}
