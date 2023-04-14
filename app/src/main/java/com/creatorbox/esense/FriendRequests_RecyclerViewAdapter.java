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
 * An adapter class that handles the RecyclerView component of Requests Fragment.
 */
public class FriendRequests_RecyclerViewAdapter extends FirebaseRecyclerAdapter<FriendRequests_ModelClass, FriendRequests_RecyclerViewAdapter.RequestsViewHolder> {

    Activity activity;
    private final Context context;
    private final DatabaseReference userReference;
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FriendRequests_RecyclerViewAdapter(@NonNull FirebaseRecyclerOptions<FriendRequests_ModelClass> options, Context context, Activity activity) {
        super(options);
        this.context = context;
        this.activity = activity;

        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    protected void onBindViewHolder(@NonNull RequestsViewHolder holder, int position, @NonNull FriendRequests_ModelClass model) {
        String user_id = getRef(position).getKey();
        String request_type = model.getRequest_type();

        //Toast.makeText(activity, user_id, Toast.LENGTH_SHORT).show();
        userReference.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String friendUUID = snapshot.getKey();
                String friendUserName = snapshot.child("name").getValue(String.class);
                String friendStatus = snapshot.child("status").getValue(String.class);
                String friendDisplayImage = snapshot.child("thumb_image").getValue(String.class);
                String friendJoinedDate = snapshot.child("account_creation_date").getValue(String.class);
                Boolean friendOnlineStatus = snapshot.child("online").getValue(Boolean.class);

                holder.userUUID.setText(user_id);
                holder.userName.setText(friendUserName);

                if (friendOnlineStatus) {
                    holder.onlineStatus.setVisibility(View.VISIBLE);
                } else {
                    holder.onlineStatus.setVisibility(View.INVISIBLE);
                }

                if (request_type.equals("sent")) {
                    String sentType = "SENT REQUEST TO USER.";
                    holder.requestType.setText(sentType);
                } else if (request_type.equals("received")) {
                    String receivedType = "USER WANTS TO BE FRIENDS!";
                    holder.requestType.setText(receivedType);
                }

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
                Intent intent = new Intent(context, OnlineUserAccountPage.class);
                intent.putExtra("user_id", user_id);
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
    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.onlinechat_requests_single_layout,
                parent, false);
        return new RequestsViewHolder(v);
    }

    public class RequestsViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView userName, requestType, userUUID;
        CircleImageView userImage, onlineStatus;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.onlinechatrequests_cardView);
            userUUID = (TextView) itemView.findViewById(R.id.onlinechatrequests_single_uuid);
            userName = (TextView) itemView.findViewById(R.id.onlinechatrequests_single_name);
            requestType = (TextView) itemView.findViewById(R.id.onlinechatrequests_single_request_type);
            userImage = (CircleImageView) itemView.findViewById(R.id.onlinechatrequests_single_image);
            onlineStatus = (CircleImageView) itemView.findViewById(R.id.onlinechatrequests_status_indicator);
        }
    }
}
