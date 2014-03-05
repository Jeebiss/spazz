package net.jeebiss.spazz.github;


public class RateLimit {

    private Resources resources;
    private Data rate;

    public class Resources {
        private Data core;
        private Data search;

        public Data getCore() { return core; }
        public Data getSearch() { return search; }
    }

    public class Data {
        private int limit;
        private int remaining;
        private long reset;

        public int getLimit() { return limit; }
        public int getRemaining() { return remaining; }
        public long getReset() { return reset; }
    }

    public Resources getResources() { return resources; }
    public Data getRate() { return rate; }

}
