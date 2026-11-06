import { useSyncExternalStore } from 'react'
import { tripStore } from '../patterns/observer.js'

export function useTrip() {
  useSyncExternalStore(tripStore.subscribe, tripStore.getSnapshot)
  return tripStore.trip
}
