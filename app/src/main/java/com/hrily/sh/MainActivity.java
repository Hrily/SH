package com.hrily.sh;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.firebase.client.core.Context;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseAdapter;

    private FirebaseDatabase db;
    private DatabaseReference dbref, chatref;


    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private String mUsername, friendName, pass, temp_pass, chat_id;
    private Uri mPhotoUrl;
    private TextView user_txt, wel_txt;
    private EditText main_msg_txt;
    private ImageView user_img;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header=navigationView.getHeaderView(0);

        Window window = getWindow();
        if(Build.VERSION.SDK_INT>=21) {
            window.setStatusBarColor(Color.parseColor("#303F9F"));
            window.setNavigationBarColor(Color.parseColor("#303F9F"));
        }

        //wel_txt = (TextView) findViewById(R.id.wel_txt);
        user_txt = (TextView) header.findViewById(R.id.user_txt);
        user_img = (ImageView)header.findViewById(R.id.user_img);
        main_msg_txt = (EditText)findViewById(R.id.main_msg_txt);

        Firebase.setAndroidContext(this);
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mUsername = mFirebaseUser.getDisplayName();
        if (mFirebaseUser.getPhotoUrl() != null) {
            mPhotoUrl = mFirebaseUser.getPhotoUrl();
            Glide.with(MainActivity.this)
                    .load(mPhotoUrl)
                    .into(user_img);
        }else{
            user_img.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_menu_camera));
            user_img.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP);
        }

        sp = getSharedPreferences("SHData", android.content.Context.MODE_PRIVATE);
        friendName = sp.getString("friendName","SH");
        pass = sp.getString("pass","----");
        if(pass.equals("----")){
            startActivity(new Intent(this,ChangePassActivity.class));
            finish();
        }
        temp_pass = pass;
        chat_id = sp.getString("chatId","err");
        if(chat_id.equals("err")){
            Toast.makeText(this, "Some Error occurred...",Toast.LENGTH_LONG).show();
            finish();
        }
        setTitle(friendName);
        //wel_txt.setText("Welcome "+mUsername);
        user_txt.setText(mUsername);

        db = FirebaseDatabase.getInstance();
        dbref = db.getReference();
        chatref = dbref.child("chats").child(chat_id);
        chatref.keepSynced(true);
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(
                Message.class,
                R.layout.message_layout,
                MessageViewHolder.class,
                chatref ) {
            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, Message model, int position) {
                viewHolder.msg_txt.setText(model.getMsg());
                Date dN = new Date(Long.parseLong(model.getTime()));
                SimpleDateFormat ft = new SimpleDateFormat("hh:mm a dd/MM/yy");
                viewHolder.time_txt.setText(ft.format(dN));
                if(model.getBy().equals(mFirebaseUser.getEmail())){
                    viewHolder.mlayout.setGravity(Gravity.RIGHT);
                    viewHolder.time_txt.setGravity(Gravity.RIGHT);
                    //Log.i("Main","msg: "+model.getMsg());
                }else{
                    viewHolder.mlayout.setGravity(Gravity.LEFT);
                    viewHolder.time_txt.setGravity(Gravity.LEFT);
                    //Log.i("Main","msg: "+model.getMsg());
                }
            }
        };
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.message_recycle);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("Exit!");
            alertDialog.setMessage("Are you sure you want to Exit?");
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alertDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("Log Out!");
            alertDialog.setMessage("Are you sure you want to log out?\nThis will delete your SH account!");
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        mFirebaseAuth.signOut();
                        dbref.child("users").child(mFirebaseUser.getEmail().replace('.','-')).removeValue();
                        Toast.makeText(MainActivity.this, "Logged out successfully...", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }catch (Exception e) {
                        Log.d("Logout", "Error: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Couldn't Log out. Please try again later...", Toast.LENGTH_LONG).show();
                    }
                }
            });
            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alertDialog.show();
        } else if (id == R.id.nav_change_pass) {
            //String temp = sp.getString("pass","----");
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("pass","----");
            editor.commit();
            startActivityForResult(new Intent(MainActivity.this, PassActivity.class),3);
            //editor.putString("pass",temp);
            //editor.apply();
        }else if(id == R.id.nav_about){
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
        }
        /* else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode!=3){
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("pass",temp_pass);
            editor.apply();
        }
    }

    public void sendMessage(){
        String msg = main_msg_txt.getText().toString();
        Date dNow = new Date();
        String time = Long.toString(dNow.getTime());
        Message m = new Message(msg, mFirebaseUser.getEmail(), time);
        chatref.push().setValue(m);
        main_msg_txt.setText("");
    }

}
