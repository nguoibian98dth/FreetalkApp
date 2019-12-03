package com.example.freetalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class ProfileUserActivity extends AppCompatActivity {

    //
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    String hisUid;
    String myUid2;

    Button mProfileSendReqBtn, mDeclineBtn;
    ImageView avatar_profile;
    CircleImageView avatar_profileC;
    ImageView cover_image;
    TextView nameTV, emailTV, phoneTV;
    FloatingActionButton fab;
    private String mCurrent_stage;

    //progress diaglog
    ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        //init
        avatar_profileC= findViewById(R.id.avatar_profile);
        cover_image= findViewById(R.id.cover);
        nameTV= findViewById(R.id.nameTV);
        emailTV= findViewById(R.id.emailTV);
        phoneTV= findViewById(R.id.phonenumberTV);
        fab =(FloatingActionButton) findViewById(R.id.fab_chat);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_send_req_btn);
        mDeclineBtn = (Button) findViewById(R.id.profile_decline_btn);
        //


        //----
        mCurrent_stage = "not_friends";

        if (user.getUid().equals(hisUid)) {

            mProfileSendReqBtn.setVisibility(View.INVISIBLE);
            mProfileSendReqBtn.setEnabled(false);

        }

        //mDeclineBtn.setVisibility(View.INVISIBLE);
        //mDeclineBtn.setEnabled(false);
        //

        Intent intent = getIntent();
        hisUid = intent.getStringExtra("uid");


        Query query = databaseReference.orderByChild("uid").equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //kiem tra yêu cầu lấy data
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    //lấy dữ liệu
                    String name = ""+ds.child("name").getValue();
                    String email = ""+ds.child("email").getValue();
                    String phone = ""+ds.child("phone").getValue();
                    String image = ""+ds.child("image").getValue();
                    String cover= ""+ds.child("cover").getValue();

                    //đổ dữ liệu
                    nameTV.setText(name);
                    emailTV.setText(email);
                    phoneTV.setText(phone);

                    try
                    {
                        Picasso.get().load(image).into(avatar_profileC);
                    }
                    catch (Exception ex)
                    {
                        //nếu ko có ảnh sẽ set giá trị mặc định
                        Picasso.get().load(R.drawable.default_avatar).into(avatar_profileC);

                    }

                    try
                    {
                        Picasso.get().load(cover).into(cover_image);
                    }
                    catch (Exception ex)
                    {
                        //nếu ko có ảnh sẽ set giá trị mặc định
                        Picasso.get().load(R.drawable.ic_add_image).into(cover_image);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //sự kiện click fab
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileUserActivity.this,ChatActivity.class);
                intent.putExtra("hisUid", hisUid);
                startActivity(intent);
                finish();
            }
        });

        // sự kiện add friend click
        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }

        });

    }

    private void checkOnlineStatus(String status){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        myUid2 = user.getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid2);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //cap nhap gia tri onlineStatus cua user hien tai
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //setup online status
        checkOnlineStatus("online");
        //
    }

    @Override
    protected void onPause() {
        super.onPause();
        //setup timestamp status
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        //
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //setup online status
        checkOnlineStatus("online");
        //
    }
}
