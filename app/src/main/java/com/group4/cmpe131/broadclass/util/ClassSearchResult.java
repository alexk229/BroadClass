package com.group4.cmpe131.broadclass.util;

/**
 * Created by conner on 12/10/16.
 */

public class ClassSearchResult {
    private String mName;
    private String mProfessor;

    public ClassSearchResult() {
        setName("");
        setProfessor("");
    }

    public ClassSearchResult(String name, String professor) {
        setName(name);
        setProfessor(professor);
    }

    public String getName() {
        return mName;
    }

    public String getProfessor() {
        return mProfessor;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setProfessor(String professor) {
        mProfessor = professor;
    }
}
