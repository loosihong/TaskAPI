package com.example.TaskAPI.core.observability;

public final class SqlStats {
    private static final ThreadLocal<Counters> HOLDER = new ThreadLocal<>();

    private SqlStats() {
    }

    public static void start() {
        HOLDER.set(new Counters());
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static void record(long millis) {
        Counters counters = HOLDER.get();

        if (counters != null) {
            counters.count++;
            counters.totalMillis += millis;
        }
    }

    public static int count() {
        Counters counters = HOLDER.get();
        return counters == null ? 0 : counters.count;
    }

    public static long totalMillis() {
        Counters counters = HOLDER.get();
        return counters == null ? 0L : counters.totalMillis;
    }

    private static final class Counters {
        private int count;
        private long totalMillis;
    }
}
