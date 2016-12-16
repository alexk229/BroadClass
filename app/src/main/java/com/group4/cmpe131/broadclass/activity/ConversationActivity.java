package com.group4.cmpe131.broadclass.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.group4.cmpe131.broadclass.R;
import com.group4.cmpe131.broadclass.adapter.BCMessageAdapter;
import com.group4.cmpe131.broadclass.app.Config;
import com.group4.cmpe131.broadclass.fragment.ClassFragment;
import com.group4.cmpe131.broadclass.model.BCClassInfo;
import com.group4.cmpe131.broadclass.model.BCContact;
import com.group4.cmpe131.broadclass.model.BCMessage;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationActivity extends AppCompatActivity {
    final public static String CHAT_ID = "CHAT_ID";
    final public static String RECIPIENT_USER_ID = "USER_ID";

    //Firebase variables and references.
    private FirebaseUser fbUser;
    private DatabaseReference fbRoot;
    private DatabaseReference fbMessageList;
    private DatabaseReference fbMemberList;

    //Chat metadata.
    private String chatId;
    private String groupId;
    private String recipientUID;
    private String chatroomTitle;
    private List<BCContact> chatMemebers = new ArrayList<BCContact>();

    //Message list adapter.
    private BCMessageAdapter messageListAdapter;

    //Messaging inputs.
    private CircleImageView sendMsgButton;
    private EditText inputMsgText;

    private Map<String, Object> userMsgMap;

    //Layout variables.
    private ListView messageListView;

    private Context activityContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Config.appTheme == 1) {
            setTheme(R.style.AppTheme_Light);
        }

        if (Config.appTheme == 2) {
            setTheme(R.style.AppTheme_Dark);
        }

        setContentView(R.layout.activity_conversation);

        activityContext = this;

        Toolbar conversationToolbar = (Toolbar) findViewById(R.id.conversation_toolbar);
        setSupportActionBar(conversationToolbar);

        //Set up the toolbar.
        ActionBar conversationAb = getSupportActionBar();
        conversationAb.setDisplayHomeAsUpEnabled(true);
        conversationAb.setDisplayShowTitleEnabled(false);

        chatroomTitle = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        chatId = getIntent().getStringExtra(CHAT_ID);
        recipientUID = getIntent().getStringExtra(RECIPIENT_USER_ID);

        conversationToolbar.setTitle(chatroomTitle);

        sendMsgButton = (CircleImageView) findViewById(R.id.send_msg_button);
        inputMsgText = (EditText) findViewById(R.id.input_msg_text);

        //Set up Firebase stuff.
        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        fbRoot = FirebaseDatabase.getInstance().getReference().getRoot();

        //Init message adapter.
        messageListAdapter = new BCMessageAdapter(this, fbUser);

        //Save layout variables.
        messageListView = (ListView) findViewById(R.id.message_list);

        if(chatId != null) {
            initUserAndMessageHandlers();
        }

        //TODO: Fix this.
        //conversationFooter.requestFocus();

        userMsgMap = new HashMap<String, Object>();

        //Sets send message button action
        sendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                inputMsgText.setText(null);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        //Check if the conversation is associated with a group. Inflate the menu if it is.
        if(chatId != null) {
            fbRoot.child("Chats").child(chatId).child("Group").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        groupId = (String) dataSnapshot.getValue();
                        getMenuInflater().inflate(R.menu.menu_conversation, menu);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem i) {
        switch(i.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.action_group_info:
                //Read class information.
                final BCClassInfo c = new BCClassInfo();

                fbRoot.child("Groups").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            final String groupName = (String) dataSnapshot.child("Name").getValue();
                            c.setClassID((String) dataSnapshot.child("Class").getValue());

                            fbRoot.child("Classes").child(c.getClassID()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()) {
                                        Iterator i = dataSnapshot.getChildren().iterator();

                                        while(i.hasNext()) {
                                            DataSnapshot s = (DataSnapshot) i.next();

                                            if(s.getKey().equals("Name")) {
                                                c.setClassName((String) s.getValue());
                                            }

                                            else if(s.getKey().equals("Professor")) {
                                                c.setProfessorID((String) s.getValue());
                                            }
                                        }

                                        //GroupInfoActivity doesn't need Professor name.
                                        c.setProfessorName(null);

                                        Intent intent = new Intent(activityContext, GroupInfoActivity.class);
                                        intent.putExtra(ClassFragment.CID, c.getClassID());
                                        intent.putExtra(ClassFragment.PID, c.getProfessorID());
                                        intent.putExtra(ClassFragment.CNAME, c.getClassName());
                                        intent.putExtra(ClassFragment.PNAME, c.getProfessorName());
                                        intent.putExtra(GroupInfoActivity.GROUP_ID, groupId);
                                        intent.putExtra(GroupInfoActivity.GROUP_NAME, groupName);
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                            });
                        }
                    }

                    @Override public void onCancelled(DatabaseError databaseError) {}
                });
                break;
        }

        return true;
    }

    /* Sends a message. */
    private void sendMessage() {
        if(chatId == null) {
            //Create new chat.
            DatabaseReference chatRoot = fbRoot.child("Chats").push();
            chatId = chatRoot.getKey();
            chatRoot.child("Users").child(fbUser.getUid()).setValue(true);
            chatRoot.child("Users").child(recipientUID).setValue(true);

            //Add user's to each other's contacts.
            fbRoot.child("Profiles").child(recipientUID).child("Contacts").child(fbUser.getUid()).setValue(chatId);
            fbRoot.child("Profiles").child(fbUser.getUid()).child("Contacts").child(recipientUID).setValue(chatId);

            initUserAndMessageHandlers();
        }

        DatabaseReference fbNewMessage = fbMessageList.push();

        userMsgMap.put("UID", fbUser.getUid());
        userMsgMap.put("Content", inputMsgText.getText().toString());
        userMsgMap.put("Timestamp", ServerValue.TIMESTAMP);

        fbNewMessage.updateChildren(userMsgMap);
    }

    private void initUserAndMessageHandlers() {
        //Save database locations.
        fbMessageList = fbRoot.child("Chats").child(chatId).child("Messages");
        fbMemberList = fbRoot.child("Chats").child(chatId).child("Users");

        //Get user information.
        fbMemberList.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                BCContact newMember = new BCContact();
                final String uid = dataSnapshot.getKey();

                newMember.setUID(uid);
                newMember.setChatKey(chatId);

                chatMemebers.add(newMember);

                //Read member name.
                fbRoot.child("Profiles").child(uid).child("Name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            //Update name in member list.
                            for (int i = 0; i < chatMemebers.size(); i++) {
                                if (chatMemebers.get(i).getUID().equals(uid)) {
                                    chatMemebers.get(i).setName((String) dataSnapshot.getValue());
                                    break;
                                }
                            }

                            //Update name in messages.
                            for (int i = 0; i < messageListAdapter.getCount(); i++) {
                                if (messageListAdapter.getItem(i).getUID().equals(uid)) {
                                    messageListAdapter.getItem(i).setName((String) dataSnapshot.getValue());
                                }
                            }

                            messageListAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(DatabaseError databaseError) {}
        });

        //Set up message handler.
        fbMessageList.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                BCMessage m = new BCMessage();
                m.setUID((String) dataSnapshot.child("UID").getValue());
                m.setContent((String) dataSnapshot.child("Content").getValue());
                try {
                    m.setTimestamp((long) dataSnapshot.child("Timestamp").getValue());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //Get name.
                for (int i = 0; i < chatMemebers.size(); i++) {
                    if(m.getUID().equals(chatMemebers.get(i).getUID())) {
                        m.setName(chatMemebers.get(i).getName());
                        break;
                    }
                }

                messageListAdapter.add(m);
                messageListAdapter.notifyDataSetChanged();
            }

            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(DatabaseError databaseError) {}
        });

        messageListView.setAdapter(messageListAdapter);
        messageListView.setDivider(null);
    }
}
