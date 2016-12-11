package com.group4.cmpe131.broadclass.activity;

import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.group4.cmpe131.broadclass.R;
import com.group4.cmpe131.broadclass.adapter.ClassSearchResultListAdapter;
import com.group4.cmpe131.broadclass.util.BCClassInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddClassActivity extends AppCompatActivity {
    DatabaseReference firebaseRoot;
    ClassSearchResultListAdapter listAdapter;

    public AddClassActivity() {
        firebaseRoot = FirebaseDatabase.getInstance().getReference().getRoot();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

        Toolbar addClassToolbar = (Toolbar) findViewById(R.id.add_class_toolbar);
        setSupportActionBar(addClassToolbar);

        ActionBar addClassActionBar = getSupportActionBar();

        addClassActionBar.setDisplayHomeAsUpEnabled(true);

        //Fake results.
        List<BCClassInfo> results = new ArrayList<BCClassInfo>();
        /*results.add(new BCClassInfo("CMPE 131", "Badari Eswar"));
        results.add(new BCClassInfo("CMPE 124", "Haluk Ozemek"));
        results.add(new BCClassInfo("ME 109", "Asdfghjkl"));*/

        listAdapter = new ClassSearchResultListAdapter(this, results);
        ListView resultsView = (ListView) findViewById(R.id.class_search_results);
        resultsView.setAdapter(listAdapter);
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
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(classTitle.getText().toString(), "");
                    firebaseRoot.updateChildren(map);

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
