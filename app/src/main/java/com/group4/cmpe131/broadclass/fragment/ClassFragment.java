package com.group4.cmpe131.broadclass.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group4.cmpe131.broadclass.R;
import com.group4.cmpe131.broadclass.activity.ClassDetailActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassFragment extends Fragment {

    //TODO: Replace this with classes from the database.
    private List<String> classList = new ArrayList<String>();
    private ArrayAdapter<String> classNameAdapter;
    private ListView classListView;

    private FloatingActionButton addClassButton;
    private EditText classTitle, classDescription;

    private DatabaseReference root;

    public ClassFragment() {
        root = FirebaseDatabase.getInstance().getReference().getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View classView = inflater.inflate(R.layout.fragment_class, container, false);

        classNameAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,
                classList);

        classListView = (ListView) classView.findViewById(R.id.class_list_view);
        classListView.setAdapter(classNameAdapter);

        classListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ClassDetailActivity.class);
                intent.putExtra(Intent.EXTRA_TITLE, classNameAdapter.getItem(position));
                intent.putExtra("username", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                startActivity(intent);
            }
        });

        addClassButton = (FloatingActionButton) classView.findViewById(R.id.add_class_fab);

        addClassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestClassDescription();
            }
        });

        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<String>();
                Iterator i = dataSnapshot.getChildren().iterator();

                while(i.hasNext()) {
                    set.add(((DataSnapshot)i.next()).getKey());
                }

                classList.clear();
                classList.addAll(set);

                classNameAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return classView;
    }

    //Dialog to create classroom
    private void requestClassDescription() {
        LayoutInflater layoutInflater = LayoutInflater.from(this.getActivity());
        View promptView = layoutInflater.inflate(R.layout.dialog_register_classroom, null);
        classTitle = (EditText)promptView.findViewById(R.id.class_title);
        classDescription = (EditText)promptView.findViewById(R.id.class_description);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getActivity());
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
                    root.updateChildren(map);

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