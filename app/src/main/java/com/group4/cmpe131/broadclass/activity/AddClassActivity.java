package com.group4.cmpe131.broadclass.activity;

import android.content.DialogInterface;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group4.cmpe131.broadclass.R;
import com.group4.cmpe131.broadclass.adapter.ClassSearchResultListAdapter;
import com.group4.cmpe131.broadclass.util.BCClassInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AddClassActivity extends AppCompatActivity {
    FirebaseUser fbUser;
    DatabaseReference firebaseRoot;
    DatabaseReference fbClasses;

    List<BCClassInfo> searchResults;
    ClassSearchResultListAdapter listAdapter;
    ListView resultsView;
    EditText searchBox;

    public AddClassActivity() {
        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseRoot = FirebaseDatabase.getInstance().getReference().getRoot();
        fbClasses = firebaseRoot.child("Classes");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

        Toolbar addClassToolbar = (Toolbar) findViewById(R.id.add_class_toolbar);
        setSupportActionBar(addClassToolbar);

        ActionBar addClassActionBar = getSupportActionBar();

        addClassActionBar.setDisplayHomeAsUpEnabled(true);

        //Initialize search results.
        searchResults = new ArrayList<BCClassInfo>();

        listAdapter = new ClassSearchResultListAdapter(this, searchResults);
        resultsView = (ListView) findViewById(R.id.class_search_results);
        resultsView.setAdapter(listAdapter);

        //Install listener for search button.
        searchBox = (EditText) findViewById(R.id.class_search_text);
        searchBox.setImeActionLabel("Search", KeyEvent.KEYCODE_ENTER);
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent e) {
                if(e.getAction() == KeyEvent.ACTION_UP) {
                    searchResults.clear();

                    final String query = v.getText().toString().toLowerCase();

                    fbClasses.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String previousChild) {
                            final BCClassInfo classInfo = new BCClassInfo();

                            classInfo.setClassID(dataSnapshot.getKey());

                            Iterator i = dataSnapshot.getChildren().iterator();

                            while (i.hasNext()) {
                                DataSnapshot s = (DataSnapshot) i.next();

                                switch (s.getKey()) {
                                    case "Name":
                                        if (query.contains(((String) s.getValue()).toLowerCase()) == false
                                                && ((String) s.getValue()).toLowerCase().contains(query) == false) {
                                            return;
                                        }

                                        classInfo.setClassName((String) s.getValue());
                                        break;

                                    case "Professor":
                                        classInfo.setProfessorID((String) s.getValue());

                                        DatabaseReference fbProfessor = firebaseRoot.child("Profiles")
                                                .child(classInfo.getProfessorID());

                                        fbProfessor.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Iterator i = dataSnapshot.getChildren().iterator();

                                                while (i.hasNext()) {
                                                    DataSnapshot s = (DataSnapshot) i.next();

                                                    if (s.getKey().equals("Name")) {
                                                        classInfo.setProfessorName((String) s.getValue());
                                                        listAdapter.notifyDataSetChanged();
                                                        return;
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                        break;
                                }
                            }

                            listAdapter.add(classInfo);
                            listAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                }

                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_class, m);
        return super.onCreateOptionsMenu(m);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem i) {
        switch(i.getItemId()) {
            case R.id.new_class_button:
                requestClassDescription();
                return true;

            default:
                return super.onOptionsItemSelected(i);
        }
    }

    //Dialog to create classroom
    private void requestClassDescription() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.dialog_register_classroom, null);

        final EditText classTitle = (EditText) promptView.findViewById(R.id.class_title);
        EditText classDescription = (EditText) promptView.findViewById(R.id.class_description);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Class Information");
        alertDialogBuilder.setView(promptView);

        alertDialogBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Empty
            }
        });

        //Cancel classroom creation
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        final AlertDialog alert = alertDialogBuilder.create();
        alert.show();

        //Attempts to create classroom
        alert.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean wantToCloseDialog = false;
                //Attempts to change user email
                if (attemptClassCreation(classTitle)) {
                    wantToCloseDialog = true;
                }

                if (wantToCloseDialog) {
                    //Add class to class list.
                    DatabaseReference classRef = firebaseRoot.child("Classes").push();
                    classRef.child("Name").setValue(classTitle.getText().toString());
                    classRef.child("Professor").setValue(fbUser.getUid());

                    //Add class key to user's class list, with professor flag set to true.
                    DatabaseReference userClassListRef = firebaseRoot.child("Profiles").child(fbUser.getUid()).child("Classes");
                    userClassListRef.child(classRef.getKey()).setValue(true);

                    //else dialog stays open.
                    alert.dismiss();
                }
            }
        });
    }

    //Checks if field are filled
    private boolean attemptClassCreation(EditText mClassTitle) {

        String classTitle = mClassTitle.getText().toString();

        mClassTitle.setError(null);

        boolean cancel = false;
        View focusView = null;

        if(classTitle.isEmpty()) {
            mClassTitle.setError(getString(R.string.error_field_required));
            focusView = mClassTitle;
            cancel = true;
        }

        if(cancel) {
            focusView.requestFocus();
        }

        return !cancel;
    }
}
