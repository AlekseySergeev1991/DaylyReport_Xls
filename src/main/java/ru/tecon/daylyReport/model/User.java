package ru.tecon.daylyReport.model;

import java.io.Serializable;
import java.util.StringJoiner;

public class User implements Serializable {

    private String userId;
    private String userName;
    private int ppNum;
    private String tag;

    public User(String userId, String userName, String tag) {
        this.userId = userId;
        this.userName = userName;
        this.tag = tag;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getPpNum() {
        return ppNum;
    }

    public void setPpNum(int ppNum) {
        this.ppNum = ppNum;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", User.class.getSimpleName() + "[", "]")
                .add("userId='" + userId + "'")
                .add("userName='" + userName + "'")
                .add("ppNum=" + ppNum)
                .add("tag='" + tag + "'")
                .toString();
    }
}
