import { useState } from 'react'
import { Activity } from '../../patterns/composite.js'
import { tripStore } from '../../patterns/observer.js'
import Icon from '../../components/Icon.jsx'

const thaiDate = (iso) => new Date(iso).toLocaleDateString('th-TH', { weekday: 'long', day: 'numeric', month: 'long' })
const hrs = (min) => {
  const h = Math.floor(min / 60), m = min % 60
  return h && m ? `${h} ชม. ${m} นาที` : h ? `${h} ชม.` : `${m} นาที`
}

function ActivityForm({ dayId, onDone }) {
  const empty = { name: '', start: '09:00', end: '10:00', cost: '', place: '' }
  const [f, setF] = useState(empty)
  const [error, setError] = useState('')
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value })
  const add = (e) => {
    e.preventDefault()
    if (!f.name.trim()) return setError('ใส่ชื่อกิจกรรมก่อน')
    if (f.end <= f.start) return setError('เวลาจบต้องหลังเวลาเริ่ม')
    tripStore.update((trip) => { trip.days.find((d) => d.id === dayId).add(new Activity(f)) })
    setF({ ...empty, start: f.end, end: f.end })
    setError('')
  }
  return (
    <form className="activity-form" onSubmit={add}>
      <label className="wide">ทำอะไร<input value={f.name} onChange={set('name')} placeholder="กินข้าวซอย" autoFocus /></label>
      <label className="wide">ที่ไหน<input value={f.place} onChange={set('place')} placeholder="ไม่ใส่ก็ได้" /></label>
      <label>เริ่ม<input type="time" value={f.start} onChange={set('start')} /></label>
      <label>จบ<input type="time" value={f.end} onChange={set('end')} /></label>
      <label>ค่าใช้จ่าย<input type="number" min="0" value={f.cost} onChange={set('cost')} placeholder="฿ 0" /></label>
      {error && <p className="error full" role="alert">{error}</p>}
      <div className="form-actions full">
        <button type="button" className="link" onClick={onDone}>ปิด</button>
        <button className="primary">เพิ่มกิจกรรม</button>
      </div>
    </form>
  )
}

function DayCard({ day, index }) {
  const [adding, setAdding] = useState(false)
  const conflicts = day.getConflicts()
  const clashIds = new Set(conflicts.flat().map((a) => a.id))
  const acts = [...day.children].sort((a, b) => a.start.localeCompare(b.start))
  const remove = (id) => tripStore.update((t) => { t.days.find((d) => d.id === day.id).remove(id) })

  return (
    <section className="day">
      <header className="day-head">
        <span className="day-no">วันที่ {index + 1}</span>
        <h2>{thaiDate(day.date)}</h2>
        <span className="day-sum">{acts.length > 0 && `฿${day.getCost().toLocaleString()} · ${hrs(day.getDuration())}`}</span>
      </header>

      {conflicts.length > 0 && (
        <p className="alert" role="alert"><Icon name="alert" />เวลาทับกัน: {conflicts.map(([a, b]) => `${a.name} กับ ${b.name}`).join(', ')} ลองเลื่อนเวลาอันใดอันหนึ่ง</p>
      )}

      {acts.length > 0 ? (
        <ol className="route">
          {acts.map((a) => (
            <li key={a.id} className={clashIds.has(a.id) ? 'stop clash' : 'stop'}>
              <time>{a.start}<span>{a.end}</span></time>
              <div className="stop-body">
                <strong>{a.name}</strong>
                {a.place && <span className="muted">{a.place}</span>}
              </div>
              <span className="stop-cost">{a.cost > 0 ? `฿${a.cost.toLocaleString()}` : 'ฟรี'}</span>
              <button className="icon-btn" onClick={() => remove(a.id)} aria-label={`ลบ ${a.name}`}><Icon name="close" size={16} /></button>
            </li>
          ))}
        </ol>
      ) : (
        !adding && <p className="empty">ยังว่างอยู่ เพิ่มที่แรกของวันนี้ได้เลย</p>
      )}

      {adding ? <ActivityForm dayId={day.id} onDone={() => setAdding(false)} /> : (
        <button className="add-btn" onClick={() => setAdding(true)}><Icon name="plus" />เพิ่มกิจกรรม</button>
      )}
    </section>
  )
}

export default function Itinerary({ trip }) {
  return <div className="stack">{trip.days.map((day, i) => <DayCard key={day.id} day={day} index={i} />)}</div>
}
