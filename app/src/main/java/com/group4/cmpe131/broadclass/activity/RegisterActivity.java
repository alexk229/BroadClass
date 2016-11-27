package com.group4.cmpe131.broadclass.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.group4.cmpe131.broadclass.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputConfirmPassword, inputFirstName, inputLastName;
    private Button btnRegister, btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fbAuth = FirebaseAuth.getInstance();

        btnRegister = (Button) findViewById(R.id.register_button);
        btnLogin = (Button) findViewById(R.id.login_button);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        inputConfirmPassword = (EditText) findViewById(R.id.confirm_password);
        inputFirstName = (EditText) findViewById(R.id.first_name);
        inputLastName = (EditText) findViewById(R.id.last_name);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

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

                progressBar.setVisibility(View.VISIBLE);

                fbAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Toast.makeText(RegisterActivity.this, "Registration Successful: " + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);

                        if(!task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Unable to register" + task.getException(), Toast.LENGTH_SHORT).show();
                        } else {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(firstName + " " + lastName)
                                    .build();
                            fbAuth.getCurrentUser().updateProfile(profileUpdates);
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        }
                    }
                });

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

}
