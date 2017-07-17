package haitsu.groupup;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import haitsu.groupup.other.ChatMessage;

public class ChatRoomActivity extends AppCompatActivity {
    private FirebaseListAdapter<ChatMessage> adapter;
    private DatabaseReference chatroom;
    private String groupID;
    private String groupName;


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
        if(groupID.equals(null)){
            chatroom = FirebaseDatabase.getInstance().getReference().child("chatrooms");
        } else {
            chatroom = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(groupID);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    EditText input = (EditText)findViewById(R.id.input);

                    // Read the input field and push a new instance
                    // of ChatMessage to the Firebase database
                    chatroom.push()
                            .setValue(new ChatMessage(input.getText().toString(),
                                    FirebaseAuth.getInstance()
                                            .getCurrentUser()
                                            .getDisplayName())  );

                    // Clear the input
                    input.setText("");
                }
        });
        displayMessages();
    }

    public void displayMessages(){
        ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.messages, chatroom) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

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

}
