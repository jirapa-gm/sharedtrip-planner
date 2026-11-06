package sharedtrip.composite;

// Composite Pattern: interface ร่วมของทุกชิ้นในทริป (Activity, Day, Trip)
// เรียก getCost() / getDuration() ได้เหมือนกันทุกชั้น
public interface TripComponent {
    double getCost();

    int getDuration(); // นาที
}
