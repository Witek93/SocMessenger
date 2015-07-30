package pl.comarch.soc.socmessenger.activities;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.comarch.soc.socmessenger.CallableOnMessage;
import pl.comarch.soc.socmessenger.Configuration;
import pl.comarch.soc.socmessenger.MessagesHandler;
import pl.comarch.soc.socmessenger.singletons.MqttConnectionHandler;
import pl.comarch.soc.socmessenger.R;
import pl.comarch.soc.socmessenger.Topics;
import pl.comarch.soc.socmessenger.UsersListAdapter;
import pl.comarch.soc.socmessenger.model.User;


public class MainActivity extends AppCompatActivity {

    private ArrayAdapter adapter;
    static private List<User> userList = new ArrayList<>();
    private MqttConnectionHandler client;
    private MessagesHandler messagesHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messagesHandler = MessagesHandler.getInstance();

        CallableOnMessage usersOnlineCallable = createUsersOnlineCallable();
        messagesHandler.register(Topics.ONLINE_USERS, usersOnlineCallable);

        CallableOnMessage userStatusCallable = createUserStatusCallable();
        messagesHandler.register(Topics.Wildcard.USER_STATUS, userStatusCallable);

        initUserListAdapter();

        client = MqttConnectionHandler.getInstance();
        try {
            client.connect();
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
                        putToUserList(new User(null, username, true));

                    } else if (message.equals("off")) {
                        putToUserList(new User(new Date(), username, false));
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
                    clearUserList();
                    for(User user: users) {
                        Log.i("User", user.getName() + ", " + user.getLastSeen());
                        putToUserList(user);
                    }
                }
            }
        };
    }


    private void initUserListAdapter() {
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


    public void putToUserList(final User newUser) {
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

    private void clearUserList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userList.clear();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void addNewUser(User newUser) {
        final String fromUser = Topics.message(Configuration.USERNAME, newUser.getLogin());
        final String toUser = Topics.message(newUser.getLogin(), Configuration.USERNAME);
        userList.add(newUser);
        try {
            client.subscribe(fromUser);
            client.subscribe(toUser);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void updateUser(User newUser) {
        for(User user: userList) {
            if(newUser.getLogin().equals(user.getLogin())) {
                user.setLastSeen(newUser.getLastSeen());
                user.setOnline(newUser.isOnline());
                break;
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
                onRefresh();
                return true;
            case R.id.action_settings:
                Toast.makeText(this, "Not supported yet", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void onRefresh() {
        if(!client.isConnected()) {
            try {
                client.connect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

}

