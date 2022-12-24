package com.example.chatroop;

public class Messages {

    private String Date, Fullname, Message, Type, Uid, Profileimage;

    public Messages(){

    }

    public Messages(String Date, String Fullname, String Message, String Type, String Uid, String Profileimage){
        this.Date = Date;
        this.Fullname = Fullname;
        this.Message = Message;
        this.Type = Type;
        this.Uid = Uid;
        this.Profileimage = Profileimage;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String Date) {
        this.Date = Date;
    }

    public String getProfileimage(){
        return Profileimage;
    }

    public void setProfileimage(String Profileimage){
        this.Profileimage = Profileimage;
    }

    public String getFullname() {
        return Fullname;
    }

    public void setFullname(String Fullname) {
        this.Fullname = Fullname;
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
