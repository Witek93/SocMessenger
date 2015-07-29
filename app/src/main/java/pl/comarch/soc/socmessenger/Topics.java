package pl.comarch.soc.socmessenger;

public class Topics {

    public static final String ONLINE_USERS = Parts.USERS + "/online";
    public static final String USER_STATUS_TOPIC = Parts.USERS + "/" + Configuration.USERNAME + "/status";

    public static String message(String to, String from) {
        return Parts.USERS + "/" + to + "/messages/" + from;
    }

    public static class Parts {
        private static final String PREFIX = "test_kw_";
        public static final String USERS = PREFIX + "users";
    }

    public static class Regex {
        public static final String USER_STATUS = Parts.USERS + "/(\\w+)/status";
        public static final String MESSAGE = ""; //TODO: create regex
    }

    public static class Content {
        public static final byte[] ONLINE_STATUS = "on".getBytes();
        public static final byte[] OFFLINE_STATUS = "off".getBytes();
    }

    public static class Wildcard {
        public static final String USER_STATUS = Parts.USERS + "/+/status";
    }

}

