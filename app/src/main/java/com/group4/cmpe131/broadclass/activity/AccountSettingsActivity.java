package com.group4.cmpe131.broadclass.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.group4.cmpe131.broadclass.R;

public class AccountSettingsActivity extends AppCompatActivity {

    private Button btnChangeEmail, btnChangePassword, btnDeleteAccount;
    private ProgressBar progressBar;
    private FirebaseAuth fbAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_account_settings));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        fbAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user == null) {
                    startActivity(new Intent(AccountSettingsActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        btnChangeEmail = (Button)findViewById(R.id.change_email_button);
        btnChangePassword = (Button)findViewById(R.id.change_password_button);
        btnDeleteAccount = (Button)findViewById(R.id.delete_account_button);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if(progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        btnChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeEmailDialog();
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog();
            }
        });

        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAccountDialog();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showChangeEmailDialog() {

        // get change_email_dialog.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(AccountSettingsActivity.this);
        View promptView = layoutInflater.inflate(R.layout.change_email_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AccountSettingsActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText mOldEmail = (EditText) promptView.findViewById(R.id.old_email);
        final EditText mNewEmail = (EditText) promptView.findViewById(R.id.new_email);
        final EditText mNewConfirmEmail = (EditText) promptView.findViewById(R.id.confirm_email);

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Empty
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });


        // create an alert dialog
        final AlertDialog alert = alertDialogBuilder.create();
        alert.show();

        //Sets change button on dialog
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Boolean wantToCloseDialog = false;
                //Do stuff, possibly set wantToCloseDialog to true then...
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user != null) {
                    if(attemptEmailChange(mOldEmail, mNewEmail, mNewConfirmEmail)) {
                        wantToCloseDialog = true;
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }
                //Change user email
                if(wantToCloseDialog) {
                    user.updateEmail(mNewEmail.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(AccountSettingsActivity.this, "Email successfully updated. Please sign in with your new email.", Toast.LENGTH_LONG).show();
                                logout();
                                progressBar.setVisibility(View.GONE);
                            } else {
                                Toast.makeText(AccountSettingsActivity.this, "Failed to update email.", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                    alert.dismiss();
                    //else dialog stays open.
                }
            }
        });
    }

    private void showChangePasswordDialog() {

        // get change_email_dialog.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(AccountSettingsActivity.this);
        View promptView = layoutInflater.inflate(R.layout.change_password_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AccountSettingsActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText mOldPassword = (EditText) promptView.findViewById(R.id.old_password);
        final EditText mNewPassword = (EditText) promptView.findViewById(R.id.new_password);
        final EditText mNewConfirmPassword = (EditText) promptView.findViewById(R.id.confirm_new_password);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //empty
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        final AlertDialog alert = alertDialogBuilder.create();
        alert.show();

        //sets change button on dialog
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Boolean wantToCloseDialog = false;
                //Do stuff, possibly set wantToCloseDialog to true then...
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user != null) {
                    if(attemptPasswordChange(mOldPassword, mNewPassword, mNewConfirmPassword)) {
                        wantToCloseDialog = true;
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }
                //Change user password
                if(wantToCloseDialog) {
                    user.updatePassword(mNewPassword.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(AccountSettingsActivity.this, "Password successfully updated. Please sign in with your new password.", Toast.LENGTH_LONG).show();
                                logout();
                                progressBar.setVisibility(View.GONE);
                            } else {
                                Toast.makeText(AccountSettingsActivity.this, "Failed to update password.", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                    alert.dismiss();
                    //else dialog stays open.
                }
            }
        });
    }

    private void showDeleteAccountDialog() {
        // get change_email_dialog.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(AccountSettingsActivity.this);
        View promptView = layoutInflater.inflate(R.layout.delete_account_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AccountSettingsActivity.this);
        alertDialogBuilder.setView(promptView);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final TextView deleteAccount = (TextView) promptView.findViewById(R.id.delete_account);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //delete user account
                        if (user != null) {
                            progressBar.setVisibility(View.VISIBLE);
                            user.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(AccountSettingsActivity.this, "Your account has been successfully deleted.", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(AccountSettingsActivity.this, LoginActivity.class));
                                                finish();
                                                progressBar.setVisibility(View.GONE);
                                            } else {
                                                Toast.makeText(AccountSettingsActivity.this, "Failed to delete your account.", Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                    }
                })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    //checks if email is valid
    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    //checks if password is valid
    private boolean isPasswordValid(String password) {
        return password.length() > 6;
    }

    private boolean attemptEmailChange(EditText mOldEmail, EditText mNewEmail, EditText mNewConfirmEmail) {

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String oldEmail = mOldEmail.getText().toString();
        String newEmail = mNewEmail.getText().toString();
        String newConfirmEmail = mNewConfirmEmail.getText().toString();

        mOldEmail.setError(null);
        mNewEmail.setError(null);
        mNewConfirmEmail.setError(null);

        boolean cancel = false;
        View focusView = null;

        if(oldEmail.isEmpty()) {
            mOldEmail.setError(getString(R.string.error_field_required));
            focusView = mOldEmail;
            cancel = true;
        }
        else if(!isEmailValid(oldEmail)) {
            mOldEmail.setError(getString(R.string.error_invalid_email));
            focusView = mOldEmail;
            cancel = true;
        }

        if(newEmail.isEmpty()) {
            mNewEmail.setError(getString(R.string.error_field_required));
            focusView = mNewEmail;
            cancel = true;
        }
        else if(!isEmailValid(newEmail)) {
            mNewEmail.setError(getString(R.string.error_invalid_email));
            focusView = mNewEmail;
            cancel = true;
        }

        if(newConfirmEmail.isEmpty()) {
            mNewConfirmEmail.setError(getString(R.string.error_field_required));
            focusView = mNewConfirmEmail;
            cancel = true;
        }
        else if(!isEmailValid(newConfirmEmail)) {
            mNewConfirmEmail.setError(getString(R.string.error_invalid_email));
            focusView = mNewConfirmEmail;
            cancel = true;
        }

        if(newEmail.equals(user.getEmail())) {
            mNewEmail.setError(getString(R.string.error_same_email));
            focusView = mNewEmail;
            cancel = true;
        }

        if(!newConfirmEmail.equals(newEmail)) {
            mNewConfirmEmail.setError(getString(R.string.error_invalid_confirm_email));
            focusView = mNewConfirmEmail;
            cancel = true;
        }

        if(!oldEmail.equals(user.getEmail())) {
            mOldEmail.setError(getString(R.string.error_invalid_old_email));
            focusView = mOldEmail;
            cancel = true;
        }

        if(cancel) {
            focusView.requestFocus();
        }

        return !cancel;
    }

    private boolean attemptPasswordChange(EditText mOldPassword, EditText mNewPassword, EditText mNewConfirmPassword) {

        String oldPassword = mOldPassword.getText().toString();
        String newPassword = mNewPassword.getText().toString();
        String newConfirmPassword = mNewConfirmPassword.getText().toString();

        mOldPassword.setError(null);
        mNewPassword.setError(null);
        mNewConfirmPassword.setError(null);

        boolean cancel = false;
        View focusView = null;

        if(oldPassword.isEmpty()) {
            mOldPassword.setError(getString(R.string.error_field_required));
            focusView = mOldPassword;
            cancel = true;
        }
        else if(!isPasswordValid(oldPassword)) {
            mOldPassword.setError(getString(R.string.minimum_password));
            focusView = mOldPassword;
            cancel = true;
        }

        if(newPassword.isEmpty()) {
            mNewPassword.setError(getString(R.string.error_field_required));
            focusView = mNewPassword;
            cancel = true;
        }
        else if(!isPasswordValid(newPassword)) {
            mNewPassword.setError(getString(R.string.minimum_password));
            focusView = mNewPassword;
            cancel = true;
        }

        if(newConfirmPassword.isEmpty()) {
            mNewConfirmPassword.setError(getString(R.string.error_field_required));
            focusView = mNewConfirmPassword;
            cancel = true;
        }
        else if(!isPasswordValid(newConfirmPassword)) {
            mNewConfirmPassword.setError(getString(R.string.minimum_password));
            focusView = mNewConfirmPassword;
            cancel = true;
        }

        if(newPassword.equals(oldPassword)) {
            mNewPassword.setError(getString(R.string.error_same_password));
            focusView = mNewPassword;
            cancel = true;
        }

        if(!newConfirmPassword.equals(newPassword)) {
            mNewConfirmPassword.setError(getString(R.string.error_invalid_confirm_password));
            focusView = mNewConfirmPassword;
            cancel = true;
        }

        if(cancel) {
            focusView.requestFocus();
        }

        return !cancel;
    }

    public void logout() {
        fbAuth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        fbAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null) {
            fbAuth.removeAuthStateListener(authStateListener);
        }
    }
}
