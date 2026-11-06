import { getBalances, getTransfers } from './settle.js'
import Avatar from '../expenses/Avatar.jsx'
import '../expenses/expenses.css'

const baht = (n) => `฿${Math.abs(Number(n)).toLocaleString(undefined, { maximumFractionDigits: 2 })}`

export default function Settlement({ trip }) {
  if (trip.expenses.length === 0) {
    return (
      <div className="sx">
        <section className="sx-panel sx-done">
          <strong>ยังไม่มีอะไรต้องโอน</strong>
          <span className="sx-hint">จดค่าใช้จ่ายในแท็บ "ค่าใช้จ่าย" แล้วยอดโอนจะสรุปให้ที่นี่</span>
        </section>
      </div>
    )
  }

  const balances = getBalances(trip)
  const transfers = getTransfers(balances)
  const maxNet = Math.max(1, ...balances.map((b) => Math.abs(b.net)))

  return (
    <div className="sx">
      <section>
        <h2 className="sx-title">ใครต้องโอนให้ใคร</h2>
        {transfers.length === 0 ? (
          <div className="sx-panel sx-done"><strong>ทุกคนจ่ายครบแล้ว</strong><span className="sx-hint">ไม่ต้องโอนเพิ่ม</span></div>
        ) : (
          <div className="sx-stubs">
            {transfers.map((t, i) => (
              <div key={i} className="sx-stub">
                <div className="sx-route">
                  <Avatar name={t.from} members={trip.members} />{t.from}
                  <span className="sx-arrow" aria-label="โอนให้" />
                  <Avatar name={t.to} members={trip.members} />{t.to}
                </div>
                <div className="sx-price">{baht(t.amount)}</div>
              </div>
            ))}
          </div>
        )}
      </section>

      <section className="sx-panel">
        <h2 className="sx-title">ยอดของแต่ละคน</h2>
        {balances.map((b) => {
          const width = `${(Math.abs(b.net) / maxNet) * 50}%`
          return (
            <div key={b.name} className="sx-bar-row">
              <span className="sx-who"><Avatar name={b.name} members={trip.members} />{b.name}</span>
              <div className="sx-bar" title={`จ่ายไป ${baht(b.paid)} · ส่วนของตัวเอง ${baht(b.owed)}`}>
                {b.net !== 0 && <div className={b.net > 0 ? 'fill-pos' : 'fill-neg'} style={{ width }} />}
              </div>
              <span className={`sx-val ${b.net < 0 ? 'neg' : b.net > 0 ? 'pos' : 'zero'}`}>
                {b.net === 0 ? 'ครบแล้ว' : `${b.net > 0 ? '+' : '−'}${baht(b.net)}`}
              </span>
            </div>
          )
        })}
        <div className="sx-legend"><span>← ต้องโอนเพิ่ม</span><span>ได้เงินคืน →</span></div>
      </section>
    </div>
  )
}
