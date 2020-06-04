package com.example.socialmedia_firebaseserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class SocialMediaActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private FirebaseAuth mAuth;
    private ImageView postimageview;
    private Button btncreatepost;
    private EditText edtdescription;
    private ListView userslistview;
    private Bitmap bitmap;
    private String imageidentifier;
    private ArrayList<String> usernames;
    private ArrayAdapter arrayAdapter;
    private ArrayList<String> uids;
    private String imagedownloadlink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media);
        mAuth = FirebaseAuth.getInstance();

        postimageview = findViewById(R.id.postimageview);
        btncreatepost = findViewById(R.id.btncreatepost);
        edtdescription = findViewById(R.id.edtdescription);
        userslistview = findViewById(R.id.userslistview);
        userslistview.setOnItemClickListener(this);
        uids = new ArrayList<>();

        usernames = new ArrayList<>();
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, usernames);
        userslistview.setAdapter(arrayAdapter);

        btncreatepost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadimagetoserver();
            }
        });

        postimageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT>=23 && ActivityCompat.checkSelfPermission(SocialMediaActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

                    requestPermissions(new String[]
                            {Manifest.permission.READ_EXTERNAL_STORAGE},1000);

                }else{
                    getchosenimage();
                }


            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.logoutitem:

                logout();
                break;

            case R.id.viewpostsitem:

                Intent intent = new Intent(this, ViewPostsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        logout();
    }

    private void logout(){
        mAuth.signOut();
        finish();

    }

    private void getchosenimage() {
       // FancyToast.makeText(SocialMediaActivity.this, "Permission Granted",
        //        Toast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 2000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1000){

            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getchosenimage();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){

            //Do something with the selected image.
            try{

                Uri selectedimage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = this.getContentResolver().query(selectedimage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnindex = cursor.getColumnIndex(filePathColumn[0]);
                String picturepath = cursor.getString(columnindex);
                cursor.close();
                bitmap = BitmapFactory.decodeFile(picturepath);
                postimageview.setImageBitmap(bitmap);
            }catch(Exception e){
                e.printStackTrace();
            }

        }

    }

    private void uploadimagetoserver(){

        if(bitmap!=null) {

            // Get the data from an ImageView as bytes
            postimageview.setDrawingCacheEnabled(true);
            postimageview.buildDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            imageidentifier = UUID.randomUUID().toString() + ".png";

            UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child("my_images").child(imageidentifier).putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast.makeText(SocialMediaActivity.this, exception.toString(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    Toast.makeText(SocialMediaActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                    edtdescription.setVisibility(View.VISIBLE);

                    FirebaseDatabase.getInstance().getReference().child("my_users").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            uids.add(dataSnapshot.getKey());
                            String username = (String)dataSnapshot.child("username").getValue();
                            usernames.add(username);
                            arrayAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){

                                imagedownloadlink = task.getResult().toString();
                            }
                        }
                    });

                }
            });
        }

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        HashMap<String, String> datamap = new HashMap<>();
        datamap.put("fromwhom", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        datamap.put("imageidentifier", imageidentifier);
        datamap.put("imagelink", imagedownloadlink);
        datamap.put("des", edtdescription.getText().toString());
        FirebaseDatabase.getInstance().getReference().child("my_users").child(uids.get(position)).child("recieved_posts")
                .push().setValue(datamap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SocialMediaActivity.this, "Post sent successfully", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
