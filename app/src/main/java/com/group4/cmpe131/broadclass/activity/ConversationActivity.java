package com.group4.cmpe131.broadclass.activity;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.group4.cmpe131.broadclass.R;

public class ConversationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Toolbar conversationToolbar = (Toolbar) findViewById(R.id.conversation_toolbar);
        setSupportActionBar(conversationToolbar);

        //Enable the Up button.
        ActionBar conversationAb = getSupportActionBar();
        conversationAb.setDisplayHomeAsUpEnabled(true);

        //TODO: Replace this with the actual conversation title.
        conversationToolbar.setTitle("Some Group");
    }

    public void sendMessage(View view) {
        //TODO: Message-sending routine.
    }

    public void showMembers(View view) {
        //TODO: Show conversation members.
    }
}
