package com.poc.dropme;

import static android.text.TextUtils.isEmpty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.poc.dropme.Driver.DriverHome;
import com.poc.dropme.Gadgets.InternetConnection.ConnectionReceiver;
import com.poc.dropme.Gadgets.PrefManager;
import com.poc.dropme.Passenger.PassengerActivity;

public class LoginActivity extends AppCompatActivity implements ConnectionReceiver.ReceiverListener {
    // UI references.
    private EditText mUsername;
    private EditText mEmailView;
    private EditText mPasswordView, mNum, mName;
    PrefManager prefManager;
    String username = "";
    String email = "";
    String password = "";
    String name = "";
    String number = "";


    private CheckBox checkBoxRememberMe;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        prefManager = new PrefManager(LoginActivity.this);
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mUsername = (EditText) findViewById(R.id.username);
        mNum = (EditText) findViewById(R.id.number);
        mName = (EditText) findViewById(R.id.name);


        askpermission();
        if (prefManager.getUsername().equals("employee")) {
            Intent i = new Intent(LoginActivity.this, PassengerHome.class);
            startActivity(i);

        } else if (prefManager.getUsername().equals("driver")) {
            Intent i = new Intent(LoginActivity.this, DriverHome.class);
            startActivity(i);

        }


        mUsername.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.username || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });


        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.password || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.name || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mNum.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.number || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });


        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLoginDetails(email, username, name, number);
                attemptLogin();
               /* Intent intent = new Intent(LoginActivity.this,DriverHome.class);
                startActivity(intent);
                finish();*/
            }
        });

        checkBoxRememberMe = (CheckBox) findViewById(R.id.checkBoxRememberMe);
        //Here we will validate saved preferences
        if (!prefManager.getEmail().equalsIgnoreCase("")) {
            //user's email and password both are saved in preferences
            startHomeActivity();
        }

    }

    private void askpermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mUsername.setError(null);
        mNum.setError(null);
        mName.setError(null);

        // Store values at the time of the login attempt.
        username = mUsername.getText().toString();
        email = mEmailView.getText().toString();
        password = mPasswordView.getText().toString();
        name = mName.getText().toString();
        number = mNum.getText().toString();
        username= username.replaceAll("\\s+", "");
        email=email.replaceAll("\\s+", "");

        boolean cancel = false;
        View focusView = null;

        if (!isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }  if (isEmpty(username)) {
            mUsername.setError(getString(R.string.error_field_required));
            focusView = mUsername;
            cancel = true;

        } if (isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;

        }  if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }  if (isEmpty(name)) {
            mName.setError(getString(R.string.error_field_required));
            focusView = mName;
            cancel = true;
        }  if (!isNumberValid(number)) {
            mNum.setError(getString(R.string.number_required));
            focusView = mNum;
            cancel = true;
        }  if (isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // save data in local shared preferences
            if (!checkBoxRememberMe.isChecked())
                saveLoginDetails(email, username, name, number);
            if (checkConnection()){
                startHomeActivity();
            }

        }
    }

    private boolean isNumberValid(String number) {
        boolean yes = false;
        if(number.length()==10){
            yes=true;
        }
        return  yes;
    }

    private void startHomeActivity() {
        if (username.equalsIgnoreCase("employee")) {
            Intent intent = new Intent(this, PassengerHome.class);
            startActivity(intent);
            finish();

        } else if (username.equalsIgnoreCase("driver")) {
            Intent intent = new Intent(this, DriverHome.class);
            startActivity(intent);
            finish();
        }
    }


    private boolean checkConnection() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.new.conn.CONNECTIVITY_CHANGE");
        registerReceiver(new ConnectionReceiver(), intentFilter);
        ConnectionReceiver.Listener = this;

        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
        showSnackBar(isConnected);
        return isConnected;
    }

    private void showSnackBar(boolean isConnected) {
        if (isConnected) {

        } else {
            Toast.makeText(getApplicationContext(), "Internet is not Connected / Please Turn On Internet", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveLoginDetails(String email, String username, String name, String number) {

        prefManager.saveEmail(email);
        prefManager.saveUsername(username);
        prefManager.saveName(name);
        prefManager.savePhoneNo(number);

    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains(".com");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        String upperCaseChars = "(.*[A-Z].*)";
        String lowerCaseChars = "(.*[a-z].*)";
        String numbers = "(.*[0-9].*)";

        return password.length() > 4;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                return;
            }
        }

    }

    @Override
    public void onNetworkChange(boolean isConnected) {
        showSnackBar(isConnected);
    }
}