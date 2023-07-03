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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.HashMap;

public class EditProfile extends AppCompatActivity {
TextView logout;
EditText edtname,edtpassword,edtrepass,edtemail;
MaterialButton savebtn;
ProgressDialog progressDialog;
String nameget=null;
ImageView profilepic;
String profile;
Uri uri;
StorageReference storageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        storageRef = FirebaseStorage.getInstance().getReference().child("UserImages");

        logout=findViewById(R.id.logoutbtn);
        edtname=findViewById(R.id.edtName);
        edtpassword=findViewById(R.id.edtPassword);
        edtrepass=findViewById(R.id.edtrepass);
        edtemail=findViewById(R.id.edtemail);
        savebtn=findViewById(R.id.save);
        profilepic=findViewById(R.id.profilepic);
        progressDialog=new ProgressDialog(EditProfile.this);
        setListeners();
        setContent();


    }



    private void setListeners() {
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(EditProfile.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        profilepic.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,
                    "Select Picture"), 1);
        });
        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                if(changenameonly()){
                    progressDialog.setTitle("Changings in Progress...");
                    progressDialog.setMessage("Please Wait...");
                    progressDialog.setIcon(R.mipmap.ic_launcher_round);
                    progressDialog.show();
                    thenchangename();
                }
                else if(passwordchangeonly()){
                    if(edtrepass.getText().toString().isEmpty()||edtrepass.getText().toString().length()<6){
                        Toast.makeText(EditProfile.this, "Enter Valid New Password", Toast.LENGTH_SHORT).show();
                        edtrepass.setError("Neccessary to change password");
                    }
                    else {
                        progressDialog.setTitle("Changings in Progress...");
                        progressDialog.setMessage("Please Wait...");
                        progressDialog.setIcon(R.mipmap.ic_launcher_round);
                        progressDialog.show();
                        thenchangepassword();
                    }
                }
                else if(emailchangesonly()){
                    if(edtpassword.getText().toString().isEmpty()){
                        Toast.makeText(EditProfile.this, "Enter Valid Old Password", Toast.LENGTH_SHORT).show();
                        edtpassword.setError("Neccessary to change Email");
                    }else{
                        progressDialog.setTitle("Changings in Progress...");
                        progressDialog.setMessage("Please Wait...");
                        progressDialog.setIcon(R.mipmap.ic_launcher_round);
                        progressDialog.show();
                        thenchangeEmail();
                    }

                    
                }
                else if(changenameandpassonly()){

                        progressDialog.setTitle("Changings in Progress...");
                        progressDialog.setMessage("Please Wait...");
                        progressDialog.setIcon(R.mipmap.ic_launcher_round);
                        progressDialog.show();
                        thenchangepassword();
                        thenchangename();


                }
                else if(changenameemailandpassonly()){


                        progressDialog.setTitle("Changings in Progress...");
                        progressDialog.setMessage("Please Wait...");
                        progressDialog.setIcon(R.mipmap.ic_launcher_round);
                        progressDialog.show();
                        thenchangepassword();
                        thenchangename();
                        thenchangeEmail();


                }
                else if(passwordandemailonly()){

                        progressDialog.setTitle("Changings in Progress...");
                        progressDialog.setMessage("Please Wait...");
                        progressDialog.setIcon(R.mipmap.ic_launcher_round);
                        progressDialog.show();
                        thenchangepassword();
                        thenchangeEmail();


                }
                else if(changenameandemailonly()){
                    if(edtpassword.getText().toString().isEmpty()){
                        Toast.makeText(EditProfile.this, "Enter Valid Entries", Toast.LENGTH_SHORT).show();
                        edtpassword.setError("Neccessary to change Email");
                    }
                    else{
                        progressDialog.setTitle("Changings in Progress...");
                        progressDialog.setMessage("Please Wait...");
                        progressDialog.setIcon(R.mipmap.ic_launcher_round);
                        progressDialog.show();
                       thenchangename();
                        thenchangeEmail();
                    }

                }
                else if(nothingchanges()){
                    progressDialog.dismiss();
                    finish();
                }


            }
        });

    }



    private boolean passwordandemailonly() {
        if(edtname.getText().toString().isEmpty()&&!edtemail.getText().toString().isEmpty()&&!edtpassword.getText().toString().isEmpty()&&!edtrepass.getText().toString().isEmpty()){
            return true;
        }
        else {
            return false;
        }
    }

    private boolean changenameemailandpassonly() {
        if(!edtname.getText().toString().isEmpty()&&!edtemail.getText().toString().isEmpty()&&!edtpassword.getText().toString().isEmpty()&&!edtrepass.getText().toString().isEmpty()){
            return true;
        }
        else {
            return false;
        }
    }


    private void thenchangeEmail() {
        progressDialog.setTitle("Changing Email only...");
        String currentEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, edtpassword.getText().toString().trim());

        FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseAuth.getInstance().getCurrentUser().updateEmail(edtemail.getText().toString().trim())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(EditProfile.this, "Email changed successfully", Toast.LENGTH_LONG).show();
                                                performChangeEmailonRealtimeDatabase();

                                            }
                                            progressDialog.dismiss();
                                        }
                                    });
                        } else {
                            Toast.makeText(EditProfile.this, "Authentication failed, wrong password?", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private void performChangeEmailonRealtimeDatabase() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid());
        HashMap<String,Object> map=new HashMap<>();
        map.put("email",edtemail.getText().toString().trim());
        ref.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        });

    }

    private boolean emailchangesonly() {
        if(edtname.getText().toString().isEmpty()&&!edtemail.getText().toString().isEmpty()&&edtpassword.getText().toString().isEmpty()&&edtrepass.getText().toString().isEmpty()){
            return true;
        }
        else{
            return false;
        }
    }

    private void thenchangepassword() {
        progressDialog.setTitle("Changing Password only...");
        String currentEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, edtpassword.getText().toString().trim());

        FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseAuth.getInstance().getCurrentUser().updatePassword(edtrepass.getText().toString().trim())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(EditProfile.this, "Password changed successfully", Toast.LENGTH_LONG).show();
                                                performChangePasswordonRealtimeDatabase();

                                            }
                                            progressDialog.dismiss();
                                        }
                                    });
                        } else {
                            Toast.makeText(EditProfile.this, "Authentication failed, wrong password?", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private void performChangePasswordonRealtimeDatabase() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid());
        HashMap<String,Object> map=new HashMap<>();
        map.put("password",edtrepass.getText().toString().trim());
        ref.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        });

    }

    private boolean passwordchangeonly() {
        if(edtname.getText().toString().isEmpty()&&edtemail.getText().toString().isEmpty()&&!edtpassword.getText().toString().isEmpty()&&!edtrepass.getText().toString().isEmpty()){
            return true;
        }
        else{
            return false;
        }
    }

    private boolean nothingchanges() {
        if(edtname.getText().toString().isEmpty()&&edtemail.getText().toString().isEmpty()&&edtpassword.getText().toString().isEmpty()&&edtrepass.getText().toString().isEmpty()){
            return true;
        }
        else{
            return false;
        }
    }

    private void thenchangename() {
       if(nameget.equalsIgnoreCase(edtname.getText().toString().trim())) {
           progressDialog.dismiss();


       }
       else{
           progressDialog.setTitle("Changing Name Only...");
           DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid());
           HashMap<String, Object> map = new HashMap<>();
           map.put("username", edtname.getText().toString().trim());
           ref.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
               @Override
               public void onSuccess(Void unused) {
                   progressDialog.dismiss();

               }
           });
       }

    }

    private Boolean changenameonly() {
        if(!edtname.getText().toString().isEmpty()&&edtemail.getText().toString().isEmpty()&&edtpassword.getText().toString().isEmpty()&&edtrepass.getText().toString().isEmpty()){
            return true;
        }
        else {
            return false;
        }
    }
    private Boolean changenameandpassonly() {
        if(!edtname.getText().toString().isEmpty()&&edtemail.getText().toString().isEmpty()&&!edtpassword.getText().toString().isEmpty()&&!edtrepass.getText().toString().isEmpty()){
            return true;
        }
        else {
            return false;
        }
    }
    private Boolean changenameandemailonly() {
        if(!edtname.getText().toString().isEmpty()&&!edtemail.getText().toString().isEmpty()&&edtpassword.getText().toString().isEmpty()&&edtrepass.getText().toString().isEmpty()){
            return true;
        }
        else {
            return false;
        }
    }
    private void setContent() {
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Getting your info");
        progressDialog.setIcon(R.mipmap.ic_launcher_round);
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               nameget= snapshot.child("username")
                       .getValue(String.class);
               edtname.setText(nameget);
                profile=snapshot.child("profilepic").getValue(String.class);
                Glide.with(getApplicationContext()).load(profile).into(profilepic);


                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                uri = data.getData();
                profilepic.setImageURI(uri);
                uploadpic(uri);
            }
        }
    }

    private void uploadpic(Uri toString) {
        progressDialog.setTitle("Please Wait...");
        progressDialog.setMessage("Changing Profile Picture...");
        progressDialog.setIcon(R.mipmap.ic_launcher_round);
        progressDialog.show();
            storageRef.child(new Date().toString()).putFile(toString).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
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
                                            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid());
                                            HashMap<String,Object> map=new HashMap<>();
                                            map.put("profilepic",url);
                                            ref.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(EditProfile.this, "Image Changed..", Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                }
                                            });



                                        } else {
                                        }

                                    }
                                });
                            }
                        });



                    } else {

                    }

                }
            });

    }

}