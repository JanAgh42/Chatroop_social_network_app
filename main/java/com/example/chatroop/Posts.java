package com.example.chatroop;

public class Posts {

    private String Uid, Date, PostImage, Description, ProfileImage, Fullname, PostImageId;
    private Long PostNumber;

    public Posts(){

    }

    public Posts(String Date, String Description, String Fullname, String PostImage, String PostImageId, String ProfileImage, String Uid, Long PostNumber) {
        this.Uid = Uid;
        this.Date = Date;
        this.PostImage = PostImage;
        this.Description = Description;
        this.ProfileImage = ProfileImage;
        this.Fullname = Fullname;
        this.PostImageId = PostImageId;
        this.PostNumber = PostNumber;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String Uid) {
        this.Uid = Uid;
    }

    public String getPostImageId(){
        return PostImageId;
    }

    public void setPostImageId(String PostImageId){
        this.PostImageId = PostImageId;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String Date) {
        this.Date = Date;
    }

    public String getPostImage() {
        return PostImage;
    }

    public void setPostImage(String PostImage) {
        this.PostImage = PostImage;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String Description) {
        this.Description = Description;
    }

    public String getProfileImage() {
        return ProfileImage;
    }

    public void setProfileImage(String ProfileImage) {
        this.ProfileImage = ProfileImage;
    }

    public String getFullname() {
        return Fullname;
    }

    public void setFullname(String Fullname) {
        this.Fullname = Fullname;
    }

    public Long getPostNumber(){
        return PostNumber;
    }

    public void setPostNumber(Long PostNumber){
        this.PostNumber = PostNumber;
    }
}
