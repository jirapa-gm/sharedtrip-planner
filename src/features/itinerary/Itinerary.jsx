import { useState } from 'react'
import { Activity } from '../../patterns/composite.js'
import { tripStore } from '../../patterns/observer.js'

const thaiDate = (iso) => new Date(iso).toLocaleDateString('th-TH', { weekday: 'short', day: 'numeric', month: 'short' })

function ActivityForm({ dayId }) {
  const empty = { name: '', start: '09:00', end: '10:00', cost: '', place: '' }
  const [f, setF] = useState(empty)
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value })
  const add = (e) => {
    e.preventDefault()
    if (!f.name.trim() || f.end <= f.start) return alert('กรอกชื่อ และเวลาสิ้นสุดต้องหลังเวลาเริ่ม')
    tripStore.update((trip) => { trip.days.find((d) => d.id === dayId).add(new Activity(f)) })
    setF(empty)
  }
  return (
    <form className="activity-form" onSubmit={add}>
      <input placeholder="กิจกรรม" value={f.name} onChange={set('name')} />
      <input placeholder="สถานที่" value={f.place} onChange={set('place')} />
      <input type="time" value={f.start} onChange={set('start')} />
      <input type="time" value={f.end} onChange={set('end')} />
      <input type="number" min="0" placeholder="฿" value={f.cost} onChange={set('cost')} />
      <button className="primary">เพิ่ม</button>
    </form>
  )
}

export default function Itinerary({ trip }) {
  const remove = (dayId, actId) => tripStore.update((t) => { t.days.find((d) => d.id === dayId).remove(actId) })

  return (
    <div className="stack">
      {trip.days.map((day, i) => {
        const conflicts = day.getConflicts()
        const clashIds = new Set(conflicts.flat().map((a) => a.id))
        const acts = [...day.children].sort((a, b) => a.start.localeCompare(b.start))
        return (
          <section key={day.id} className="card">
            <div className="day-head">
              <h2>วันที่ {i + 1} <span className="muted">{thaiDate(day.date)}</span></h2>
              <span className="muted">฿{day.getCost().toLocaleString()} · {Math.round(day.getDuration() / 6) / 10} ชม.</span>
            </div>
            {conflicts.length > 0 && (
              <div className="alert">เวลาชนกัน: {conflicts.map(([a, b]) => `${a.name} กับ ${b.name}`).join(', ')}</div>
            )}
            <ul className="list">
              {acts.map((a) => (
                <li key={a.id} className={clashIds.has(a.id) ? 'clash' : ''}>
                  <span className="time">{a.start}–{a.end}</span>
                  <span className="grow">{a.name}{a.place && <span className="muted"> · {a.place}</span>}</span>
                  <span>฿{a.cost.toLocaleString()}</span>
                  <button className="link" onClick={() => remove(day.id, a.id)}>ลบ</button>
                </li>
              ))}
              {acts.length === 0 && <li className="muted">ยังไม่มีกิจกรรม</li>}
            </ul>
            <ActivityForm dayId={day.id} />
          </section>
        )
      })}
    </div>
  )
}
