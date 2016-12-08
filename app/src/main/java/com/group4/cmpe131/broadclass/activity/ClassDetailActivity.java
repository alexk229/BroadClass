package com.group4.cmpe131.broadclass.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.group4.cmpe131.broadclass.R;

public class ClassDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);

        Toolbar classDetailToolbar = (Toolbar) findViewById(R.id.class_detail_toolbar);
        setSupportActionBar(classDetailToolbar);

        //Set up the toolbar.
        ActionBar classDetailAb = getSupportActionBar();
        classDetailAb.setDisplayHomeAsUpEnabled(true);
        classDetailAb.setDisplayShowTitleEnabled(false);

        String title = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        classDetailToolbar.setTitle(title);
    }
}
