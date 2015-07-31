package pl.comarch.soc.socmessenger.messageHandling;


public interface CallableOnMessage {
    void call(String topic, String message);
}
