package com.group4.cmpe131.broadclass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.group4.cmpe131.broadclass.R;
import com.group4.cmpe131.broadclass.fragment.ClassFragment;

public class ClassDetailActivity extends AppCompatActivity {


    private String classID;
    private String className;
    private String professorID;
    private String professorName;

    private MenuInflater inflater;

    private ListView announcementList;

    private FirebaseUser user;
    private FirebaseAuth mFbAuth;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);

        mFbAuth = FirebaseAuth.getInstance();
        user = mFbAuth.getCurrentUser();

        inflater = getMenuInflater();

        announcementList = (ListView) findViewById(R.id.class_detail_announcements);

        Toolbar classDetailToolbar = (Toolbar) findViewById(R.id.class_detail_toolbar);
        setSupportActionBar(classDetailToolbar);

        //Set up the toolbar.
        ActionBar classDetailAb = getSupportActionBar();
        classDetailAb.setDisplayHomeAsUpEnabled(true);
        classDetailAb.setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        classID = intent.getStringExtra(ClassFragment.CID);
        className = intent.getStringExtra(ClassFragment.CNAME);
        professorID = intent.getStringExtra(ClassFragment.PID);
        professorName = intent.getStringExtra(ClassFragment.PNAME);

        classDetailToolbar.setTitle(className);
        classDetailToolbar.setSubtitle(professorName);

    }

        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_class_detail, menu);

            return true;
        }


    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(ClassDetailActivity.this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_manage_class) {
            if (user.getUid().equals(professorID)) {
                //TODO: add in the manage class activity
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }


        /*
        if current user ID matches Prof ID enable class management features in toolbar/settings
        need an adapter to construct the list

        request server to provide members list via ClassID

         */

}
