package com.example.ronen.firstprojtry;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import android.widget.LinearLayout.LayoutParams;


public class DisplayMessageActivity extends AppCompatActivity {
    private BufferedReader reader;
    private Socket clientSoc;
    private String name;
    private PrintWriter out;
    int prevTextId=0;
    private ScrollView scrollLayout;
    private RelativeLayout relativeLayout;

    protected void addMsg(String msg,int isMe)
    { //isme==1 means this client sent the msg

        TextView textView1=new TextView(this);
        textView1.setText(msg);
        RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        if (prevTextId!=0)
        {
            llp.addRule(RelativeLayout.BELOW,prevTextId);
        }
        if (isMe==1)
        {
            llp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }
        else
        {
            llp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
        textView1.setId(prevTextId+1);
        prevTextId++;
        textView1.setLayoutParams(llp);
        textView1.setBackgroundResource(R.drawable.rounded_corner);
        textView1.setPadding(20,20,20,20);
        textView1.setTextColor(Color.rgb(0,0,51));
        relativeLayout.addView(textView1);
        scrollLayout.post(new Runnable() {
            @Override
            public void run() {

                scrollLayout.scrollTo(0, scrollLayout.getBottom());

            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Chat room");
        setContentView(R.layout.chat_window);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.DisplayBar);
        setSupportActionBar(myToolbar);
        myToolbar.setBackgroundResource(R.color.colorPrimaryDark);
        ScrollView tmpView=(ScrollView) findViewById(R.id.scrollLayout);
        scrollLayout=tmpView;
        RelativeLayout relLayout=new RelativeLayout(this);
        relLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        relLayout.setPadding(16,16,16,16);
        relativeLayout=relLayout;
        tmpView.addView(relLayout);
        Intent intent= getIntent();
        String ip=intent.getStringExtra("IP");
        name=intent.getStringExtra("USERNAME");
        String port=intent.getStringExtra("PORT");
        try {
            MsgPrinter sockReader=new MsgPrinter(ip,port);
            Thread stamThread=new Thread(sockReader);
            stamThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendMessage(View view)
    {
        EditText edText=(EditText) findViewById(R.id.msgHolder);
        String msg=edText.getText().toString();
        addMsg(name+":\r\n "+msg,1);

        edText.setText(null);
        MsgHandler sendMsg=new MsgHandler(name+":\r\n "+msg);
        Thread tmpThread=new Thread(sendMsg);
        tmpThread.start();
    }
    public class MsgHandler implements Runnable
    {
        private String msg;
        public MsgHandler(String msg)
        {
            this.msg=msg;
        }
        @Override
        public void run()
        {
            out.println(msg);
        }
    }
    public class MsgPrinter implements Runnable {
        private String port;
        private String ip;
        private String output;

        public MsgPrinter(String ip,String port) throws IOException {
            this.ip=ip;
            this.port=port;
        }

        @Override
        public void run() {
            InetAddress serverAdd= null;
            try {
                serverAdd = InetAddress.getByName(ip);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            try {
                clientSoc = new Socket(serverAdd, Integer.parseInt(port));
                reader = new BufferedReader(new InputStreamReader(
                        clientSoc.getInputStream()));
                OutputStream ronen = clientSoc.getOutputStream();
                out=new PrintWriter(new OutputStreamWriter(ronen),true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    output = reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable(){
                    public void run() {
                        addMsg(output,0);
                    }
                 });
            }
        }

    }


}
