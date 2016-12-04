package com.group4.cmpe131.broadclass.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.group4.cmpe131.broadclass.R;

import java.util.ArrayList;
import java.util.List;

public class ClassFragment extends Fragment {

    public ClassFragment() {
        // Required empty public constructor
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

        //TODO: Replace this with classes from database.
        List<String> classNameArray = new ArrayList<String>();
        classNameArray.add("CMPE 124");
        classNameArray.add("CMPE 131");

        ArrayAdapter<String> classNameAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, classNameArray);

        ListView classList = (ListView) classView.findViewById(R.id.class_list);
        classList.setAdapter(classNameAdapter);

        return classView;
    }

}