package com.example.chatroop;

public class Users {

    private String ProfileImage, Fullname, Username, status;

    public Users(){

    }

    public Users(String profileImage, String fullname, String username, String status) {
        ProfileImage = profileImage;
        Fullname = fullname;
        Username = username;
        this.status = status;
    }

    public String getProfileImage() {
        return ProfileImage;
    }

    public void setProfileImage(String profileImage) {
        ProfileImage = profileImage;
    }

    public String getFullname() {
        return Fullname;
    }

    public void setFullname(String fullname) {
        Fullname = fullname;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getStatus(){
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }
}
