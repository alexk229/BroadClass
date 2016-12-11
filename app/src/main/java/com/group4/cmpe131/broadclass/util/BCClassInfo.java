package com.group4.cmpe131.broadclass.util;

import java.util.List;

public class BCClassInfo {
    private String ClassID;
    private String ClassName;
    private String ProfessorID;
    private String ProfessorName;

    public BCClassInfo() {
        setClassID("");
        setClassName("");
        setProfessorID("");
        setProfessorName("");
    }

    public BCClassInfo(String classID, String className, String professorID, String professorName) {
        setClassID(classID);
        setClassName(className);
        setProfessorID(professorID);
        setProfessorName(professorName);
    }

    public String getClassID() {
        return ClassID;
    }

    public void setClassID(String classID) {
        ClassID = classID;
    }

    public String getClassName() {
        return ClassName;
    }

    public void setClassName(String className) {
        ClassName = className;
    }

    public String getProfessorID() {
        return ProfessorID;
    }

    public void setProfessorID(String professorID) {
        ProfessorID = professorID;
    }

    public String getProfessorName() {
        return ProfessorName;
    }

    public void setProfessorName(String professorName) {
        ProfessorName = professorName;
    }
}
