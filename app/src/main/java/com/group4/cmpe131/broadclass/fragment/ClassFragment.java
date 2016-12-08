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
import com.group4.cmpe131.broadclass.activity.ClassDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class ClassFragment extends Fragment {

    //TODO: Replace this with classes from the database.
    List<String> classNameArray = new ArrayList<String>();
    ArrayAdapter<String> classNameAdapter;

    public ClassFragment() {
        classNameArray.add("CMPE 124");
        classNameArray.add("CMPE 131");
        classNameArray.add("ME 109");
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
                classNameArray);

        ListView classList = (ListView) classView.findViewById(R.id.class_list);
        classList.setAdapter(classNameAdapter);

        classList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ClassDetailActivity.class);
                intent.putExtra(Intent.EXTRA_TITLE, classNameAdapter.getItem(position));
                startActivity(intent);
            }
        });

        return classView;
    }
}