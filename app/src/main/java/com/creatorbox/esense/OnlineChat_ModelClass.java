/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

/**
 * ONLINE MODE
 * A model class being used by the OnlineChat_MessageAdapter to parse and retrieve data from Firebase Databse.
 */
public class OnlineChat_ModelClass {

    String message;
    String from;
    String type;
    String date;
    String time;

    public OnlineChat_ModelClass() {}

    public OnlineChat_ModelClass(String message, String from, String type, String date, String time) {
        this.message = message;
        this.from = from;
        this.type = type;
        this.date = date;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }
    public String getType() {return type;}
    public void setType(String type) {this.type = type;}
    public String getDate() {return date;}
    public void setDate(String date) {this.date = date;}
    public String getTime() {return time;}
    public void setTime(String time) {this.time = time;}
}
