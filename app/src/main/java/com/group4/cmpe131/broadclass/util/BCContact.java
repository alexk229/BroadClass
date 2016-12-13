package com.group4.cmpe131.broadclass.util;

/* A simple class for holding contact information for use with lists and Firebase queries. */
public class BCContact {
    private String Name;
    private String UID;
    private String ChatKey;

    public String getChatKey() {
        return ChatKey;
    }

    public void setChatKey(String chatKey) {
        ChatKey = chatKey;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public BCContact() {
        Name = "";
        UID = "";
        ChatKey = "";
    }

    public BCContact(String name, String uid, String chatKey) {
        Name = name;
        UID = uid;
        ChatKey = chatKey;
    }
}
