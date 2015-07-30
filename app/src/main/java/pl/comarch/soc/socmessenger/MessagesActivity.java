package pl.comarch.soc.socmessenger;

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

import pl.comarch.soc.socmessenger.model.Message;
import pl.comarch.soc.socmessenger.singletons.MqttConnectionHandler;


public class MessagesActivity extends Activity implements CallableOnMessage {

    private List<Message> messageList;
    private ArrayAdapter adapter;
    private String toUser, fromUser;
    private String publishingTopic, subscribingTopic;
    private MessageDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);


        Bundle bundle = getIntent().getExtras();
        fromUser = bundle.getString("other_user");
        toUser = bundle.getString("this_user");
        publishingTopic = Topics.message(fromUser, toUser);
        subscribingTopic = Topics.message(toUser, fromUser);

        dbHelper = new MessageDatabaseHelper(this);

        MessagesHandler messagesHandler = MessagesHandler.getInstance();
        messagesHandler.register(subscribingTopic, this);

        messageList = new ArrayList<>(dbHelper.selectAll(fromUser, toUser));
        adapter = new MessageListAdapter(this, messageList);
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
                messageList.add(message);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void onSendButtonClick(View view) {
        TextView messageText = (TextView) findViewById(R.id.messageText);
        String content = messageText.getText().toString();
        if(!content.isEmpty()) {
            Message message = new Message(content, Configuration.USERNAME, new Date());
            long id = dbHelper.insert(message.getContent(), message.getWhen().getTime(), toUser, fromUser);
            Toast.makeText(MessagesActivity.this, id + "", Toast.LENGTH_SHORT).show();
            messageList.add(message);
            adapter.notifyDataSetChanged();
            try {
                MqttConnectionHandler.getInstance().publish(publishingTopic, content.getBytes(), Configuration.DEFAULT_QOS, false);
            } catch (MqttException e) {
                e.printStackTrace();
            }
            messageText.setText("");
        }

    }
}
