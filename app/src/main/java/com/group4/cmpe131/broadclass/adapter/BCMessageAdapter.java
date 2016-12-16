package com.group4.cmpe131.broadclass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.group4.cmpe131.broadclass.R;
import com.group4.cmpe131.broadclass.model.BCMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BCMessageAdapter extends BaseAdapter {
    final private static int LEFT_BUBBLE_VIEW_TYPE  = 1;
    final private static int RIGHT_BUBBLE_VIEW_TYPE = 2;

    private Context mContext;
    private List<BCMessage> mList;
    private FirebaseUser mUser;

    public BCMessageAdapter(Context context, FirebaseUser user) {
        mContext = context;
        mUser = user;
        mList = new ArrayList<BCMessage>();
    }

    public int getCount() {
        return mList.size();
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater i = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        switch(getItemViewType(position)) {
            case LEFT_BUBBLE_VIEW_TYPE:
                view = i.inflate(R.layout.conversation_left_bubble, parent, false);
                break;

            case RIGHT_BUBBLE_VIEW_TYPE:
                view = i.inflate(R.layout.conversation_right_bubble, parent, false);
                break;
        }

        ((TextView) view.findViewById(R.id.bubble_name)).setText(mList.get(position).getName());
        ((TextView) view.findViewById(R.id.bubble_content)).setText(mList.get(position).getContent());
        ((TextView) view.findViewById(R.id.bubble_time)).setText(mList.get(position).getTimestampString());

        return view;
    }

    public long getItemId(int position) {
        return mList.get(position).getID();
    }

    public BCMessage getItem(int position) {
        return mList.get(position);
    }

    public int getItemViewType(int position) {
        if(mList.get(position).getUID().equals(mUser.getUid())) {
            return RIGHT_BUBBLE_VIEW_TYPE;
        }

        return LEFT_BUBBLE_VIEW_TYPE;
    }

    public boolean hasStableIds() {
        return true;
    }

    public int getItemViewTypeCount() {
        return 2;
    }

    public void add(BCMessage m) {
        m.setID(mList.size());  //Unique ID for the message.
        mList.add(m);
        Collections.sort(mList);
        notifyDataSetChanged();
    }
}
