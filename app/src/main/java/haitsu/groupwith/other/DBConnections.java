package haitsu.groupwith.other;

import android.widget.EditText;
import android.widget.TextView;

import com.amplitude.api.Amplitude;
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
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import haitsu.groupwith.other.Models.Group;
import haitsu.groupwith.other.Models.Groups;
import haitsu.groupwith.other.Models.Notification;
import haitsu.groupwith.other.Models.Report;
import haitsu.groupwith.other.Models.User;
import haitsu.groupwith.other.Models.UserRequest;

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


    public void createUserAccount(EditText username, String gender, String email, TextView age) {
        User user = new User(username.getText().toString(), gender, email, age.getText().toString(), null, null, null, 0, 0);
        databaseRef.child("users").child(mFirebaseUser.getUid()).setValue(user);//Add user}
    }

    public void userRequest(final String groupID, final String groupName, final String groupCategory, final String groupAdminId,
                            final String groupType, final String groupGender) {
        DatabaseReference userRef = databaseRef.child("users").child(mFirebaseUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                request = snapshot.getValue(UserRequest.class);
                request.setUserId(snapshot.getKey());
                request.setGroupName(groupName);
                request.setGroupId(groupID);
                request.setGroupCategory(groupCategory);
                request.setType(groupType);
                // Not even needed, what has the users own groups has to do with anything.
                // Groups group = snapshot.child("groups").getValue(Groups.class);
                // System.out.println("group admin is " + group.getName());
                joinGroup(groupID, groupName, groupCategory, groupAdminId, groupType, groupGender, request);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void newGroupRequest(final String groupCategory, final String groupType, final String groupName, final String groupDescription, final String memberCount) {
        DatabaseReference userRef = databaseRef.child("users").child(mFirebaseUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                // Not even needed, what has the users own groups has to do with anything.
                // Groups group = snapshot.child("groups").getValue(Groups.class);
                // System.out.println("group admin is " + group.getName());
                submitNewGroup(groupCategory, groupType, groupName, groupDescription, user.getGender(), memberCount, user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void joinGroup(final String groupID, String groupName, String groupCategory, final String groupAdminId,
                          String groupType, String groupGender,
                          UserRequest request) {
        //String userid = databaseRef.child("Group").push().getKey();
        final DatabaseReference groupRef = databaseRef.child("group").child(groupCategory).child(groupType).child(groupID);


        // Adds user requested to be joined in admins tree. Delete also needs a rework to remove from userRequest tree.
        groupRef.child("members").child(request.getUserId()).setValue(false);//Adds Members, need approval from admin before true.

        //Adds to users group tree when user has sent join request.
        DatabaseReference usersGroupsTree = databaseRef.child("users").child((request.getUserId())).child("groups").child(request.getGroupId());
        usersGroupsTree.child("name").setValue(request.getGroupName());
        usersGroupsTree.child("category").setValue(request.getGroupCategory());
        usersGroupsTree.child("type").setValue(groupType);
        usersGroupsTree.child("gender").setValue(groupGender);
        usersGroupsTree.child("admin").setValue(false);
        usersGroupsTree.child("adminID").setValue(groupAdminId);
        usersGroupsTree.child("userApproved").setValue(false);

        databaseRef.child("users").child(groupAdminId).child("userRequest").child(groupID).child(request.getUserId()).setValue(request);

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

                        System.out.println("Group count is " + memberCount + " " + mFirebaseUser.getUid());
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

        DatabaseReference groupId2 = databaseRef.child("group").child(groupCategory).child(groupType).push();
        DatabaseReference notifications = databaseRef.child("notifications").child(mFirebaseUser.getUid()).push();
        String groupId = groupId2.getKey();//Stores key in local variable for testing purposes.
        // String notificationId = notifications.getKey();

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
        groupId2.child("gender_memberLimit").setValue(groupGender + "_" + memberLimit);
        groupId2.child("latitude").setValue(user.getLatitude());
        groupId2.child("longitude").setValue(user.getLongitude());


        //Adds to users tree
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupId).child("category").setValue(groupCategory);
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupId).child("type").setValue(groupType);
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupId).child("gender").setValue(groupGender);
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupId).child("name").setValue(groupName.toString());
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupId).child("admin").setValue(true);
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupId).child("adminID").setValue(mFirebaseUser.getUid());
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupId).child("userApproved").setValue(true);
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupId).child("memberCount").setValue(1);
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupId).child("memberLimit").setValue(Integer.parseInt(memberLimit));


        //Add to notifications
        notifications.setValue(new Notification("You created the group " + groupName.toString() + "!"));


        // Amplitude Event Tracking.
        JSONObject jo = new JSONObject();
        try {
            jo.put("Admin ID", mFirebaseUser.getUid());
            jo.put("Group ID", groupId);
            jo.put("Group Name", groupName);
            jo.put("Group Description", groupDescription);
            jo.put("Group Category", groupCategory);
            jo.put("Group Type", groupType);
            jo.put("Member Limit", memberLimit);
            jo.put("Group Gemder", groupGender);
            Amplitude.getInstance().logEvent("Created Group in " + groupCategory, jo);
            Amplitude.getInstance().logEvent("Created Group of type " + groupType, jo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void createChatRoom(String groupId) {
        DatabaseReference chatRooms = databaseRef.child("chatrooms");
        chatRooms.child(groupId).child("admin").setValue(mFirebaseUser.getUid());
    }

    public void deleteAccount() {
        final String userId = mFirebaseUser.getUid();
        final CountDownLatch lock = new CountDownLatch(1);
        Query findGroupsWithUserId = databaseRef.child("users").child(mFirebaseUser.getUid());
        findGroupsWithUserId.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("datasnapshot parent is " + dataSnapshot);
                if (dataSnapshot.hasChild("groups")) {
                    for (DataSnapshot groupsSnapshot : dataSnapshot.child("groups").getChildren()) {
                        Groups group = groupsSnapshot.getValue(Groups.class);
                        String groupId = groupsSnapshot.getKey();
                        System.out.println("events " + groupsSnapshot.getValue());
                        // Delete group the user is admin of.
                        deleteGroups(groupId, group.getCategory(), group.getType(), group.getName(), group.getMemberCount(),
                                group.getMemberLimit(), group.getGender(), group);
                        // Remove user from group where they were a non-admin member.

//                                    eventSnapshot.child("members").child(userId).getRef().removeValue();
//                                    long memberCount = eventSnapshot.child("members").getChildrenCount();
//                                    System.out.println("count is  " + memberCount + " " + eventSnapshot.child("memberCount").getRef());


                    }
                    System.out.println("User is waiting.");
                    lock.countDown();
                }
                try

                {
                    lock.await(2000, TimeUnit.MILLISECONDS);
                    System.out.println("User is countdown.");
//                    databaseRef.child("users").child(mFirebaseUser.getUid()).removeValue();
                    readyToDelete();
                } catch (
                        InterruptedException e)

                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // Need the ability to kick members too
        // Need to delete firebase instance too.
    }

    public void readyToDelete() {
        final ValueEventListener listener = databaseRef.child("users").child(mFirebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot groupSnapshot) {
                if (!groupSnapshot.hasChild("groups")) {
                    System.out.println("User is deleted.");
                    databaseRef.child("users").child(mFirebaseUser.getUid()).removeValue();
//                    databaseRef.child("users").child(mFirebaseUser.getUid()).removeEventListener(listener);
                    System.out.println("group admin snapshot is " + groupSnapshot);
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void checkGroup(final String groupID, final String groupCategory, final String groupType, final String groupName
            , final int groupSize, final int groupLimit, final String groupGender) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(groupID);
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
                    databaseRef.child("group").child(groupCategory).child(groupType).child(groupID).removeValue();

                    //Delete from the users group tree
                    databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupID).removeValue();

                    //Deletes the chatroom for everyone.
                    databaseRef.child("chatrooms").child(groupID).removeValue();

                    // Amplitude Event Tracking.
                    JSONObject jo = new JSONObject();
                    try {
                        jo.put("Group ID", groupID);
                        jo.put("Group Name", groupName);
                        jo.put("Admin ID", mFirebaseUser.getUid());
                        jo.put("Member Count", groupSize);
                        jo.put("Member Limit", groupLimit);
                        jo.put("Group Gemder", groupGender);
                        jo.put("Group Category", groupCategory);
                        jo.put("Group Type", groupType);
                        Amplitude.getInstance().logEvent("Deleted Group in " + groupCategory, jo);
                        Amplitude.getInstance().logEvent("Deleted Group of type " + groupType, jo);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void deleteGroups(final String groupID, final String groupCategory, final String groupType, final String groupName
            , final int groupSize, final int groupLimit, final String groupGender, final Groups group) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(groupID);
        System.out.println("Category is" + groupCategory);

        Query groupMembers = FirebaseDatabase.getInstance().getReference().child("users").orderByChild(groupID);
        groupMembers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (group.getAdmin()) {
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        //System.out.println("Snapshot " + snapshot);
                        if (snapshot.child("groups").hasChild(groupID)) {
                            //Path: users/userid/groups/groupid/lastMessage
                            snapshot.child("groups").child(groupID).getRef().removeValue();
                        }
                    }

                    //Delete from group tree, which contains detail about the name and its members
                    databaseRef.child("group").child(groupCategory).child(groupType).child(groupID).removeValue();

                    //Delete from the users group tree
                    databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupID).removeValue();

                    //Deletes the chatroom for everyone.
                    databaseRef.child("chatrooms").child(groupID).removeValue();
                } else {
                    System.out.println("group id is " + group.getAdminId());

                    databaseRef.child("users").child(group.getAdminId()).child("userRequest").child(groupID).child(mFirebaseUser.getUid()).removeValue();
                    final DatabaseReference groupRef = databaseRef.child("group").child(groupCategory).child(groupType).child(groupID);
                    System.out.println("Category is" + groupCategory);


                    groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            System.out.println("snapshot delete " + dataSnapshot);
                            //Delete member from group tree
                            dataSnapshot.child("members").child(mFirebaseUser.getUid()).getRef().removeValue();

                            //Delete from the users group tree
                            databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupID).removeValue();

                            long memberCount = dataSnapshot.child("members").getChildrenCount();

                            if (memberCount != 1) {
                                memberCount = memberCount - 1;
                            }

                            System.out.println("Member count is after removal " + memberCount);

                            // Updates member count for group for all members.
                            getUserByGroup(group.getAdminId(), groupID, memberCount, dataSnapshot.child("memberLimit").getValue(Long.class));
                            // Updates member count for group.
                            groupRef.child("memberCount").setValue(memberCount);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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
    public void leaveGroup(final String groupID, final String groupAdminId, final String groupCategory, final String groupType) {
        Query query = databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").orderByChild("admin").equalTo(false);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (dataSnapshot.getValue() != null) {
                    final DatabaseReference groupRef = databaseRef.child("group").child(groupCategory).child(groupType).child(groupID);
                    System.out.println("Category is" + groupCategory);


                    groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            System.out.println("snapshot delete " + dataSnapshot);
                            //Delete member from group tree
                            dataSnapshot.child("members").child(mFirebaseUser.getUid()).getRef().removeValue();

                            long memberCount = dataSnapshot.child("members").getChildrenCount();

                            if (memberCount != 1) {
                                memberCount = memberCount - 1;
                            }

                            System.out.println("Member count is after removal " + memberCount);

                            // Updates member count for group for all members.
                            getUserByGroup(groupAdminId, groupID, memberCount, dataSnapshot.child("memberLimit").getValue(Long.class));
                            // Updates member count for group.
                            groupRef.child("memberCount").setValue(memberCount);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                //Delete from the users group tree
                databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupID).removeValue();

                //Change group to not full, so group should reappear again in results
                revertGroupType(groupCategory, groupID, groupType);

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Done by the user who changes their mind about joining a specific group
    public void cancelJoinRequest(final String groupID, final String groupCategory, final String groupAdminId, final String groupType) {
        // Deletes request from admin's user tree
        databaseRef.child("users").child(groupAdminId).child("userRequest").child(groupID).removeValue();
        // Deletes group from the user's user tree
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").child(groupID).removeValue();
        // Delete member from the group tree
        databaseRef.child("group").child(groupCategory).child(groupType).child(groupID).child("members").child(mFirebaseUser.getUid()).removeValue();
        // Decrease member count by 1
        databaseRef.child("group").child(groupCategory).child(groupType).child(groupID).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long memberCount = dataSnapshot.child("members").getChildrenCount();
                        getUserByGroup(groupAdminId, groupID, memberCount, dataSnapshot.child("memberLimit").getValue(Long.class));
                        databaseRef.child("group").child(groupCategory).child(groupType).child(groupID).child("memberCount").setValue(memberCount);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        // Group is no longer full anymore, so revert the type back to normal to reappear in group results.
        revertGroupType(groupCategory, groupID, groupType);
    }

    public void removeUser(String userId, final String groupId, final String groupCategory, final String groupType) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(groupId);
        databaseRef.child("users").child(userId).child("groups").child(groupId).removeValue();
        databaseRef.child("group").child(groupCategory).child(groupType).child(groupId).child("members").child(userId).removeValue();
        // Updates member count
        databaseRef.child("group").child(groupCategory).child(groupType).child(groupId).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long memberCount = dataSnapshot.child("members").getChildrenCount();
                        databaseRef.child("group").child(groupCategory).child(groupType).child(groupId).child("memberCount").setValue(memberCount);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        //Reverts group type from full if already so.
        revertGroupType(groupCategory, groupId, groupType);
    }

    public void revertGroupType(final String groupCategory, final String groupID, final String groupType) {
        // Separate full and type of group, should reappear as avaliable.
        databaseRef.child("group").child(groupCategory).child(groupType).child(groupID).child("type")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String type = dataSnapshot.getValue(String.class);
                        if (type.contains("FULL")) {
                            String[] parts = type.split("FULL_");
                            String groupType = parts[1]; // group type
                            System.out.println("group type is now " + groupType);
                            //Reverts back type to normal
                            databaseRef.child("group").child(groupCategory).child(groupType).child(groupID).child("type").setValue(groupType);

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
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot requestSnapshot : groupSnapshot.getChildren()) {
                        UserRequest request = requestSnapshot.getValue(UserRequest.class);
                        if (request.getGroupId().equals(groupID)) {
//                        System.out.println("Ref is " + request.getGroupId() + " grouo id" + request.getGroupId());
                            System.out.println("Ref is " + requestSnapshot.getRef());
                            requestSnapshot.getRef().removeValue();
                        }
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
