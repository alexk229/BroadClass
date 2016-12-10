package com.group4.cmpe131.broadclass.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.group4.cmpe131.broadclass.R;
import com.group4.cmpe131.broadclass.activity.ConversationActivity;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment {

    //TODO: Replace this with groups from the database.
    private List<String> groupNameList = new ArrayList<String>();
    private ArrayAdapter<String> groupNameAdapter;

    public GroupFragment() {
        groupNameList.add("Group A");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View groupView = inflater.inflate(R.layout.fragment_group, container, false);

        groupNameAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,
                groupNameList);

        ListView groupList = (ListView) groupView.findViewById(R.id.group_list);
        groupList.setAdapter(groupNameAdapter);

        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ConversationActivity.class);
                intent.putExtra(Intent.EXTRA_TITLE, groupNameAdapter.getItem(position));
                startActivity(intent);
            }
        });

        return groupView;
    }

}