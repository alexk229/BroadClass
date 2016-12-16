package com.group4.cmpe131.broadclass.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BCMessage implements Comparable {
    private long   ID;
    private String UID;
    private String Name;
    private String Content;
    private String TimestampString;
    private long   Timestamp;

    public Long getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(Long timestamp) throws ParseException {
        Timestamp = timestamp;

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
        TimestampString = dateFormat.format(new Date(timestamp));

    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getTimestampString() {
        return TimestampString;
    }

    public int compareTo(Object o) {
        if(Timestamp == ((BCMessage) o).Timestamp) {
            return 0;
        }

        else if(Timestamp > ((BCMessage) o).Timestamp) {
            return 1;
        }

        else {
            return -1;
        }
    }
}
