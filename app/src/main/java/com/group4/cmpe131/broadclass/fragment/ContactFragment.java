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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group4.cmpe131.broadclass.R;
import com.group4.cmpe131.broadclass.activity.ConversationActivity;
import com.group4.cmpe131.broadclass.model.BCContact;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment {
    private FirebaseUser fbUser;
    private DatabaseReference fbRoot;
    private DatabaseReference fbContacts;

    private List<BCContact> contactList = new ArrayList<BCContact>();
    private ArrayAdapter<String> contactNameAdapter;
    private ListView contactListView;

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contactNameAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1);

        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        fbRoot = FirebaseDatabase.getInstance().getReference().getRoot();
        fbContacts = fbRoot.child("Profiles").child(fbUser.getUid()).child("Contacts");

        fbContacts.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addContactToList(dataSnapshot);
            }

            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_contact, container, false);

        contactListView = (ListView) v.findViewById(R.id.contact_list_view);
        contactListView.setAdapter(contactNameAdapter);

        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), ConversationActivity.class);
                i.putExtra(ConversationActivity.CHAT_ID, contactList.get(position).getChatKey());
                i.putExtra(Intent.EXTRA_TITLE, contactList.get(position).getName());
                startActivity(i);
            }
        });

        return v;
    }

    /* Add info from ChildEvent DataSnapshot to new BCContact in contact list.
     * Also updates contact name adapter. */
    private void addContactToList(DataSnapshot dataSnapshot) {
        final BCContact newContact = new BCContact();

        String contactID = dataSnapshot.getKey();
        newContact.setUID(contactID);

        newContact.setChatKey((String) dataSnapshot.getValue());

        DatabaseReference fbContactProfile = fbRoot.child("Profiles").child(contactID);

        fbContactProfile.child("Name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    newContact.setName((String) dataSnapshot.getValue());

                    contactList.add(newContact);
                    contactNameAdapter.add(newContact.getName());
                    contactNameAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}