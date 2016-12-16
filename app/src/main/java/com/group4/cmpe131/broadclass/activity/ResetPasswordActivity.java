package com.group4.cmpe131.broadclass.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.group4.cmpe131.broadclass.R;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText inputEmail;
    private Button btnReset, btnBack;
    private FirebaseAuth fbAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences getData = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String themeValues = getData.getString("theme", "dark");

        if (themeValues.equals("light")) {
            setTheme(R.style.AppTheme_Light);
        }

        if (themeValues.equals("dark")) {
            setTheme(R.style.AppTheme_Dark);
        }

        setContentView(R.layout.activity_reset_password);

        inputEmail = (EditText)findViewById(R.id.email);
        btnReset = (Button)findViewById(R.id.reset_password_button);
        btnBack = (Button)findViewById(R.id.back_button);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        fbAuth = FirebaseAuth.getInstance();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Button to reset password
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString();

                if(TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Please enter your email", Toast.LENGTH_SHORT);
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //Attempts to send email to user
                fbAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(ResetPasswordActivity.this, "We have sent you an email to reset your password.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, "Failed to send email. The email is either invalid or not registered.", Toast.LENGTH_LONG).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }
}
