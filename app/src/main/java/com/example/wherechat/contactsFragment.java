package com.example.wherechat;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import java.awt.font.TextAttribute;

import de.hdodenhof.circleimageview.CircleImageView;


public class contactsFragment extends Fragment
{
    private View contactsView;
    private RecyclerView myContactsList;
    private DatabaseReference contactsRef,usersRef;
    private FirebaseAuth mAuth;
    private  String currentUserID;



    public contactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
       contactsView= inflater.inflate(R.layout.fragment_contacts, container, false);

       myContactsList=(RecyclerView)contactsView.findViewById(R.id.contacts_list);
       myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();

       contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef=FirebaseDatabase.getInstance().getReference().child("Users");

       return  contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<contacts>()
                .setQuery(contactsRef,contacts.class).build();

        FirebaseRecyclerAdapter<contacts,ContactsViewHolder>adapter
                =new FirebaseRecyclerAdapter<contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder contactsViewHolder, int i, @NonNull contacts contacts)
            {
               final String usersID=getRef(i).getKey();
                 usersRef.child(usersID).addValueEventListener(new ValueEventListener()
                 {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                     {
                         if(dataSnapshot.exists())
                         {
                             if(dataSnapshot.child("userState").hasChild("state"))
                             {
                                 String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                 String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                 String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                 if(state.equals("Online"))
                                 {
                                    contactsViewHolder.onlineIcon.setVisibility(View.VISIBLE);


                                 }
                                 else if(state.equals("Offline"))
                                 {
                                    contactsViewHolder.onlineIcon.setVisibility(View.INVISIBLE);

                                 }

                             }
                             else
                             {
                               contactsViewHolder.userStatus.setText("Offline");

                             }

                             if(dataSnapshot.hasChild("image"))
                             {
                                 String  userImage=dataSnapshot.child("image").getValue().toString();
                                 String profileName =dataSnapshot.child("name").getValue().toString();
                                 String  profileStatus=dataSnapshot.child("status").getValue().toString();

                                 contactsViewHolder .userName.setText(profileName);
                                 contactsViewHolder .userStatus.setText(profileStatus);
                                 Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(contactsViewHolder.profileImage);

                             }
                             else
                             {
                                 String profileName =dataSnapshot.child("name").getValue().toString();
                                 String  profileStatus=dataSnapshot.child("status").getValue().toString();

                                 contactsViewHolder .userName.setText(profileName);
                                 contactsViewHolder .userStatus.setText(profileStatus);
                             }
                         }

                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {

                     }
                 });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
               View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
               ContactsViewHolder viewHolder=new ContactsViewHolder(view);
               return  viewHolder;

            }
        };

        myContactsList.setAdapter(adapter);
        adapter.startListening();

    }


    public static class ContactsViewHolder extends  RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;



        public ContactsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            onlineIcon=(ImageView) itemView.findViewById(R.id.user_online_status);
        }
    }
}
