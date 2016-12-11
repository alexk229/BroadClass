package com.group4.cmpe131.broadclass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.group4.cmpe131.broadclass.util.BCClassInfo;
import com.group4.cmpe131.broadclass.R;

import java.util.List;

public class ClassSearchResultListAdapter extends BaseAdapter {
    private Context mContext;
    private List<BCClassInfo> mSearchResults;

    public ClassSearchResultListAdapter(Context context, List<BCClassInfo> resultList) {
        mContext = context;
        mSearchResults = resultList;
    }

    public int getCount() {
        return mSearchResults.size();
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater i = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = i.inflate(R.layout.class_search_result, parent, false);

        ((TextView) view.findViewById(R.id.class_search_result_title)).setText(mSearchResults.get(position).getClassName());
        ((TextView) view.findViewById(R.id.class_search_result_professor)).setText(mSearchResults.get(position).getProfessorName());

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
}
