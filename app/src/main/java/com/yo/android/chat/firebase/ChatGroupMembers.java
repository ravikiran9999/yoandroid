package com.yo.android.chat.firebase;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.yo.android.R;
import com.yo.android.model.GroupMembers;
import com.yo.android.model.RoomInfo;
import com.yo.android.model.UserProfile;
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rdoddapaneni on 5/26/2017.
 */

public class ChatGroupMembers {
    private Context mContext;
    private List<GroupMembers> groupMembersList;
    HashMap<String, GroupMembers> groupMembersHashMap;

    public List<GroupMembers> getGroupMembersList(Context context, Firebase authReference, String roomName, String firebaseRoomId) {
        mContext = context;
        groupMembersList = new ArrayList<>();
        groupMembersHashMap = new HashMap<>();

        Firebase roomInfo = authReference.child(Constants.ROOMS).child(firebaseRoomId).child(Constants.ROOM_INFO);
        roomInfo.keepSynced(true);
        roomInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                new GroupMembersTask().execute(dataSnapshot);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        return groupMembersList;
    }


    private class GroupMembersTask extends AsyncTask<DataSnapshot, Void, List<GroupMembers>> {

        @Override
        protected List<GroupMembers> doInBackground(DataSnapshot... params) {
            /*RoomInfo roomInfo = params[0].getValue(RoomInfo.class);
            if (roomInfo.getName() != null) {
                Firebase membersReference = params[0].getRef().getParent().child(Constants.MEMBERS);
                membersReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnapshotUser : dataSnapshot.getChildren()) {

                            com.yo.android.model.GroupMembers groupMembers = new com.yo.android.model.GroupMembers();
                            groupMembers.setAdmin(dataSnapshotUser.getValue().toString());
                            groupMembers.setUserId(dataSnapshotUser.getKey());

                            groupMembersHashMap.put(dataSnapshotUser.getKey(), groupMembers);

                            Firebase membersProfileReference = dataSnapshot.getRef().getRoot().child(Constants.USERS).child(dataSnapshotUser.getKey()).child(Constants.PROFILE);
                            membersProfileReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                                    String nameFromNumber = mContactsSyncManager.getContactNameByPhoneNumber(userProfile.getPhoneNumber());
                                    if (userProfile != null && !TextUtils.isEmpty(userProfile.getMobileNumber()) && userProfile.getPhoneNumber().equalsIgnoreCase(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER))) {
                                        userProfile.setFullName(mContext.getString(R.string.you));
                                    } else if (userProfile != null && !TextUtils.isEmpty(userProfile.getPhoneNumber())) {
                                        if (!TextUtils.isEmpty(nameFromNumber)) {
                                            userProfile.setFullName(nameFromNumber);
                                        } else {
                                            userProfile.setFullName(userProfile.getMobileNumber());
                                        }
                                    }
                                    for (Map.Entry m : groupMembersHashMap.entrySet()) {
                                        if (dataSnapshot.getRef().getParent().getKey().equals(m.getKey())) {
                                            com.yo.android.model.GroupMembers groupMembers = (com.yo.android.model.GroupMembers) m.getValue();
                                            groupMembers.setUserProfile(userProfile);
                                            groupMembersList.add(groupMembers);
                                        }
                                    }
                                    //loadAlphabetOrder(groupMembersList);
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                    firebaseError.getMessage();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }*/
            return groupMembersList;
        }

        @Override
        protected void onPostExecute(List<GroupMembers> groupMemberses) {
            super.onPostExecute(groupMemberses);
            groupMembersList = groupMemberses;
        }
    }
}
