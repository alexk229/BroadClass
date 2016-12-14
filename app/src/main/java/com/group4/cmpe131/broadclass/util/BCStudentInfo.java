package com.group4.cmpe131.broadclass.util;

public class BCStudentInfo implements Comparable {
    private long    mID;
    private String  mUID;
    private String  mName;
    private boolean mIsRegistered;

    public BCStudentInfo() {
        mUID = "";
        mName = "";
        mIsRegistered = false;
    }

    public BCStudentInfo(String uid, String name, boolean is_registered) {
        mUID = uid;
        mName = name;
        mIsRegistered = is_registered;
    }

    public String getUID() {
        return mUID;
    }

    public void setUID(String uid) {
        mUID = uid;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public boolean isRegistered() {
        return mIsRegistered;
    }

    public void setRegistered(boolean is_registered) {
        mIsRegistered = is_registered;
    }

    public long getID() {
        return mID;
    }

    public void setID(long id) {
        mID = id;
    }

    public int compareTo(Object o) {
        //Sort by registration status first, then by name.
        if(((BCStudentInfo) o).mIsRegistered == false && mIsRegistered == true) {
            return 1;
        }

        if(((BCStudentInfo) o).mIsRegistered == true && mIsRegistered == false) {
            return -1;
        }

        return -1 * ((BCStudentInfo) o).mName.compareTo(mName);
    }
}
