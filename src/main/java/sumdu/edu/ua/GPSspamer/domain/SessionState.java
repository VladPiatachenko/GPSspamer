package sumdu.edu.ua.GPSspamer.domain;

import org.springframework.stereotype.Component;
import sumdu.edu.ua.GPSspamer.config.AttackConfig;

@Component
public class SessionState {

    public record SelectedFlight(String flightId) {}
    public record PairPoint(long t, TrackPoint truth, TrackPoint attack) {}

    private volatile SelectedFlight selectedFlight;
    private volatile AttackConfig attack = new AttackConfig(false, AttackType.NONE, 0, 0, 0);
    private volatile PairPoint last;

    public SelectedFlight getSelectedFlight() { return selectedFlight; }
    public void setSelectedFlight(SelectedFlight f) { this.selectedFlight = f; }

    public AttackConfig getAttack() { return attack; }
    public void setAttack(AttackConfig a) { this.attack = a; }

    public PairPoint getLast() { return last; }
    public void setLast(PairPoint p) { this.last = p; }
}
