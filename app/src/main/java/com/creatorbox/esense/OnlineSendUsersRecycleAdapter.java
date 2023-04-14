package com.creatorbox.esense;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * ONLINE MODE
 * An adapter being used by the OnlineSend_Users to retrieve user's friends and be listed in the RecyclerView.
 */
public class OnlineSendUsersRecycleAdapter extends FirebaseRecyclerAdapter<Friends_ModelClass, OnlineSendUsersRecycleAdapter.MySendViewHolder> {

    Activity activity;
    private final Context context;
    private final DatabaseReference userReference;

    private String friend_uid, friend_name, friends_since_date;

    String userName;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public OnlineSendUsersRecycleAdapter(@NonNull FirebaseRecyclerOptions<Friends_ModelClass> options, Context context, Activity activity) {
        super(options);

        this.activity = activity;
        this.context = context;

        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        userReference.keepSynced(true);

    }


    protected void onStartNewActivityAnimations() {
        activity.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    @Override
    protected void onBindViewHolder(@NonNull OnlineSendUsersRecycleAdapter.MySendViewHolder holder, int position, @NonNull Friends_ModelClass model) {
        friend_uid = getRef(position).getKey();
        friend_name = model.getFriend_name();
        friends_since_date = model.getFriend_since_date();

        holder.userName.setText(friend_name);
        holder.friendsSinceDate.setText("Friends Since: "+friends_since_date);

        userReference.child(friend_uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String friendUUID = snapshot.getKey();
                String friendUserName = snapshot.child("name").getValue(String.class);
                String friendStatus = snapshot.child("status").getValue(String.class);
                String friendDisplayImage = snapshot.child("thumb_image").getValue(String.class);
                String friendJoinedDate = snapshot.child("account_creation_date").getValue(String.class);
                Boolean friendOnlineStatus = snapshot.child("online").getValue(Boolean.class);
                holder.userStatus.setText(friendStatus);

                if (!friendDisplayImage.equals("default")) {
                    Picasso.get().load(friendDisplayImage).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.baseline_person_dp).into(holder.userImage,
                                    new Callback() {
                                        @Override
                                        public void onSuccess() {}
                                        @Override
                                        public void onError(Exception e) {
                                            Picasso.get().load(friendDisplayImage).placeholder(R.drawable.ic_person)
                                                    .into(holder.userImage);
                                        }
                                    });
                }

                if (friendOnlineStatus) {
                    holder.onlineStatus.setVisibility(View.VISIBLE);
                } else {
                    holder.onlineStatus.setVisibility(View.INVISIBLE);
                }

                Intent intent = new Intent(context, OnlineSendFiles.class);
                intent.putExtra("userNameSend", userName);
                intent.putExtra("otherNameSend", friendUserName);
                intent.putExtra("otherNameSendID", friendUUID);

                intent.putExtra("user_id", friendUUID);
                intent.putExtra("other_name", friendUserName);
                intent.putExtra("image_url", friendDisplayImage);
                intent.putExtra("status", friendStatus);
                intent.putExtra("joined_date", friendJoinedDate);

                holder.cardView.setOnClickListener(v -> context.startActivity(intent));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @NonNull
    @Override
    public MySendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.onlinechat_friends_single_layout,
                parent, false);
        return new OnlineSendUsersRecycleAdapter.MySendViewHolder(v);
    }

    public static class MySendViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView userName, userStatus, friendsSinceDate;
        CircleImageView userImage, onlineStatus;

        public MySendViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.onlinechatfriends_cardView);
            userName = (TextView) itemView.findViewById(R.id.onlinechat_friends_single_name);
            friendsSinceDate = (TextView) itemView.findViewById(R.id.onlinechat_friends_since);
            userStatus = (TextView) itemView.findViewById(R.id.onlinechat_friends_single_status);
            userImage = (CircleImageView) itemView.findViewById(R.id.onlinechat_friends_single_image);
            onlineStatus = (CircleImageView) itemView.findViewById(R.id.onlinechat_friends_online_status);
        }
    }
}
