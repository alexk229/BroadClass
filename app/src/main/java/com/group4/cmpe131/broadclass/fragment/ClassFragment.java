package com.group4.cmpe131.broadclass.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.group4.cmpe131.broadclass.activity.AddClassActivity;
import com.group4.cmpe131.broadclass.activity.ClassDetailActivity;
import com.group4.cmpe131.broadclass.adapter.ClassListItemAdapter;
import com.group4.cmpe131.broadclass.model.BCClassInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClassFragment extends Fragment {
    private List<BCClassInfo> mClassList = new ArrayList<BCClassInfo>();
    private ClassListItemAdapter classAdapter;
    private ListView classListView;

    private FloatingActionButton addClassButton;

    private FirebaseAuth mFbAuth;

    private DatabaseReference mFbRoot;
    private DatabaseReference mFbProfile;

    final public static String CID = "ClassID";
    final public static String CNAME = "ClassName";
    final public static String PID = "ProfessorID";
    final public static String PNAME = "ProfessorName";

    public ClassFragment() {
        mFbAuth = FirebaseAuth.getInstance();

        mFbRoot = FirebaseDatabase.getInstance().getReference().getRoot();

        FirebaseUser user = mFbAuth.getCurrentUser();
        mFbProfile = mFbRoot.child("Profiles").child(user.getUid());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        classAdapter = new ClassListItemAdapter(getActivity(), mClassList);

        //Listen for new classes to be added.
        mFbProfile.child("Classes").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addClassSnapshotToList(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String classKey = dataSnapshot.getKey();

                classAdapter.remove(classKey);
                classAdapter.notifyDataSetChanged();
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
        View classView = inflater.inflate(R.layout.fragment_class, container, false);

        classListView = (ListView) classView.findViewById(R.id.class_list_view);
        classListView.setAdapter(classAdapter);

        classListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ClassDetailActivity.class);

                //Pass the Class ID to the detail activity.
                intent.putExtra(CID, ((BCClassInfo) classAdapter.getItem(position)).getClassID());
                intent.putExtra(CNAME, ((BCClassInfo) classAdapter.getItem(position)).getClassName());
                intent.putExtra(PID, ((BCClassInfo) classAdapter.getItem(position)).getProfessorID());
                intent.putExtra(PNAME, ((BCClassInfo) classAdapter.getItem(position)).getProfessorName());

                startActivity(intent);
            }
        });

        classListView.setFastScrollEnabled(true);

        addClassButton = (FloatingActionButton) classView.findViewById(R.id.add_class_fab);

        addClassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), AddClassActivity.class);
                startActivity(i);
            }
        });

        return classView;
    }

    /* Add a data snapshot pointing to a class ID from Firebase to the class list. */
    private void addClassSnapshotToList(DataSnapshot classIdSnapshot) {
        final BCClassInfo classInfo = new BCClassInfo();

        final String classId = classIdSnapshot.getKey();

        classInfo.setClassID(classId);

        final DatabaseReference classReference = mFbRoot.child("Classes").child(classId);

        classReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot classSnapshot) {
                    if(classSnapshot.exists()) {
                        Iterator i = classSnapshot.getChildren().iterator();

                        while (i.hasNext()) {
                            DataSnapshot s = (DataSnapshot) i.next();

                            switch (s.getKey()) {
                                case "Name":
                                    classInfo.setClassName((String) s.getValue());
                                    break;

                                case "Professor":
                                    classInfo.setProfessorID((String) s.getValue());
                                    break;
                            }
                        }

                        final DatabaseReference professorReference = mFbRoot.child("Profiles").child(classInfo.getProfessorID());

                        professorReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot professorSnapshot) {
                                if(professorSnapshot.exists()) {
                                    Iterator i = professorSnapshot.getChildren().iterator();

                                    while (i.hasNext()) {
                                        DataSnapshot s = (DataSnapshot) i.next();

                                        if (s.getKey().equals("Name")) {
                                            classInfo.setProfessorName((String) s.getValue());
                                            classAdapter.add(classInfo);
                                            classAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}