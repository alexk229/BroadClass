package com.group4.cmpe131.broadclass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.group4.cmpe131.broadclass.R;

public class GroupInfoActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private String groupInfoTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        toolbar = (Toolbar) findViewById(R.id.group_info_toolbar);

        groupInfoTitle = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        System.out.println(groupInfoTitle);

        setSupportActionBar(toolbar);

        ActionBar groupInfoAb = getSupportActionBar();
        groupInfoAb.setDisplayHomeAsUpEnabled(true);
        groupInfoAb.setDisplayShowTitleEnabled(true);
        groupInfoAb.setTitle(groupInfoTitle);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
