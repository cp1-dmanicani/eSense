/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * OFFLINE MODE
 * An activity that handles offline account creation.
 */
public class OfflineCreateUser extends AppCompatActivity {

    EditText email, password, retypePass;
    Button confirm;
    DBHelper DB;
    Boolean biometricVal = false;
    public static final String userEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_createuseroffline);

        TextView signIn = (TextView) findViewById(R.id.signInText);
        signIn.setOnClickListener(v -> goToSignInActivity());

        email = (EditText) findViewById(R.id.inputNewEmailOffline);
        password = (EditText) findViewById(R.id.inputNewPassOffline);
        retypePass = (EditText) findViewById(R.id.inputRetypeNewPassOffline);
        confirm = (Button) findViewById(R.id.confirmBtnOffline);
        DB = new DBHelper(this);

        confirm.setOnClickListener(v -> {

            String emailVal = email.getText().toString();
            String passVal = password.getText().toString();
            String rePassVal = retypePass.getText().toString();

            if (!emailVal.contains("@") || emailVal.isEmpty() || !emailVal.contains(".")) {
                Toast.makeText(this, "Email is not valid.", Toast.LENGTH_SHORT).show();
            }
            else if (passVal.length()<7) {
                Toast.makeText(this, "Pass length must not be less than 7 characters.", Toast.LENGTH_SHORT).show();
            }
            else if (passVal.isEmpty() || rePassVal.isEmpty()) {
                Toast.makeText(this, "Password is empty.", Toast.LENGTH_SHORT).show();
            }
            else {
                if (passVal.equals(rePassVal)) {
                    Boolean checkUser = DB.checkEmail(emailVal);
                    if(checkUser == false) {
                        Boolean insert = DB.insert(emailVal, passVal);
                        if (insert == true) {
                            Toast.makeText(OfflineCreateUser.this, "Account Creation Successful!", Toast.LENGTH_SHORT).show();
                            //goToSignInActivity();
                            Intent intentSignIn = new Intent(this, OfflineLogin.class);
                            intentSignIn.putExtra("email", email.getText().toString());
                            startActivity(intentSignIn);
                            finish();
                        }
                        else {
                            Toast.makeText(OfflineCreateUser.this, "Account Creation Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(OfflineCreateUser.this, "Account already exists. Sign In Instead.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(OfflineCreateUser.this, "Passwords do not match. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * Go to offline login activity.
     */
    public void goToSignInActivity () {
        Intent intentSignIn = new Intent(this, OfflineLogin.class);
        startActivity(intentSignIn);
        finish();
    }

    public void clearEditText() {
        email.getText().clear();
        password.getText().clear();
        retypePass.getText().clear();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}