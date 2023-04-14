/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * ONLINE MODE
 * An activity that handles the creation of new Online Mode users.
 */
public class OnlineCreateUser extends AppCompatActivity {

    private Button confirm;
    private EditText nameVal, emailVal, passwordVal, reTypePassVal;
    private TextView signIn;
    private ProgressDialog loadingBar;

    private final String firebaseDBURL = "https://smart-edubox-90c5d-default-rtdb.asia-southeast1.firebasedatabase.app/";
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance(firebaseDBURL);
    DatabaseReference myRef = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_createuseronline);

        init();
    }

    /**
     * Initialize all components.
     */
    private void init() {
        confirm = (Button) findViewById(R.id.confirmBtnOnline);
        nameVal = (EditText) findViewById(R.id.displayNameTextOnline);
        emailVal = (EditText) findViewById(R.id.inputNewEmailOnline);
        passwordVal = (EditText) findViewById(R.id.inputNewPassOnline);
        reTypePassVal = (EditText) findViewById(R.id.inputRetypeNewPassOnline);
        signIn = (TextView) findViewById(R.id.signInOnlineText);

        loadingBar = new ProgressDialog(OnlineCreateUser .this);

        signIn.setOnClickListener(v -> {
            goToSignInForm();
            clearEditTextBoxes();
        });

        confirm.setOnClickListener(v -> {

            if (!isNetworkConnected()) {
                Toast.makeText(this, "Device not connected to the internet. Please connect first.", Toast.LENGTH_SHORT).show();
            } else {
                if (internetIsConnected()) {
                    checkCredentials();
                    clearEditTextBoxes();

                    SharedPreferences sharedPreferences = getSharedPreferences("rememberMeCBOnline", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("rememberMeOnline", "false");
                    editor.apply();
                }
            }
        });
    }

    /**
     * A function that verifies whether user is connect to a network or not.
     * @return returns a boolean value of the status.
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    /**
     * Check if current network is connected to the internet.
     * @return returns a boolean value of the status.
     */
    public boolean internetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check user credentials and sets conditions and errors.
     */
    public void checkCredentials() {
        String nameStr, emailStr, passStr, retypePassStr;

        nameStr = nameVal.getText().toString();
        emailStr = emailVal.getText().toString();
        passStr = passwordVal.getText().toString();
        retypePassStr = reTypePassVal.getText().toString();

        if (!retypePassStr.equals(passStr)) {
            passwordVal.setError("Passwords do not match. Please try again.");
            passwordVal.requestFocus();
            passwordVal.setText("");
        }
        if (nameStr.isEmpty()) {
            nameVal.setError("Name must not be blank.");
            nameVal.requestFocus();
            nameVal.setText("");
        }

        if (emailStr.isEmpty()) {
            emailVal.setError("Email must not be blank.");
            emailVal.requestFocus();
            emailVal.setText("");
        }
        if (passStr.isEmpty() && retypePassStr.isEmpty()) {
            passwordVal.setError("Password must not be empty.");
            passwordVal.requestFocus();
            passwordVal.setText("");
        }
        else {
            loadingBar.setTitle("Registration");
            loadingBar.setMessage("Please wait while we register your account.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            register_user(nameStr, emailStr, passStr);
        }
    }

    /**
     * A function that registers the user's credentials to the Firebase Auth.
     * @param display_name the value of the user's UserName.
     * @param email the value of the user's email.
     * @param password the value of the user's password.
     */
    private void register_user(final String display_name, String email, String password) {

        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
        String date = currentDate.format(new Date());

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    loadingBar.cancel();
                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                    switch (errorCode) {
                        case "ERROR_INVALID_CUSTOM_TOKEN":
                            Toast.makeText(OnlineCreateUser.this, "The custom token format is incorrect. Please check the documentation.", Toast.LENGTH_LONG).show();
                            break;

                        case "ERROR_CUSTOM_TOKEN_MISMATCH":
                            Toast.makeText(OnlineCreateUser.this, "The custom token corresponds to a different audience.", Toast.LENGTH_LONG).show();
                            break;

                        case "ERROR_INVALID_CREDENTIAL":
                            Toast.makeText(OnlineCreateUser.this, "The supplied auth credential is malformed or has expired.", Toast.LENGTH_LONG).show();
                            break;

                        case "ERROR_INVALID_EMAIL":
                            Toast.makeText(OnlineCreateUser.this, "The email address is badly formatted.", Toast.LENGTH_LONG).show();
                            emailVal.setError("The email address is badly formatted.");
                            emailVal.requestFocus();
                            break;

                        case "ERROR_WRONG_PASSWORD":
                            Toast.makeText(OnlineCreateUser.this, "The password is invalid or the user does not have a password.", Toast.LENGTH_LONG).show();
                            passwordVal.setError("Password is incorrect ");
                            passwordVal.requestFocus();
                            passwordVal.setText("");
                            break;

                        case "ERROR_USER_MISMATCH":
                            Toast.makeText(OnlineCreateUser.this, "The supplied credentials do not correspond to the previously signed in user.", Toast.LENGTH_LONG).show();
                            break;

                        case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                            Toast.makeText(OnlineCreateUser.this, "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.", Toast.LENGTH_LONG).show();
                            break;

                        case "ERROR_EMAIL_ALREADY_IN_USE":
                            Toast.makeText(OnlineCreateUser.this, "The email address is already in use by another account.   ", Toast.LENGTH_LONG).show();
                            emailVal.setError("The email address is already in use by another account.");
                            emailVal.requestFocus();
                            break;

                        case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                            Toast.makeText(OnlineCreateUser.this, "This credential is already associated with a different user account.", Toast.LENGTH_LONG).show();
                            break;

                        case "ERROR_OPERATION_NOT_ALLOWED":
                            Toast.makeText(OnlineCreateUser.this, "This operation is not allowed. You must enable this service in the console.", Toast.LENGTH_LONG).show();
                            break;

                        case "ERROR_WEAK_PASSWORD":
                            Toast.makeText(OnlineCreateUser.this, "The given password is invalid.", Toast.LENGTH_LONG).show();
                            passwordVal.setError("The password is invalid it must 6 characters at least");
                            passwordVal.requestFocus();
                            break;
                    }
                } else {
                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();
                    myRef = FirebaseDatabase.getInstance(firebaseDBURL).getReference().child("Users").child(uid);

                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("name", display_name);
                    userMap.put("status", "Hi, I'm using the eSense App. Let's chat!");
                    userMap.put("image", "default");
                    userMap.put("thumb_image", "default");
                    userMap.put("account_creation_date", date);
                    myRef.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(OnlineCreateUser.this, "Account creation successful.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(OnlineCreateUser.this, OnlineLogin.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    });
                }
            });
    }

    /**
     * Go to the Online Login activity.
     */
    public void goToSignInForm () {
        Intent goToSignInIntent = new Intent(this, OnlineLogin.class);
        startActivity(goToSignInIntent);
        finish();
    }

    /**
     * Clear EditText value for the password fields.
     */
    public void clearEditTextBoxes() {
        passwordVal.getText().clear();
        reTypePassVal.getText().clear();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}