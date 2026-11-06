import { useState } from 'react'
import { useTrip } from './hooks/useTrip.js'
import { tripStore } from './patterns/observer.js'
import CreateTrip from './features/trips/CreateTrip.jsx'
import Icon from './components/Icon.jsx'
import TripToolbar from './features/trips/TripToolbar.jsx'
import Itinerary from './features/itinerary/Itinerary.jsx'
import Expenses from './features/expenses/Expenses.jsx'
import Settlement from './features/settlement/Settlement.jsx'

const TABS = [
  { id: 'plan', label: 'แผนการเดินทาง' },
  { id: 'expenses', label: 'ค่าใช้จ่าย' },
  { id: 'settle', label: 'สรุปยอดโอน' },
]

const shortDate = (iso) => new Date(iso).toLocaleDateString('th-TH', { day: 'numeric', month: 'short' })
const baht = (n) => `฿${Math.round(n).toLocaleString()}`

export default function App() {
  const trip = useTrip()
  const [tab, setTab] = useState('plan')

  if (!trip) return <CreateTrip />

  const spent = trip.getTotalSpent()
  const pct = trip.budget > 0 ? Math.min(100, (spent / trip.budget) * 100) : 0
  const over = trip.isOverBudget()
  const first = trip.days[0]?.date
  const last = trip.days[trip.days.length - 1]?.date
  const hours = Math.round((trip.getDuration() / 60) * 10) / 10

  return (
    <div className="app">
      <TripToolbar />

      <header className="ticket">
        <div className="ticket-main">
          <p className="ticket-route"><Icon name="cloud" />{shortDate(first)}{last !== first && ` – ${shortDate(last)}`} · {trip.days.length} วัน</p>
          <h1>{trip.name}</h1>
          <ul className="members">
            {trip.members.map((m) => <li key={m}>{m}</li>)}
          </ul>
        </div>
        <div className="ticket-stub">
          <span className="stub-label">ใช้ไปแล้ว</span>
          <strong className={over ? 'stub-amount over' : 'stub-amount'}>{baht(spent)}</strong>
          {trip.budget > 0 && (
            <>
              <div className="meter" role="meter" aria-valuenow={Math.round(pct)} aria-valuemin="0" aria-valuemax="100" aria-label="สัดส่วนงบที่ใช้">
                <div style={{ width: `${pct}%` }} className={over ? 'over' : ''} />
              </div>
              <span className="stub-note">
                {over ? `เกินงบ ${baht(spent - trip.budget)}` : `เหลือ ${baht(trip.budget - spent)} จาก ${baht(trip.budget)}`}
              </span>
            </>
          )}
          <span className="stub-note">กิจกรรมรวม {hours} ชม.</span>
        </div>
      </header>

      <nav className="tabs" role="tablist">
        {TABS.map((t) => (
          <button key={t.id} role="tab" aria-selected={tab === t.id} className={tab === t.id ? 'active' : ''} onClick={() => setTab(t.id)}>
            {t.label}
          </button>
        ))}
      </nav>

      <main>
        {tab === 'plan' && <Itinerary trip={trip} />}
        {tab === 'expenses' && <Expenses trip={trip} />}
        {tab === 'settle' && <Settlement trip={trip} />}
      </main>

      <footer className="footer">
        <button className="link" onClick={() => confirm('ลบทริปนี้แล้วเริ่มทริปใหม่?') && tripStore.reset()}>เริ่มทริปใหม่</button>
      </footer>
    </div>
  )
}
