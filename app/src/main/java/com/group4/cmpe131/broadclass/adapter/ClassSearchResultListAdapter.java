package com.group4.cmpe131.broadclass.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group4.cmpe131.broadclass.R;
import com.group4.cmpe131.broadclass.activity.AddClassActivity;
import com.group4.cmpe131.broadclass.model.BCClassInfo;

import java.util.Iterator;
import java.util.List;

public class ClassSearchResultListAdapter extends BaseAdapter {
    private Context mContext;
    private List<BCClassInfo> mSearchResults;
    private String classDescription;

    private FirebaseUser user;
    private DatabaseReference fbRoot;
    private DatabaseReference fbPendingClasses;

    public ClassSearchResultListAdapter(Context context, List<BCClassInfo> resultList) {
        mContext = context;
        mSearchResults = resultList;

        user = FirebaseAuth.getInstance().getCurrentUser();
        fbRoot = FirebaseDatabase.getInstance().getReference().getRoot();
        fbPendingClasses = fbRoot.child("Profiles").child(user.getUid()).child("Pending_Classes");
    }

    public int getCount() {
        return mSearchResults.size();
    }

    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater i = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = i.inflate(R.layout.class_list_item, parent, false);

        //Turn the item view into a ViewGroup so that the join button can be added dynamically.
        final ViewGroup viewGroup = (ViewGroup) view;

        ((TextView) view.findViewById(R.id.class_list_item_title)).setText(mSearchResults.get(position).getClassName());
        ((TextView) view.findViewById(R.id.class_list_item_professor)).setText(mSearchResults.get(position).getProfessorName());

        DatabaseReference fbRegisteredClasses = fbRoot.child("Profiles").child(user.getUid()).child("Classes");

        fbRegisteredClasses.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();

                while(i.hasNext()) {
                    DataSnapshot s = (DataSnapshot) i.next();

                    if(s.getKey().equals(mSearchResults.get(position).getClassID())) {
                        return;
                    }
                }

                fbPendingClasses.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterator i = dataSnapshot.getChildren().iterator();

                        while(i.hasNext()) {
                            DataSnapshot s = (DataSnapshot) i.next();

                            if(s.getKey().equals(mSearchResults.get(position).getClassID())) {
                                return;
                            }
                        }

                        Button joinButton = new Button(mContext);
                        joinButton.setText(R.string.join);

                        viewGroup.addView(joinButton);

                        RelativeLayout.LayoutParams joinButtonParams = (RelativeLayout.LayoutParams) joinButton.getLayoutParams();
                        joinButtonParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                        joinButtonParams.addRule(RelativeLayout.CENTER_VERTICAL);
                        joinButton.setLayoutParams(joinButtonParams);

                        joinButton.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
                        joinButton.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);

                        joinButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showClassDescription(position);

                            }
                        });
                    }

                    @Override public void onCancelled(DatabaseError databaseError) {}
                });
            }

            @Override public void onCancelled(DatabaseError databaseError) {}
        });

        return view;
    }

    public long getItemId(int position) {
        return position;
    }

    public Object getItem(int position) {
        return mSearchResults.get(position);
    }

    public void add(BCClassInfo classInfo) {
        mSearchResults.add(classInfo);
    }

    public void clear() {
        mSearchResults.clear();
    }

    //Shows class description dialog
    private void showClassDescription(final int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View promptView = layoutInflater.inflate(R.layout.dialog_class_description, null);
        final AlertDialog.Builder alertDialog= new AlertDialog.Builder(mContext);

        TextView classDescriptionTextView = (TextView) promptView.findViewById(R.id.class_description_dialog);
        TextView classProfessorTextView = (TextView) promptView.findViewById(R.id.class_professor_dialog);

        //Gets class description from database
        DatabaseReference fbClassDescriptionRef = fbRoot
                .child("Classes")
                .child(mSearchResults.get(position).getClassID())
                .child("Class_Description");

        fbClassDescriptionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                    classDescription = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        classProfessorTextView.setText(mSearchResults.get(position).getProfessorName());
        classDescriptionTextView.setText(classDescription);

        alertDialog.setView(promptView);

        alertDialog.setTitle(mSearchResults.get(position).getClassName());
        alertDialog.setPositiveButton("REQUEST", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int i) {
                DatabaseReference fbPendingStudents = fbRoot
                        .child("Classes")
                        .child(mSearchResults.get(position).getClassID())
                        .child("Pending_Students");

                //Add the student to the pending students list for the class.
                fbPendingStudents.child(user.getUid()).setValue(true);

                //Add the class to the pending classes list for the student.
                fbPendingClasses.child(mSearchResults.get(position).getClassID()).setValue(true);

                //Make a toast indicating that the join request was placed.
                Toast.makeText(mContext, "Asked to join \"" + mSearchResults.get(position).getClassName() + ".\"", Toast.LENGTH_LONG).show();

                //Close the search panel.
                ((AddClassActivity) mContext).finish();
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int i) {

            }
        });

        alertDialog.show();
    }
}
