package pl.comarch.soc.socmessenger;


import java.util.Map;

public interface CallableOnMessage {
    void call(String topic, String message);
}
