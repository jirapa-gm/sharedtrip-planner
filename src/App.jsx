import { useState } from 'react'
import { useTrip } from './hooks/useTrip.js'
import { tripStore } from './patterns/observer.js'
import CreateTrip from './features/trips/CreateTrip.jsx'
import TripToolbar from './features/trips/TripToolbar.jsx'
import Itinerary from './features/itinerary/Itinerary.jsx'
import Expenses from './features/expenses/Expenses.jsx'
import Settlement from './features/settlement/Settlement.jsx'

const TABS = [
  { id: 'plan', label: 'แผนการเดินทาง' },
  { id: 'expenses', label: 'ค่าใช้จ่าย' },
  { id: 'settle', label: 'สรุปยอดโอน' },
]

export default function App() {
  const trip = useTrip()
  const [tab, setTab] = useState('plan')

  if (!trip) return <CreateTrip />

  const spent = trip.getTotalSpent()
  return (
    <div className="app">
      <header className="header">
        <div>
          <h1>{trip.name}</h1>
          <p className="muted">{trip.members.join(' · ')} · {trip.days.length} วัน</p>
        </div>
        <TripToolbar />
      </header>

      <section className="summary">
        <div className="stat"><span>ใช้ไปแล้ว</span><strong>฿{spent.toLocaleString()}</strong></div>
        <div className="stat"><span>งบประมาณ</span><strong>฿{trip.budget.toLocaleString()}</strong></div>
        <div className="stat"><span>เวลากิจกรรมรวม</span><strong>{Math.round(trip.getDuration() / 60 * 10) / 10} ชม.</strong></div>
      </section>
      {trip.isOverBudget() && (
        <div className="alert">เกินงบประมาณ ฿{(spent - trip.budget).toLocaleString()}</div>
      )}

      <nav className="tabs">
        {TABS.map((t) => (
          <button key={t.id} className={tab === t.id ? 'active' : ''} onClick={() => setTab(t.id)}>{t.label}</button>
        ))}
      </nav>

      {tab === 'plan' && <Itinerary trip={trip} />}
      {tab === 'expenses' && <Expenses trip={trip} />}
      {tab === 'settle' && <Settlement trip={trip} />}

      <footer className="footer">
        <button className="link" onClick={() => confirm('ลบทริปนี้และเริ่มใหม่?') && tripStore.reset()}>เริ่มทริปใหม่</button>
      </footer>
    </div>
  )
}
