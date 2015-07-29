package pl.comarch.soc.socmessenger;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MessagesHandler {

    private Map<String, CallableOnMessage> instances;
    private static MessagesHandler instance;

    private MessagesHandler() {
        instances = new HashMap<>();
    }

    synchronized static public MessagesHandler getInstance() {
        if(instance == null) {
            instance = new MessagesHandler();
        }
        return instance;
    }

    public void register(String name, CallableOnMessage instance) {
        instances.put(name, instance);
    }

    public void handle(String topic, String message) {
        Log.i("MessagesHandler", topic + ": " + message);
        if(instances.containsKey(topic)) {
            instances.get(topic).call(topic, message);
        } else {
            Pattern pattern = Pattern.compile(Topics.Regex.USER_STATUS);
            Matcher matcher = pattern.matcher(topic);
            if(matcher.matches()) {
                instances.get(Topics.Wildcard.USER_STATUS).call(topic, message);
            } else {
                Log.w("MessagesHandler", "Topic is not supported. " + topic);
            }
        }
    }

}
