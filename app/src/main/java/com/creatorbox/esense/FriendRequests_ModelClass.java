package com.creatorbox.esense;

/**
 * ONLINE MODE
 * A model class being used by the FriendRequests_RecyclerViewAdapter to parse and retrieve data from Firebase Databse.
 */
public class FriendRequests_ModelClass {

    String request_type;

    public FriendRequests_ModelClass() {}

    public FriendRequests_ModelClass(String request_type) {
        this.request_type = request_type;
    }

    public String getRequest_type() {return request_type;}
    public void setRequest_type(String request_type) {this.request_type = request_type;}
}
