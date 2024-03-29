package pl.comarch.soc.socmessenger.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.comarch.soc.socmessenger.messageHandling.CallableOnMessage;
import pl.comarch.soc.socmessenger.constants.Configuration;
import pl.comarch.soc.socmessenger.db.DBMessageHelper;
import pl.comarch.soc.socmessenger.MessageListAdapter;
import pl.comarch.soc.socmessenger.messageHandling.MessagesHandler;
import pl.comarch.soc.socmessenger.R;
import pl.comarch.soc.socmessenger.constants.Topics;
import pl.comarch.soc.socmessenger.model.Message;
import pl.comarch.soc.socmessenger.singletons.MqttConnector;


public class MessagesActivity extends Activity implements CallableOnMessage {

    private List<Message> messagesList;
    private ArrayAdapter adapter;
    private String toUser, fromUser;
    private String publishingTopic, subscribingTopic;
    private DBMessageHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        Bundle bundle = getIntent().getExtras();
        fromUser = bundle.getString("other_user");
        toUser = bundle.getString("this_user");
        publishingTopic = Topics.message(fromUser, toUser);
        subscribingTopic = Topics.message(toUser, fromUser);

        dbHelper = new DBMessageHelper(this);

        MessagesHandler messagesHandler = MessagesHandler.getInstance();
        messagesHandler.register(subscribingTopic, this);

        messagesList = new ArrayList<>(dbHelper.selectAll(fromUser, toUser));
        adapter = new MessageListAdapter(this, messagesList);
        ListView messageListView = (ListView) findViewById(R.id.messageList);
        messageListView.setAdapter(adapter);
    }

    @Override
    synchronized public void call(String topic, final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message(content, fromUser, new Date());
                long id = dbHelper.insert(message.getContent(), message.getWhen().getTime(), fromUser, toUser);
                Toast.makeText(MessagesActivity.this, id + "", Toast.LENGTH_SHORT).show();
                messagesList.add(message);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void onSendButtonClick(View view) {
        TextView messageText = (TextView) findViewById(R.id.messageText);
        String content = messageText.getText().toString();
        if(!content.isEmpty()) {
            addMessage(content);
            publishMessage(content);
            messageText.setText("");
        }

    }

    private void publishMessage(String content) {
        try {
            MqttConnector.getInstance().publish(publishingTopic, content.getBytes(), Configuration.DEFAULT_QOS, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void addMessage(String content) {
        Message message = new Message(content, Configuration.USERNAME, new Date());
        dbHelper.insert(message.getContent(), message.getWhen().getTime(), toUser, fromUser);
        messagesList.add(message);
        adapter.notifyDataSetChanged();
    }
}
