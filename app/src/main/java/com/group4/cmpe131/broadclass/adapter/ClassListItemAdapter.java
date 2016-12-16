package com.group4.cmpe131.broadclass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.group4.cmpe131.broadclass.R;
import com.group4.cmpe131.broadclass.model.BCClassInfo;

import java.util.List;

public class ClassListItemAdapter extends BaseAdapter {
    private Context mContext;
    private List<BCClassInfo> mList;

    public ClassListItemAdapter(Context context, List<BCClassInfo> list) {
        mContext = context;
        mList = list;
    }

    public int getCount() {
        return mList.size();
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater i = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = i.inflate(R.layout.class_list_item, parent, false);

        ((TextView) view.findViewById(R.id.class_list_item_title)).setText(mList.get(position).getClassName());
        ((TextView) view.findViewById(R.id.class_list_item_professor)).setText(mList.get(position).getProfessorName());

        return view;
    }

    public long getItemId(int position) {
        return position;
    }

    public Object getItem(int position) {
        return mList.get(position);
    }

    public void add(BCClassInfo classInfo) {
        mList.add(classInfo);
    }

    public void remove(String classKey) {
        for(int i = 0; i < getCount(); i++) {
            if(mList.get(i).getClassID().equals(classKey)) {
                mList.remove(i);
            }
        }
    }
}
