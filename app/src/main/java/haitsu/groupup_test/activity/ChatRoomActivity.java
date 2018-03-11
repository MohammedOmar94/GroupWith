package haitsu.groupup_test.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import haitsu.groupup_test.R;
import haitsu.groupup_test.activity.Account.ReportActivity;
import haitsu.groupup_test.activity.Groups.GroupMembersActivity;
import haitsu.groupup_test.other.Models.ChatMessage;
import haitsu.groupup_test.other.Models.Groups;

import static android.graphics.Color.WHITE;

public class ChatRoomActivity extends AppCompatActivity {
    private FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder> mFirebaseAdapter;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private DatabaseReference chatrooms;
    private DatabaseReference lastMessage;
    private DatabaseReference messageReceived;
    private Query queryStuff;

    private String groupID;
    private String groupName;
    private String groupCategory;
    private String username;

    private ValueEventListener mListener;

    private ImageView mAddMessageImageView;
    private static final int REQUEST_IMAGE = 2;


    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView messageUser;
        TextView messageTime;
        ImageView profileImage;
        //CircleImageView messengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            messageText = (TextView) v.findViewById(R.id.message_text);
            messageUser = (TextView) v.findViewById(R.id.message_user);
            messageTime = (TextView) v.findViewById(R.id.message_time);
            profileImage = (ImageView) v.findViewById(R.id.messengerImageView);
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
        groupCategory = extras.getString("GROUP_CATEGORY");

        FirebaseMessaging.getInstance().subscribeToTopic(groupID);

        getSupportActionBar().setTitle(groupName);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(mFirebaseUser.getUid()).child("username");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Creates a reference to the chatrooms node from the JSON tree.
        //Location: https://group-up-34ab2.firebaseio.com/chatrooms
        chatrooms = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(groupID);
        chatrooms.keepSynced(true);
        //Creates a reference to the lastMessage node from the JSON tree.
        queryStuff = FirebaseDatabase.getInstance().getReference().child("users").orderByChild(groupID);
        queryStuff.keepSynced(true);

        messageReceived = FirebaseDatabase.getInstance().getReference().child("users").child(mFirebaseUser.getUid()).child("groups").child(groupID).child("lastMessage");
        mListener = messageReceived.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.child("messageCount").getRef().removeValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        /*queryStuff.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            //Path: users/userid/groups/groupid/lastMessage
                            lastMessage = FirebaseDatabase.getInstance().getReference().child("users").child(snapshot.getKey())
                                    .child("groups").child(groupID).child("lastMessage");
                            Groups groupData = snapshot.getValue(Groups.class);
                            ChatMessage usersLastMessage = groupData.getLastMessage();
                            lastMessage.setValue(usersLastMessage);
                            System.out.println("Snapshot " + snapshot.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
     */
        //The send message button.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.send_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText input = (EditText) findViewById(R.id.input);
                final String message = input.getText().toString();
                final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(ChatRoomActivity.this);
                System.out.println("photo old " + account.getPhotoUrl());

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                chatrooms.push()
                        .setValue(new ChatMessage(groupName, mFirebaseUser.getUid(), input.getText().toString(), username, account.getPhotoUrl().toString()));

                // When added in Firebase using the ChatMessage model, messageCount is set to 0 by default;
                final ChatMessage usersLastMessage = new ChatMessage(groupName, mFirebaseUser.getUid(), message, username, account.getPhotoUrl().toString());

                // All users that have are apart of this group.
                queryStuff.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            //System.out.println("Snapshot " + snapshot);
                            if (snapshot
                                    .child("groups").hasChild(groupID)) {
                                //Path: users/userid/groups/groupid/lastMessage
                                lastMessage = FirebaseDatabase.getInstance().getReference().child("users").child(snapshot.getKey())
                                        .child("groups").child(groupID).child("lastMessage");
                                System.out.println("user id " + snapshot.getKey());
                                lastMessage.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        // Do this with all lastMessage in groups, count total unread messages
                                        // Can add all unread messages this way in a single notification.
                                        // Makes sure doesn't add count if admin is sending.
//                                        System.out.println("this is datasnap " + dataSnapshot.child("userId").getValue(String.class));
                                        // Each user vs message id.

                                        //snapshot.getKey == current user in group (loop). Useless otherwise.
                                        //dataSnapshot.child("userid") == last person who sent message.
                                        if(!mFirebaseUser.getUid().equals(snapshot.getKey())) {
                                            if (!dataSnapshot.hasChild("messageCount")) {
                                                System.out.println("this is uid " + snapshot.getKey());
                                                dataSnapshot.child("messageCount").getRef().setValue(1);
                                                usersLastMessage.setMessageCount(1);
                                            } else {
                                                int messageCount = dataSnapshot.child("messageCount").getValue(Integer.class);
                                                System.out.println("this is " + (messageCount + 1) + " " + snapshot.getKey());
                                                dataSnapshot.child("messageCount").getRef().setValue(messageCount + 1);
                                                usersLastMessage.setMessageCount(messageCount + 1);
                                            }
                                            lastMessage.setValue(usersLastMessage);
                                        } else {
                                            usersLastMessage.setMessageCount(0);
                                            lastMessage.setValue(usersLastMessage);
                                            System.out.println("this was sent from your id " + snapshot.getKey());
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                Groups groupData = snapshot.getValue(Groups.class);
//                                System.out.println("Snapshot " + snapshot);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // Clear the input
                input.setText("");
            }
        });

        // Initialize ProgressBar and RecyclerView.
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

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

        mAddMessageImageView = (ImageView) findViewById(R.id.addMessageImageView);
        mAddMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });
    }

    public void displayRecycler() {
        mFirebaseAdapter = new FirebaseRecyclerAdapter<ChatMessage,
                MessageViewHolder>(
                ChatMessage.class,
                R.layout.messages,
                MessageViewHolder.class,
                chatrooms) {

            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, final ChatMessage model, int position) {

                // Set their text
                viewHolder.messageText.setText(model.getMessageText());
                viewHolder.messageUser.setText(model.getMessageUser());
                if (model.getImageUrl() == null) {
                    viewHolder.profileImage.setImageDrawable(ContextCompat.getDrawable(ChatRoomActivity.this,
                            R.drawable.ic_account_circle_black_36dp));
                } else {
                    System.out.println("URI is " + model.getImageUrl());
                    Glide.with(ChatRoomActivity.this)
                            .load(model.getImageUrl())
                            .into(viewHolder.profileImage);
                }

                // Format the date before showing it
                viewHolder.messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm)",
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Handles the back button click.
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_report:
                Intent intent = new Intent(ChatRoomActivity.this, ReportActivity.class);
                Bundle extras = new Bundle();
                extras.putString("GROUP_ID", groupID);
                extras.putString("REPORT_TYPE", "group");
                intent.putExtras(extras);
                startActivity(intent);
                break;
            case R.id.action_view_members:
                Intent intent2 = new Intent(ChatRoomActivity.this, GroupMembersActivity.class);
                Bundle extras2 = new Bundle();
                extras2.putString("GROUP_ID", groupID);
                extras2.putString("GROUP_CATEGORY", groupCategory);
                extras2.putString("REPORT_TYPE", "group");
                intent2.putExtras(extras2);
                startActivity(intent2);
                break;


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.report, menu);
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(WHITE, PorterDuff.Mode.SRC_ATOP);
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        messageReceived.removeEventListener(mListener);
    }
}
