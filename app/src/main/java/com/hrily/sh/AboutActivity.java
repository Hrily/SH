package com.hrily.sh;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void go(View v){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://facebook.com/hrishi.hiraskar7"));
        startActivity(intent);
    }

    public void mail(View v){
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"hrishihiraskar@gmaiml.com"});
        startActivity(Intent.createChooser(emailIntent, "Send mail to Dev..."));

    }

}
