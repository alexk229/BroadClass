package com.group4.cmpe131.broadclass.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationActivity extends AppCompatActivity {
    final public static String CHAT_ID = "CHAT_ID";
    final public static String USERNAME = "USERNAME";

    private FirebaseUser user;
    private DatabaseReference fbRoot;

    private String chatId;

    private CircleImageView sendMsgButton;
    private ImageButton sendImageButton;
    private EditText inputMsgText;

    private String username, chatroomTitle;
    private Image imageToSend;

    private String tempChatKey;
    private Map<String, Object> userMsgMap;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 111;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    //Layout variables.
    private LayoutInflater inflater;
    private LinearLayout conversationLayout;
    private TextView conversationFooter, chatMsgText;
    private ScrollView chatScrollView;

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
        username = getIntent().getStringExtra(USERNAME);
        chatId = getIntent().getStringExtra(CHAT_ID);

        conversationToolbar.setTitle(chatroomTitle);

        sendMsgButton = (CircleImageView) findViewById(R.id.send_msg_button);
        sendImageButton = (ImageButton) findViewById(R.id.send_image_button);
        inputMsgText = (EditText) findViewById(R.id.input_msg_text);

        //TODO: change textview ui
 //       chatMsgText = (TextView) findViewById(R.id.chat_msg_text);
        //Save layout variables.
        inflater = getLayoutInflater();
        conversationLayout = (LinearLayout) findViewById(R.id.conversation_layout);
        conversationFooter = (TextView) findViewById(R.id.conversation_footer);

        conversationFooter.requestFocus();

        user = FirebaseAuth.getInstance().getCurrentUser();

        //sets display name for chat conversation

        fbRoot = FirebaseDatabase.getInstance().getReference().child(chatroomTitle);

        chatScrollView = (ScrollView) findViewById(R.id.conversation_scroller);

        userMsgMap = new HashMap<String, Object>();

        //Sets send message button action
        sendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                inputMsgText.setText(null);
            }
        });

        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
                inputMsgText.setText(null);
            }
        });

        fbRoot.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                appendChatConversation(dataSnapshot);
            }

            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversation, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_group_info) {
            Intent intent = new Intent(ConversationActivity.this, GroupInfoActivity.class);
            intent.putExtra(Intent.EXTRA_TITLE, chatroomTitle);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                sendImageButton.setEnabled(true);
            }
        }
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
        tempChatKey = fbRoot.push().getKey();
        fbRoot.updateChildren(keyMap);

        DatabaseReference messageRoot = fbRoot.child(tempChatKey);
        userMsgMap.put("Name", username);
        userMsgMap.put("Image", imageToSend);
        userMsgMap.put("Msg", inputMsgText.getText().toString());
        userMsgMap.put("Timestamp", ServerValue.TIMESTAMP);

        messageRoot.updateChildren(userMsgMap);
    }

    private void launchCamera() {
        //Checks permission of device
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }

            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
            }
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void showMembers(View view) {
        //TODO: Show conversation members.
    }

    //Creates message conversation
    private void appendChatConversation(DataSnapshot dataSnapshot) {
        String name = "", content = "", time = "";

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

                    //Converts timestamp to appropriate format
                    Long temp = Long.parseLong(time);
                    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                    time = format.format(new Date(temp));
                    break;

                default:
                    break;

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
