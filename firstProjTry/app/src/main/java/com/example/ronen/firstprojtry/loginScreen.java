package com.example.ronen.firstprojtry;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;


public class loginScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Login");
        setContentView(R.layout.activity_login_screen);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

    }
    public void sendMsg(View view)
    {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText edText=(EditText) findViewById(R.id.enterName);
        String msg=edText.getText().toString();
        intent.putExtra("USERNAME",msg);
        edText=(EditText) findViewById(R.id.enterPort);
        intent.putExtra("PORT",edText.getText().toString());
        edText=(EditText) findViewById(R.id.enterIP);
        intent.putExtra("IP",edText.getText().toString());
        startActivity(intent);
    }

}
