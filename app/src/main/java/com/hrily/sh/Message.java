package com.hrily.sh;

/**
 * Created by hrishi on 9/6/16.
 */
public class Message {
    String msg, by, time;

    public Message(){}

    public Message(String msg, String by, String time) {
        this.msg = msg;
        this.by = by;
        this.time = time;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
