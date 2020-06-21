package com.example.wherechat;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class chatsFragment extends Fragment
{

    private  View  privateChatsView;
    private RecyclerView chatsList;
    private DatabaseReference chatsRef,usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;




    public chatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView=inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        chatsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef=FirebaseDatabase.getInstance().getReference().child("Users");

        chatsList=(RecyclerView) privateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return  privateChatsView;

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<contacts>options=
                new FirebaseRecyclerOptions.Builder<contacts>()
                        .setQuery(chatsRef,contacts.class)
                        .build();


        FirebaseRecyclerAdapter<contacts,chatsViewHolder>adapter=new
                FirebaseRecyclerAdapter<contacts, chatsViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull final chatsViewHolder chatsViewHolder, int i, @NonNull contacts contacts)
                    {
                        final String usersID=getRef(i).getKey();
                        final String[] retImage = {"default_image"};
                        usersRef.child(usersID).addValueEventListener
                                (new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                       if(dataSnapshot.exists())
                                       {
                                           if (dataSnapshot.hasChild("image"))
                                           {
                                                retImage[0] =dataSnapshot.child("image").getValue().toString();
                                               Picasso.get().load(retImage[0]).into(chatsViewHolder.profileImage);
                                           }
                                           final String  retName=dataSnapshot.child("name").getValue().toString();
                                           final String  retStatus=dataSnapshot.child("name").getValue().toString();

                                           chatsViewHolder.userName.setText(retName);


                                           if(dataSnapshot.child("userState").hasChild("state"))
                                           {
                                               String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                               String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                               String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                               if(state.equals("Online"))
                                               {
                                                   chatsViewHolder.userStatus.setText("Online");

                                               }
                                               else if(state.equals("Offline"))
                                               {
                                                   chatsViewHolder.userStatus.setText("Last seen:"+date+ " "+time);

                                               }

                                           }
                                           else
                                           {
                                               chatsViewHolder.userStatus.setText("Offline");

                                           }



                                           chatsViewHolder.itemView.setOnClickListener(new View.OnClickListener()
                                           {
                                               @Override
                                               public void onClick(View view)
                                               {
                                                   Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                                   chatIntent.putExtra("visit_user_id",usersID);
                                                   chatIntent.putExtra("visit_user_name",retName);
                                                   chatIntent.putExtra("visit_image",retImage);

                                                   startActivity(chatIntent);


                                               }
                                           });
                                       }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError)
                                    {

                                    }
                                });


                    }

                    @NonNull
                    @Override
                    public chatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View  view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                        return  new chatsViewHolder(view);

                    }
                };
        chatsList.setAdapter(adapter);
        adapter.startListening();


    }

    public  static class chatsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImage;
        TextView userStatus;
        TextView userName;


        public chatsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            profileImage=itemView.findViewById(R.id.users_profile_image);
           userStatus=itemView.findViewById(R.id.user_status);
          userName=itemView.findViewById(R.id.user_profile_name);


        }
    }
}
