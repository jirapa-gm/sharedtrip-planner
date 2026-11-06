// วงกลมตัวอักษรแรกของชื่อ สีพาสเทลคงที่ต่อคน ช่วยให้ดูออกเร็วว่าใครจ่าย/ใครโอน
const COLORS = ['#ffc8de', '#c8f2df', '#d3e8ff', '#ddd2fb', '#fff0b0', '#ffd9c2']

// ถ้าส่งรายชื่อสมาชิกมา ใช้ลำดับในรายชื่อ (สีไม่ซ้ำกันจนกว่าจะเกิน 6 คน)
export const colorOf = (name, members = []) => {
  const i = members.indexOf(name)
  if (i >= 0) return COLORS[i % COLORS.length]
  let h = 0
  for (const ch of name) h = (h * 31 + ch.codePointAt(0)) >>> 0
  return COLORS[h % COLORS.length]
}

// เอาพยัญชนะ/ตัวอักษรตัวแรก (ข้ามสระที่ขึ้นต้นไม่ได้)
const initial = (name) => (name.match(/[A-Za-z0-9ก-ฮ]/)?.[0] ?? name[0] ?? '?').toUpperCase()

export default function Avatar({ name, members }) {
  return <span className="sx-avatar" style={{ background: colorOf(name, members) }} aria-hidden="true">{initial(name)}</span>
}
