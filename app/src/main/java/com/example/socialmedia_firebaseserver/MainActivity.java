package com.example.socialmedia_firebaseserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private EditText edtemail,edtusername,edtpassword;
    private Button btnsignup,btnsignin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        edtemail = findViewById(R.id.edtEmail);
        edtusername = findViewById(R.id.edtusername);
        edtpassword = findViewById(R.id.edtPassword);
        btnsignin = findViewById(R.id.btnSignIn);
        btnsignup = findViewById(R.id.btnSignUp);

        btnsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUp();
            }
        });
        btnsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             SignIn();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //Transition to next activity
            transitiontosocialmediaactivity();
        }
    }

    private void SignUp(){

        mAuth.createUserWithEmailAndPassword(edtemail.getText().toString(), edtpassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(MainActivity.this, "SignUp successful", Toast.LENGTH_SHORT).show();
                            FirebaseDatabase.getInstance().getReference().child("my_users").child(task.getResult().getUser().getUid())
                                    .child("username").setValue(edtusername.getText().toString());

                            UserProfileChangeRequest profileupdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(edtusername.getText().toString())
                                    .build();

                            FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileupdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(MainActivity.this, "Display name updated", Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });

                            transitiontosocialmediaactivity();

                        } else {

                            Toast.makeText(MainActivity.this, "SignUp failed", Toast.LENGTH_SHORT).show();

                        }

                    }
                });

    }
  private void SignIn(){

        mAuth.signInWithEmailAndPassword(edtemail.getText().toString(), edtpassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    Toast.makeText(MainActivity.this, "SignIn successful", Toast.LENGTH_SHORT).show();
                    transitiontosocialmediaactivity();

                }else {

                    Toast.makeText(MainActivity.this, "SignIn failed", Toast.LENGTH_SHORT).show();
                }

            }
        });

  }

  private void transitiontosocialmediaactivity(){

      Intent intent = new Intent(MainActivity.this, SocialMediaActivity.class);
      startActivity(intent);
  }
}
