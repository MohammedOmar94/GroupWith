package haitsu.groupup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import haitsu.groupup.R;
import haitsu.groupup.other.ChatMessage;
import haitsu.groupup.other.Groups;
import haitsu.groupup.other.Notification;
import haitsu.groupup.other.User;
import haitsu.groupup.other.UserRequest;

public class JoinRequests extends AppCompatActivity {


    private ListView mListView;


    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_my_groups);
        String title = "Join requests";
        getSupportActionBar().setTitle(title);


        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mListView = (ListView) findViewById(R.id.listview);
        mListView.setFocusable(false);//PREVENTS FROM JUMPING TO BOTTOM OF PAGE

        final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference groupRef = databaseRef.child("users").child(mFirebaseUser.getUid()).child("userRequest");
        final FirebaseListAdapter<UserRequest> usersAdapter = new FirebaseListAdapter<UserRequest>(this, UserRequest.class, R.layout.group_chat, groupRef) {
            protected void populateView(View view, UserRequest request, int position) {
                System.out.println("ayy " + request.getGroupName());
                String[] parts = request.getAge().split("/");
                int year = Integer.parseInt(parts[2]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[0]);
                LocalDate birthdate = new LocalDate (year, month, day);
                LocalDate now = new LocalDate();
                Years age = Years.yearsBetween(birthdate, now);
                ((TextView) view.findViewById(R.id.message_user)).setText(request.getGroupName().toString());
                ((TextView) view.findViewById(R.id.message_text)).setText("User: " + request.getUsername() + " Age: " + age.getYears() + " City: " + request.getCity() + "/ " + request.getCountry());
                ((TextView) view.findViewById(R.id.message_time)).setText((DateFormat.format("dd-MM-yyyy", request.getTimeOfRequest())));
                // ((TextView) view.findViewById(R.id.message_text)).setText("Be the first to say Hello!");
                //    ((TextView) view.findViewById(R.id.message_time)).setText(DateFormat.format("HH:mm:ss", message.getMessageTime()));
            }

            //  (


/*
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                // Initialize a TextView for ListView each Item
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                TextView tv2 = (TextView) view.findViewById(android.R.id.text2);

                // Set the text color of TextView (ListView Item)
                tv.setTextColor(Color.BLACK);
                tv2.setTextColor(Color.BLACK);

                // Generate ListView Item using TextView
                return view;
            }
*/

        };


        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                mListView.setAdapter(usersAdapter);
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                        mListView.setFocusable(true);//HACKS
                        String requestId = usersAdapter.getRef(position).getKey();//Gets key of listview item
//                        UserRequest request = ((UserRequest) mListView.getItemAtPosition(position));
//                        selectedGroupName = group.getName();
//                        Intent intent = new Intent(getActivity(), ChatRoomActivity.class);
//                        Bundle extras = new Bundle();
//                        extras.putString("GROUP_ID", selectedGroup);
//                        extras.putString("GROUP_NAME", selectedGroupName);
//                        intent.putExtras(extras);
//                        startActivity(intent);
                        //User id2 = (User) mListView.getItemAtPosition(position); //
                        System.out.println("ID IS " + requestId);
                    }


                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
