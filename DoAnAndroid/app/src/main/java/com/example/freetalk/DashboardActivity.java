package com.example.freetalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.freetalk.notification.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    ActionBar actionBar;
    private FirebaseAuth mAuth;

    String mUID;
    String myUid2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_dashboard);

        //Actionbar and its title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Trang cá nhân");

        mAuth = FirebaseAuth.getInstance();
        firebaseAuth= FirebaseAuth.getInstance();
        //nav_main
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_main);
        bottomNavigationView.setOnNavigationItemSelectedListener(selectedListener);

        //khởi tạo mặc định
        actionBar.setTitle("Gần đây");
        ChatListFragment fragment1 = new ChatListFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1,"");
        ft1.commit();

        checkUserStatus();

        //update token
        //updateToken(FirebaseInstanceId.getInstance().getToken());
    }


    public void updateToken(String token)
    {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUID).setValue(mToken);
    }




    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener=
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    // xử lý những sự kiện click item trong BotttomNav
                    switch (menuItem.getItemId())
                    {
                        //item trò chuyện
                        case R.id.nav_home:
                            // gọi fragment trò chuyện
//                            actionBar.setTitle("Home");
//                            HomeFragment fragment1 = new HomeFragment();
//                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
//                            ft1.replace(R.id.content, fragment1,"");
//                            ft1.commit();

                            actionBar.setTitle("Gần đây");
                            ChatListFragment fragment1 = new ChatListFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content, fragment1,"");
                            ft1.commit();


                            //
                            return true;
                        case R.id.nav_profile:
                            // gọi fragment profile
                            actionBar.setTitle("Cá nhân");
                            ProfileFragment fragment2 = new ProfileFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content, fragment2,"");
                            ft2.commit();
                            //
                            return true;
                        case R.id.nav_users:
                            // gọi fragment bạn bè
                            actionBar.setTitle("Cộng đồng Freetalk");
                            UsersFragment fragment3 = new UsersFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content, fragment3,"");
                            ft3.commit();
                            //
                            return true;
                    }

                    return false;
                }
            };

    private void checkUserStatus()
    {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user !=null)
        {
            //được đăng nhập tại đây
            mUID =user.getUid();

            //luu uid cua tai khoan hien tai trong SharedPreferences
            SharedPreferences sp = getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();
            updateToken(FirebaseInstanceId.getInstance().getToken());

        }
        else
        {
            //sendToStart();
            //đã đăng nhập mở trang main
            startActivity(new Intent(DashboardActivity.this, StartActivity.class));
            finish();
        }
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
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onStart() {

        super.onStart();
        //checkUserStatus();

        checkOnlineStatus("online");

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {

            sendToStart();

        } else {

            //mUserRef.child("online").setValue("true");

        }

    }



    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
        checkOnlineStatus("online");
    }


    @Override
    protected void onPause() {
        super.onPause();

        //setup timeStamp khi home Clicked
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        //
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //setup timeStamp khi restart
        checkOnlineStatus("online");
        //
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //setup timeStamp khi have stopped
//        String timestamp = String.valueOf(System.currentTimeMillis());
//        checkOnlineStatus(timestamp);
        //
    }

    private void sendToStart() {
        Intent startIntent = new Intent(DashboardActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    //handle menu Item Logout click
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if(id == R.id.action_logout)
//        {
//            firebaseAuth.signOut();
//            checkUserStatus();
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
