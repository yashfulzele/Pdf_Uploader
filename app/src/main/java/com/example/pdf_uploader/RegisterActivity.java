package com.example.pdf_uploader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText usernameEt, emailEt, passwordEt;
    private MaterialButton registerBt;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViews();
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("User");

        registerBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateUsername() && validateEmail() && validatePassword()){
                    final String username = Objects.requireNonNull(usernameEt.getText()).toString();
                    final String email = Objects.requireNonNull(emailEt.getText()).toString();
                    String password = Objects.requireNonNull(passwordEt.getText()).toString();
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                                        FirebaseUser user = mAuth.getCurrentUser();

                                        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest
                                                .Builder()
                                                .setDisplayName(username)
                                                .build();
                                        user.updateProfile(profileChangeRequest);

                                        Map<String, String> userMap = new HashMap<>();
                                        userMap.put("uid", uid);
                                        userMap.put("username", username);
                                        userMap.put("email", email);

                                        dbRef.child(uid)
                                                .setValue(userMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(RegisterActivity.this, "Registration complete!", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(RegisterActivity.this, UploaderPageActivity.class));
                                                        RegisterActivity.this.finish();
                                                    }
                                                });
                                    }
                                }
                            });
                }
            }
        });
    }

    private boolean validateUsername() {
        String username = Objects.requireNonNull(usernameEt.getText()).toString();
        if (username.isEmpty()) {
            usernameEt.setError("Username field can't be empty!");
            return false;
        } else {
            usernameEt.setError(null);
            return true;
        }
    }

    private boolean validateEmail() {
        String email = Objects.requireNonNull(emailEt.getText()).toString();
        if (email.isEmpty()) {
            emailEt.setError("Email field can't be empty!");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEt.setError("Invalid Email!");
            return false;
        } else {
            emailEt.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String password = Objects.requireNonNull(passwordEt.getText()).toString();
        if (password.isEmpty()) {
            passwordEt.setError("Password field can't be empty!");
            return false;
        } else if (password.length() < 6) {
            passwordEt.setError("Minimum length of password should be 6.");
            return false;
        } else {
            passwordEt.setError(null);
            return true;
        }
    }

    private void initViews() {
        usernameEt = findViewById(R.id.username);
        emailEt = findViewById(R.id.email);
        passwordEt = findViewById(R.id.password);
        registerBt = findViewById(R.id.register_button);
    }
}