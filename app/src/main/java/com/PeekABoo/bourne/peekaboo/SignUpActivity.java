package com.PeekABoo.bourne.peekaboo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.PeekABoo.bourne.peekaboo.R;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpActivity extends Activity {

    protected EditText mUsername;
    protected EditText mPassword;
    protected EditText mEmail;
    protected Button mSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mUsername = (EditText) findViewById(R.id.userNameFieldSignUp);
        mPassword = (EditText) findViewById(R.id.passwordFieldSignUp);
        mEmail = (EditText) findViewById(R.id.emailField);
        mSignUpButton = (Button) findViewById(R.id.signUpButton);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();
                String email = mEmail.getText().toString();

                username = username.trim();
                password = password.trim();
                email = email.trim();

                if(username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    errorSignUp();
                }
                else {
                    signUp(username, password, email);
                }
            }
        });
    }

    // A Dialog on an error shows up
    protected void errorSignUp() {
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setMessage(R.string.signUp_error_message)
                .setTitle(R.string.error_title)
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // SignUpInBackground is in process at parse.com and login is validated.
    // If parseException does not return a null, it shows an error dialog box
    protected void signUp(String username, String password, String email) {
        ParseUser newUser = new ParseUser();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(email);
        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    //Success in signing up
                    Intent intent = new Intent(SignUpActivity.this, MyActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else {
                    //Error in signing up in parse.com
                    AlertDialog.Builder builder= new AlertDialog.Builder(SignUpActivity.this);
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

}
