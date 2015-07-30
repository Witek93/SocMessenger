package pl.comarch.soc.socmessenger.model;

import com.google.gson.annotations.SerializedName;

import java.util.Comparator;
import java.util.Date;


public class User {
    private String login;
    private String name;
    @SerializedName("last_seen")
    private Date lastSeen;
    private boolean online;

    public User() {}

    public User(Date lastSeen, String name, boolean online) {
        this.login = this.name = name;
        this.lastSeen = lastSeen;
        this.online = online;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
