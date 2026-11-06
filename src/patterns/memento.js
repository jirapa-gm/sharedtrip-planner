// Memento Pattern: เก็บสถานะทริปไว้ย้อนกลับ (Undo) และทำซ้ำ (Redo)

export class TripMemento {
  constructor(state) { this.state = JSON.stringify(state) }
  getState() { return JSON.parse(this.state) }
}

export class History {
  constructor(limit = 50) { this.undoStack = []; this.redoStack = []; this.limit = limit }
  save(memento) {
    this.undoStack.push(memento)
    if (this.undoStack.length > this.limit) this.undoStack.shift()
    this.redoStack = []
  }
  undo(current) {
    if (!this.undoStack.length) return null
    this.redoStack.push(current)
    return this.undoStack.pop()
  }
  redo(current) {
    if (!this.redoStack.length) return null
    this.undoStack.push(current)
    return this.redoStack.pop()
  }
  canUndo() { return this.undoStack.length > 0 }
  canRedo() { return this.redoStack.length > 0 }
}
