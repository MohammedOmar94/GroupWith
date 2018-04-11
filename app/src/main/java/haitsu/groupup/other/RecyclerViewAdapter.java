package haitsu.groupup.other;

/**
 * Created by moham on 01/04/2018.
 */


import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import haitsu.groupup.R;
import haitsu.groupup.activity.ChatRoomActivity;
import haitsu.groupup.other.Models.ChatMessage;
import haitsu.groupup.other.Models.Group;

/**
 * Created by phanvanlinh on 12/04/2017.
 * phanvanlinh.94vn@gmail.com
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private List<ChatMessage> chatMessageList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView messageText;
        private TextView messageUser;
        private TextView messageTime;
        private ImageView profileImage;

        public MyViewHolder(View v) {
            super(v);
            messageText = (TextView) v.findViewById(R.id.message_text);
            messageUser = (TextView) v.findViewById(R.id.message_user);
            messageTime = (TextView) v.findViewById(R.id.message_time);
            profileImage = (ImageView) v.findViewById(R.id.messengerImageView);
        }
    }

    public RecyclerViewAdapter(List<ChatMessage> chatMessageList) {
        System.out.println("Chat " + chatMessageList);
        this.chatMessageList = chatMessageList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.messages, parent, false);
        context = parent.getContext();

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final ChatMessage model = chatMessageList.get(position);
        System.out.println("Model " + model);
        // Set their text
        holder.messageText.setText(model.getMessageText());
        holder.messageUser.setText(model.getMessageUser());
        if (model.getImageUrl() == null) {
            holder.profileImage.setImageDrawable(ContextCompat.getDrawable(context,
                    R.drawable.ic_account_circle_black_36dp));
        } else {
            System.out.println("URI is " + model.getImageUrl());
            Glide.with(context)
                    .load(model.getImageUrl())
                    .into(holder.profileImage);
        }

        // Format the date before showing it
        holder.messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm)",
                model.getMessageTime()));

        holder.messageUser.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(context, model.getMessageUser(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        System.out.println("size " + chatMessageList.size());
        return chatMessageList.size();
    }

    public List<ChatMessage> getChatMessageList() {
        return getChatMessageList();
    }
}