package com.creatorbox.esense;

/**
 * ONLINE MODE
 * Model class to parse and retrieve data from Firebase to the AllUsers_RecyclerViewAdapter
 */
public class AllUsers_ModelClass {

    public String uid, name, image, status, thumb_image, account_creation_date;
    public boolean online;

    public AllUsers_ModelClass() {}

    public AllUsers_ModelClass(String uid, String name, String image, String status, String thumb_image, String account_creation_date, boolean online) {
        this.uid = uid;
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumb_image = thumb_image;
        this.account_creation_date = account_creation_date;
        this.online = online;
    }

    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getThumb_image() {
        return thumb_image;
    }
    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }
    public String getAccount_creation_date() {
        return account_creation_date;
    }
    public void setAccount_creation_date(String account_creation_date) {this.account_creation_date = account_creation_date;}
    public Boolean getOnline() {return online;}
    public void setOnline(Boolean online) {this.online = online;}
}
