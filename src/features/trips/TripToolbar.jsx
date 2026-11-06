import { useTrip } from '../../hooks/useTrip.js'
import { tripStore } from '../../patterns/observer.js'

export default function TripToolbar() {
  const trip = useTrip()
  const { history } = tripStore

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
      <button disabled={!history.canUndo()} onClick={() => tripStore.undo()}>↶ Undo</button>
      <button disabled={!history.canRedo()} onClick={() => tripStore.redo()}>↷ Redo</button>
      <button onClick={save}>บันทึก JSON</button>
    </div>
  )
}
