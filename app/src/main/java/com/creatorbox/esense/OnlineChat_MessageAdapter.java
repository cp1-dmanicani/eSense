package com.creatorbox.esense;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * ONLINE MODE
 * The adapter that handles the RecyclerView of the OnlineChat_Basic.
 */
public class OnlineChat_MessageAdapter extends  RecyclerView.Adapter<OnlineChat_MessageAdapter.MessageViewHolder>{
    Context context;
    List<OnlineChat_ModelClass> list;
    String senderID;
    private final PinMessageListener pinMessageListener;

    boolean status;
    int send;
    int receive;

    FirebaseDatabase database;
    DatabaseReference reference;
    private final String firebaseDBURL = "https://smart-edubox-90c5d-default-rtdb.asia-southeast1.firebasedatabase.app/";

    public OnlineChat_MessageAdapter(PinMessageListener pinMessageListener, Context context, List<OnlineChat_ModelClass> list, String senderID) {
        this.pinMessageListener = pinMessageListener;
        this.context = context;
        this.list = list;
        this.senderID = senderID;

        status = false;
        send = 1; //was 1
        receive = 2; //was 2

        database = FirebaseDatabase.getInstance(firebaseDBURL);
        reference = database.getReference();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v;
        if (viewType == send) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.onlinechat_card_send, parent, false);
        }
        else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.onlinechat_card_receive, parent, false);
        }
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        try {
            if (!list.get(position).getMessage().isEmpty()) {
                holder.timeStamp.setVisibility(View.GONE);
                holder.messageText.setText(list.get(position).getMessage());
                String pinnedMessage = list.get(position).getMessage();
                String time = list.get(position).getTime();
                String date = list.get(position).getDate();
                holder.timeStamp.setText(time + " " + date);

                holder.cardView.setOnLongClickListener(v -> {
                    // Initializing the popup menu and giving the reference as current context
                    PopupMenu popupMenu = new PopupMenu(context, v);

                    // Inflating popup menu from popup_menu.xml file
                    popupMenu.getMenuInflater().inflate(R.menu.onlinechat_popup_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getTitle().equals("Pin Message")) {
                            Toast.makeText(context, "Message Pinned.", Toast.LENGTH_SHORT).show();
                            pinMessageListener.onPin(pinnedMessage);
                        }
                        if (item.getTitle().equals("View Time Stamp")) {
                            holder.timeStamp.setVisibility(View.VISIBLE);
                        }
                        if (item.getTitle().equals("Hide Time Stamp")) {
                            holder.timeStamp.setVisibility(View.GONE);
                        }
                        return true;
                    });
                    // Showing the popup menu
                    popupMenu.show();
                    return true;
                });
            } else {
                Toast.makeText(context, "Conversation is empty.", Toast.LENGTH_SHORT).show();
            }
        } catch (NullPointerException ex) {
            Toast.makeText(context, "Conversation is empty.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, pinnedMessage, timeStamp;
        CardView cardView;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            if (status) {
                messageText = itemView.findViewById(R.id.textViewSend);
                timeStamp = itemView.findViewById(R.id.textViewSendTimeStamp);
                cardView = itemView.findViewById(R.id.sendCardView);
            }
            else {
                messageText = itemView.findViewById(R.id.textViewReceive);
                timeStamp = itemView.findViewById(R.id.textViewReceiveTimeStamp);
                cardView = itemView.findViewById(R.id.receiveCardView);
            }
            pinnedMessage = itemView.findViewById(R.id.pinnedMessageText);
        }
    }

    /**
     * if (list.get(position).getFrom().equals(name)) will determine whether the sender is a receiver
     * or not. Sender textview card will be placed on the right while the other user on the left.
     *
     * @param position position of the data inside the RecyclerView list.
     * @return returns position value.
     */
    @Override
    public int getItemViewType(int position) {
        try {
            if (list.get(position).getFrom().equals(senderID)) {
                status = true;
                return send;
            } else {
                status = false;
                return receive;
            }
        } catch (NullPointerException e) {
            Log.e("NullPointer", e.getMessage());
            e.printStackTrace();
        }
        return position;
    }
}
