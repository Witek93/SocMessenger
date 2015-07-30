package pl.comarch.soc.socmessenger.messageHandling;


import java.util.Map;

public interface CallableOnMessage {
    void call(String topic, String message);
}
