package haitsu.groupup;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import haitsu.groupup.other.ChatMessage;

public class ChatRoomActivity extends AppCompatActivity {
    private FirebaseListAdapter<ChatMessage> adapter;
    private FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder>
            mFirebaseAdapter;
    private DatabaseReference chatroom;
    private String groupID;
    private String groupName;


    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView messageUser;
        TextView messageTime;
        //CircleImageView messengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            messageText = (TextView) v.findViewById(R.id.message_text);
            messageUser = (TextView) v.findViewById(R.id.message_user);
            messageTime = (TextView) v.findViewById(R.id.message_time);
            // messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        Bundle extras = getIntent().getExtras();
        groupID = extras.getString("GROUP_ID");
        groupName = extras.getString("GROUP_NAME");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        getSupportActionBar().setTitle(groupName);


        System.out.println("groupID " + groupID);
        if (groupID.equals(null)) {
            chatroom = FirebaseDatabase.getInstance().getReference().child("chatrooms");
        } else {
            chatroom = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(groupID);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText) findViewById(R.id.input);

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                chatroom.push()
                        .setValue(new ChatMessage(input.getText().toString(),
                                FirebaseAuth.getInstance()
                                        .getCurrentUser()
                                        .getDisplayName()));

                // Clear the input
                input.setText("");
            }
        });

        // Initialize ProgressBar and RecyclerView.
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);


        //displayMessages();
        displayRecycler();

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mMessageRecyclerView.getContext(),
                mLinearLayoutManager.getOrientation());
        mMessageRecyclerView.addItemDecoration(dividerItemDecoration);
    }


/*
    public void displayMessages() {
        ListView listOfMessages = (ListView) findViewById(R.id.list_of_messages);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.messages, chatroom) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView) v.findViewById(R.id.message_text);
                TextView messageUser = (TextView) v.findViewById(R.id.message_user);
                TextView messageTime = (TextView) v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(adapter);
    }
*/

    public void displayRecycler() {
        mFirebaseAdapter = new FirebaseRecyclerAdapter<ChatMessage,
                MessageViewHolder>(
                ChatMessage.class,
                R.layout.messages,
                MessageViewHolder.class,
                chatroom) {

            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, final ChatMessage model, int position) {

                // Set their text
                viewHolder.messageText.setText(model.getMessageText());
                viewHolder.messageUser.setText(model.getMessageUser());

                // Format the date before showing it
                viewHolder.messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));


                viewHolder.messageUser.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), model.getMessageUser(), Toast.LENGTH_LONG).show();
                    }
                });


            }
        };

    }

}
