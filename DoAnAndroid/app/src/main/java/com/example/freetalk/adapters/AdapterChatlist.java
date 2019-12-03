package com.example.freetalk.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.freetalk.ChatActivity;
import com.example.freetalk.R;
import com.example.freetalk.models.ModelChatlist;
import com.example.freetalk.models.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder> {

    Context context;
    List<ModelUser> userList; //lay thong tin
    private HashMap<String,String> lastMessageMap;


    //constructor
    public AdapterChatlist(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //inflater layout row_chatlist.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist,viewGroup,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //get data
        final String hisUid = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String lastMessage = lastMessageMap.get(hisUid);

        if(lastMessage == null || lastMessage.equals("default"))
        {
            holder.lastMessageTV.setVisibility(View.VISIBLE);
            holder.lastMessageTV.setText("Nói xin chào !!");
        }
        else
        {
            holder.lastMessageTV.setVisibility(View.VISIBLE);
            holder.lastMessageTV.setText(lastMessage);
        }
        try
        {
            Picasso.get().load(userImage).placeholder(R.drawable.default_avatar).into(holder.profileIV);
        }
        catch (Exception e)
        {
            Picasso.get().load(R.drawable.default_avatar).into(holder.profileIV);

        }
        //set onlineStatus of other user in chatlist
        if(userList.get(position).getOnlineStatus().equals("online"))
        {
            //onl
            holder.onlineStatusIV.setImageResource(R.drawable.cricle_online);

        }
        else
        {
            //off
            holder.onlineStatusIV.setImageResource(R.drawable.cricle_offline);
        }

        holder.nameTV.setText(userName);

        //handle click user in chatlist

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start chat activity with that user
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid",hisUid);
                context.startActivity(intent);
            }
        });
    }



    public void setLastMessageMap(String userId, String lastMessage)
    {
        lastMessageMap.put(userId,lastMessage);
    }

    @Override
    public int getItemCount() {
        return userList.size() ;
    }

    class MyHolder extends RecyclerView.ViewHolder
    {

        //view of row_chatlist.xml
        ImageView profileIV, onlineStatusIV;
        TextView nameTV, lastMessageTV;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //INIT
            profileIV = itemView.findViewById(R.id.profileIV);
            onlineStatusIV = itemView.findViewById(R.id.onlineStatusIV);
            nameTV = itemView.findViewById(R.id.nameTV);

            lastMessageTV = itemView.findViewById(R.id.lastMessageTV);

        }
    }
}
