package com.group4.cmpe131.broadclass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.group4.cmpe131.broadclass.R;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ConversationActivity extends AppCompatActivity {

    private ImageButton sendMsgButton;
    private EditText inputMsgText;
    private TextView chatMsgText;
    private FirebaseUser user;
    private String username, chatroomTitle;
    private DatabaseReference root;
    private String tempChatKey, chatMsg, chatUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Toolbar conversationToolbar = (Toolbar) findViewById(R.id.conversation_toolbar);
        setSupportActionBar(conversationToolbar);

        //Set up the toolbar.
        ActionBar conversationAb = getSupportActionBar();
        conversationAb.setDisplayHomeAsUpEnabled(true);
        conversationAb.setDisplayShowTitleEnabled(false);

        chatroomTitle = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        username = getIntent().getStringExtra("username");
        conversationToolbar.setTitle(chatroomTitle);

        sendMsgButton = (ImageButton) findViewById(R.id.send_msg_button);
        inputMsgText = (EditText) findViewById(R.id.input_msg_text);
        chatMsgText = (TextView) findViewById(R.id.chat_msg_text);

        user = FirebaseAuth.getInstance().getCurrentUser();

        //sets display name for chat conversation

        root = FirebaseDatabase.getInstance().getReference().child(chatroomTitle);

        //Sets send message button action
        sendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                inputMsgText.setText(null);
            }
        });

        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                appendChatConversation(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                appendChatConversation(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        Map<String, Object> keyMap = new HashMap<String, Object>();
        tempChatKey = root.push().getKey();
        root.updateChildren(keyMap);

        DatabaseReference messageRoot = root.child(tempChatKey);
        Map<String, Object> userMsgMap = new HashMap<String, Object>();
        userMsgMap.put("Name", username);
        userMsgMap.put("Msg", inputMsgText.getText().toString());

        messageRoot.updateChildren(userMsgMap);
    }

    private void showMembers(View view) {
        //TODO: Show conversation members.
    }

    //Creates message conversation
    private void appendChatConversation(DataSnapshot dataSnapshot) {

        Iterator i = dataSnapshot.getChildren().iterator();

        while(i.hasNext()) {

            chatUsername = (String) ((DataSnapshot)i.next()).getValue();
            chatMsgText.append(chatUsername + ": " + chatMsg + " \n" );
        }
    }
}
