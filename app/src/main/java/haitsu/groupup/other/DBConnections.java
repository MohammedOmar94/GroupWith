package haitsu.groupup.other;

import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import haitsu.groupup.R;

/**
 * Created by moham on 17/06/2017.
 */

public class DBConnections {

    //Can access any child element within the database using this reference.
    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

    //Firebase details
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();


    String groupId;

    public DBConnections(){

    }

    public void joinGroup(TextView text) {
        //String userid = databaseRef.child("Group").push().getKey();
        DatabaseReference userid2 = databaseRef.child("Group").child(groupId);
        userid2.child("Members").child(mFirebaseUser.getUid()).setValue(true);//Adds Members
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("Groups").child(groupId).child("Name").setValue(text.getText());
    }

    public void submitNewGroup(TextView text) {
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        DatabaseReference groupId2 = databaseRef.child("Group").push();
         groupId = groupId2.getKey();//
        //databaseRef.child("users").child(mFirebaseUser.getUid()).child("Group").child("Name").setValue(group.getText());//Add users

        groupId2.child("Category").setValue("Sports");
        groupId2.child("AdminID").setValue(mFirebaseUser.getUid());//Adds AdminID
        groupId2.child("Name").setValue(text.getText());//Adds Category to Group

        //databaseRef.child("Groups").child("").child("Name").setValue(group.getText());//Add users
        //databaseRef.child("Group").child(group.getText().toString()).child("Members").child(mFirebaseUser.getUid()).setValue(true);//Add users
        //Use push() for auto generated id node.
    }

}
