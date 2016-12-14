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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group4.cmpe131.broadclass.R;
import com.group4.cmpe131.broadclass.util.BCClassInfo;
import com.group4.cmpe131.broadclass.util.BCGroupInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BCGroupAdapter extends BaseAdapter {
    private Context mContext;
    private List<BCGroupInfo> mList;
    private BCClassInfo mClassInfo;

    private DatabaseReference fbRoot;
    private DatabaseReference fbClassGroupList;

    public BCGroupAdapter(Context context, BCClassInfo classInfo) {
        mContext = context;
        mList = new ArrayList<BCGroupInfo>();
        mClassInfo = classInfo;

        fbRoot = FirebaseDatabase.getInstance().getReference().getRoot();
        fbClassGroupList = fbRoot.child("Classes").child(mClassInfo.getClassID()).child("Groups");
    }

    public int getCount() {
        return mList.size();
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final LayoutInflater i = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(mClassInfo.getProfessorID())) {
            view = i.inflate(R.layout.professor_group_list_item, parent, false);

            Button removeButton = (Button) view.findViewById(R.id.remove_button);

            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);

                    dialogBuilder.setTitle("Remove " + mList.get(position).getName());
                    dialogBuilder.setMessage("Are you sure you want to remove " + mList.get(position).getName() + "?");
                    dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    dialogBuilder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeGroupFromClass(mList.get(position));
                            dialog.dismiss();
                        }
                    });

                    dialogBuilder.show();
                }
            });
        }

        else {
            view = i.inflate(R.layout.group_list_item, parent, false);
        }

        ((TextView) view.findViewById(R.id.group_list_item_name)).setText(mList.get(position).getName());

        return view;
    }

    /* Removes a group from the class group list and all user group lists and deletes the group data. */
    private void removeGroupFromClass(final BCGroupInfo g) {
        fbClassGroupList.child(g.getGID()).removeValue();

        //Remove group ID from student group lists.
        fbRoot.child("Groups").child(g.getGID()).child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Iterator i = dataSnapshot.getChildren().iterator();

                    while(i.hasNext()) {
                        DataSnapshot s = (DataSnapshot) i.next();

                        fbRoot.child("Profiles").child(s.getKey()).child("Groups").child(g.getGID()).removeValue();
                    }
                }

                //Remove group chat and group metadata.
                fbRoot.child("Groups").child(g.getGID()).child("Chat_Key").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            fbRoot.child("Chats").child((String) dataSnapshot.getValue()).removeValue();
                            fbRoot.child("Groups").child(g.getGID()).removeValue();
                        }
                    }

                    @Override public void onCancelled(DatabaseError databaseError) {}
                });
            }

            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public long getItemId(int position) {
        return mList.get(position).getID();
    }

    public BCGroupInfo getItem(int position) {
        return mList.get(position);
    }

    public void add(BCGroupInfo g) {
        g.setID(mList.size());
        mList.add(g);
        Collections.sort(mList);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        mList.remove(position);
        Collections.sort(mList);
        notifyDataSetChanged();
    }
}
