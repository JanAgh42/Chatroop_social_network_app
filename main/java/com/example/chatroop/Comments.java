package com.example.chatroop;

public class Comments {

    private String Uid, Time, Comment, Fullname, ProfileImage;

    public Comments(){

    }

    public Comments(String uid, String time, String comment, String fullname, String profileImage) {
        Uid = uid;
        Time = time;
        Comment = comment;
        Fullname = fullname;
        ProfileImage = profileImage;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public String getFullname() {
        return Fullname;
    }

    public void setFullname(String fullname) {
        Fullname = fullname;
    }

    public String getProfileImage() {
        return ProfileImage;
    }

    public void setProfileImage(String profileImage) {
        ProfileImage = profileImage;
    }
}
