package com.example.socialmedia_firebaseserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ViewPostsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {

    private ListView postslistview;
    private ArrayList<String> usernames;
    private ArrayAdapter adapter;
    private FirebaseAuth firebaseAuth;
    private ImageView sentpostimageview;
    private TextView txtdescription;
    private ArrayList<DataSnapshot> dataSnapshots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);

        sentpostimageview = findViewById(R.id.sentpostimageview);
        txtdescription = findViewById(R.id.txtdescription);

        firebaseAuth = FirebaseAuth.getInstance();

        postslistview = findViewById(R.id.postslistview);
        usernames = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, usernames);
        postslistview.setAdapter(adapter);
        dataSnapshots = new ArrayList<>();
        postslistview.setOnItemClickListener(this);
        postslistview.setOnItemLongClickListener(this);

        FirebaseDatabase.getInstance().getReference().child("my_users")
                .child(firebaseAuth.getCurrentUser().getUid())
                .child("recieved_posts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                dataSnapshots.add(dataSnapshot);
                String fromwhomusername = (String)dataSnapshot.child("fromwhom").getValue();
                usernames.add(fromwhomusername);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for(DataSnapshot snapshot : dataSnapshots){

                    if(snapshot.getKey().equals(dataSnapshot.getKey())){
                        dataSnapshots.remove(i);
                        usernames.remove(i);
                    }
                    i++;
                }
                adapter.notifyDataSetChanged();
                sentpostimageview.setImageResource(R.drawable.wormhole);
                txtdescription.setText("Description");

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DataSnapshot mydatasnapshot = dataSnapshots.get(position);
        String downloadlink =(String) mydatasnapshot.child("imagelink").getValue();

        Picasso.get().load(downloadlink).into(sentpostimageview);
        txtdescription.setText((String)mydatasnapshot.child("des").getValue());
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

        AlertDialog alertDialog = new AlertDialog.Builder(ViewPostsActivity.this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation

                        FirebaseStorage.getInstance().getReference()
                                .child("my_images").child((String)dataSnapshots.get(position).child("imageidentifier").getValue())
                                .delete();

                        FirebaseDatabase.getInstance().getReference()
                                .child("my_users").child(firebaseAuth.getCurrentUser()
                                .getUid()).child("recieved_posts")
                                .child(dataSnapshots.get(position).getKey()).removeValue();

                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        return true;
    }
}
