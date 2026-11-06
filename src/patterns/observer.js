// Observer Pattern: TripStore เป็น Subject เมื่อข้อมูลเปลี่ยน ทุกหน้าจอที่ subscribe อยู่จะอัปเดตเอง
import { Trip } from './composite.js'
import { History, TripMemento } from './memento.js'

const STORAGE_KEY = 'sharedtrip:trip'

class TripStore {
  constructor() {
    this.observers = new Set()
    this.history = new History()
    this.trip = this.load()
    this.version = 0
  }
  subscribe = (fn) => { this.observers.add(fn); return () => this.observers.delete(fn) }
  getSnapshot = () => this.version
  notify() {
    this.version++
    try { localStorage.setItem(STORAGE_KEY, JSON.stringify(this.trip)) } catch {}
    this.observers.forEach((fn) => fn())
  }
  load() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY)
      return raw ? Trip.fromJSON(JSON.parse(raw)) : null
    } catch { return null }
  }
  // ทุกการแก้ไขผ่าน update() เพื่อบันทึก memento ก่อนเปลี่ยน
  update(mutator) {
    if (this.trip) this.history.save(new TripMemento(this.trip))
    const draft = this.trip ? Trip.fromJSON(JSON.parse(JSON.stringify(this.trip))) : null
    this.trip = mutator(draft) ?? draft
    this.notify()
  }
  setTrip(trip) { this.update(() => trip) }
  undo() {
    const m = this.history.undo(new TripMemento(this.trip))
    if (m) { this.trip = Trip.fromJSON(m.getState()); this.notify() }
  }
  redo() {
    const m = this.history.redo(new TripMemento(this.trip))
    if (m) { this.trip = Trip.fromJSON(m.getState()); this.notify() }
  }
  reset() { this.history = new History(); this.trip = null; this.notify() }
}

export const tripStore = new TripStore()
