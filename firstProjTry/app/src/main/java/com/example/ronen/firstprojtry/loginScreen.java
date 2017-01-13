package com.example.ronen.firstprojtry;

import android.content.Intent;
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
        Toolbar myToolbar = (Toolbar) findViewById(R.id.loginToolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setBackgroundResource(R.color.colorPrimaryDark);

    }
    public void sendMsg(View view)
    {
        Intent intent = new Intent(this, GroupList.class);
        EditText edText=(EditText) findViewById(R.id.enterID);
        String msg=edText.getText().toString();
        if(msg.length()!=9)
        {
            edText.setError("ID has to be exactly 9 characters");
            return;
        }
        intent.putExtra("USERID",msg);
        edText=(EditText) findViewById(R.id.enterPW);
        intent.putExtra("PASSWORD",edText.getText().toString());
        startActivity(intent);
    }

}
