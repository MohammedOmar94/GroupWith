package haitsu.groupup.other;

import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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


    //String groupId;

    public DBConnections(){

    }

    public void joinGroup(String selectedGroup, String groupName) {
        //String userid = databaseRef.child("Group").push().getKey();
        DatabaseReference userid2 = databaseRef.child("group").child(selectedGroup);
        userid2.child("members").child(mFirebaseUser.getUid()).setValue(true);//Adds Members

        databaseRef.child("users").child(mFirebaseUser.getUid()).child("Groups").child(selectedGroup).child("name").setValue(groupName);
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("Groups").child(selectedGroup).child("admin").setValue(false);
    }

    public void submitNewGroup(String selectedCategory, EditText text) {
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        DatabaseReference groupId2 = databaseRef.child("group").push();
        String groupId = groupId2.getKey();//Stores key in local variable for testing purposes.

        //Adds to group tree
        groupId2.child("members").child(mFirebaseUser.getUid()).setValue(true);//Adds Members
        groupId2.child("category").setValue(selectedCategory);
        groupId2.child("adminID").setValue(mFirebaseUser.getUid());//Adds AdminID
        groupId2.child("name").setValue(text.getText().toString());//Adds Category to Group

        //Adds to users tree
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("Groups").child(groupId).child("name").setValue(text.getText().toString());
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("Groups").child(groupId).child("admin").setValue(true);
    }



}
