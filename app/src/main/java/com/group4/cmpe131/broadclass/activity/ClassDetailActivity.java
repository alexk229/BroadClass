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
import com.group4.cmpe131.broadclass.R;
import com.group4.cmpe131.broadclass.fragment.ClassFragment;
import com.group4.cmpe131.broadclass.util.BCClassInfo;

public class ClassDetailActivity extends AppCompatActivity {
    private BCClassInfo classInfo;

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

        classInfo = new BCClassInfo();

        Intent intent = getIntent();

        classInfo.setClassID(intent.getStringExtra(ClassFragment.CID));
        classInfo.setProfessorID(intent.getStringExtra(ClassFragment.PID));
        classInfo.setClassName(intent.getStringExtra(ClassFragment.CNAME));
        classInfo.setProfessorName(intent.getStringExtra(ClassFragment.PNAME));

        classDetailToolbar.setTitle(classInfo.getClassName());
        classDetailToolbar.setSubtitle(classInfo.getProfessorName());
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

        Intent i;

        switch(id) {
            //Settings menu
            case R.id.action_settings:
                startActivity(new Intent(ClassDetailActivity.this, SettingsActivity.class));
                return true;

            //Student list
            case R.id.action_class_students:
                i = new Intent(this, ListStudentsActivity.class);
                i.putExtra(ClassFragment.CID, classInfo.getClassID());
                i.putExtra(ClassFragment.PID, classInfo.getProfessorID());
                i.putExtra(ClassFragment.CNAME, classInfo.getClassName());
                i.putExtra(ClassFragment.PNAME, classInfo.getProfessorName());
                startActivity(i);
                return true;

            //Class groups
            case R.id.action_class_groups:
                i = new Intent(this, ListGroupsActivity.class);
                i.putExtra(ClassFragment.CID, classInfo.getClassID());
                i.putExtra(ClassFragment.PID, classInfo.getProfessorID());
                i.putExtra(ClassFragment.CNAME, classInfo.getClassName());
                i.putExtra(ClassFragment.PNAME, classInfo.getProfessorName());
                startActivity(i);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
