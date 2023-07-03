package com.saarimtech.findmyphone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class Login extends AppCompatActivity {
Button loginbtn;
EditText logemail,logpassword;
TextView signupredirect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        loginbtn=findViewById(R.id.signinbtn);
        logemail=findViewById(R.id.logEmail);
        logpassword=findViewById(R.id.logPassword);
        signupredirect=findViewById(R.id.signupredirect);

        setListeners();
    }

    private void setListeners() {
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!getError()){
                    loginUser();
                }
            }
        });
        signupredirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Login.this,SignUp.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private Boolean getError(){
        if(logemail.getText().toString().isEmpty()) {
            logemail.setError("Enter E-Mail");
            return true;
        }

        else if(logpassword.getText().toString().isEmpty()) {
            logpassword.setError("Enter Password");
            return true;
        }
        else
            return false;
    }
    private void loginUser() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(logemail.getText().toString().toLowerCase(Locale.ROOT).trim(),logpassword.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Intent intent=new Intent(Login.this,MainActivity.class);

                startActivity(intent);
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Login.this, "Failed to login: "+e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
