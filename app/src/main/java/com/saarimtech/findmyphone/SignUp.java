package com.saarimtech.findmyphone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class SignUp extends AppCompatActivity {
TextView loginjump;
EditText regName,regEmail,regPassword;
MaterialButton signupbtn;
String uidget=null;
ProgressDialog dialog;
ImageView profilepic;

    StorageReference storageRef;
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        loginjump=findViewById(R.id.loginjump);
        regName=findViewById(R.id.regName);
        regEmail=findViewById(R.id.regEmail);
        regPassword=findViewById(R.id.regPassword);
        signupbtn=findViewById(R.id.signupbtn);
        profilepic=findViewById(R.id.profilepic);
        dialog=new ProgressDialog(SignUp.this);
        FirebaseAuth auth;
        setlisteners();


        storageRef = FirebaseStorage.getInstance().getReference().child("UserImages");
    }

    private void setlisteners() {
        loginjump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SignUp.this,Login.class);
                startActivity(intent);
                finish();
            }
        });
        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!getError()) {
                    dialog.setTitle("Signing Up..");
                    dialog.setMessage("Uploading Profile Picture..");
                    dialog.setIcon(R.mipmap.ic_launcher);
                    dialog.show();
                    uploaduserPicture();
                }

            }
        });

        profilepic.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,
                    "Select Picture"), 1);
        });

    }

    private void registerUsertoAuth(String uri) {
        dialog.setMessage("Completing Profile...");
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(regEmail.getText().toString().toLowerCase(Locale.ROOT).trim(),regPassword.getText().toString().trim()
        ).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Users");
                HashMap<Object,String> map=new HashMap<>();
                map.put("username",regName.getText().toString().trim());
                map.put("email",regEmail.getText().toString().trim().toLowerCase(Locale.ROOT));
                map.put("password",regPassword.getText().toString().trim());
                map.put("profilepic",uri);
                ref.child(authResult.getUser().getUid()).setValue(map);
                Toast.makeText(SignUp.this, "User Registered.", Toast.LENGTH_SHORT).show();
                loginUser();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(SignUp.this, "Error: "+e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser() {
        dialog.setTitle("Logging you in....");
        FirebaseAuth.getInstance().signInWithEmailAndPassword(regEmail.getText().toString().toLowerCase(Locale.ROOT).trim(),regPassword.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                dialog.dismiss();
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUp.this, "Failed to login: "+e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Boolean getError(){
        if(regName.getText().toString().isEmpty()) {
            regName.setError("Enter Full Name");
            return true;
        }
        else if(regEmail.getText().toString().isEmpty()) {
            regEmail.setError("Enter E-Mail Address");
            return true;
        }
        else if(regPassword.getText().toString().isEmpty()) {
            regPassword.setError("Enter Password");
            return true;
        }
        else if(uri==null){
            Toast.makeText(SignUp.this, "Select an Image", Toast.LENGTH_SHORT).show();
            return true;
        }
        else
            return false;
    }
    private void uploaduserPicture() {


        storageRef.child(new Date().toString()).putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        String url = task.getResult().toString();
                                        registerUsertoAuth(url);

                                    } else {
                                        Toast.makeText(SignUp.this, "Something is wrong, Check your internet connection", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }
                    });



                } else {

                    Toast.makeText(SignUp.this, "Something is wrong, Check your internet connection", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                uri = data.getData();
                profilepic.setImageURI(uri);
            }
        }
    }
}