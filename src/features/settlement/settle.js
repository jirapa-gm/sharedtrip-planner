// คำนวณยอดคงเหลือของแต่ละคน และสรุปว่าใครต้องโอนให้ใคร
import { ExpenseSplitter } from '../../patterns/splitStrategy.js'

const satang = (baht) => Math.round(Number(baht) * 100)

// คืน [{ name, paid, owed, net }] โดย net > 0 = ได้เงินคืน, net < 0 = ต้องจ่ายเพิ่ม
export function getBalances(trip) {
  const rows = new Map(trip.members.map((m) => [m, { name: m, paid: 0, owed: 0 }]))
  const row = (name) => rows.get(name) ?? rows.set(name, { name, paid: 0, owed: 0 }).get(name)

  for (const e of trip.expenses) {
    row(e.paidBy).paid += satang(e.amount)
    const shares = ExpenseSplitter.sharesOf(e)
    for (const [name, share] of Object.entries(shares)) row(name).owed += satang(share)
  }
  return [...rows.values()].map((r) => ({
    name: r.name, paid: r.paid / 100, owed: r.owed / 100, net: (r.paid - r.owed) / 100,
  }))
}

// Greedy: จับคู่คนที่ติดเงินมากที่สุดกับคนที่ต้องได้คืนมากที่สุดไปเรื่อย ๆ
// ได้จำนวนครั้งการโอนไม่เกิน (จำนวนคน - 1)
export function getTransfers(balances) {
  const debtors = balances.filter((b) => b.net < 0).map((b) => ({ name: b.name, amt: satang(-b.net) }))
  const creditors = balances.filter((b) => b.net > 0).map((b) => ({ name: b.name, amt: satang(b.net) }))
  debtors.sort((a, b) => b.amt - a.amt)
  creditors.sort((a, b) => b.amt - a.amt)

  const transfers = []
  let i = 0, j = 0
  while (i < debtors.length && j < creditors.length) {
    const pay = Math.min(debtors[i].amt, creditors[j].amt)
    if (pay > 0) transfers.push({ from: debtors[i].name, to: creditors[j].name, amount: pay / 100 })
    debtors[i].amt -= pay
    creditors[j].amt -= pay
    if (debtors[i].amt === 0) i++
    if (creditors[j].amt === 0) j++
  }
  return transfers
}
