package com.hrily.sh;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PassActivity extends AppCompatActivity {

    private String pass,curpass;
    private boolean isConn, userExist, friendExist;

    private TextView pass_txt, pass_info, hello_txt;
    private ProgressBar PB;
    private RelativeLayout buttons;
    private Button clear;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private FirebaseDatabase db;
    private DatabaseReference dbref,userref;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass);
        pass_txt = (TextView)findViewById(R.id.pass_txt);
        pass_info = (TextView)findViewById(R.id.pass_info);
        hello_txt = (TextView)findViewById(R.id.hello_txt);
        buttons = (RelativeLayout)findViewById(R.id.buttons);
        clear = (Button)findViewById(R.id.button_clear);
        PB = (ProgressBar)findViewById(R.id.pass_prog);

        PB.getIndeterminateDrawable().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP);

        Window window = getWindow();
        if(Build.VERSION.SDK_INT>=21) {
            window.setStatusBarColor(Color.parseColor("#303F9F"));
            window.setNavigationBarColor(Color.parseColor("#303F9F"));
        }

        Firebase.setAndroidContext(this);
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        db = Utils.getDatabase();
        dbref = db.getReference();
        userref = dbref.child("users");
        userref.keepSynced(true);
        Log.d("Main", String.valueOf(mFirebaseUser));

        doChecks();

    }

    public void doChecks(){
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        pass_txt.setVisibility(View.GONE);
        pass_info.setVisibility(View.GONE);
        buttons.setVisibility(View.GONE);
        clear.setVisibility(View.GONE);

        sp = getSharedPreferences("SHData", android.content.Context.MODE_PRIVATE);
        isConn = sp.getBoolean("isConn",false);
        pass = sp.getString("pass","----");

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(PassActivity.this, LoginActivity.class));
            finish();
            return;
        } else if(!isConn) {
            PB.setVisibility(View.VISIBLE);
            userref.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                    boolean userExist = dataSnapshot.hasChild(mFirebaseUser.getEmail().replace(".", "-"));
                    Log.d("Pass", "UserExists: "+dataSnapshot.hasChild(mFirebaseUser.getEmail().replace(".", "-")));
                    if (!userExist) {
                        Intent it = new Intent(PassActivity.this, ConnectActivity.class);
                        it.putExtra("userExist", userExist);
                        startActivity(it);
                        finish();
                    } else {
                        try {
                            Log.d("Pass","Thread sleeping...");
                            Thread.sleep(3000);
                        }catch (Exception e){
                            Log.d("Pass","ThreadSleep: Error: "+e.getMessage());
                        }
                        Log.d("Pass","Thread awake...");
                        String friend = dataSnapshot.child(mFirebaseUser.getEmail().replace('.', '-')).child("friend_email").getValue().toString();
                        boolean friendExist = dataSnapshot.hasChild(friend.replace('.', '-'));
                        if (!friendExist) {
                            Intent it = new Intent(PassActivity.this, ConnectActivity.class);
                            it.putExtra("userExist", userExist);
                            it.putExtra("friendExist", friendExist);
                            startActivity(it);
                            finish();
                        } else {
                            SharedPreferences sharedPreferences = getSharedPreferences("SHData", android.content.Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isConn", true);
                            editor.putString("chatId", dataSnapshot.child(friend.replace('.', '-')).child("pass").getValue().toString());
                            editor.putString("friendName", dataSnapshot.child(friend.replace('.', '-')).child("name").getValue().toString());
                            editor.apply();
                            PB.setVisibility(View.GONE);
                            pass_txt.setVisibility(View.VISIBLE);
                            pass_info.setVisibility(View.VISIBLE);
                            buttons.setVisibility(View.VISIBLE);
                            clear.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("Main:", "UserChk: " + databaseError.getDetails());
                    PB.setVisibility(View.GONE);
                }
            });
        }

        hello_txt.setText("Hello!\n"+mFirebaseUser.getDisplayName().split(" ")[0]+" :)");

        if(isConn) {
            PB.setVisibility(View.GONE);
            pass_txt.setVisibility(View.VISIBLE);
            pass_info.setVisibility(View.VISIBLE);
            buttons.setVisibility(View.VISIBLE);
            clear.setVisibility(View.VISIBLE);
        }

        curpass = "";
        if(pass.equals("----")){
            //TODO: Set pass
            pass_info.setText(R.string.pass_set);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            doChecks();
        }else if(requestCode==5){
            Toast.makeText(this,"pass finished",Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void press(View v) {
        switch (v.getId()){
            case R.id.button0:
                passAdd(0,v);
                break;
            case R.id.button1:
                passAdd(1,v);
                break;
            case R.id.button2:
                passAdd(2,v);
                break;
            case R.id.button3:
                passAdd(3,v);
                break;
            case R.id.button4:
                passAdd(4,v);
                break;
            case R.id.button5:
                passAdd(5,v);
                break;
            case R.id.button6:
                passAdd(6,v);
                break;
            case R.id.button7:
                passAdd(7,v);
                break;
            case R.id.button8:
                passAdd(8,v);
                break;
            case R.id.button9:
                passAdd(9,v);
                break;
            case R.id.button_clear:
                curpass = "";
                pass_txt.setText(" ");
                break;
        }
    }



    void passAdd(int i,View v){
        if(curpass.length()<3){
            Log.d("Pass","currpass.lenght: "+ Integer.toString(curpass.length()));
            curpass+=Integer.toString(i);
            pass_txt.setText(pass_txt.getText().toString()+"* ");
        }else{
            curpass+=Integer.toString(i);
            pass_txt.setText(pass_txt.getText().toString()+"* ");
            if(pass.equals("----")){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Set PassCode");
                alertDialog.setMessage("Would you like to set\n"+curpass+"\nas the PassCode?");
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("pass",curpass);
                        editor.apply();
                        pass = curpass;
                        curpass = "";
                        pass_txt.setText(" ");
                        pass_info.setText("Enter PassCode");
                    }
                });
                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        curpass = "";
                        pass_txt.setText(" ");
                    }
                });
                alertDialog.show();
            }else if(pass.equals(curpass)){
                //TODO: grant access
                startActivity(new Intent(PassActivity.this, MainActivity.class));
                finish();
            }else{
                curpass = "";
                pass_txt.setText(" ");
                Snackbar.make(v, "Incorrect PassCode!!!", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        }
    }

}
