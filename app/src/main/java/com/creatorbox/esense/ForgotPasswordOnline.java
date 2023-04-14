/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/**
 * ONLINE MODE
 * An activity that handles password resetting of users by providing their email.
 */
public class ForgotPasswordOnline extends AppCompatActivity {

    Button resetBtn;
    EditText emailText;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_online);

        resetBtn = (Button) findViewById(R.id.reset);
        emailText = (EditText) findViewById(R.id.inputEmailForPassReset);
        firebaseAuth = FirebaseAuth.getInstance();

        resetBtn.setOnClickListener(v -> {
            try {
                resetPassword();
            }
            catch (Exception e) {
                Toast.makeText(ForgotPasswordOnline.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * A function that handles the resetting of user's password.
     * Email will be sent to the user in order to reset their password.
     * Resetting of password is not handles in-app rather through a browser.
     */
    public void resetPassword() {
        String email = emailText.getText().toString().trim();

        if (email.isEmpty()) {
            emailText.setError("Email is required.");
            emailText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() ) {
            emailText.setError("Email is not valid.");
            emailText.requestFocus();
            return;
        }

        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPasswordOnline.this, "Check your email to reset your password.", Toast.LENGTH_SHORT).show();
                    emailText.getText().clear();
                }
                else {
                    Toast.makeText(ForgotPasswordOnline.this, "Try again. Something wrong happened.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}