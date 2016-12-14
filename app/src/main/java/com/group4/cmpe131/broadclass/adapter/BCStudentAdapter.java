package com.group4.cmpe131.broadclass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.group4.cmpe131.broadclass.R;
import com.group4.cmpe131.broadclass.util.BCStudentInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BCStudentAdapter extends BaseAdapter{
    final private static int REGISTERED_VIEW_TYPE = 0;
    final private static int PENDING_VIEW_TYPE = 1;

    private Context mContext;
    private List<BCStudentInfo> mList;
    private String mClassID;

    private DatabaseReference fbRoot;

    public BCStudentAdapter(Context context, String classID) {
        mContext = context;
        mList = new ArrayList<BCStudentInfo>();
        mClassID = classID;

        fbRoot = FirebaseDatabase.getInstance().getReference().getRoot();
    }

    public int getCount() {
        return mList.size();
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater i = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final BCStudentInfo student = mList.get(position);

        switch(getItemViewType(position)) {
            case REGISTERED_VIEW_TYPE:
                //Just the name
                view = i.inflate(R.layout.student_list_item, parent, false);
                break;

            case PENDING_VIEW_TYPE:
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
                        removeFromPending(student);
                        addToRegistered(student);
                    }
                });

                denyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Drop student:
                        //  Remove student from class pending list
                        //  Remove class from student pending list
                        removeFromPending(student);
                    }
                });
                break;
        }

        //Fill in the student's name.
        ((TextView) view.findViewById(R.id.student_list_item_name)).setText(mList.get(position).getName());

        return view;
    }

    private void addToRegistered(BCStudentInfo s) {
        //Add student to class registered list.
        DatabaseReference fbClassRegisteredList = fbRoot.child("Classes").child(mClassID).child("Registered_Students");
        fbClassRegisteredList.child(s.getUID()).setValue(true);

        //Add class to student registered list.
        DatabaseReference fbStudentRegisteredList = fbRoot.child("Profiles").child(s.getUID()).child("Classes");
        fbStudentRegisteredList.child(mClassID).setValue(false);    //False for non-professor. Currently unused.
    }

    private void removeFromPending(BCStudentInfo s) {
        //Remove student from class registered list.
        DatabaseReference fbClassPendingList = fbRoot.child("Classes").child(mClassID).child("Pending_Students");
        fbClassPendingList.child(s.getUID()).removeValue();

        //Remove class from student registered list.
        DatabaseReference fbStudentPendingList = fbRoot.child("Profiles").child(s.getUID()).child("Pending_Classes");
        fbStudentPendingList.child(mClassID).removeValue();
    }

    public long getItemId(int position) {
        return mList.get(position).getID();
    }

    public BCStudentInfo getItem(int position) {
        return mList.get(position);
    }

    public int getItemViewType(int position) {
        if(mList.get(position).isRegistered()) {
            return REGISTERED_VIEW_TYPE;
        }

        return PENDING_VIEW_TYPE;
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
