package haitsu.groupup.other;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import haitsu.groupup.activity.SignInActivity;

/**
 * Created by moham on 17/06/2017.
 */

public class DBConnections {

    //Can access any child element within the database using this reference.
    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

    //Firebase details
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();

    boolean groupAdmin;


    //String groupId;

    public DBConnections() {

    }

    public void joinGroup(String groupID, String groupName, String groupCategory) {
        //String userid = databaseRef.child("Group").push().getKey();
        DatabaseReference userid2 = databaseRef.child("group").child(groupID);
        DatabaseReference notifications = databaseRef.child("notifications").child(mFirebaseUser.getUid()).push();

        userid2.child("members").child(mFirebaseUser.getUid()).setValue(true);//Adds Members


        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupID).child("category").setValue(groupCategory);
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupID).child("name").setValue(groupName);
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupID).child("admin").setValue(false);

        //Add to notifications
        notifications.setValue(new Notification("You joined the group " + groupName + "!"));

        notifications.child("messageText").setValue("You joined the group " + groupName + "!");

    }

    public void submitNewGroup(String groupCategory, EditText groupName, EditText groupDescription, String groupGender) {
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        DatabaseReference groupId2 = databaseRef.child("group").push();
        DatabaseReference notifications = databaseRef.child("notifications").child(mFirebaseUser.getUid()).push();
        String groupId = groupId2.getKey();//Stores key in local variable for testing purposes.
        // String notificationId = notifications.getKey();

        //Adds to group tree
        groupId2.child("members").child(mFirebaseUser.getUid()).setValue(true);//Adds Members
        groupId2.child("category").setValue(groupCategory);
        groupId2.child("adminID").setValue(mFirebaseUser.getUid());//Adds AdminID
        groupId2.child("name").setValue(groupName.getText().toString());//Adds Category to Group
        groupId2.child("description").setValue(groupDescription.getText().toString());
        groupId2.child("genders").setValue(groupGender);


        //Adds to users tree
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupId).child("category").setValue(groupCategory);
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupId).child("name").setValue(groupName.getText().toString());
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupId).child("admin").setValue(true);

        //Add to notifications
        notifications.setValue(new Notification("You created the group " + groupName.getText().toString() + "!"));

        //databaseRef.child("notifications").child(mFirebaseUser.getUid()).push().setValue("You joined the group " + text.getText().toString() + "!");
        //Create chats room with group id
        //  createChatRoom(groupId);
    }

    public void createChatRoom(String groupId) {
        DatabaseReference chatRooms = databaseRef.child("chatrooms");
        chatRooms.child(groupId).child("admin").setValue(mFirebaseUser.getUid());
    }

    public void deleteGroup(String groupID) {
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        //Delete from group tree, which contains detail about the name and its members
        databaseRef.child("group").child(groupID).removeValue();

        //Delete from the users group tree
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupID).removeValue();

        //Deletes the chatroom
        databaseRef.child("chatrooms").child(groupID).removeValue();
    }

    public boolean checkGroup(final String groupID) {
        DatabaseReference query = databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupID).child("admin");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){//Delete group if it's the users
                    System.out.println("Group ID is " + groupID + " " + dataSnapshot.getValue());
                    groupAdmin = true;
                } else {
                    System.out.println("Group ID is " + groupID + " " + dataSnapshot.getValue());
                    groupAdmin = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        System.out.println("Group ID is " + groupAdmin);
        return groupAdmin;
    }

    public void addToChatRoom() {

    }


}
