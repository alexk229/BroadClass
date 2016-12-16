package com.group4.cmpe131.broadclass.model;

public class BCClassInfo {
    private String ClassID;
    private String ClassName;
    private String ProfessorID;
    private String ProfessorName;
    private String ClassDescription;

    public BCClassInfo() {
        setClassID("");
        setClassName("");
        setProfessorID("");
        setProfessorName("");
        setClassDescription("");
    }

    public BCClassInfo(String classID, String className, String professorID, String professorName, String classDescription) {
        setClassID(classID);
        setClassName(className);
        setProfessorID(professorID);
        setProfessorName(professorName);
        setClassDescription(classDescription);
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

    public String getClassDescription() { return ClassDescription; }

    public void setClassDescription(String classDescription) { ClassDescription = classDescription; }
}
