package com.example.ronen.firstprojtry;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
    private InetAddress serverAdd=null;
    private Calendar curCalendar=Calendar.getInstance();
    private SimpleDateFormat formatting=new SimpleDateFormat("dd:MM:yyyy HH:mm:ss");
    private String errorMsg;

    private String logName="Group1"; //TODO: when adding multiple groups make the logName a variable in addToLog

    //check for heartbeats
    private String getDate()
    {
        return formatting.format(curCalendar.getTime());
    }
    synchronized private void addToLog(String lineToAdd)
    {
        String curTime=getDate();
        try {
            //TODO: check if file is too large
            File logFile= new File(Environment.getExternalStorageDirectory(),"Groups");
            if (!logFile.exists())
            {
                logFile.mkdirs();
            }
            //File filepath=new File(logFile,logName+".txt");
            File filepath=new File(Environment.getExternalStorageDirectory()+File.separator+"Groups"+File.separator+logName+".txt");
            if (!filepath.exists())
            {
                filepath.createNewFile();
            }
            FileWriter writer=new FileWriter(filepath,true);
            String tmp=" [ "+curTime+" ]-"+lineToAdd;
            writer.append(tmp);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            logError("Failed to open file!");
        } catch (IOException e) {
            logError("Failed to write to file!");
        }
    }
    private void loadFromLog() {
        try {
            File logFile= new File(Environment.getExternalStorageDirectory(),"Groups"+File.separator+logName+".txt");
            if (!logFile.exists())
            {
                return;
            }
            FileReader in=new FileReader(Environment.getExternalStorageDirectory()+File.separator+"Groups"+File.separator+logName+".txt");
            BufferedReader file_buf=new BufferedReader(in);
            String currentLine="";
            String thisName="";
            int tmp;
            int isString,isMe,isName;
            isString=0;
            isName=0;
            while ((tmp=file_buf.read())!=-1)
            {
                if (tmp==194)
                {
                    if (thisName.equals(name))
                    {
                        isMe=1;
                    }
                    else
                    {
                        isMe=0;
                    }
                    addMsg(currentLine,isMe);
                    currentLine="";
                    thisName="";
                    isString=0;
                    continue;
                }
                if (tmp=='-' && isString==0)
                {
                    isName=1;
                    isString=1;
                    continue;
                }
                if (isName==1 && (char)tmp==':')
                {
                    isName=0;
                }
                if (isString==1)
                {
                    if (isName==1)
                    {
                        thisName+=(char)tmp;
                    }
                    currentLine+=(char)tmp;
                }
            }
            file_buf.close();
        } catch (FileNotFoundException e) {
            logError("Failed to open input file in load operation!");
        } catch (IOException e) {
            logError("Failed to close input file in load operation!");
        }

    }
    private void addMsg(String msg,int isMe)
    {
        TextView textView1=new TextView(this);
        textView1.setText(msg);
        RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        llp.setMargins(0,10,0,10);
        if (prevTextId!=0)
        {
            llp.addRule(RelativeLayout.BELOW,prevTextId);
        }
        if (isMe==1)
        {
            textView1.setBackgroundResource(R.drawable.rounded_corner);
            llp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }
        else
        {
            textView1.setBackgroundResource(R.drawable.rounded_corner1);
            llp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
        textView1.setId(prevTextId+1);
        prevTextId++;
        textView1.setLayoutParams(llp);
        textView1.setPadding(20,20,20,20);
        textView1.setTextColor(Color.rgb(0,0,51));
        relativeLayout.addView(textView1);
        scrollLayout.post(new Runnable() {
            @Override
            public void run() {
                scrollLayout.fullScroll(ScrollView.FOCUS_DOWN);
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
            connError();
        }
    }
    public void sendMessage(View view)
    {

        EditText edText=(EditText) findViewById(R.id.msgHolder);
        String msg=edText.getText().toString();
        addMsg(name+":\r\n "+msg,1);
        String msgToSend =name + ":\r\n " + msg + (char) 194;

        addToLog(msgToSend);

        edText.setText(null);
        MsgHandler sendMsg=new MsgHandler(msgToSend);
        Thread tmpThread=new Thread(sendMsg);
        tmpThread.start();
    }
    private class MsgHandler implements Runnable
    {
        private String msg;
        MsgHandler(String msg)
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
    private void connError()
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
    private void logError(String msg)
    {
        errorMsg=msg;
        runOnUiThread(new Runnable() {
            public void run() {
                new AlertDialog.Builder(DisplayMessageActivity.this)
                        .setTitle("ERROR")
                        .setMessage(errorMsg)
                        .setCancelable(false)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        });
    }

    private class MsgPrinter implements Runnable {
        private String id;
        private String pw;
        private String output;
        private String tmpString;
        MsgPrinter(String id,String pw) throws IOException {
            this.id=id;
            this.pw=pw;
            this.output="";
        }
        public void run() {
            try {
                serverAdd= InetAddress.getByName("192.168.1.28");
                if(!serverAdd.isReachable(300))
                {
                   connError();
                }
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
                }
                else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            loadFromLog();
                        }
                    });
                    name = answer;
                    while (true) {
                        try {
                            output="";
                            char tmp;
                            int weirdChar=194;
                            while ((tmp = (char) reader.read()) != (char) weirdChar) {
                                output += tmp;
                            }
                            synchronized (mutex) {
                                tmpString = output;
                            }
                            runOnUiThread(new Runnable() {
                            public void run() {
                                synchronized (mutex) {
                                    addToLog(tmpString);
                                    addMsg(tmpString, 0);
                                }

                            }
                            });
                        } catch (IOException e) {
                        }
                    }
                }
            } catch (IOException e) {
                //display an error and move to previous activity
                connError();
            }
        }
    }

}
