package com.example.pdf_uploader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText emailEt, passwordEt;
    private MaterialButton loginBt;
    private TextView registerTv;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        mAuth = FirebaseAuth.getInstance();

        loginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateEmail() && validatePassword()) {
                    String email = Objects.requireNonNull(emailEt.getText()).toString().trim();
                    String password = Objects.requireNonNull(passwordEt.getText()).toString().trim();

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        updateUI();
                                        startActivity(new Intent(MainActivity.this, UploaderPageActivity.class));
                                        MainActivity.this.finish();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Error occurred!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        registerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                MainActivity.this.finish();
            }
        });
    }

    private void updateUI() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "User is logged in!", Toast.LENGTH_SHORT).show();
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
        emailEt = findViewById(R.id.email);
        passwordEt = findViewById(R.id.password);
        loginBt = findViewById(R.id.login_button);
        registerTv = findViewById(R.id.register);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.signOut();
        updateUI();
    }
}