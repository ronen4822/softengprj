package com.example.ronen.firstprojtry;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
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
    private int prevTextId=0;
    private ScrollView scrollLayout;
    private RelativeLayout relativeLayout;
    private Object mutex;

    //check for heartbeats
    protected void addMsg(String msg,int isMe)
    {
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
        String id=intent.getStringExtra("USERID");
        String password=intent.getStringExtra("PASSWORD");

        try {
            MsgPrinter sockReader=new MsgPrinter(id,password);
            Thread readFromSoc=new Thread(sockReader);
            readFromSoc.start();
        } catch (IOException e) {
            //display an error and move to previous activity
            runOnUiThread(new Runnable() {
                public void run() {
                    new AlertDialog.Builder(DisplayMessageActivity.this)
                            .setTitle("ERROR")
                            .setMessage("Connection failed")
                            .setCancelable(false)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(getApplicationContext(), loginScreen.class);
                                    startActivity(intent);
                                }
                            }).show();

                }
            });
            return;
        }
    }
    public void sendMessage(View view)
    {
        EditText edText=(EditText) findViewById(R.id.msgHolder);
        String msg=edText.getText().toString();
        addMsg(name+":\r\n "+msg,1);

        edText.setText(null);
        MsgHandler sendMsg=new MsgHandler(name+":\r\n "+msg+(char)194);
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
            out.print(msg);
            out.flush();
        }
    }
    public class MsgPrinter implements Runnable {
        private String id;
        private String pw;
        private String output;
        private String tmpString;
        public MsgPrinter(String id,String pw) throws IOException {
            this.id=id;
            this.pw=pw;
            this.output="";
        }
        public void run() {
            InetAddress serverAdd= null;
            try {
                serverAdd= InetAddress.getByName("192.168.1.28");
                if(serverAdd.isReachable(300)==false)
                {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            new AlertDialog.Builder(DisplayMessageActivity.this)
                                    .setTitle("ERROR")
                                    .setMessage("Connection failed")
                                    .setCancelable(false)
                                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(getApplicationContext(), loginScreen.class);
                                            startActivity(intent);
                                        }
                                    }).show();

                        }
                    });
                }
                //serverAdd = InetAddress.getByName("192.168.1.28");
                clientSoc = new Socket(serverAdd, 55555);
                reader = new BufferedReader(new InputStreamReader(
                        clientSoc.getInputStream()));
                OutputStream ronen = clientSoc.getOutputStream();
                out=new PrintWriter(new OutputStreamWriter(ronen),true);
                out.println(id);
                out.println(pw);
                String answer=reader.readLine();
                if (answer.equals("Access denied"))
                {
                    out.close();
                    reader.close();
                    clientSoc.close();
                    //display an error and move to previous activity
                    runOnUiThread(new Runnable() {
                        public void run() {
                            new AlertDialog.Builder(DisplayMessageActivity.this)
                                    .setTitle("ERROR")
                                    .setMessage("Invalid username or password... returning to login screen")
                                    .setCancelable(false)
                                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(getApplicationContext(), loginScreen.class);
                                            startActivity(intent);
                                        }
                                    }).show();

                        }
                    });
                    return;

                }
                else {
                    name = answer;
                    while (true) {
                        try {
                            output="";
                            char tmp;
                            int weirdChar=194;
                            while ((tmp = (char) reader.read()) != (char) weirdChar) {
                                output += tmp;
                            }
                            tmpString=output;
                        } catch (IOException e) {
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                addMsg(tmpString, 0);
                            }
                        });

                    }
                }
            } catch (IOException e) {
                //display an error and move to previous activity
                runOnUiThread(new Runnable() {
                    public void run() {
                        new AlertDialog.Builder(DisplayMessageActivity.this)
                                .setTitle("ERROR")
                                .setMessage("Connection failed")
                                .setCancelable(false)
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(getApplicationContext(), loginScreen.class);
                                        startActivity(intent);
                                    }
                                }).show();

                    }
                });
                return;
            }
        }
    }
}
