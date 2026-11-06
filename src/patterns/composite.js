// Composite Pattern: Trip -> Day -> Activity
// ทุกชิ้นมี getCost() และ getDuration() เหมือนกัน ทำให้คำนวณรวมได้จากชั้นบนสุด

let nextId = 1
export const newId = () => `${Date.now().toString(36)}-${nextId++}`

const toMinutes = (hhmm) => {
  const [h, m] = hhmm.split(':').map(Number)
  return h * 60 + m
}

export class Activity {
  constructor({ id = newId(), name, start, end, cost = 0, place = '' }) {
    Object.assign(this, { id, name, start, end, cost: Number(cost), place })
  }
  getCost() { return this.cost }
  getDuration() { return Math.max(0, toMinutes(this.end) - toMinutes(this.start)) }
  overlaps(other) {
    return toMinutes(this.start) < toMinutes(other.end) && toMinutes(other.start) < toMinutes(this.end)
  }
  toJSON() { return { id: this.id, name: this.name, start: this.start, end: this.end, cost: this.cost, place: this.place } }
}

class Group {
  constructor() { this.children = [] }
  add(child) { this.children.push(child); return this }
  remove(id) { this.children = this.children.filter((c) => c.id !== id); return this }
  getCost() { return this.children.reduce((sum, c) => sum + c.getCost(), 0) }
  getDuration() { return this.children.reduce((sum, c) => sum + c.getDuration(), 0) }
}

export class Day extends Group {
  constructor({ id = newId(), date, activities = [] }) {
    super()
    this.id = id
    this.date = date
    activities.forEach((a) => this.add(a instanceof Activity ? a : new Activity(a)))
  }
  getConflicts() {
    const sorted = [...this.children].sort((a, b) => a.start.localeCompare(b.start))
    const pairs = []
    for (let i = 0; i < sorted.length; i++)
      for (let j = i + 1; j < sorted.length; j++)
        if (sorted[i].overlaps(sorted[j])) pairs.push([sorted[i], sorted[j]])
    return pairs
  }
  toJSON() { return { id: this.id, date: this.date, activities: this.children.map((a) => a.toJSON()) } }
}

export class Trip extends Group {
  constructor({ id = newId(), name, budget = 0, members = [], days = [], expenses = [] }) {
    super()
    Object.assign(this, { id, name, budget: Number(budget), members, expenses })
    days.forEach((d) => this.add(d instanceof Day ? d : new Day(d)))
  }
  get days() { return this.children }
  isOverBudget() { return this.budget > 0 && this.getTotalSpent() > this.budget }
  getTotalSpent() {
    return this.getCost() + this.expenses.reduce((s, e) => s + Number(e.amount), 0)
  }
  toJSON() {
    return {
      id: this.id, name: this.name, budget: this.budget, members: this.members,
      days: this.children.map((d) => d.toJSON()), expenses: this.expenses,
    }
  }
  static fromJSON(data) { return new Trip(data) }
}
