package com.positron.alepapadop.sdy51ergasi3;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "SDY51";

    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private EditText mEmailField;
    private EditText mPasswordField;

    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views
        mStatusTextView = findViewById(R.id.status);
        mDetailTextView = findViewById(R.id.detail);
        mEmailField = findViewById(R.id.field_email);
        mPasswordField = findViewById(R.id.field_password);

        // Buttons
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_sign_up_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.next_button).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
    }

    private void updateUI(FirebaseUser user) {

        if (user != null) {
            mStatusTextView.setText(getString(R.string.emailpassword_status_fmt,
                    user.getEmail(), user.isEmailVerified()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
            findViewById(R.id.email_password_fields).setVisibility(View.GONE);
            findViewById(R.id.sign_out_next_buttons).setVisibility(View.VISIBLE);

        } else {
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);

            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_next_buttons).setVisibility(View.GONE);
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }



    private void signIn(String email, String password) {
        Log.d(TAG, "signin 1");
        if (!validateForm()) {
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signin 2");
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "signin 3");
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }


                        if (!task.isSuccessful()) {
                            mStatusTextView.setText(R.string.auth_failed);
                        }
                    }
                });
    }

    private void signUp(String email, String password) {
        Log.d(TAG, "signup 1");
        if (!validateForm()) {
            return;
        }
        Log.d(TAG, "signup 2");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signup 3");
                        if (task.isSuccessful()) {

                            Log.d(TAG, "signup 4");

                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            sendEmailVerification();
                        } else {

                            Log.d(TAG, "signup 5");

                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);

                            Log.d(TAG, task.getException().getMessage());
                        }

                    }
                });
    }


    private void sendEmailVerification() {

        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification();
    }

    private void signOut() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        // Set a title for alert dialog
        builder.setTitle("Select your answer.");

        // Ask the final question
        builder.setMessage("Are you sure to Sign Out?");

        // Set the alert dialog yes button click listener
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when user clicked the Yes button
                // Set the TextView visibility GONE

                mAuth.signOut();
                updateUI(null);
            }
        });

        // Set the alert dialog no button click listener
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when No button clicked
                Toast.makeText(getApplicationContext(),
                        "No Button Clicked",Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();
        // Display the alert dialog on interface
        dialog.show();

    }

    private void statNextIntent() {
        Intent intent = new Intent(this, SendData.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "click");
        int i = v.getId();
        if (i == R.id.email_sign_up_button) {
            Log.d(TAG, "signup");
            signUp(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.email_sign_in_button) {
            Log.d(TAG, "signin");
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.sign_out_button) {
            signOut();
        } else if (i == R.id.next_button) {
            statNextIntent();
        }
    }
}
