package com.creatorbox.esense;

/**
 * ONLINE MODE
 * A model class being used by the Friends_RecyclerViewAdapter to parse and retrieve data from Firebase Databse.
 */
public class Friends_ModelClass {
    public String friend_name, friend_since_date;

    public Friends_ModelClass() {}

    public Friends_ModelClass(String friend_name, String friend_since_date) {
        this.friend_since_date = friend_since_date;
        this.friend_name = friend_name;
    }

    public String getFriend_name() {return friend_name;}
    public void setFriend_name(String friend_name) {this.friend_name = friend_name;}
    public String getFriend_since_date() {return friend_since_date;}
    public void setFriend_since_date(String friend_since_date) {this.friend_since_date = friend_since_date;}
}
