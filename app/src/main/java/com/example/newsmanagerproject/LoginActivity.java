package com.example.newsmanagerproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newsmanagerproject.services.ModelManager;
import com.example.newsmanagerproject.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private CheckBox rememberMeCheckbox;
    private Button loginButton;
    private Button cancelButton;
    private ProgressBar progressBar;
    private TextView errorText;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        
        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        rememberMeCheckbox = findViewById(R.id.remember_me_checkbox);
        loginButton = findViewById(R.id.login_submit_button);
        cancelButton = findViewById(R.id.cancel_button);
        progressBar = findViewById(R.id.login_progress);
        errorText = findViewById(R.id.login_error_text);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void attemptLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            usernameInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        // Show progress
        showLoading(true);
        errorText.setVisibility(View.GONE);

        // Perform login in background thread
        Thread loginThread = new Thread(() -> {
            try {
                ModelManager mm = ModelManager.getInstance();
                mm.login(username, password);

                // Login successful
                runOnUiThread(() -> {
                    showLoading(false);
                    
                    // Save credentials if Remember Me is checked
                    if (rememberMeCheckbox.isChecked()) {
                        sessionManager.saveSession(mm.getIdUser(), mm.getAuthType(), mm.getApikey(), username);
                    }
                    
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                // Login failed
                runOnUiThread(() -> {
                    showLoading(false);
                    errorText.setText("Login failed: " + e.getMessage());
                    errorText.setVisibility(View.VISIBLE);
                });
            }
        });
        loginThread.start();
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
        usernameInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
    }
}
