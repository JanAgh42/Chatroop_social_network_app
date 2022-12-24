package com.example.chatroop;

public class SentMessages {

    private String Profileimage, Date, Username, Message, Type, Uid;

    public SentMessages(){

    }

    public SentMessages(String Profileimage, String Date, String Username, String Message, String Type, String Uid){
        this.Date = Date;
        this.Username = Username;
        this.Message = Message;
        this.Type = Type;
        this.Uid = Uid;
        this.Profileimage = Profileimage;
    }

    public String getProfileimage(){
        return Profileimage;
    }

    public void setProfileimage(String Profileimage){
        this.Profileimage = Profileimage;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String Date) {
        this.Date = Date;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String Username) {
        this.Username = Username;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String Message) {
        this.Message = Message;
    }

    public String getType() {
        return Type;
    }

    public void setType(String Type) {
        this.Type = Type;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String Uid) {
        this.Uid = Uid;
    }
}