package com.group4.cmpe131.broadclass.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.group4.cmpe131.broadclass.model.BCClassInfo;
import com.group4.cmpe131.broadclass.model.BCStudentInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ListStudentsActivity extends AppCompatActivity {
    private BCClassInfo classInfo;

    private FirebaseUser fbUser;
    private DatabaseReference fbRoot;
    private DatabaseReference fbRegisteredStudents;
    private DatabaseReference fbPendingStudents;

    private BCStudentAdapter studentList;

    private ListView studentListView;

    private Context activityContext;    //For starting a conversation via Intent.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences getData = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String themeValues = getData.getString("theme", "dark");

        if (themeValues.equals("light")) {
            setTheme(R.style.AppTheme_Light);
        }

        if (themeValues.equals("dark")) {
            setTheme(R.style.AppTheme_Dark);
        }

        setContentView(R.layout.activity_list_students);
        activityContext = this;

        classInfo = new BCClassInfo();

        Intent i = getIntent();

        classInfo.setClassID(i.getStringExtra(ClassFragment.CID));
        classInfo.setProfessorID(i.getStringExtra(ClassFragment.PID));
        classInfo.setClassName(i.getStringExtra(ClassFragment.CNAME));
        classInfo.setProfessorName(i.getStringExtra(ClassFragment.PNAME));

        Toolbar listStudentsToolbar = (Toolbar) findViewById(R.id.list_students_toolbar);
        setSupportActionBar(listStudentsToolbar);

        ActionBar listStudentsAb = getSupportActionBar();
        listStudentsAb.setDisplayHomeAsUpEnabled(true);
        listStudentsAb.setDisplayShowTitleEnabled(false);

        listStudentsToolbar.setTitle("Students");
        listStudentsToolbar.setSubtitle(classInfo.getClassName());

        studentList = new BCStudentAdapter(this, classInfo);

        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        fbRoot = FirebaseDatabase.getInstance().getReference().getRoot();
        fbRegisteredStudents = fbRoot.child("Classes").child(classInfo.getClassID()).child("Registered_Students");
        fbPendingStudents = fbRoot.child("Classes").child(classInfo.getClassID()).child("Pending_Students");

        if(classInfo.getProfessorID().equals(fbUser.getUid())) {
            //Set up listener for pending students.
            fbPendingStudents.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    //Add student with UID, name, and isRegistered as false.
                    final BCStudentInfo newStudent = new BCStudentInfo();

                    newStudent.setUID(dataSnapshot.getKey());
                    newStudent.setRegistered(false);

                    //Read name.
                    fbRoot.child("Profiles").child(newStudent.getUID()).child("Name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                newStudent.setName((String) dataSnapshot.getValue());
                                studentList.add(newStudent);
                            }
                        }

                        @Override public void onCancelled(DatabaseError databaseError) {}
                    });
                }

                @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

                @Override public void onChildRemoved(DataSnapshot dataSnapshot) {
                    //Remove student from list where UID matches and isRegistered is false.
                    for (int j = 0; j < studentList.getCount(); j++) {
                        if(studentList.getItem(j).getUID().equals(dataSnapshot.getKey()) && studentList.getItem(j).isRegistered() == false) {
                            studentList.remove(j);
                            return;
                        }
                    }
                }

                @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                @Override public void onCancelled(DatabaseError databaseError) {}
            });
        }

        //Set up listener for registered students.
        fbRegisteredStudents.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Add student with UID, name, and isRegistered as true.
                final BCStudentInfo newStudent = new BCStudentInfo();

                newStudent.setUID(dataSnapshot.getKey());
                newStudent.setRegistered(true);

                //Read name.
                fbRoot.child("Profiles").child(newStudent.getUID()).child("Name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            newStudent.setName((String) dataSnapshot.getValue());
                            studentList.add(newStudent);
                        }
                    }

                    @Override public void onCancelled(DatabaseError databaseError) {}
                });
            }

            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //Remove student from list where UID matches and isRegistered is true.
                for (int j = 0; j < studentList.getCount(); j++) {
                    if(studentList.getItem(j).getUID().equals(dataSnapshot.getKey()) && studentList.getItem(j).isRegistered()) {
                        studentList.remove(j);
                        return;
                    }
                }
            }

            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(DatabaseError databaseError) {}
        });

        studentListView = (ListView) findViewById(R.id.list_students_lv);
        studentListView.setAdapter(studentList);

        studentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final String studentUID = studentList.getItem(position).getUID();

                //Search for student in user's contacts.
                showDialogOptions(studentUID, position);
            }
        });
    }


    //Shows a options dialog when selecting a user
    private void showDialogOptions(final String studentUID, final int position) {
        final List<String> mOptionsList = new ArrayList<String>();
        if(!fbUser.getUid().equals(studentUID)) {
            mOptionsList.add("Message");
        }
        mOptionsList.add("View Profile");
        final CharSequence[] mOptions = mOptionsList.toArray(new String[mOptionsList.size()]);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setItems(mOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String option = mOptionsList.get(i);

                switch (option) {
                    case "Message":
                        startConversation(studentUID, position);
                        break;
                    case "View Profile":
                        viewProfile(studentUID);
                        break;
                    default:
                        break;
                    }
            }
        });
        AlertDialog alertDialogObject = alertDialog.create();
        alertDialogObject.show();
    }

    private void startConversation(final String studentUID, final int position) {
        fbRoot.child("Profiles").child(fbUser.getUid()).child("Contacts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Intent intent = new Intent(activityContext, ConversationActivity.class);
                intent.putExtra(Intent.EXTRA_TITLE, studentList.getItem(position).getName());
                intent.putExtra(ConversationActivity.RECIPIENT_USER_ID, studentList.getItem(position).getUID());

                if (dataSnapshot.exists()) {
                    Iterator i = dataSnapshot.getChildren().iterator();

                    while (i.hasNext()) {
                        DataSnapshot s = (DataSnapshot) i.next();

                        if (s.getKey().equals(studentUID)) {
                            intent.putExtra(ConversationActivity.CHAT_ID, (String) s.getValue());
                            break;
                        }
                    }
                }

                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void viewProfile(final String studentUID) {
        Intent intent = new Intent(activityContext, UserProfileActivity.class);
        intent.putExtra("UserID", studentUID);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem i) {
        switch(i.getItemId()) {
            case android.R.id.home:
                //This prevents the class detail activity from restarting and appearing blank
                //upon hitting the Up button.
                finish();
        }

        return true;
    }
}
