package com.creatorbox.esense;

/**
 * ONLINE MODE
 * A model class being used by the OnlineUser_RecyclerAdapter to parse and retrieve data from Firebase Databse.
 */
public class OnlineUser_ModelClass{
    public String userID;
    public String userName;
    public String userStatus;
    public String userThumbImage;
    public String userAccountCreationDate;

    public OnlineUser_ModelClass() {}

    public OnlineUser_ModelClass(String userID, String userName, String userStatus, String userThumbImage, String userAccountCreationDate) {
        this.userID = userID;
        this.userName = userName;
        this.userStatus = userStatus;
        this.userThumbImage = userThumbImage;
        this.userAccountCreationDate = userAccountCreationDate;
    }

    public String getUserID() {
        return userID;
    }
    public String getUserName() {return userName;}
    public String getUserStatus() {return userStatus;}
    public String getUserThumbImage() {return userThumbImage;}
    public String getUserAccountCreationDate() {return userAccountCreationDate;}

    public void setUserID(String userID) {
        this.userID = userID;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }
    public void setUserThumbImage(String userThumbImage) {
        this.userThumbImage = userThumbImage;
    }
    public void setUserAccountCreationDate(String userAccountCreationDate) {
        this.userAccountCreationDate = userAccountCreationDate;
    }
}
