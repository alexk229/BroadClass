package com.group4.cmpe131.broadclass.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.group4.cmpe131.broadclass.R;
import com.group4.cmpe131.broadclass.util.BCClassInfo;
import com.group4.cmpe131.broadclass.util.BCGroupInfo;
import com.group4.cmpe131.broadclass.util.BCStudentInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BCStudentAdapter extends BaseAdapter{
    final private static int CLASS_REGISTERED_VIEW_TYPE = 0;
    final private static int CLASS_PENDING_VIEW_TYPE = 1;
    final private static int GROUP_PROFESOR_VIEW_TYPE = 2;
    final private static int GROUP_STUDENT_VIEW_TYPE = 3;

    private Context mContext;
    private List<BCStudentInfo> mList;
    private BCClassInfo mClassInfo;
    private BCGroupInfo mGroupInfo;

    private DatabaseReference fbRoot;

    /* This constructor is used when the adapter is to be used for managing students in a class. */
    public BCStudentAdapter(Context context, BCClassInfo classInfo) {
        mContext = context;
        mList = new ArrayList<BCStudentInfo>();
        mClassInfo = classInfo;
        mGroupInfo = null;

        fbRoot = FirebaseDatabase.getInstance().getReference().getRoot();
    }

    /* This constructor is used when the adapter is to be used for managing students in a group. */
    public BCStudentAdapter(Context context, BCClassInfo classInfo, BCGroupInfo groupInfo) {
        mContext = context;
        mList = new ArrayList<BCStudentInfo>();
        mClassInfo = classInfo;
        mGroupInfo = groupInfo;

        fbRoot = FirebaseDatabase.getInstance().getReference().getRoot();
    }

    public int getCount() {
        return mList.size();
    }

    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater i = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final BCStudentInfo student = mList.get(position);

        if(mGroupInfo == null) {
            //Create view for class management list.
            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(mClassInfo.getProfessorID())) {
                //Professor view
                switch (getItemViewType(position)) {
                    case CLASS_REGISTERED_VIEW_TYPE:
                        //Name and remove button
                        view = i.inflate(R.layout.professor_student_list_item, parent, false);

                        Button removeButton = (Button) view.findViewById(R.id.remove_button);

                        removeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);

                                dialogBuilder.setTitle("Remove Student");
                                dialogBuilder.setMessage("Are you sure you want to remove " + mList.get(position).getName() + " from the class?");
                                dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                                dialogBuilder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        class_removeFromRegistered(mList.get(position));
                                        dialog.dismiss();
                                    }
                                });

                                dialogBuilder.show();
                            }
                        });
                        break;

                    case CLASS_PENDING_VIEW_TYPE:
                        //Name and add/deny buttons
                        view = i.inflate(R.layout.pending_student_list_item, parent, false);

                        Button addButton = (Button) view.findViewById(R.id.add_button);
                        Button denyButton = (Button) view.findViewById(R.id.deny_button);

                        addButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Add student to class:
                                //  Remove student from class pending list
                                //  Remove class from student pending list
                                //  Add student to class registered list
                                //  Add class to student registered list
                                class_removeFromPending(student);
                                class_addToRegistered(student);
                            }
                        });

                        denyButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Drop student:
                                //  Remove student from class pending list
                                //  Remove class from student pending list
                                class_removeFromPending(student);
                            }
                        });
                        break;
                }
            } else {
                //Student view
                view = i.inflate(R.layout.student_list_item, parent, false);
            }

            //Fill in the student's name.
            ((TextView) view.findViewById(R.id.student_list_item_name)).setText(mList.get(position).getName());
        }

        else {
            //Create view for group management list.
            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(mClassInfo.getProfessorID())) {
                //Professor view
                view = i.inflate(R.layout.professor_group_list_item, parent, false);

                Button removeButton = (Button) view.findViewById(R.id.remove_button);

                removeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);

                        dialogBuilder.setTitle("Remove Student");
                        dialogBuilder.setMessage("Are you sure you want to remove " + mList.get(position).getName() + " from the group?");
                        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        dialogBuilder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                group_removeStudent(mList.get(position));
                                dialog.dismiss();
                            }
                        });

                        dialogBuilder.show();
                    }
                });
            }

            else {
                //Student view
                view = i.inflate(R.layout.student_list_item, parent, false);
            }

            //Fill in the student's name.
            ((TextView) view.findViewById(R.id.group_list_item_name)).setText(mList.get(position).getName());
        }

        return view;
    }

    private void class_addToRegistered(BCStudentInfo s) {
        //Add student to class registered list.
        DatabaseReference fbClassRegisteredList = fbRoot.child("Classes").child(mClassInfo.getClassID()).child("Registered_Students");
        fbClassRegisteredList.child(s.getUID()).setValue(true);

        //Add class to student registered list.
        DatabaseReference fbStudentRegisteredList = fbRoot.child("Profiles").child(s.getUID()).child("Classes");
        fbStudentRegisteredList.child(mClassInfo.getClassID()).setValue(false);    //False for non-professor. Currently unused.
    }

    private void class_removeFromPending(BCStudentInfo s) {
        //Remove student from class pending list.
        DatabaseReference fbClassPendingList = fbRoot.child("Classes").child(mClassInfo.getClassID()).child("Pending_Students");
        fbClassPendingList.child(s.getUID()).removeValue();

        //Remove class from student pending list.
        DatabaseReference fbStudentPendingList = fbRoot.child("Profiles").child(s.getUID()).child("Pending_Classes");
        fbStudentPendingList.child(mClassInfo.getClassID()).removeValue();
    }

    private void class_removeFromRegistered(BCStudentInfo s) {
        //Remove student from class registered list.
        DatabaseReference fbClassPendingList = fbRoot.child("Classes").child(mClassInfo.getClassID()).child("Registered_Students");
        fbClassPendingList.child(s.getUID()).removeValue();

        //Remove class from student registered list.
        DatabaseReference fbStudentPendingList = fbRoot.child("Profiles").child(s.getUID()).child("Registered_Classes");
        fbStudentPendingList.child(mClassInfo.getClassID()).removeValue();
    }

    private void group_removeStudent(BCStudentInfo s) {
        fbRoot.child("Groups").child(mGroupInfo.getGID()).child("Users").child(s.getUID()).removeValue();
        fbRoot.child("Profiles").child(s.getUID()).child("Groups").child(mGroupInfo.getGID()).removeValue();
    }

    public long getItemId(int position) {
        return mList.get(position).getID();
    }

    public BCStudentInfo getItem(int position) {
        return mList.get(position);
    }

    public int getItemViewType(int position) {
        if(mGroupInfo == null) {
            if (mList.get(position).isRegistered()) {
                return CLASS_REGISTERED_VIEW_TYPE;
            }

            return CLASS_PENDING_VIEW_TYPE;
        }

        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(mClassInfo.getProfessorID())) {
            return GROUP_PROFESOR_VIEW_TYPE;
        }

        return GROUP_STUDENT_VIEW_TYPE;
    }

    public int getItemViewTypeCount() {
        return 2;
    }

    public void add(BCStudentInfo s) {
        s.setID(mList.size());
        mList.add(s);
        Collections.sort(mList);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        mList.remove(position);
        Collections.sort(mList);
        notifyDataSetChanged();
    }
}
