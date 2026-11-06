import { useState } from 'react'
import { TripBuilder } from '../../patterns/builder.js'
import { tripStore } from '../../patterns/observer.js'
import { Trip } from '../../patterns/composite.js'

export default function CreateTrip() {
  const [form, setForm] = useState({ name: '', budget: '', start: '', end: '', members: '' })
  const [error, setError] = useState('')
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const submit = (e) => {
    e.preventDefault()
    try {
      const builder = new TripBuilder().setName(form.name).setBudget(form.budget).setDateRange(form.start, form.end)
      form.members.split(',').forEach((m) => builder.addMember(m))
      tripStore.setTrip(builder.build())
    } catch (err) { setError(err.message) }
  }

  const openFile = (e) => {
    const file = e.target.files[0]
    if (!file) return
    file.text().then((txt) => tripStore.setTrip(Trip.fromJSON(JSON.parse(txt)))).catch(() => setError('ไฟล์ไม่ถูกต้อง'))
  }

  return (
    <div className="create">
      <h1>SharedTrip Planner</h1>
      <p className="muted">วางแผนทริปและหารค่าใช้จ่ายกับเพื่อน</p>
      <form className="card form" onSubmit={submit}>
        <label>ชื่อทริป<input value={form.name} onChange={set('name')} placeholder="เช่น เชียงใหม่ 3 วัน 2 คืน" /></label>
        <div className="row">
          <label>วันเริ่ม<input type="date" value={form.start} onChange={set('start')} /></label>
          <label>วันสิ้นสุด<input type="date" value={form.end} onChange={set('end')} /></label>
        </div>
        <label>งบประมาณ (บาท)<input type="number" min="0" value={form.budget} onChange={set('budget')} /></label>
        <label>สมาชิก (คั่นด้วยจุลภาค)<input value={form.members} onChange={set('members')} placeholder="นาย, ปทุมพร, มิ้นท์" /></label>
        {error && <p className="error">{error}</p>}
        <button className="primary">สร้างทริป</button>
        <label className="file">หรือเปิดไฟล์ทริป (.json)<input type="file" accept=".json" onChange={openFile} /></label>
      </form>
    </div>
  )
}
