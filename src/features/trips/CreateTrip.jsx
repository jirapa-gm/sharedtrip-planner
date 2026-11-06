import { useState } from 'react'
import { TripBuilder } from '../../patterns/builder.js'
import { tripStore } from '../../patterns/observer.js'
import { Trip } from '../../patterns/composite.js'
import Icon from '../../components/Icon.jsx'

export default function CreateTrip() {
  const [form, setForm] = useState({ name: '', budget: '', start: '', end: '', members: '' })
  const [error, setError] = useState('')
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })
  const members = form.members.split(',').map((m) => m.trim()).filter(Boolean)

  const submit = (e) => {
    e.preventDefault()
    try {
      const builder = new TripBuilder().setName(form.name).setBudget(form.budget).setDateRange(form.start, form.end)
      members.forEach((m) => builder.addMember(m))
      tripStore.setTrip(builder.build())
    } catch (err) { setError(err.message) }
  }

  const openFile = (e) => {
    const file = e.target.files[0]
    if (!file) return
    file.text().then((txt) => tripStore.setTrip(Trip.fromJSON(JSON.parse(txt)))).catch(() => setError('เปิดไฟล์ไม่ได้ ต้องเป็นไฟล์ .json ที่บันทึกจากแอปนี้'))
  }

  return (
    <div className="create">
      <p className="brand"><Icon name="plane" size={22} />SharedTrip</p>
      <h1 className="create-title">ไปเที่ยวไหนกันดี <span className="title-cloud"><Icon name="cloud" size={48} /></span></h1>
      <p className="lede">วางแผนแต่ละวัน จดค่าใช้จ่าย แล้วให้ระบบคิดว่าใครต้องโอนให้ใคร</p>

      <form className="ticket ticket-form" onSubmit={submit}>
        <div className="ticket-main form">
          <label>ชื่อทริป<input value={form.name} onChange={set('name')} placeholder="เชียงใหม่ 3 วัน 2 คืน" autoFocus /></label>
          <div className="row">
            <label>วันไป<input type="date" value={form.start} onChange={set('start')} /></label>
            <label>วันกลับ<input type="date" value={form.end} min={form.start} onChange={set('end')} /></label>
          </div>
          <label>ใครไปบ้าง <span className="hint">คั่นชื่อด้วยจุลภาค</span>
            <input value={form.members} onChange={set('members')} placeholder="นาย, ปทุมพร, มิ้นท์" />
          </label>
          {members.length > 0 && <ul className="members">{members.map((m) => <li key={m}>{m}</li>)}</ul>}
        </div>
        <div className="ticket-stub form">
          <label>งบทั้งทริป<input type="number" min="0" value={form.budget} onChange={set('budget')} placeholder="฿ 0" /></label>
          {error && <p className="error" role="alert">{error}</p>}
          <button className="primary">สร้างทริป</button>
        </div>
      </form>

      <div className="open-file">
        <span className="muted">หรือเคยบันทึกทริปไว้แล้ว</span>
        <label className="file-btn">
          <Icon name="folder" />เปิดทริปที่บันทึกไว้
          <input type="file" accept=".json" onChange={openFile} />
        </label>
      </div>
    </div>
  )
}
