package com.group4.cmpe131.broadclass.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group4.cmpe131.broadclass.R;
import com.group4.cmpe131.broadclass.activity.ConversationActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GroupFragment extends Fragment {
    private List<String> groupNameList = new ArrayList<String>();
    private ArrayAdapter<String> groupNameAdapter;
    private List<String> groupChatKeyList = new ArrayList<String>();

    private FirebaseUser fbUser;
    private DatabaseReference fbRoot;
    private DatabaseReference fbGroupsRef;

    public GroupFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        groupNameAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,
                groupNameList);

        fbUser = FirebaseAuth.getInstance().getCurrentUser();

        fbRoot = FirebaseDatabase.getInstance().getReference().getRoot();

        fbGroupsRef = fbRoot.child("Profiles").child(fbUser.getUid()).child("Groups");

        fbGroupsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String groupKey = dataSnapshot.getKey();

                DatabaseReference groupRef = fbRoot.child("Groups").child(groupKey);

                groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            Iterator i = dataSnapshot.getChildren().iterator();

                            while (i.hasNext()) {
                                DataSnapshot s = (DataSnapshot) i.next();

                                if (s.getKey().equals("Name")) {
                                    groupNameAdapter.add((String) s.getValue());
                                    groupNameAdapter.notifyDataSetChanged();
                                }

                                else if(s.getKey().equals("Chat_Key")) {
                                    groupChatKeyList.add((String) s.getValue());
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String groupKey = dataSnapshot.getKey();

                DatabaseReference groupRef = fbRoot.child("Groups").child(groupKey);

                groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            Iterator i = dataSnapshot.getChildren().iterator();

                            while (i.hasNext()) {
                                DataSnapshot s = (DataSnapshot) i.next();

                                if (s.getKey().equals("Name")) {
                                    groupNameAdapter.remove((String) s.getValue());
                                    groupNameAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View groupView = inflater.inflate(R.layout.fragment_group, container, false);

        ListView groupList = (ListView) groupView.findViewById(R.id.group_list);
        groupList.setAdapter(groupNameAdapter);

        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), ConversationActivity.class);
                i.putExtra(Intent.EXTRA_TITLE, groupNameList.get(position));
                i.putExtra(ConversationActivity.CHAT_ID, groupChatKeyList.get(position));
                startActivity(i);
            }
        });

        return groupView;
    }


}