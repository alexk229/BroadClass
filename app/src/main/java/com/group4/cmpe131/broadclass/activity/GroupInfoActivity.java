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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import com.group4.cmpe131.broadclass.adapter.BCStudentAdapter;
import com.group4.cmpe131.broadclass.fragment.ClassFragment;
import com.group4.cmpe131.broadclass.util.BCClassInfo;
import com.group4.cmpe131.broadclass.util.BCGroupInfo;
import com.group4.cmpe131.broadclass.util.BCStudentInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GroupInfoActivity extends AppCompatActivity {
    final public static String GROUP_ID = "GROUP_ID";
    final public static String GROUP_NAME = "GROUP_NAME";

    private BCClassInfo classInfo;
    private BCGroupInfo groupInfo;

    private FirebaseUser fbUser;
    private DatabaseReference fbRoot;
    private DatabaseReference fbMemberList;

    private Context activityContext;

    private ListView membersListView;
    private FloatingActionButton addMemberButton;

    private BCStudentAdapter memberList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityContext = this;

        classInfo = new BCClassInfo();
        groupInfo = new BCGroupInfo();

        Intent i = getIntent();

        classInfo.setClassID(i.getStringExtra(ClassFragment.CID));
        classInfo.setProfessorID(i.getStringExtra(ClassFragment.PID));
        classInfo.setClassName(i.getStringExtra(ClassFragment.CNAME));
        classInfo.setProfessorName(i.getStringExtra(ClassFragment.PNAME));

        groupInfo.setName(i.getStringExtra(GROUP_NAME));
        groupInfo.setGID(i.getStringExtra(GROUP_ID));

        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        fbRoot = FirebaseDatabase.getInstance().getReference().getRoot();
        fbMemberList = fbRoot.child("Groups").child(groupInfo.getGID()).child("Users");

        memberList = new BCStudentAdapter(activityContext, classInfo, groupInfo);

        fbMemberList.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final BCStudentInfo newMember = new BCStudentInfo();
                newMember.setUID(dataSnapshot.getKey());

                //Read name.
                fbRoot.child("Profiles").child(newMember.getUID()).child("Name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            newMember.setName((String) dataSnapshot.getValue());
                            newMember.setRegistered(true);
                            memberList.add(newMember);
                        }
                    }

                    @Override public void onCancelled(DatabaseError databaseError) {}
                });
            }

            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for (int j = 0; j < memberList.getCount(); j++) {
                    if(memberList.getItem(j).getUID().equals((String) dataSnapshot.getKey())) {
                        memberList.remove(j);
                        return;
                    }
                }
            }

            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(DatabaseError databaseError) {}
        });

        if(fbUser.getUid().equals(classInfo.getProfessorID())) {
            setContentView(R.layout.activity_group_info_professor);

            addMemberButton = (FloatingActionButton) findViewById(R.id.add_member_fab);

            //Procedure:
            //  Get list of all students and store in List<BCStudentInfo>
            //  Pull names out of list and put in to array of strings
            //  Build dialog box using name array
            //  Add user to group/chat and group to user based on which one was chosen
            fbRoot.child("Classes").child(classInfo.getClassID()).child("Registered_Students").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        final List<BCStudentInfo> students = new ArrayList<BCStudentInfo>();

                        //Used to determine when all student names have been read and it's safe to
                        //install the handler for the Add Member button.
                        final long numStudents = dataSnapshot.getChildrenCount();
                        Iterator i = dataSnapshot.getChildren().iterator();

                        while(i.hasNext()) {
                            final DataSnapshot s = (DataSnapshot) i.next();

                            students.add(new BCStudentInfo(s.getKey(), null, true));

                            //Read name.
                            fbRoot.child("Profiles").child(s.getKey()).child("Name").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()) {
                                        for (int j = 0; j < students.size(); j++) {
                                            if (students.get(j).getUID().equals(s.getKey())) {
                                                students.get(j).setName((String) dataSnapshot.getValue());
                                                break;
                                            }
                                        }

                                        if (students.size() == numStudents) {
                                            //Install Add Member button handler.
                                            addMemberButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    List<String> studentNames = new ArrayList<String>();

                                                    for (int j = 0; j < students.size(); j++) {
                                                        studentNames.add(students.get(j).getName());
                                                    }

                                                    String[] studentNameArray = new String[studentNames.size()];

                                                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activityContext);

                                                    dialogBuilder.setTitle("Add Student");
                                                    dialogBuilder.setItems(studentNames.toArray(studentNameArray), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, final int which) {
                                                            fbMemberList.child(students.get(which).getUID()).setValue(true);
                                                            fbRoot.child("Profiles").child(students.get(which).getUID())
                                                                    .child("Groups").child(groupInfo.getGID()).setValue(true);

                                                            //Read chat key.
                                                            fbRoot.child("Groups")
                                                                    .child(groupInfo.getGID())
                                                                    .child("Chat_Key")
                                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    if(dataSnapshot.exists()) {
                                                                        fbRoot.child("Chats")
                                                                                .child((String) dataSnapshot.getValue())
                                                                                .child("Users")
                                                                                .child(students.get(which).getUID())
                                                                                .setValue(true);
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {}
                                                            });

                                                        }
                                                    });

                                                    dialogBuilder.show();
                                                }
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                            });
                        }
                    }
                }

                @Override public void onCancelled(DatabaseError databaseError) {}
            });
        }

        else {
            setContentView(R.layout.activity_group_info_student);
        }

        Toolbar groupInfoToolbar = (Toolbar) findViewById(R.id.group_info_toolbar);
        setSupportActionBar(groupInfoToolbar);

        ActionBar groupInfoAb = getSupportActionBar();
        groupInfoAb.setDisplayHomeAsUpEnabled(true);
        groupInfoAb.setDisplayShowTitleEnabled(false);

        groupInfoToolbar.setTitle(groupInfo.getName());
        groupInfoToolbar.setSubtitle(classInfo.getClassName());

        membersListView = (ListView) findViewById(R.id.group_info_lv);
        membersListView.setAdapter(memberList);

        membersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO: Launch chat.
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
