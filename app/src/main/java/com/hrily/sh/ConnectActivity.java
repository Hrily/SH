package com.hrily.sh;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.client.core.Tag;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConnectActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, com.google.firebase.database.ValueEventListener {

    private static final String TAG = "Conn";
    private EditText conn_txt;
    private Button conn_btn, resend;
    private boolean userExist, friendExist;
    private TextView no_friend_txt, pass_info;
    private ProgressDialog PD;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase db;
    private DatabaseReference dbref,userref;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        conn_txt = (EditText) findViewById(R.id.conn_txt);
        conn_btn = (Button)   findViewById(R.id.conn_btn);
        resend = (Button)   findViewById(R.id.resend);
        no_friend_txt = (TextView)findViewById(R.id.no_friend_txt);
        pass_info = (TextView)findViewById(R.id.pass_info);
        PD = new ProgressDialog(this);
        PD.setMessage("Loading...");
        PD.setCanceledOnTouchOutside(false);

        Window window = getWindow();
        if(Build.VERSION.SDK_INT>=21) {
            window.setStatusBarColor(Color.parseColor("#303F9F"));
            window.setNavigationBarColor(Color.parseColor("#303F9F"));
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(AppInvite.API)
                .enableAutoManage(this, this)
                .build();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        dbref = db.getReference();
        userref = dbref.child("users");
        Intent it = getIntent();
        userExist = it.getBooleanExtra("userExist",false);
        friendExist = it.getBooleanExtra("friendExist",false);
        if(userExist){
            conn_btn.setVisibility(View.GONE);
            conn_txt.setVisibility(View.GONE);
            no_friend_txt.setVisibility(View.VISIBLE);
            resend.setVisibility(View.VISIBLE);
        }
        //Toast.makeText(this, "UserExist: "+userExist+"\nFriendExist: "+friendExist, Toast.LENGTH_LONG).show();
        conn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFriend();
            }
        });
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userref.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        final String friend = dataSnapshot.child(mFirebaseUser.getEmail().replace('.','-')).child("friend_email").getValue().toString();
                        sendMail(friend);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("Conn", "onConnectionFailed:" + connectionResult);
    }

    public void getFriend(){
        PD.show();
        final String friend = conn_txt.getText().toString();
        final String FRIEND = friend.replace('.','-');
        userref.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                friendExist = dataSnapshot.hasChild(FRIEND);
                Log.d(TAG, "Friend exists: "+friendExist);
                if(friendExist){
                    Log.d(TAG,"Friend exists: "+FRIEND);
                    if(dataSnapshot.child(FRIEND).child("friend_email").getValue().toString().equals(mFirebaseUser.getEmail())) {
                        conn_txt.setHint("Enter Pass...");
                        conn_txt.setText(null);
                        conn_txt.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                        pass_info.setVisibility(View.VISIBLE);
                        PD.dismiss();
                        conn_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getPass(friend);
                            }
                        });
                    }else{
                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ConnectActivity.this);
                        alertDialog.setTitle("Ooops...");
                        alertDialog.setMessage("Your friend seems to have connected to some other user.\nMake sure you give correct email of your friend.");
                        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alertDialog.show();
                    }
                }else{
                    sendMail(friend);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Conn", "ChkFriend: "+databaseError.toString());
                Toast.makeText(ConnectActivity.this, "There was an error...", Toast.LENGTH_LONG).show();
                PD.dismiss();
            }
        });
    }

    public void sendMail(final String friend){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ConnectActivity.this);
        alertDialog.setTitle("Send Mail?");
        alertDialog.setMessage("Your friend is not connected.\nPlease click OK to send him invitation mail.");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PD.show();
                userref.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        try {
                            int pass = Integer.parseInt(dataSnapshot.child("_no_users").getValue().toString()) + 1;
                            User mUser = new User();
                            if(!userExist) {
                                userref.child("_no_users").setValue(Integer.toString(pass));
                                Log.d(TAG, "No_Users: " + Integer.toString(pass));
                                mUser = new User(mFirebaseUser.getDisplayName(), mFirebaseUser.getEmail(), Integer.toString(pass), friend);
                            }else{
                                pass = Integer.parseInt(dataSnapshot.child(mFirebaseUser.getEmail().replace('.','-')).child("pass").getValue().toString());
                            }
                            Log.d(TAG, "Sending Invitation...");
                            Intent emailIntent = new Intent(Intent.ACTION_SEND);
                            emailIntent.setData(Uri.parse("mailto:"));
                            emailIntent.setType("text/html");
                            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{friend});
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.inv_sub));
                            emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(new StringBuilder()
                                    .append("<p>Hi there...<br/><br/>Let\'s try the Secret Hangout App.<br/>We can chat secretly on this app.<br/>Download it <a href=\'https://docs.google.com/uc?export=download&id=0B9boJdznIbrMQjMyZGlxZnRFeEU\'>here</a><br/>You need to use this Pass:</p>SH#")
                                    .append(pass)
                                    .append("<br/><p>I'm waiting for you... :)</p>").toString()));
                            startActivity(Intent.createChooser(emailIntent, "Send mail to friend..."));
                            Log.i(TAG, "Mail sent successfully...");
                            Log.d(TAG, "Pushing user...");
                            if(!userExist) {
                                userref.child(mFirebaseUser.getEmail().replace(".", "-")).setValue(mUser);
                            }
                            conn_btn.setVisibility(View.GONE);
                            conn_txt.setVisibility(View.GONE);
                            no_friend_txt.setVisibility(View.VISIBLE);
                            resend.setVisibility(View.VISIBLE);

                        } catch (android.content.ActivityNotFoundException ex) {
                            Log.i(TAG, "Error sending mail: " + ex.getMessage());
                            Toast.makeText(ConnectActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
                        }
                        PD.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("Conn", "AddUser: " + databaseError.toString());
                        Toast.makeText(ConnectActivity.this, "There was an error...", Toast.LENGTH_LONG).show();
                        PD.dismiss();
                    }
                });
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PD.dismiss();
            }
        });
        PD.dismiss();
        alertDialog.show();
    }

    public void getPass(final String friend){
        PD.show();
        final String pass = conn_txt.getText().toString();
        final String FRIEND = friend.replace('.','-');
        userref.child(FRIEND).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                if(pass.equals("SH#"+dataSnapshot.child("pass").getValue().toString())){
                    //TODO: create chat db
                    SharedPreferences sp = getSharedPreferences("SHData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("isConn",true);
                    editor.putString("chatId",dataSnapshot.child("pass").getValue().toString());
                    editor.putString("friendName",dataSnapshot.child("name").getValue().toString());
                    editor.apply();
                    User mUser = new User(mFirebaseUser.getDisplayName(), mFirebaseUser.getEmail(), dataSnapshot.child("pass").getValue().toString(), friend);
                    userref.child(mFirebaseUser.getEmail().replace(".","-")).setValue(mUser);
                    startActivity(new Intent(ConnectActivity.this, PassActivity.class));
                    finish();
                }else{
                    Toast.makeText(ConnectActivity.this, "Incorrect pass...", Toast.LENGTH_LONG).show();
                }
                PD.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                PD.dismiss();
            }
        });
    }

    @Override
    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
