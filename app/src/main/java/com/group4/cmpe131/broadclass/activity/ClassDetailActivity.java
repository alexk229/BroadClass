package com.group4.cmpe131.broadclass.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.group4.cmpe131.broadclass.R;
import com.group4.cmpe131.broadclass.fragment.ClassFragment;
import com.group4.cmpe131.broadclass.util.BCClassInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ClassDetailActivity extends AppCompatActivity {
    private BCClassInfo classInfo;

    private MenuInflater inflater;

    private ListView announcementList;

    private ArrayAdapter announcementSubjectList;
    private List<String> announcementChatKeyList;

    private FirebaseUser user;
    private FirebaseAuth mFbAuth;
    private DatabaseReference fbRoot;

    private Context activityContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityContext = this;

        mFbAuth = FirebaseAuth.getInstance();
        user = mFbAuth.getCurrentUser();
        fbRoot = FirebaseDatabase.getInstance().getReference().getRoot();

        classInfo = new BCClassInfo();

        Intent intent = getIntent();

        classInfo.setClassID(intent.getStringExtra(ClassFragment.CID));
        classInfo.setProfessorID(intent.getStringExtra(ClassFragment.PID));
        classInfo.setClassName(intent.getStringExtra(ClassFragment.CNAME));
        classInfo.setProfessorName(intent.getStringExtra(ClassFragment.PNAME));

        announcementSubjectList = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        announcementChatKeyList = new ArrayList<String>();

        fbRoot.child("Classes").child(classInfo.getClassID()).child("Announcements").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                announcementSubjectList.add((String) dataSnapshot.child("Subject").getValue());
                announcementChatKeyList.add((String) dataSnapshot.child("Chat_Key").getValue());

                announcementSubjectList.notifyDataSetChanged();
            }

            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(DatabaseError databaseError) {}
        });

        if(user.getUid().equals(classInfo.getProfessorID())) {
            setContentView(R.layout.activity_class_detail_professor);

            FloatingActionButton makeAnnouncementButton = (FloatingActionButton) findViewById(R.id.make_announcement_fab);

            makeAnnouncementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater layoutInflater = LayoutInflater.from(activityContext);
                    View promptView = layoutInflater.inflate(R.layout.dialog_make_announcement, null);

                    final EditText subjectText = (EditText) promptView.findViewById(R.id.announcement_subject);
                    final EditText contentText = (EditText) promptView.findViewById(R.id.announcement_content);

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activityContext);

                    dialogBuilder.setTitle("Make Announcement");
                    dialogBuilder.setView(promptView);

                    dialogBuilder.setPositiveButton("Announce", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String subject = subjectText.getText().toString();
                            String content = contentText.getText().toString();

                            DatabaseReference fbAnnouncement = fbRoot.child("Classes")
                                    .child(classInfo.getClassID()).child("Announcements").push();

                            final DatabaseReference fbChat = fbRoot.child("Chats").push();

                            //Make announcement struct in class announcement list.
                            HashMap<String, Object> announcementData = new HashMap<String, Object>();
                            announcementData.put("Subject", subject);
                            announcementData.put("Chat_Key", fbChat.getKey());

                            fbAnnouncement.updateChildren(announcementData);

                            //Put the Professor in the chat.
                            fbChat.child("Users").child(user.getUid()).setValue(true);

                            DatabaseReference fbContent = fbChat.child("Messages").push();

                            //Make the first message with the content of the announcement.
                            fbContent.child("Content").setValue(content);
                            fbContent.child("UID").setValue(user.getUid());
                            fbContent.child("Timestamp").setValue(ServerValue.TIMESTAMP);

                            //Add all class members to the chat.
                            fbRoot.child("Classes").child(classInfo.getClassID()).child("Registered_Students")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()) {
                                        Iterator i = dataSnapshot.getChildren().iterator();

                                        while(i.hasNext()) {
                                            DataSnapshot s = (DataSnapshot) i.next();

                                            fbChat.child("Users").child(s.getKey()).setValue(true);
                                        }
                                    }
                                }

                                @Override public void onCancelled(DatabaseError databaseError) {}
                            });
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
            setContentView(R.layout.activity_class_detail);
        }

        inflater = getMenuInflater();

        announcementList = (ListView) findViewById(R.id.class_detail_announcements);
        announcementList.setAdapter(announcementSubjectList);

        announcementList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(activityContext, ConversationActivity.class);
                i.putExtra(Intent.EXTRA_TITLE, (String) announcementSubjectList.getItem(position));
                i.putExtra(ConversationActivity.CHAT_ID, announcementChatKeyList.get(position));
                startActivity(i);
            }
        });

        Toolbar classDetailToolbar = (Toolbar) findViewById(R.id.class_detail_toolbar);
        setSupportActionBar(classDetailToolbar);

        //Set up the toolbar.
        ActionBar classDetailAb = getSupportActionBar();
        classDetailAb.setDisplayHomeAsUpEnabled(true);
        classDetailAb.setDisplayShowTitleEnabled(false);

        classDetailToolbar.setTitle(classInfo.getClassName());
        classDetailToolbar.setSubtitle(classInfo.getProfessorName());
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_class_detail, menu);

        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent i;

        switch(id) {
            //Settings menu
            case R.id.action_settings:
                startActivity(new Intent(ClassDetailActivity.this, SettingsActivity.class));
                return true;

            //Student list
            case R.id.action_class_students:
                i = new Intent(this, ListStudentsActivity.class);
                i.putExtra(ClassFragment.CID, classInfo.getClassID());
                i.putExtra(ClassFragment.PID, classInfo.getProfessorID());
                i.putExtra(ClassFragment.CNAME, classInfo.getClassName());
                i.putExtra(ClassFragment.PNAME, classInfo.getProfessorName());
                startActivity(i);
                return true;

            //Class groups
            case R.id.action_class_groups:
                i = new Intent(this, ListGroupsActivity.class);
                i.putExtra(ClassFragment.CID, classInfo.getClassID());
                i.putExtra(ClassFragment.PID, classInfo.getProfessorID());
                i.putExtra(ClassFragment.CNAME, classInfo.getClassName());
                i.putExtra(ClassFragment.PNAME, classInfo.getProfessorName());
                startActivity(i);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
