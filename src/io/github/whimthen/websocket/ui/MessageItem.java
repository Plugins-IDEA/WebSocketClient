package io.github.whimthen.websocket.ui;

import javax.swing.*;
import java.io.Serializable;

public class MessageItem implements Serializable {

    private Icon icon;
    private String message;
    private String datetime;

    private MessageItem() {}

    public static MessageItem newInstance() {
        return new MessageItem();
    }

    public Icon getIcon() {
        return icon;
    }

    public MessageItem setIcon(Icon icon) {
        this.icon = icon;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public MessageItem setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getDatetime() {
        return datetime;
    }

    public MessageItem setDatetime(String datetime) {
        this.datetime = datetime;
        return this;
    }

    @Override
    public String toString() {
        return "MessageItem{" +
                   "icon=" + icon +
                   ", message='" + message + '\'' +
                   ", datetime='" + datetime + '\'' +
                   '}';
    }
}
