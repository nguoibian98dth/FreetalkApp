package com.example.freetalk.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.freetalk.ChatActivity;
import com.example.freetalk.ProfileUserActivity;
import com.example.freetalk.models.ModelUser;
import com.example.freetalk.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterUsers  extends  RecyclerView.Adapter<AdapterUsers.MyHolder>{


    Context context;
    List<ModelUser> userList;

    //constructor
    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }
    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int position) {
        //lay data
        final String hisUID = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String userEmail = userList.get(position).getEmail();

        //tra ve data
        myHolder.mNameTV.setText(userName);
        myHolder.mEmailTV.setText(userEmail);
        try
        {
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.default_avatar)
                    .into(myHolder.mAvatarIV);
        }
        catch (Exception ex)
        {

        }



        //xu ly item click
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context,""+userEmail,Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(context, ChatActivity.class);
//                intent.putExtra("hisUid", hisUID);
//                context.startActivity(intent);


                //show dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Thông tin", "Bắt đầu trò chuyện"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            //Thông tin clicked
                            Intent intent =  new Intent(context, ProfileUserActivity.class);
                            intent.putExtra("uid",hisUID);
                            context.startActivity(intent);
                        }
                        if (which == 1) {
                            //Chat clicked

                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("hisUid", hisUID);
                            context.startActivity(intent);
                        }
                    }
                });
                builder.create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder
    {
        ImageView mAvatarIV;
        TextView mNameTV, mEmailTV;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init ánh xạ
            mAvatarIV = itemView.findViewById(R.id.avatarIV);
            mNameTV = itemView.findViewById(R.id.nameTV);
            mEmailTV = itemView.findViewById(R.id.emailTV);

        }
    }
}
