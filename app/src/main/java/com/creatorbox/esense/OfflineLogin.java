/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
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

import java.util.concurrent.Executor;

/**
 * OFFLINE MODE
 * An activity that handles the OFFLINE user login.
 */
public class OfflineLogin extends AppCompatActivity {

    public static ImageButton fingerprintBtn;
    public static TextView signInTextView;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private SharedPreferences sharedPreferencesOffline;

    private EditText emailVar, passwordVar;
    private TextView registerNow;
    private Button signIn;
    private DBHelper DB;
    private Vibrator vibe;
    private ProgressDialog progressDialog;
    private Boolean checkUserPass, checkUser;
    private CheckBox showPassword, rememberMe;
    public static final int Theme_Material_Light_Dialog_Alert = 0;
    private String emailSignInVal, passSignInVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_offline);

        init();
    }

    /**
     * Initialize all components.
     */
    private void init() {
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        registerNow = (TextView) findViewById(R.id.registerNowOfflineText);
        emailVar = (EditText) findViewById(R.id.inputEmailOfflineText);
        passwordVar = (EditText) findViewById(R.id.inputPassOfflineText);
        signIn = (Button) findViewById(R.id.signInOfflineBtn);
        fingerprintBtn = (ImageButton)findViewById(R.id.imgBtnFingerprint);
        signInTextView = (TextView)findViewById(R.id.sigInTextOffline);
        showPassword = (CheckBox) findViewById(R.id.showPasswordCBOffline);
        rememberMe = (CheckBox) findViewById(R.id.rememberMeCBOffline);
        progressDialog = new ProgressDialog(OfflineLogin.this, Theme_Material_Light_Dialog_Alert);
        DB = new DBHelper(this);

        registerNow.setOnClickListener(v -> createNewAccountActivity());

        signIn.setOnClickListener(v -> {
            emailSignInVal = emailVar.getText().toString();
            passSignInVal= passwordVar.getText().toString();
            if (emailSignInVal.equals("")) {
                Toast.makeText(OfflineLogin.this, "Email is empty. Please try again.", Toast.LENGTH_SHORT).show();
            }
            else if (passSignInVal.equals("")) {
                Toast.makeText(OfflineLogin.this, "Password is empty. Please try again.", Toast.LENGTH_SHORT).show();
            }
            else {
                showProgressDialog("Logging in " + emailSignInVal);
                checkUserPass = DB.checkEmailPassword(emailSignInVal, passSignInVal);
                if (checkUserPass) {
                    SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                    editor.putString("email", emailSignInVal);
                    editor.putString("password", passSignInVal);
                    editor.putBoolean("isLogin", true);
                    editor.apply();

                    Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        Intent intentSignIn = new Intent(OfflineLogin.this, HomeScreenOffline.class);
                        intentSignIn.putExtra("email", emailSignInVal);
                        startActivity(intentSignIn);
                        onStartNewActivityAnimations();

                        emailVar.getText().clear();
                        passwordVar.getText().clear();
                        progressDialog.dismiss();
                        finish();
                    }, 1000);
                }
                else {
                    Toast.makeText(OfflineLogin.this,
                            "Invalid Credentials. Please try again.", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
        biometricsInit();
        checkRememberCBState();
        showPassword();

        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
        fingerprintBtn.setOnClickListener(view -> {
            biometricPrompt.authenticate(promptInfo);
            vibe.vibrate(50);
        });
    }

    /**
     * Initialize Biometrics.
     */
    private void biometricsInit() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "App can authenticate using biometrics. ");
                enableButton(true);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                //Log.e("MY_APP_TAG", "No biometric features available on this device.");
                Toast.makeText(this, "Sensor not available on this device.", Toast.LENGTH_SHORT).show();
                enableButton(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                //Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                Toast.makeText(this, "No biometric features available on this device.", Toast.LENGTH_SHORT).show();
                enableButton(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                Toast.makeText(this, "Register at least one fingerprint on your device.", Toast.LENGTH_SHORT).show();
                enableButton(false, true);
                final Intent enrollIntent = new Intent(android.provider.Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(android.provider.Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                startActivity(enrollIntent);
                break;
        }

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(OfflineLogin.this,
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

            /**
             * A method that handles result of the authentication.
             * @param result a success result will check the users credentials and if successful, log them in
             *               to the app.
             */
            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                sharedPreferencesOffline = getSharedPreferences("data",MODE_PRIVATE);
                boolean isLogin = sharedPreferencesOffline.getBoolean("isLogin", false);

                if (isLogin) checkUserCredentialsForFingerprint();
                else displayToast("Please sign in first before using the fingerprint.");
            }

            /**
             * When biometrics fails to register. This is called everytime the biometrics fails.
             */
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed. Please tap the sensor again.",
                                Toast.LENGTH_SHORT)
                        .show();
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

    void enableButton(boolean enable) {
        fingerprintBtn.setEnabled(enable);
    }

    void enableButton(boolean enable, boolean enroll) {
        enableButton(enable);
        if (!enroll) {
            Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
            enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    BIOMETRIC_STRONG | BIOMETRIC_WEAK);
            startActivity(enrollIntent);
        }
    }

    /**
     * Check user Credentials for using the fingerprint authentication
     */
    private void checkUserCredentialsForFingerprint() {
        String email = sharedPreferencesOffline.getString("email", "");
        String password = sharedPreferencesOffline.getString("password", "");
        showProgressDialog("Logging in " + email);

        checkUser = DB.checkEmailPassword(email, password);
        if (checkUser) {
            SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
            editor.putString("email", email);
            editor.putString("password", password);
            editor.putBoolean("isLogin", true);
            editor.apply();

            Handler handler = new Handler();
            handler.postDelayed(() -> {
                Intent intentSignIn = new Intent(OfflineLogin.this, HomeScreenOffline.class);
                intentSignIn.putExtra("email", emailSignInVal);
                startActivity(intentSignIn);
                onStartNewActivityAnimations();

                emailVar.getText().clear();
                passwordVar.getText().clear();
                progressDialog.dismiss();
                finish();
            }, 1000);
        }
        else {
            Toast.makeText(getApplicationContext(),
                    "User not found. Authentication failed.", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    /**
     * A function that handles the show password checkbox.
     */
    private void showPassword() {
        showPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwordVar.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            else {
                passwordVar.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
    }

    /**
     * A function that handles the remember me checkbox to remember user's credentials.
     */
    private void rememberCredentials() {
        rememberMe.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (rememberMe.isChecked()) {
                if (emailVar.getText().toString().equals("") && passwordVar.getText().toString().equals("")) {
                    displayToast("Credentials are empty. Nothing to remember.");
                    rememberMe.setChecked(false);
                }
                else {
                    sharedPreferencesOffline = getSharedPreferences("rememberMeCB", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferencesOffline.edit();
                    editor.putString("remember", "true");
                    editor.apply();
                    displayToast("Credentials remembered.");
                }
            }
            else {
                sharedPreferencesOffline = getSharedPreferences("rememberMeCB", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferencesOffline.edit();
                editor.putString("remember", "false");
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
        sharedPreferencesOffline = getSharedPreferences("rememberMeCB", MODE_PRIVATE);
        String rememberMeVal = sharedPreferencesOffline.getString("remember", "");

        sharedPreferencesOffline = getSharedPreferences("user_fingerprint_preference_offline", MODE_PRIVATE);
        boolean checkBiometricsPreferences = sharedPreferencesOffline.getBoolean("switch_status_offline", false);

        sharedPreferencesOffline = getSharedPreferences("data",MODE_PRIVATE);
        String email = sharedPreferencesOffline.getString("email", "");
        String password = sharedPreferencesOffline.getString("password", "");

        if (!rememberMeVal.equals("true")) {
            //displayToast("Please sign in first.");
            signInTextView.setVisibility(View.INVISIBLE);
            fingerprintBtn.setVisibility(View.INVISIBLE);
        }
        else {
            if (checkBiometricsPreferences) {
                biometricPrompt.authenticate(promptInfo);
                vibe.vibrate(50);

                signInTextView.setVisibility(View.VISIBLE);
                fingerprintBtn.setVisibility(View.VISIBLE);
            }
            else {
                checkUserCredentialsForFingerprint();
            }
        }
        rememberCredentials();
    }

    /**
     * Show progress dialog.
     * @param message the message of the progress dialog.
     */
    public void showProgressDialog(String message) {
        progressDialog.setTitle("LOGGING IN");
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    /**
     * Go to offline account creation activity.
     */
    public void createNewAccountActivity() {
        Intent intentCreate = new Intent(this, OfflineCreateUser.class);
        startActivity(intentCreate);
        onStartNewActivityAnimations();
    }

    /**
     * Display a toast message.
     * @param message the message of the toast.
     */
    private void displayToast(String message) {
        Toast.makeText(OfflineLogin.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //Handles animation when going from activity to activity.
    protected void onStartNewActivityAnimations() {
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }
}