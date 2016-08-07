package com.hrily.sh;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

public class ChangePassActivity extends AppCompatActivity {

    private TextView pass_txt, pass_info, hello_txt;
    private ProgressBar PB;
    private RelativeLayout buttons;
    private Button clear;

    private SharedPreferences sp;

    private String curpass, pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass);
        pass_txt = (TextView)findViewById(R.id.pass_txt);
        pass_info = (TextView)findViewById(R.id.pass_info);
        hello_txt = (TextView)findViewById(R.id.hello_txt);
        buttons = (RelativeLayout)findViewById(R.id.buttons);
        clear = (Button)findViewById(R.id.button_clear);

        Window window = getWindow();
        if(Build.VERSION.SDK_INT>=21) {
            window.setStatusBarColor(Color.parseColor("#303F9F"));
            window.setNavigationBarColor(Color.parseColor("#303F9F"));
        }

        sp = getSharedPreferences("SHData", android.content.Context.MODE_PRIVATE);
        pass = sp.getString("pass","----");
        curpass = "";

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
                setResult(3,new Intent(ChangePassActivity.this, MainActivity.class));
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
