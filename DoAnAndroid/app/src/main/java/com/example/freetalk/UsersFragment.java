package com.example.freetalk;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.freetalk.adapters.AdapterUsers;
import com.example.freetalk.models.ModelUser;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {

    //
    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUser> userList;

    String myUid, myUid2;

    FirebaseAuth firebaseAuth;
    private  DatabaseReference mUserRef;
    //

    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        firebaseAuth = FirebaseAuth.getInstance();



        recyclerView = view.findViewById(R.id.users_recycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (firebaseAuth.getCurrentUser() != null) {

            mUserRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        }

        //khoi tao
        userList = new ArrayList<>();

        //lay all users
        getAllUsers();
        return view;
    }

    private void searchUsers(final String query)
    {

        //lay user hien tai
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //lay path of firebase db named "Users"
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //lay all data tu duong dan trong Users
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren())
                {
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    //lay all user search tru user hien tai
                    if(!modelUser.getUid().equals(fUser.getUid()))
                    {

                        if(modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                                modelUser.getEmail().toLowerCase().contains(query.toLowerCase()))
                        {
                            userList.add(modelUser);
                        }

                    }

                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(),userList);

                    //do du lieu tu adapter vao recyclerView
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void getAllUsers()
    {
        //lay user hien tai
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //lay path of firebase db named "Users"
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //lay all data tu duong dan trong Users
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren())
                {
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    //lay all user tru user hien tai
                    if(!modelUser.getUid().equals(fUser.getUid()))
                    {
                        userList.add(modelUser);
                    }

                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(),userList);

                    //do du lieu tu adapter vao recyclerView
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus()
    {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user !=null)
        {
            //được đăng nhập tại đây
            myUid = user.getUid();
        }
        else
        {

            startActivity(new Intent(getActivity(), StartActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);// lam xuat hien menu
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {

        //menu inflate
        menuInflater.inflate(R.menu.menu_main, menu);

        //search view
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView =(SearchView) MenuItemCompat.getActionView(item);
        super.onCreateOptionsMenu(menu, menuInflater);

        //search listerner
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //duoc goi khi user nhap gia tri cho nut search

                //neu kq query != null
                if(!TextUtils.isEmpty(query.trim()))
                {
                    //tim thay kq
                    searchUsers(query);
                }
                else
                {
                    //ko tim thay kq >> tra ve all users
                    getAllUsers();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //neu kq query != null
                if(!TextUtils.isEmpty(newText.trim()))
                {
                    //tim thay kq
                    searchUsers(newText);
                }
                else
                {
                    //ko tim thay kq >> tra ve all users
                    getAllUsers();
                }

                return false;
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


    //handle menu Item Logout click

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout)
        {
            //setup timeStamp khi logout
//            String timestamp = String.valueOf(System.currentTimeMillis());
//            checkOnlineStatus(timestamp);
//
//            LoginManager.getInstance().logOut();
//            FirebaseAuth.getInstance().signOut();
//            checkUserStatus();

        }
        else
        {

        }
        return super.onOptionsItemSelected(item);
    }
}
