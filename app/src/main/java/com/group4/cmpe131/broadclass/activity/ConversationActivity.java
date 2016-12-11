package com.group4.cmpe131.broadclass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.group4.cmpe131.broadclass.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ConversationActivity extends AppCompatActivity {

    private ImageButton sendMsgButton;
    private EditText inputMsgText;
    private FirebaseUser user;
    private String username, chatroomTitle;
    private DatabaseReference root;
    private String tempChatKey;

    //Layout variables.
    private LayoutInflater inflater;
    private LinearLayout conversationLayout;
    private TextView conversationFooter, chatMsgText;
    private ScrollView chatScrollView;
    private static String currentTime;

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

        //TODO: change textview ui
 //       chatMsgText = (TextView) findViewById(R.id.chat_msg_text);
        //Save layout variables.
        inflater = getLayoutInflater();
        conversationLayout = (LinearLayout) findViewById(R.id.conversation_layout);
        conversationFooter = (TextView) findViewById(R.id.conversation_footer);

        Calendar calendar = Calendar.getInstance();
        currentTime = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

        conversationFooter.requestFocus();

        user = FirebaseAuth.getInstance().getCurrentUser();

        //sets display name for chat conversation

        root = FirebaseDatabase.getInstance().getReference().child(chatroomTitle);

        chatScrollView = (ScrollView) findViewById(R.id.conversation_scroller);

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
                //appendChatConversation(dataSnapshot);
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

    //Sends message
    /* Add a left-aligned text bubble to the end of the chat. */
    private void addLeftBubble(String name, String content, String time) {
        View bubble = inflater.inflate(R.layout.conversation_left_bubble, conversationLayout, false);

        fillBubbleFields(bubble, name, content, time);

        conversationLayout.addView(bubble, conversationLayout.getChildCount() - 1);

        chatScrollView.post(new Runnable()
        {
            public void run()
            {
                chatScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    /* Add a right-aligned text bubble to the end of the chat. */
    private void addRightBubble(String name, String content, String time) {
        View bubble = inflater.inflate(R.layout.conversation_right_bubble, conversationLayout, false);

        fillBubbleFields(bubble, name, content, time);

        conversationLayout.addView(bubble, conversationLayout.getChildCount() - 1);

        chatScrollView.post(new Runnable()
        {
            public void run()
            {
                chatScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    /* Fill the text fields of a text bubble. Invoked by addLeftBubble() and addRightBubble(). */
    private void fillBubbleFields(View bubble, String name, String content, String time) {
        ((TextView) bubble.findViewById(R.id.bubble_name)).setText(name);
        ((TextView) bubble.findViewById(R.id.bubble_content)).setText(content);
        ((TextView) bubble.findViewById(R.id.bubble_time)).setText(time);
    }

    private void sendMessage() {
        Map<String, Object> keyMap = new HashMap<String, Object>();
        tempChatKey = root.push().getKey();
        root.updateChildren(keyMap);

        DatabaseReference messageRoot = root.child(tempChatKey);
        Map<String, Object> userMsgMap = new HashMap<String, Object>();
        userMsgMap.put("Name", username);
        userMsgMap.put("Msg", inputMsgText.getText().toString());
        userMsgMap.put("Timestamp", ServerValue.TIMESTAMP);

        messageRoot.updateChildren(userMsgMap);
    }

    private void showMembers(View view) {
        //TODO: Show conversation members.
    }

    //Creates message conversation
    private void appendChatConversation(DataSnapshot dataSnapshot) {
        String name = "", content = "", time = ""; //TODO: Actual time.

        Iterator i = dataSnapshot.getChildren().iterator();

        while(i.hasNext()) {
            DataSnapshot snapshot = (DataSnapshot)i.next();

            switch (snapshot.getKey()) {
                case "Name":
                    name = (String) snapshot.getValue();
                    break;

                case "Msg":
                    content = (String) snapshot.getValue();
                    break;

                case "Timestamp":
                    time = snapshot.getValue().toString();
                    Long temp = Long.parseLong(time);
                    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                    time = format.format(new Date(temp));
                    System.out.println(time);

            }

            if(name != "" && content != "" && time != "") {
                if(name.equals(username)) {
                    addRightBubble(name, content, time);
                }

                else {
                    addLeftBubble(name, content, time);
                }

                name = "";
                content = "";
                time = "";
            }
        }
    }
}
