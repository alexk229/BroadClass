package com.group4.cmpe131.broadclass.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group4.cmpe131.broadclass.R;
import com.group4.cmpe131.broadclass.app.Config;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputConfirmPassword, inputFirstName, inputLastName;
    private Button btnRegister, btnLogin;

    private FirebaseAuth fbAuth;
    private DatabaseReference fbRoot;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Config.appTheme == 1) {
            setTheme(R.style.AppTheme_Light);
        }

        if (Config.appTheme == 2) {
            setTheme(R.style.AppTheme_Dark);
        }

        setContentView(R.layout.activity_register);

        fbAuth = FirebaseAuth.getInstance();

        btnRegister = (Button) findViewById(R.id.register_button);
        btnLogin = (Button) findViewById(R.id.login_button);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        inputConfirmPassword = (EditText) findViewById(R.id.confirm_password);
        inputFirstName = (EditText) findViewById(R.id.first_name);
        inputLastName = (EditText) findViewById(R.id.last_name);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //Button to register user
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();
                String confirmPassword = inputConfirmPassword.getText().toString();
                final String firstName = inputFirstName.getText().toString();
                final String lastName = inputLastName.getText().toString();

                boolean cancel = false;
                View focusView = null;

                inputEmail.setError(null);
                inputFirstName.setError(null);
                inputLastName.setError(null);
                inputPassword.setError(null);
                inputConfirmPassword.setError(null);

                //The following validates edit text fields
                if(TextUtils.isEmpty(firstName)) {
                    inputFirstName.setError(getString(R.string.error_field_required));
                    focusView = inputFirstName;
                    cancel = true;
                }

                if(TextUtils.isEmpty(lastName)) {
                    inputLastName.setError(getString(R.string.error_field_required));
                    focusView = inputLastName;
                    cancel = true;
                }

                if(TextUtils.isEmpty(email)) {
                    inputEmail.setError(getString(R.string.error_field_required));
                    focusView = inputEmail;
                    cancel = true;
                } else if(!email.contains("@")) {
                    inputEmail.setError(getString(R.string.error_invalid_email));
                    focusView = inputEmail;
                    cancel = true;
                }

                if(TextUtils.isEmpty(password)) {
                    inputPassword.setError(getString(R.string.error_field_required));
                    focusView = inputPassword;
                    cancel = true;
                } else if(password.length() < 6) {
                    inputPassword.setError(getString(R.string.minimum_password));
                    focusView = inputPassword;
                    cancel = true;
                }

                if(TextUtils.isEmpty(confirmPassword)) {
                    inputConfirmPassword.setError(getString(R.string.error_field_required));
                    focusView = inputConfirmPassword;
                    cancel = true;
                }

                if(!confirmPassword.equals(password)) {
                    inputConfirmPassword.setError("This password does not match.");
                    focusView = inputConfirmPassword;
                    cancel = true;
                }

                if(cancel) {
                    focusView.requestFocus();
                    return;
                }

                pDialog.setMessage("Registering ...");
                showDialog();

                //Attempts to create a new user
                fbAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(!task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Unable to register: " + task.getException(), Toast.LENGTH_SHORT).show();
                            hideDialog();
                        } else {
                            hideDialog();
                            Toast.makeText(RegisterActivity.this, "Registration Successful" , Toast.LENGTH_SHORT).show();

                            addUserToDatabase(firstName, lastName);

                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        }
                    }
                });

            }
        });

        //Button to login screen
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void addUserToDatabase(String firstName, String lastName) {
        final String displayName = firstName + " " + lastName;

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        fbAuth.getCurrentUser().updateProfile(profileUpdates);

        fbRoot = FirebaseDatabase.getInstance().getReference().getRoot();

        final DatabaseReference fbProfile = fbRoot.child("Profiles").child(fbAuth.getCurrentUser().getUid());

        //Check if User exists in database yet.
        fbProfile.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() == false) {
                    //Write name and empty bio to child of Profiles with key getUid().
                    fbProfile.child("Name").setValue(displayName);
                    fbProfile.child("Bio").setValue("");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
