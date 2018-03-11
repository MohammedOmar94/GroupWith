package haitsu.groupup_test.other;

import android.widget.EditText;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import haitsu.groupup_test.other.Models.Group;
import haitsu.groupup_test.other.Models.Notification;
import haitsu.groupup_test.other.Models.Report;
import haitsu.groupup_test.other.Models.User;
import haitsu.groupup_test.other.Models.UserRequest;

/**
 * Created by moham on 17/06/2017.
 */

public class DBConnections {

    //Can access any child element within the database using this reference.
    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
    private GeoFire geoFire = new GeoFire(databaseRef);


    //Firebase details
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();

    private UserRequest request = new UserRequest();

    boolean groupAdmin;


    //String groupId;

    public DBConnections() {
        databaseRef.keepSynced(true);
    }

    public void geoFireTest() {
        geoFire.setLocation("firebase-hq", new GeoLocation(37.7853889, -122.4056973), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    System.err.println("There was an error saving the location to GeoFire: " + error);
                } else {
                    System.out.println("Location saved on server successfully!");
                }
            }

        });
        geoFire.setLocation("firebase-hq2", new GeoLocation(37.7853889, -122.4056973), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    System.err.println("There was an error saving the location to GeoFire: " + error);
                } else {
                    System.out.println("Location saved on server successfully!");
                }
            }

        });
    }

    public void geoFireTestGet() {
        geoFire.getLocation("firebase-hq", new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    System.out.println(String.format("The location for key %s is [%f,%f]", key, location.latitude, location.longitude));
                } else {
                    System.out.println(String.format("There is no location for key %s in GeoFire", key));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("There was an error getting the GeoFire location: " + databaseError);
            }
        });
    }

    public void geoFireTestNearby() {
        // creates a new query around [37.7832, -122.4056] with a radius of 0.6 kilometers
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(37.7832, -122.4056), 0.6);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                System.out.println(String.format("The Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
            }

            // Key has left radius of 0.6
            @Override
            public void onKeyExited(String key) {

            }

            // Key has moved but is still within radius of 0.6
            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    public void addGeoToGroups(String groupId, double latitude, double longitude) {
        geoFire.setLocation(groupId + "-key", new GeoLocation(latitude, -longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    System.err.println("There was an error saving the location to GeoFire: " + error);
                } else {
                    System.out.println("Location saved on server successfully!");
                }
            }

        });
    }


    public void createUserAccount(EditText username, String gender, String email, TextView age, String city, String country, double latitude, double longitude) {
        User user = new User(username.getText().toString(), gender, email, age.getText().toString(), city, country, null, latitude, longitude);
        databaseRef.child("users").child(mFirebaseUser.getUid()).setValue(user);//Add user}
    }

    public void userRequest(final String groupID, final String groupName, final String groupCategory, final String groupAdminId) {
        DatabaseReference userRef = databaseRef.child("users").child(mFirebaseUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                request = snapshot.getValue(UserRequest.class);
                request.setUserId(snapshot.getKey());
                request.setGroupName(groupName);
                request.setGroupId(groupID);
                request.setGroupCategory(groupCategory);
                // Not even needed, what has the users own groups has to do with anything.
                // Groups group = snapshot.child("groups").getValue(Groups.class);
                // System.out.println("group admin is " + group.getName());
                joinGroup(groupID, groupName, groupCategory, groupAdminId, request);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void newGroupRequest(final String groupCategory, final String groupType, final String groupName, final String groupDescription, final String groupGender, final String memberCount) {
        DatabaseReference userRef = databaseRef.child("users").child(mFirebaseUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                // Not even needed, what has the users own groups has to do with anything.
                // Groups group = snapshot.child("groups").getValue(Groups.class);
                // System.out.println("group admin is " + group.getName());
                submitNewGroup(groupCategory, groupType, groupName, groupDescription, groupGender, memberCount, user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void joinGroup(final String groupID, String groupName, String groupCategory, final String groupAdminId, UserRequest request) {
        //String userid = databaseRef.child("Group").push().getKey();
        final DatabaseReference groupRef = databaseRef.child("group").child(groupCategory).child(groupID);


        // Adds user requested to be joined in admins tree. Delete also needs a rework to remove from userRequest tree.
        groupRef.child("members").child(request.getUserId()).setValue(false);//Adds Members, need approval from admin before true.

        //Adds to users group tree when user has sent join request.
        DatabaseReference usersGroupsTree = databaseRef.child("users").child((request.getUserId())).child("groups").child(request.getGroupId());
        usersGroupsTree.child("name").setValue(request.getGroupName());
        usersGroupsTree.child("category").setValue(request.getGroupCategory());
        usersGroupsTree.child("admin").setValue(false);
        usersGroupsTree.child("userApproved").setValue(false);

        databaseRef.child("users").child(groupAdminId).child("userRequest").push().setValue(request);

        // Checks if member count has now exceeded after this new member has joined
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("Count is " + dataSnapshot.child("members").getChildrenCount());
                long memberCount = dataSnapshot.child("members").getChildrenCount();
                groupRef.child("memberCount").setValue(memberCount);
                getUserByGroup(groupAdminId, groupID, memberCount, dataSnapshot.child("memberLimit").getValue(Long.class));
                Group group = dataSnapshot.getValue(Group.class);
                if (dataSnapshot.child("members").getChildrenCount() == group.getMemberLimit()) { // Can maybe get snapshot of groupref, find member limit there?
                    // Change group type to Full, will no longer show in results.
                    // Will also be the case until admin approves or declines member.
                    groupRef.child("type").setValue("FULL_" + group.getType());//Combine Full and type of group, remove if member leaves or gets kicked. Group should then show up in results
                } else {
                    // Most likely nothing. Group should still appear in results search
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//
//        //Add to notifications
//        notifications.setValue(new Notification("You joined the group " + groupName + "!"));
//
//        notifications.child("messageText").setValue("You joined the group " + groupName + "!");

    }

    public void getUserByGroup(final String groupAdminId, final String groupID, final long memberCount, final long memberLimit) {
        Query allUsersFromGroup = FirebaseDatabase.getInstance().getReference().child("users").orderByChild(groupID);

        //If this isn't Single Value, message updates continously forever.
        allUsersFromGroup.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("groups").hasChild(groupID)) {
                        //Path: users/userid/groups/groupid/
                        databaseRef.child("users").child(snapshot.getKey()).child("groups").child(groupID).child("memberCount").setValue(memberCount);
                        databaseRef.child("users").child(snapshot.getKey()).child("groups").child(groupID).child("memberLimit").setValue(memberLimit);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void approveMember(String groupID, String groupName, String groupCategory, String userId) {
        //String userid = databaseRef.child("Group").push().getKey();
        DatabaseReference groupRef = databaseRef.child("group").child(groupCategory).child(groupID);
        DatabaseReference notifications = databaseRef.child("notifications").child(mFirebaseUser.getUid()).push();

        groupRef.child("members").child(userId).setValue(true);//Adds Members, need approval from admin before true.

        // Updates that users nodes
        databaseRef.child("users").child(userId).child("groups").child(groupID).child("category").setValue(groupCategory);
        databaseRef.child("users").child(userId).child("groups").child(groupID).child("name").setValue(groupName);
        databaseRef.child("users").child(userId).child("groups").child(groupID).child("admin").setValue(false);

        //Add to notifications
        notifications.setValue(new Notification("You joined the group " + groupName + "!"));

        notifications.child("messageText").setValue("You joined the group " + groupName + "!");

    }

    public void submitNewGroup(String groupCategory, String groupType, String groupName, String groupDescription, String groupGender, String memberLimit, User user) {
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        DatabaseReference groupId2 = databaseRef.child("group").child(groupCategory).push();
        DatabaseReference notifications = databaseRef.child("notifications").child(mFirebaseUser.getUid()).push();
        String groupId = groupId2.getKey();//Stores key in local variable for testing purposes.
        // String notificationId = notifications.getKey();

        GeoFire geoFire = new GeoFire(databaseRef.child("group").child(groupCategory));
        geoFire.setLocation(groupId, new GeoLocation(user.getLatitude(), user.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    System.err.println("There was an error saving the location to GeoFire: " + error);
                } else {
                    System.out.println("Location saved on server successfully!");
                }
            }

        });

        //Adds to group tree
        groupId2.child("members").child(mFirebaseUser.getUid()).setValue(true);//Adds Members
        //groupId2.child("category").setValue(groupCategory);
        groupId2.child("adminID").setValue(mFirebaseUser.getUid());//Adds AdminID
        groupId2.child("name").setValue(groupName.toString());//Adds Category to Group
        groupId2.child("memberLimit").setValue(Integer.parseInt(memberLimit));
        groupId2.child("memberCount").setValue(1);
        groupId2.child("description").setValue(groupDescription.toString());
        groupId2.child("genders").setValue(groupGender);
        groupId2.child("type").setValue(groupType);
        groupId2.child("type_gender_memberLimit").setValue(groupType + "_" + groupGender + "_" + memberLimit);
        groupId2.child("latitude").setValue(user.getLatitude());
        groupId2.child("longitude").setValue(user.getLongitude());


        //Adds to users tree
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupId).child("category").setValue(groupCategory);
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupId).child("name").setValue(groupName.toString());
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupId).child("admin").setValue(true);
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupId).child("userApproved").setValue(true);
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupId).child("memberCount").setValue(1);
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupId).child("memberLimit").setValue(Integer.parseInt(memberLimit));


        //Add to notifications
        notifications.setValue(new Notification("You created the group " + groupName.toString() + "!"));
    }

    public void createChatRoom(String groupId) {
        DatabaseReference chatRooms = databaseRef.child("chatrooms");
        chatRooms.child(groupId).child("admin").setValue(mFirebaseUser.getUid());
    }

    public void deleteAccount() {
        final String userId = mFirebaseUser.getUid();
        databaseRef.child("users").child(userId).removeValue();
        Query findGroupsWithUserId = databaseRef.child("group").orderByChild(userId);
        findGroupsWithUserId.keepSynced(true);
        findGroupsWithUserId.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                        String adminID = groupSnapshot.child("adminID").getValue(String.class);
                        if (adminID.equals(userId)) {
                            System.out.println("group created " + groupSnapshot.getRef());
                            groupSnapshot.getRef().removeValue();
                        } else if (groupSnapshot.child("members").hasChild(userId)) {
                            System.out.println("group joined " +
                                    groupSnapshot.child("members").child(userId).getRef());
                            ;
                            groupSnapshot.child("members").child(userId).getRef().removeValue();
                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        // Need the ability to kick members too
        // Need to delete firebase instance too.
    }

    public void checkGroup(final String groupID, final String groupCategory) {
        Query query = databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").orderByChild("admin").equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (dataSnapshot.getValue() != null) {
                    System.out.println("Category is" + groupCategory);
                    deleteRequest(groupID);
                    deleteGroupFromUsers(groupID);
                    //Delete from group tree, which contains detail about the name and its members
                    databaseRef.child("group").child(groupCategory).child(groupID).removeValue();

                    //Delete from the users group tree
                    databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupID).removeValue();

                    //Deletes the chatroom for everyone.
                    databaseRef.child("chatrooms").child(groupID).removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void deleteGroupFromUsers(final String groupID) {
        Query groupMembers = FirebaseDatabase.getInstance().getReference().child("users").orderByChild(groupID);
        groupMembers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //System.out.println("Snapshot " + snapshot);
                    if (snapshot.child("groups").hasChild(groupID)) {
                        //Path: users/userid/groups/groupid/lastMessage
                        snapshot.child("groups").child(groupID).getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // If an Approved user wants to leave the group they're in
    public void leaveGroup(final String groupID, final String groupAdminId, final String groupCategory) {
        Query query = databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").orderByChild("admin").equalTo(false);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (dataSnapshot.getValue() != null) {
                    System.out.println("Category is" + groupCategory);
                    //Delete member from group tree
                    databaseRef.child("group").child(groupCategory).child(groupID).child("members").child(mFirebaseUser.getUid())
                            .removeValue();

                    databaseRef.child("group").child(groupCategory).child(groupID).
                            addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    long memberCount = dataSnapshot.child("members").getChildrenCount();
                                    getUserByGroup(groupAdminId, groupID, memberCount, dataSnapshot.child("memberLimit").getValue(Long.class));
                                    databaseRef.child("group").child(groupCategory).child(groupID).child("memberCount").setValue(memberCount);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }

                //Delete from the users group tree
                databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupID).removeValue();

                //Change group to not full, so group should reappear again in results
                revertGroupType(groupCategory, groupID);

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Done by the user who changes their mind about joining a specific group
    public void cancelJoinRequest(final String groupID, final String groupCategory, final String groupAdminId) {
        // Deletes request from admin's user tree
        databaseRef.child("users").child(groupAdminId).child("userRequest").removeValue();
        // Deletes group from the user's user tree
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupID).removeValue();
        // Delete member from the group tree
        databaseRef.child("group").child(groupCategory).child(groupID).child("members").child(mFirebaseUser.getUid()).removeValue();
        // Decrease member count by 1
        databaseRef.child("group").child(groupCategory).child(groupID).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long memberCount = dataSnapshot.child("members").getChildrenCount();
                        getUserByGroup(groupAdminId, groupID, memberCount, dataSnapshot.child("memberLimit").getValue(Long.class));
                        databaseRef.child("group").child(groupCategory).child(groupID).child("memberCount").setValue(memberCount);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        // Group is no longer full anymore, so revert the type back to normal to reappear in group results.
        revertGroupType(groupCategory, groupID);
    }

    public void revertGroupType(final String groupCategory, final String groupID) {
        // Separate full and type of group, should reappear as avaliable.
        databaseRef.child("group").child(groupCategory).child(groupID).child("type")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String type = dataSnapshot.getValue(String.class);
                        if (type.contains("FULL")) {
                            String[] parts = type.split("FULL_");
                            String groupType = parts[1]; // group type
                            System.out.println("group type is now " + groupType);
                            //Reverts back type to normal
                            databaseRef.child("group").child(groupCategory).child(groupID).child("type").setValue(groupType);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    public void deleteRequest(final String groupID) {
        DatabaseReference requestRef = databaseRef.child("users").child(mFirebaseUser.getUid()).child("userRequest");
        requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    UserRequest request = requestSnapshot.getValue(UserRequest.class);
                    if (request.getGroupId().equals(groupID)) {
//                        System.out.println("Ref is " + request.getGroupId() + " grouo id" + request.getGroupId());
                        System.out.println("Ref is " + requestSnapshot.getRef());
                        requestSnapshot.getRef().removeValue();
                    }
                }
                //dataSnapshot.getRef().removeValue()

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void submitReport(String groupID, String reason, String reportedMember, String reportingMember, String comments) {
        Report report = new Report(groupID, reason, reportedMember, reportingMember, comments);
        databaseRef.child("reports").push().setValue(report);
    }

}
