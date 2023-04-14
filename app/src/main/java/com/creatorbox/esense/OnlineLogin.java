/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

import java.util.concurrent.Executor;

/**
 * ONLINE MODE
 * An activity that handles the logging in of user when they choose Online Mode.
 */
public class OnlineLogin extends AppCompatActivity {


    private ProgressDialog loadingBar;
    private SharedPreferences sharedPreferencesOnline;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    private TextView forgotPassword, createOnlineUser, orText, signInText;
    private ImageButton fingerprintBtn;
    private EditText userEmail, userPass;
    private Button signIn;
    private CheckBox showPassword, rememberMe;
    public static final int Theme_Material_Light_Dialog_Alert = 0;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private Vibrator vibe;
    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_online);

        init();
    }

    /**
     * Initialize all components.
     */
    private void init() {
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        forgotPassword = (TextView) findViewById(R.id.forgotPasswordText);
        signIn = (Button) findViewById(R.id.signInOnlineBtn);
        orText = (TextView) findViewById(R.id.orText);
        signInText = (TextView) findViewById(R.id.sigInText);
        fingerprintBtn = (ImageButton) findViewById(R.id.imgBtnFingerprintOnline);
        createOnlineUser = (TextView)  findViewById(R.id.registerNowOnlineText);
        userEmail = (EditText) findViewById(R.id.inputEmailOnlineText);
        userPass = (EditText) findViewById(R.id.inputPassOnlineText);
        showPassword = (CheckBox) findViewById(R.id.showPasswordCB);
        rememberMe = (CheckBox) findViewById(R.id.rememberMeCB);
        loadingBar = new ProgressDialog(OnlineLogin.this, Theme_Material_Light_Dialog_Alert);

        //Store email and pass in shared preferences
        sharedPreferencesOnline = getSharedPreferences("dataOnline",MODE_PRIVATE);
        email = sharedPreferencesOnline.getString("emailOnline", "");
        password = sharedPreferencesOnline.getString("passwordOnline", "");

        forgotPassword.setOnClickListener(v -> goToForgotPassForm());
        signIn.setOnClickListener(v -> {
            email = userEmail.getText().toString();
            password = userPass.getText().toString();

            if (!isNetworkConnected()) {
                displayToast("Device not connected to WiFI or Mobile Data. Please connect first.");
            }
            else {
                if (internetIsConnected()) {
                    if (!email.equals("") && !password.equals("")) {
                        showProgressDialog("Logging in as " + email);
                        signInFirebase(email, password);
                        clearEditText();
                    }
                    else {
                        Toast.makeText(OnlineLogin.this, "Incorrect Credentials. Please try again.", Toast.LENGTH_SHORT).show();
                        clearEditText();
                    }
                }
                else {
                    displayToast("Device not connected to the internet. Please connect first.");
                }
            }
        });
        showPassword();
        createOnlineUser.setOnClickListener(v -> goToRegisterOnlineUser());
        biometricInit();
        checkRememberCBState();

        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
        fingerprintBtn.setOnClickListener(view -> {
            biometricPrompt.authenticate(promptInfo);
            vibe.vibrate(50);
        });
    }

    /**
     * Initialize the Biometrics.
     */
    public void biometricInit() {
        //Initialization of biometric manager for fingerprint
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "App can authenticate using biometrics. ");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                //Log.e("MY_APP_TAG", "No biometric features available on this device.");
                Toast.makeText(this, "Sensor not available on this device.", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                //Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                Toast.makeText(this, "No biometric features available on this device.", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                Toast.makeText(this, "Register at least one fingerprint on your device.", Toast.LENGTH_SHORT).show();
                final Intent enrollIntent = new Intent(android.provider.Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(android.provider.Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                startActivity(enrollIntent);
                break;
        }

        executeBiometricPrompt();
    }

    /**
     * Show the Biometric prompt to the user.
     */
    private void executeBiometricPrompt() {
        //Executes biometric prompt window
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(OnlineLogin.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                                "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
                vibe.vibrate(50);
            }

            //This method is run after a successful authentication
            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                if (!isNetworkConnected()) {
                    displayToast("Device not connected to WiFi or Mobile Data. Please connect first.");
                }
                else {
                    if (internetIsConnected()) {
                        showProgressDialog("Logging in as " + email);
                        //Use stored credentials in the sharedPreferences to log-in
                        signInFirebase(email, password);
                    } else {
                        displayToast("Device not connected to the internet. Please connect first.");
                    }
                }
            }

            //When authentication failed
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed. Please tap the sensor again.",
                                Toast.LENGTH_SHORT)
                        .show();
                vibe.vibrate(50);
            }
        });

        //Shows prompt window for fingerprint.
        if (Build.VERSION.SDK_INT >= 29) {
            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric LOGIN")
                    .setSubtitle("Log in using your fingerprint.")
                    .setAllowedAuthenticators(DEVICE_CREDENTIAL | BIOMETRIC_STRONG)
                    .build();
        } else {
            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric LOGIN")
                    .setSubtitle("Log in using your fingerprint.")
                    .setNegativeButtonText("Cancel")
                    .build();
        }
    }

    /**
     * TODO: Use token instead of user's credentials.
     * Handles the remember me check box which saves the user's credentials to the SharedPreference object.
     */
    private void rememberCredentials() {
        rememberMe.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (rememberMe.isChecked()) {
                if (userEmail.getText().toString().equals("") && userPass.getText().toString().equals("")) {
                    displayToast("Credentials are empty. Nothing to remember.");
                    rememberMe.setChecked(false);
                }
                else {
                    sharedPreferencesOnline = getSharedPreferences("rememberMeCBOnline", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferencesOnline.edit();
                    editor.putString("rememberMeOnline", "true");
                    editor.apply();
                    displayToast("Credentials remembered.");
                }
            }
            else {
                sharedPreferencesOnline = getSharedPreferences("rememberMeCBOnline", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferencesOnline.edit();
                editor.putString("rememberMeOnline", "false");
                editor.apply();
            }
        });
    }

    /**
     * Checks if the "Remember Me" check box was checked by the last user who logged in.
     * If it is checked, previous logged in user will be automatically redirected to the Home Screen.
     * If not, user will need to enter their credentials first.
     */
    private void checkRememberCBState() {
        sharedPreferencesOnline = getSharedPreferences("rememberMeCBOnline", MODE_PRIVATE);
        String rememberMeVal = sharedPreferencesOnline.getString("rememberMeOnline", "");

        sharedPreferencesOnline= getSharedPreferences("user_fingerprint_preference", MODE_PRIVATE);
        boolean checkBiometricsPreferences = sharedPreferencesOnline.getBoolean("switch_status", false);

        if (rememberMeVal.equals("true")) {
            if (checkBiometricsPreferences) {
                biometricPrompt.authenticate(promptInfo);
                vibe.vibrate(50);

                orText.setVisibility(View.VISIBLE);
                signInText.setVisibility(View.VISIBLE);
                fingerprintBtn.setVisibility(View.VISIBLE);
            } else {
                if (!isNetworkConnected()) {
                    displayToast("Device not connected to WiFi or Mobile Data. Please connect first.");
                }
                else {
                    if (internetIsConnected()) {
                        showProgressDialog("Logging in as " + email);
                        //Use stored credentials in the sharedPreferences to log-in
                        signInFirebase(email, password);
                    } else {
                        displayToast("Device not connected to the internet. Please connect first.");
                    }
                }
            }
        } else {
            orText.setVisibility(View.INVISIBLE);
            signInText.setVisibility(View.INVISIBLE);
            fingerprintBtn.setVisibility(View.INVISIBLE);
        }
        rememberCredentials();
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
     * Go to the forgot password activity.
     */
    public void goToForgotPassForm() {
        Intent goToForgotPassIntent = new Intent(this, ForgotPasswordOnline.class);
        startActivity(goToForgotPassIntent);
        onStartNewActivityAnimations();
    }

    /**
     * Clears the EditText for the user email and password.
     */
    private void clearEditText() {
        userEmail.getText().clear();
        userPass.getText().clear();
    }

    //Method for signing in to Firebase server.
    public void signInFirebase(String email, String password) {
        showProgressDialog("Logging in as " + email);

        if (!email.isEmpty() && !password.isEmpty()) {
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    loadingBar.dismiss();
                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                    switch (errorCode) {
                        case "ERROR_INVALID_CREDENTIAL":
                            Toast.makeText(OnlineLogin.this, "The supplied auth credential is malformed or has expired.", Toast.LENGTH_LONG).show();
                            break;

                        case "ERROR_INVALID_EMAIL":
                            Toast.makeText(OnlineLogin.this, "The email address is badly formatted.", Toast.LENGTH_LONG).show();
                            userEmail.setError("The email address is badly formatted.");
                            userEmail.requestFocus();
                            break;

                        case "ERROR_WRONG_PASSWORD":
                            Toast.makeText(OnlineLogin.this, "The password is invalid or the user does not have a password.", Toast.LENGTH_LONG).show();
                            userPass.setError("password is incorrect ");
                            userPass.requestFocus();
                            userPass.setText("");
                            break;

                        case "ERROR_USER_MISMATCH":
                            Toast.makeText(OnlineLogin.this, "The supplied credentials do not correspond to the previously signed in user.", Toast.LENGTH_LONG).show();
                            break;

                        case "ERROR_USER_DISABLED":
                            Toast.makeText(OnlineLogin.this, "The user account has been disabled by an administrator.", Toast.LENGTH_LONG).show();
                            break;

                        case "ERROR_USER_TOKEN_EXPIRED":
                        case "ERROR_INVALID_USER_TOKEN":
                            Toast.makeText(OnlineLogin.this, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                            break;

                        case "ERROR_USER_NOT_FOUND":
                            Toast.makeText(OnlineLogin.this, "There is no user record corresponding to this identifier. The user may have been deleted.", Toast.LENGTH_LONG).show();
                            break;

                        case "ERROR_OPERATION_NOT_ALLOWED":
                            Toast.makeText(OnlineLogin.this, "This operation is not allowed. You must enable this service in the console.", Toast.LENGTH_LONG).show();
                            break;
                    }
                }
                else {
                    SharedPreferences.Editor editor = getSharedPreferences("dataOnline",MODE_PRIVATE).edit();
                    editor.putString("emailOnline", email);
                    editor.putString("passwordOnline", password);
                    editor.putBoolean("isLoginOnline", true);
                    editor.apply();

                    Intent intent = new Intent(OnlineLogin.this, HomeScreenOnline.class);
                    intent.putExtra("email", email);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    onStartNewActivityAnimations();
                    loadingBar.dismiss();
                    finish();
                }
            });
        } else {
            loadingBar.dismiss();
            displayToast("Email and Password is empty. Please input first.");
        }
    }

    /**
     * A function that handles the show password checkbox.
     */
    private void showPassword() {
        showPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                    userPass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            else {
                    userPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
    }

    private void displayToast (String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    //Goes to register user activity
    public void goToRegisterOnlineUser() {
        Intent registerOnlineIntent = new Intent (this, OnlineCreateUser.class);
        startActivity(registerOnlineIntent);
        onStartNewActivityAnimations();
    }

    /**
     * Show a progress dialog after signing in.
     * @param message the message of the dialog.
     */
    public void showProgressDialog(String message) {
        loadingBar.setTitle("LOGGING IN");
        loadingBar.setMessage(message);
        loadingBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //Handles the animation when going from activity to activity.
    protected void onStartNewActivityAnimations() {
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }
}