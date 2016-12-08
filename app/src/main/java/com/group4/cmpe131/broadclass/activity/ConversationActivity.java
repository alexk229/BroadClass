package com.group4.cmpe131.broadclass.activity;

import android.content.Intent;
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

        //Set up the toolbar.
        ActionBar conversationAb = getSupportActionBar();
        conversationAb.setDisplayHomeAsUpEnabled(true);
        conversationAb.setDisplayShowTitleEnabled(false);

        String title = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        conversationToolbar.setTitle(title);
    }

    public void sendMessage(View view) {
        //TODO: Message-sending routine.
    }

    public void showMembers(View view) {
        //TODO: Show conversation members.
    }
}
