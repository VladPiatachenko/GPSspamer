package sumdu.edu.ua.GPSspamer.domain;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Component
public class TrackStore {

    private final Deque<TrackRow> rows = new ArrayDeque<>();
    private final int maxRows = 50_000;

    public synchronized void add(TrackRow row) {
        rows.addLast(row);
        while (rows.size() > maxRows) rows.removeFirst();
    }

    public synchronized List<TrackRow> last(int limit) {
        int n = rows.size();
        int skip = Math.max(0, n - limit);
        List<TrackRow> out = new ArrayList<>(Math.min(limit, n));
        int i = 0;
        for (TrackRow r : rows) {
            if (i++ < skip) continue;
            out.add(r);
        }
        return out;
    }

    public synchronized void clear() {
        rows.clear();
    }

    public synchronized int size() {
        return rows.size();
    }
}
