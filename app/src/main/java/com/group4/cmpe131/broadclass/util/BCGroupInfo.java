package com.group4.cmpe131.broadclass.util;

public class BCGroupInfo implements Comparable {
    private long    mID;
    private String  mGID;
    private String  mName;

    public BCGroupInfo() {
        mGID = "";
        mName = "";
    }

    public BCGroupInfo(String gid, String name) {
        mGID = gid;
        mName = name;
    }

    public String getGID() {
        return mGID;
    }

    public void setGID(String gid) {
        mGID = gid;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public long getID() {
        return mID;
    }

    public void setID(long id) {
        mID = id;
    }

    public int compareTo(Object o) {
        return -1 * ((BCGroupInfo) o).mName.compareTo(mName);
    }
}
