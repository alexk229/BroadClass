package com.group4.cmpe131.broadclass.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
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
import com.group4.cmpe131.broadclass.adapter.BCGroupAdapter;
import com.group4.cmpe131.broadclass.app.Config;
import com.group4.cmpe131.broadclass.fragment.ClassFragment;
import com.group4.cmpe131.broadclass.model.BCClassInfo;
import com.group4.cmpe131.broadclass.model.BCGroupInfo;

public class ListGroupsActivity extends AppCompatActivity {
    private BCClassInfo classInfo;

    private FirebaseUser fbUser;
    private DatabaseReference fbRoot;
    private DatabaseReference fbClassGroupList;

    private Context activityContext;

    private ListView groupListView;
    private FloatingActionButton addGroupButton;

    private BCGroupAdapter groupList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Config.appTheme == 1) {
            setTheme(R.style.AppTheme_Light);
        }

        if (Config.appTheme == 2) {
            setTheme(R.style.AppTheme_Dark);
        }

        activityContext = this;

        classInfo = new BCClassInfo();

        Intent i = getIntent();

        classInfo.setClassID(i.getStringExtra(ClassFragment.CID));
        classInfo.setProfessorID(i.getStringExtra(ClassFragment.PID));
        classInfo.setClassName(i.getStringExtra(ClassFragment.CNAME));
        classInfo.setProfessorName(i.getStringExtra(ClassFragment.PNAME));

        groupList = new BCGroupAdapter(activityContext, classInfo);

        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        fbRoot = FirebaseDatabase.getInstance().getReference().getRoot();
        fbClassGroupList = fbRoot.child("Classes").child(classInfo.getClassID()).child("Groups");

        fbClassGroupList.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final BCGroupInfo newGroup = new BCGroupInfo();
                newGroup.setGID(dataSnapshot.getKey());

                //Read name.
                fbRoot.child("Groups").child(newGroup.getGID()).child("Name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            newGroup.setName((String) dataSnapshot.getValue());
                            groupList.add(newGroup);
                        }
                    }

                    @Override public void onCancelled(DatabaseError databaseError) {}
                });
            }

            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for (int j = 0; j < groupList.getCount(); j++) {
                    if(groupList.getItem(j).getGID().equals((String) dataSnapshot.getKey())) {
                        groupList.remove(j);
                        return;
                    }
                }
            }

            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(DatabaseError databaseError) {}
        });

        if(fbUser.getUid().equals(classInfo.getProfessorID())) {
            setContentView(R.layout.activity_list_groups_professor);

            addGroupButton = (FloatingActionButton) findViewById(R.id.add_group_fab);

            addGroupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater i = LayoutInflater.from(activityContext);

                    View dialogView = i.inflate(R.layout.dialog_add_group, null);

                    final EditText groupTitleText = (EditText) dialogView.findViewById(R.id.group_title);

                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activityContext);

                    dialogBuilder.setTitle("Create Group");
                    dialogBuilder.setView(dialogView);

                    dialogBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Create the new group.
                            //Procedure:
                            //  Create chat.
                            //  Add Professor to chat/group members.
                            //  Save chat key.
                            //  Create group.
                            //  Add group to class group list.
                            //  Add group to profesor's group list.
                            String groupName = groupTitleText.getText().toString();

                            DatabaseReference groupChat = fbRoot.child("Chats").push();
                            DatabaseReference group = fbRoot.child("Groups").push();

                            groupChat.child("Group").setValue(group.getKey());
                            groupChat.child("Users").child(fbUser.getUid()).setValue(true);

                            group.child("Chat_Key").setValue(groupChat.getKey());
                            group.child("Name").setValue(groupName);
                            group.child("Class").setValue(classInfo.getClassID());
                            group.child("Users").child(fbUser.getUid()).setValue(true);

                            fbClassGroupList.child(group.getKey()).setValue(true);

                            fbRoot.child("Profiles").child(fbUser.getUid()).child("Groups").child(group.getKey()).setValue(true);
                        }
                    });

                    dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    dialogBuilder.show();
                }
            });
        }

        else {
            setContentView(R.layout.activity_list_groups_student);
        }

        Toolbar listGroupsToolbar = (Toolbar) findViewById(R.id.list_groups_toolbar);
        setSupportActionBar(listGroupsToolbar);

        ActionBar listGroupsAb = getSupportActionBar();
        listGroupsAb.setDisplayHomeAsUpEnabled(true);
        listGroupsAb.setDisplayShowTitleEnabled(false);

        listGroupsToolbar.setTitle("Groups");
        listGroupsToolbar.setSubtitle(classInfo.getClassName());

        groupListView = (ListView) findViewById(R.id.list_groups_lv);
        groupListView.setAdapter(groupList);

        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(activityContext, GroupInfoActivity.class);
                i.putExtra(ClassFragment.CID, classInfo.getClassID());
                i.putExtra(ClassFragment.PID, classInfo.getProfessorID());
                i.putExtra(ClassFragment.CNAME, classInfo.getClassName());
                i.putExtra(ClassFragment.PNAME, classInfo.getProfessorName());
                i.putExtra(GroupInfoActivity.GROUP_ID, groupList.getItem(position).getGID());
                i.putExtra(GroupInfoActivity.GROUP_NAME, groupList.getItem(position).getName());
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem i) {
        switch(i.getItemId()) {
            case android.R.id.home:
                finish();
        }

        return true;
    }
}
