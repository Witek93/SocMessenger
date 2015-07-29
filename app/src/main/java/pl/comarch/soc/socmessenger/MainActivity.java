package pl.comarch.soc.socmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.comarch.soc.socmessenger.model.User;


public class MainActivity extends AppCompatActivity {

    private CallableOnMessage usersOnlineCallable, userStatusCallable;
    private ArrayAdapter adapter;
    private List<User> userList;
    private MqttSingleton client;
    private MessagesHandler messagesHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messagesHandler = MessagesHandler.getInstance();

        usersOnlineCallable = createUsersOnlineCallable();
        messagesHandler.register(Topics.ONLINE_USERS, usersOnlineCallable);

        userStatusCallable = createUserStatusCallable();
        messagesHandler.register(Topics.Wildcard.USER_STATUS, userStatusCallable);


        initUserListAdapter();

        client = MqttSingleton.getInstance();
        try {
            client.subscribe(Topics.ONLINE_USERS);
            client.subscribe(Topics.Wildcard.USER_STATUS);
            client.publish(Topics.USER_STATUS_TOPIC, new MqttMessage(Topics.Content.ONLINE_STATUS));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private CallableOnMessage createUserStatusCallable() {
        return new CallableOnMessage() {
            @Override
            public void call(String topic, String message) {
                Log.i("MainActivity", topic + ": " + message + ", " + Topics.Regex.USER_STATUS);
                Pattern pattern = Pattern.compile(Topics.Regex.USER_STATUS);
                Matcher matcher = pattern.matcher(topic);
                if(matcher.matches()) {
                    String username = matcher.group(1);
                    Log.i("username", username);
                    if(message.equals("on")) {
                        updateUserlist(new User(null, username, true));

                    } else if (message.equals("off")) {
                        updateUserlist(new User(new Date(), username, false));
                    }
                }
            }
        };
    }

    private CallableOnMessage createUsersOnlineCallable() {
        return new CallableOnMessage() {
            @Override
            public void call(String topic, String message) {
                if(!message.equals("get")) {
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();
                    User[] users = gson.fromJson(message, User[].class);
                    userList.clear();
                    for(User user: users) {
                        Log.i("User", user.getName() + ", " + user.getLastSeen());
                        updateUserlist(user);
                    }
                }
            }
        };
    }

    private void initUserListAdapter() {
        userList = new ArrayList<>();

        adapter = new UsersListAdapter(this, userList);
        ListView usersList = (ListView) findViewById(R.id.usersList);
        usersList.setAdapter(adapter);

        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, MessagesActivity.class);
                intent.putExtra("other_user", userList.get(position).getLogin());
                intent.putExtra("this_user", Configuration.USERNAME);
                startActivity(intent);
            }
        });
    }

    public void updateUserlist(final User newUser) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!newUser.getLogin().equals(Configuration.USERNAME)) {

                    if (isNewUser(newUser)) {
                        addNewUser(newUser);
                    } else {
                        updateUser(newUser);
                    }

                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void addNewUser(User newUser) {
        final String fromOtherUser = Topics.message(Configuration.USERNAME, newUser.getLogin());
        final String toOtherUser = Topics.message(newUser.getLogin(), Configuration.USERNAME);
        userList.add(newUser);
        try {
            client.subscribe(fromOtherUser);
            client.subscribe(toOtherUser);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void updateUser(User newUser) {
        for(User user: userList) {
            if(newUser.getLogin().equals(user.getLogin())) {
                user.setLastSeen(newUser.getLastSeen());
                user.setOnline(newUser.isOnline());
                return;
            }
        }
    }

    private boolean isNewUser(User newUser) {
        for(User user: userList) {
            if(newUser.getLogin().equals(user.getLogin())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
            getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_refresh:
                connectMqtt();
                return true;
            case R.id.action_settings:
                Toast.makeText(this, "Not supported yet", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void connectMqtt() {
        if(!client.isConnected()) {
            try {
                client.connect();
                client.subscribe(Topics.ONLINE_USERS);
                client.subscribe(Topics.Wildcard.USER_STATUS);
                client.publish(Topics.USER_STATUS_TOPIC, new MqttMessage(Topics.Content.ONLINE_STATUS));
                client.publish(Topics.ONLINE_USERS, new MqttMessage("get".getBytes()));
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }


}

