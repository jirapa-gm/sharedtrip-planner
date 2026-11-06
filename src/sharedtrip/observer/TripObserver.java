package sharedtrip.observer;

import sharedtrip.composite.Trip;

// Observer Pattern: หน้าจอที่อยากรู้เมื่อทริปเปลี่ยน
public interface TripObserver {
    void onTripChanged(Trip trip);
}
