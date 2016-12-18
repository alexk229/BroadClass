package com.group4.cmpe131.broadclass.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
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
import com.group4.cmpe131.broadclass.model.BCClassInfo;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AddClassActivity extends AppCompatActivity {
    FirebaseUser fbUser;
    DatabaseReference firebaseRoot;
    DatabaseReference fbClasses;

    private List<BCClassInfo> searchResults;
    private ClassSearchResultListAdapter listAdapter;
    private ListView resultsView;
    private MaterialSearchView searchView;
    private TextView emptyListTextView;

    public AddClassActivity() {
        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseRoot = FirebaseDatabase.getInstance().getReference().getRoot();
        fbClasses = firebaseRoot.child("Classes");
    }

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

        setContentView(R.layout.activity_add_class);

        Toolbar addClassToolbar = (Toolbar) findViewById(R.id.add_class_toolbar);
        setSupportActionBar(addClassToolbar);

        ActionBar addClassActionBar = getSupportActionBar();

        addClassActionBar.setDisplayHomeAsUpEnabled(true);
        addClassActionBar.setDisplayHomeAsUpEnabled(true);

        //Initialize search results.
        searchResults = new ArrayList<BCClassInfo>();

        listAdapter = new ClassSearchResultListAdapter(this, searchResults);
        resultsView = (ListView) findViewById(R.id.class_search_results);
        resultsView.setAdapter(listAdapter);

        emptyListTextView = (TextView) findViewById(R.id.empty_search_text);
        emptyListTextView.setVisibility(View.VISIBLE);

        //Install listener for search button.
        searchView = (MaterialSearchView) findViewById(R.id.class_search_text);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                searchResults.clear();
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
                                            if(dataSnapshot.exists()) {
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

                return true;

            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                emptyListTextView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onSearchViewClosed() {
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_class, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem i) {
        switch(i.getItemId()) {
            case R.id.new_class_button:
                createClassDialog();
                return true;

            default:
                return super.onOptionsItemSelected(i);
        }
    }

    //Dialog to create classroom
    private void createClassDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.dialog_register_classroom, null);

        final EditText classTitle = (EditText) promptView.findViewById(R.id.class_title);
        final EditText classDescription = (EditText) promptView.findViewById(R.id.class_description);

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

                    DatabaseReference classDescriptionRef = firebaseRoot
                            .child("Classes")
                            .child(classRef.getKey())
                            .child("Class_Description");
                    classDescriptionRef.setValue(classDescription.getText().toString());

                    alert.dismiss();

                    startActivity(new Intent(AddClassActivity.this, MainActivity.class));
                    finish();
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
