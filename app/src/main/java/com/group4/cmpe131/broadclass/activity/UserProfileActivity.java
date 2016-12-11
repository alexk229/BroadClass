package com.group4.cmpe131.broadclass.activity;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.group4.cmpe131.broadclass.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private CircleImageView mProfilePic;
    private Button updateBioButton;
    private EditText editUserBio;
    private TextView userBio;
    private FloatingActionButton cameraButton;
    private static final int REQUEST_IMAGE_CAPTURE = 111;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        mProfilePic = (CircleImageView) findViewById(R.id.profile_image);
        userBio = (TextView)findViewById(R.id.user_bio);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        cameraButton = (FloatingActionButton) findViewById(R.id.camera_fab);

        //Validates if user is logged in
        if (user != null) {
            toolbar.setTitle(user.getDisplayName().toString());

            //Attempts to get user profile picture
            if(user.getPhotoUrl() != null) {
                try {
                    Bitmap imageBitmap = decodeFromFirebaseBase64(user.getPhotoUrl().toString());
                    mProfilePic.setImageBitmap(imageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                //If fails attempt to obtain profile picture from firebase url else use default profile pic
                Glide.with(this.getApplicationContext())
                        .load(user.getPhotoUrl())
                        .error(R.drawable.com_facebook_profile_picture_blank_portrait)
                        .into(mProfilePic);
            }
        }

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        //Sets camera floating action button
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    onLaunchCamera();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        updateBioButton = (Button) findViewById(R.id.update_bio_button);

        updateBioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestBioDialog();
            }
        });

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                cameraButton.setEnabled(true);
            }
        }
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

    public void onLaunchCamera() throws IOException {

        //Checks permission of device
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }

            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                cameraButton.setEnabled(false);
                requestPermissions(new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
            }
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Image File name");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
/*                case PICK_IMAGE_REQUEST://actionCode
                    if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                        //For Image Gallery
                    }
                    return;*/

                case REQUEST_IMAGE_CAPTURE://actionCode
                    if (resultCode == RESULT_OK) {
                        //For CAMERA

                        Bundle extras = data.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        mProfilePic.setImageBitmap(imageBitmap);
                        encodeBitmapAndSaveToFirebase(imageBitmap);
                    }
            }

    }

    public void encodeBitmapAndSaveToFirebase(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(imageEncoded))
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("UserProfileActivity: ", "User profile updated.");
                        } else {
                            Log.d("UserProfileActivity: ", "User profile failed.");
                        }
                    }
                });
    }

    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    //Dialog to create classroom
    //TODO: needs to send data to server
    private void requestBioDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(UserProfileActivity.this);
        View promptView = layoutInflater.inflate(R.layout.dialog_update_user_bio, null);
        editUserBio = (EditText)promptView.findViewById(R.id.bio_description);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UserProfileActivity.this);
        alertDialogBuilder.setTitle("Enter bio information: ");
        alertDialogBuilder.setView(promptView);

        //Attempts to change user bio
        alertDialogBuilder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Updates user profile
                if(!editUserBio.getText().toString().isEmpty()) {
                    userBio.setText(editUserBio.getText().toString());
                }

                //TODO: update information to sever
            }
        });

        //Cancel user bio change
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        final AlertDialog alert = alertDialogBuilder.create();
        alert.show();

    }

}
