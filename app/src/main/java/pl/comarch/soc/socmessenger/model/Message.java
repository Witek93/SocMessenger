package pl.comarch.soc.socmessenger.model;

import java.util.Date;

/**
 * Created by Comarch on 2015-07-27.
 */
public class Message {
    String content;
    String author;
    Date when;

    public Message(String content, String author, Date when) {
        this.content = content;
        this.author = author;
        this.when = when;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getWhen() {
        return when;
    }

    public void setWhen(Date when) {
        this.when = when;
    }
}
