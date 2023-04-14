package com.creatorbox.esense;

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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * ONLINE MODE
 * The adapter class that handles the RecyclerView of the AllUsersFragment
 */
public class AllUsers_RecyclerViewAdapter extends FirebaseRecyclerAdapter<AllUsers_ModelClass, AllUsers_RecyclerViewAdapter.UsersViewHolder> {
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    private final Context context;
    public AllUsers_RecyclerViewAdapter(@NonNull FirebaseRecyclerOptions<AllUsers_ModelClass> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull AllUsers_ModelClass model) {
        String uid = getRef(position).getKey();
        String name = model.getName();
        String status = model.getStatus();
        String image = model.getThumb_image();
        String joined_date = model.getAccount_creation_date();
        Boolean online = model.getOnline();

        holder.userUUID.setText(uid);
        holder.userName.setText(name);
        holder.userStatus.setText(status);
        if (!image.equals("default")) {
            Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.ic_person_black).into(holder.userImage,
                            new Callback() {
                                @Override
                                public void onSuccess() {}
                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(image).placeholder(R.drawable.ic_person_black).into(holder.userImage);
                                }
                            });
        }

        if (online) {
            holder.onlineStatus.setVisibility(View.VISIBLE);
        } else {
            holder.onlineStatus.setVisibility(View.INVISIBLE);
        }

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OnlineUserAccountPage.class);
            intent.putExtra("user_id", uid);
            intent.putExtra("other_name", name);
            intent.putExtra("image_url", image);
            intent.putExtra("status", status);
            intent.putExtra("joined_date", joined_date);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.onlinechat_users_single_layout,
                parent, false);
        return new UsersViewHolder(v);
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView userName, userStatus, userUUID;
        CircleImageView userImage, onlineStatus;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.onlinechatusers_cardView);
            userUUID = (TextView) itemView.findViewById(R.id.onlineuser_single_uuid);
            userName = (TextView) itemView.findViewById(R.id.onlineuser_single_name);
            userStatus = (TextView) itemView.findViewById(R.id.onlineuser_single_status);
            userImage = (CircleImageView) itemView.findViewById(R.id.onlineuser_single_image);
            onlineStatus = (CircleImageView) itemView.findViewById(R.id.onlineuser_status_indicator);
        }
    }
}
