import { useEffect } from 'react'
import { useTrip } from '../../hooks/useTrip.js'
import { tripStore } from '../../patterns/observer.js'
import Icon from '../../components/Icon.jsx'

export default function TripToolbar() {
  const trip = useTrip()
  const { history } = tripStore

  useEffect(() => {
    const onKey = (e) => {
      if (!(e.ctrlKey || e.metaKey) || e.target.matches('input, textarea, select')) return
      if (e.key === 'z' && !e.shiftKey) { e.preventDefault(); tripStore.undo() }
      if (e.key === 'y' || (e.key === 'z' && e.shiftKey)) { e.preventDefault(); tripStore.redo() }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [])

  const save = () => {
    const blob = new Blob([JSON.stringify(trip, null, 2)], { type: 'application/json' })
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = `${trip.name || 'trip'}.json`
    a.click()
    URL.revokeObjectURL(a.href)
  }

  return (
    <div className="toolbar">
      <span className="brand"><Icon name="plane" size={22} />SharedTrip</span>
      <div className="toolbar-actions">
        <button disabled={!history.canUndo()} onClick={() => tripStore.undo()} title="ย้อนกลับ (Ctrl+Z)"><Icon name="undo" size={16} />ย้อนกลับ</button>
        <button disabled={!history.canRedo()} onClick={() => tripStore.redo()} title="ทำซ้ำ (Ctrl+Y)"><Icon name="redo" size={16} />ทำซ้ำ</button>
        <button onClick={save}><Icon name="save" size={16} />บันทึกไฟล์</button>
      </div>
    </div>
  )
}
