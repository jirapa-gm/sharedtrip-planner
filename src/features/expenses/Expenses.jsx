import { useState } from 'react'
import { newId } from '../../patterns/composite.js'
import { tripStore } from '../../patterns/observer.js'
import { STRATEGIES, getStrategy, ExpenseSplitter } from '../../patterns/splitStrategy.js'
import Avatar from './Avatar.jsx'
import './expenses.css'

const baht = (n) => `฿${Number(n).toLocaleString(undefined, { maximumFractionDigits: 2 })}`

function ExpenseForm({ members }) {
  const empty = { title: '', amount: '', paidBy: members[0], participants: [...members], type: 'equal', values: {} }
  const [f, setF] = useState(empty)
  const [error, setError] = useState('')
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value })

  const toggle = (m) => setF({
    ...f,
    participants: f.participants.includes(m) ? f.participants.filter((p) => p !== m) : [...f.participants, m],
  })
  const setValue = (m) => (e) => setF({ ...f, values: { ...f.values, [m]: e.target.value } })

  // ใช้ Context ตัวเดียว แล้วสลับ strategy ตามที่ผู้ใช้เลือก
  const splitter = new ExpenseSplitter(getStrategy(f.type))
  const participants = members.filter((m) => f.participants.includes(m)) // คงลำดับตามรายชื่อสมาชิก
  const values = Object.fromEntries(participants.map((p) => [p, Number(f.values[p] || 0)]))
  const problem = splitter.validate(f.amount, participants, values)
  const preview = problem ? null : splitter.split(f.amount, participants, values)

  const submit = (e) => {
    e.preventDefault()
    if (!f.title.trim()) return setError('กรอกชื่อรายการ เช่น ค่าที่พัก')
    if (problem) return setError(problem)
    const expense = {
      id: newId(), title: f.title.trim(), amount: Number(f.amount), paidBy: f.paidBy,
      participants, split: { type: f.type, values: f.type === 'equal' ? {} : values },
    }
    tripStore.update((trip) => { trip.expenses.push(expense) })
    setF(empty)
    setError('')
  }

  const needsValue = f.type !== 'equal'
  return (
    <form className="sx-panel sx-pink sx-form" onSubmit={submit}>
      <h2 className="sx-title">จดค่าใช้จ่าย</h2>
      <div className="sx-row">
        <label className="sx-field wide">จ่ายค่าอะไร
          <input className="sx-input" value={f.title} onChange={set('title')} placeholder="เช่น ค่าที่พัก" />
        </label>
        <label className="sx-field">เท่าไร (บาท)
          <input className="sx-input" type="number" min="0" step="0.01" value={f.amount} onChange={set('amount')} placeholder="฿ 0" />
        </label>
        <label className="sx-field">ใครออกเงิน
          <select className="sx-input" value={f.paidBy} onChange={set('paidBy')}>
            {members.map((m) => <option key={m}>{m}</option>)}
          </select>
        </label>
      </div>

      <div>
        <span className="sx-label">หารแบบไหน</span>
        <div className="sx-seg" role="radiogroup">
          {STRATEGIES.map((s) => (
            <button type="button" key={s.type} role="radio" aria-checked={f.type === s.type}
              className={f.type === s.type ? 'on' : ''} onClick={() => setF({ ...f, type: s.type })}>
              {s.label}
            </button>
          ))}
        </div>
      </div>

      <div>
        <span className="sx-label">
          หารกับใครบ้าง {needsValue && <span className="sx-hint">{f.type === 'percent' ? 'ใส่ % ของแต่ละคน' : 'ใส่ยอดของแต่ละคน'}</span>}
        </span>
        <div className="sx-chips">
          {members.map((m) => {
            const on = f.participants.includes(m)
            return (
              <div key={m} className={`sx-chip${on ? ' on' : ''}`}>
                <button type="button" aria-pressed={on} onClick={() => toggle(m)}><Avatar name={m} members={members} />{m}</button>
                {needsValue && on && (
                  <input className="sx-weight" type="number" min="0" step="0.01" aria-label={`ส่วนของ ${m}`}
                    placeholder={f.type === 'percent' ? '%' : '฿'} value={f.values[m] ?? ''} onChange={setValue(m)} />
                )}
              </div>
            )
          })}
        </div>
      </div>

      {preview && Number(f.amount) > 0 && (
        <div className="sx-preview">
          {Object.entries(preview).map(([n, v]) => <span key={n}>{n} <b>{baht(v)}</b></span>)}
        </div>
      )}
      {(error || (problem && f.amount && needsValue)) && <p className="sx-error">{error || problem}</p>}
      <div><button className="sx-btn">บันทึกค่าใช้จ่าย</button></div>
    </form>
  )
}

export default function Expenses({ trip }) {
  const remove = (id) => tripStore.update((t) => { t.expenses = t.expenses.filter((e) => e.id !== id) })
  const total = trip.expenses.reduce((s, e) => s + Number(e.amount), 0)

  return (
    <div className="sx">
      <ExpenseForm members={trip.members} />
      <section className="sx-panel">
        <h2 className="sx-title">รายการค่าใช้จ่าย</h2>
        <ul className="sx-receipt">
          {[...trip.expenses].reverse().map((e) => {
            const shares = ExpenseSplitter.sharesOf(e)
            const strategy = getStrategy(e.split?.type)
            return (
              <li key={e.id}>
                <Avatar name={e.paidBy} members={trip.members} />
                <span className="grow">
                  {e.title}<span className={`sx-tag ${strategy.type}`}>{strategy.label}</span>
                  <span className="sub">
                    {e.paidBy} ออกเงิน · {Object.entries(shares).map(([n, v]) => `${n} ${baht(v)}`).join(', ')}
                  </span>
                </span>
                <span className="amount">{baht(e.amount)}</span>
                <button className="sx-del" onClick={() => remove(e.id)}>ลบ</button>
              </li>
            )
          })}
          {trip.expenses.length === 0 && <li className="sx-empty">ยังไม่มีค่าใช้จ่าย จดรายการแรกจากฟอร์มด้านบน</li>}
        </ul>
        {trip.expenses.length > 0 && (
          <div className="sx-total"><span>รวมทั้งหมด</span><span>{baht(total)}</span></div>
        )}
      </section>
    </div>
  )
}
