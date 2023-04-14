package com.creatorbox.esense;

/**
 * Interface that is used when pinning messages from RecyclerView to that component's activity.
 * This is utilized when user Pins a message from the RecyclerView, pass the message value to the OnlineChat_Basic
 * activity's Pin Message TextView to be uploaded to the Firebase Realtime Database.
 */
public interface PinMessageListener {
    void onPin(String value);
}
