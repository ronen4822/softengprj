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
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.widget.LinearLayout.LayoutParams;


public class DisplayMessageActivity extends AppCompatActivity {
    public static final String DISPLAY_MESSAGE_ACTIVITY = "DisplayMessageActivity";
    public static final String ERROR = "ERROR";
    public static final String OK = "ok";
    private static final Object mutex = new Object();
    private String name;
    private int prevTextId = 0;
    private ScrollView scrollLayout;
    private RelativeLayout relativeLayout;
    private Calendar curCalendar = Calendar.getInstance();
    private SimpleDateFormat formatting = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss");
    private String errorMsg;
    private SocketHandler connHandler;
    private String logName;

    //TODO:check for heartbeats
    public String getDate() {
        return formatting.format(curCalendar.getTime());
    }

    synchronized private void addToLog(String lineToAdd) {
        String curTime = getDate();
        try {
            //TODO: check if file is too large
            File logFile = new File(Environment.getExternalStorageDirectory(), "Groups");
            if (!logFile.exists()) {
                logFile.mkdirs();
            }
            int isGroup = 1;
            String groupName = "";
            String thisMsg = "";
            for (int i = 0; i < lineToAdd.length(); ++i) {
                if (isGroup == 1 && lineToAdd.charAt(i) == ':') {
                    isGroup = 0;
                    continue;
                }
                if (isGroup == 1) {
                    groupName += lineToAdd.charAt(i);
                    continue;
                }
                if (isGroup == 0) {
                    thisMsg += lineToAdd.charAt(i);
                }
            }
            File filepath = new File(Environment.getExternalStorageDirectory() + File.separator + "Groups" + File.separator + groupName + ".txt");
            if (!filepath.exists()) {
                filepath.createNewFile();
            }
            //lineToAdd.replace(groupName+":","");
            FileWriter writer = new FileWriter(filepath, true);
            String tmp = "[ " + curTime + " ]-" + thisMsg;
            writer.append(tmp);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            logError("Failed to open file!");
        } catch (IOException e) {
            logError("Failed to write to file!");
        }
    }

    synchronized private void loadFromLog() {
        try {
            File logFile = new File(Environment.getExternalStorageDirectory(), "Groups" + File.separator + logName + ".txt");
            if (!logFile.exists()) {
                return;
            }
            FileReader in = new FileReader(Environment.getExternalStorageDirectory() + File.separator + "Groups" + File.separator + logName + ".txt");
            BufferedReader file_buf = new BufferedReader(in);
            String currentLine = "";
            String thisName = "";
            int tmp;
            int isString, isMe, isName;
            isString = 0;
            isName = 0;
            while ((tmp = file_buf.read()) != -1) {
                if (tmp == 194)//the char that marks the end of the message
                {
                    if (thisName.equals(name)) {
                        isMe = 1;
                    } else {
                        isMe = 0;
                    }
                    addMsg(currentLine, isMe, 0);//group isnt in the msg
                    currentLine = "";
                    thisName = "";
                    isString = 0;
                    continue;
                }
                if (tmp == '-' && isString == 0) {
                    isName = 1;
                    isString = 1;
                    continue;
                }
                if (isName == 1 && (char) tmp == ':') {
                    isName = 0;
                }
                if (isString == 1) {
                    if (isName == 1) {
                        thisName += (char) tmp;
                    }
                    currentLine += (char) tmp;
                }
            }
            file_buf.close();
        } catch (FileNotFoundException e) {
            logError("Failed to open input file in load operation!");
        } catch (IOException e) {
            logError("Failed to close input file in load operation!");
        }
    }

    private void addMsg(String msg, int isMe, int isGroupThere) {

        int isGroup = 1;
        String groupName = "";
        String thisMsg = "";
        if (isGroupThere == 1) {
            for (int i = 0; i < msg.length(); ++i) {
                if (isGroup == 1 && msg.charAt(i) == ':') {
                    isGroup = 0;
                    continue;
                }
                if (isGroup == 1) {
                    groupName += msg.charAt(i);
                    continue;
                }
                if (isGroup == 0) {
                    thisMsg += msg.charAt(i);
                }
            }
            if (!groupName.equals(logName)) {
                return;
            }
        } else {
            thisMsg = msg;
        }
        TextView textView1 = new TextView(this);
        textView1.setText(thisMsg);
        RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        llp.setMargins(0, 10, 0, 10);
        if (prevTextId != 0) {
            llp.addRule(RelativeLayout.BELOW, prevTextId);
        }
        if (isMe == 1) {
            textView1.setBackgroundResource(R.drawable.rounded_corner);
            llp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else {
            textView1.setBackgroundResource(R.drawable.rounded_corner1);
            llp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
        textView1.setId(prevTextId + 1);
        prevTextId++;
        textView1.setLayoutParams(llp);
        textView1.setPadding(20, 20, 20, 20);
        textView1.setTextColor(Color.rgb(0, 0, 51));
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
        setContentView(R.layout.chat_window);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.DisplayBar);
        setSupportActionBar(myToolbar);
        myToolbar.setBackgroundResource(R.color.colorPrimaryDark);
        ScrollView tmpView = (ScrollView) findViewById(R.id.scrollLayout);
        scrollLayout = tmpView;
        RelativeLayout relLayout = new RelativeLayout(this);
        relLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        relLayout.setPadding(16, 16, 16, 16);
        relativeLayout = relLayout;
        tmpView.addView(relLayout);
        Intent intent = getIntent();
        logName = intent.getStringExtra(SocketHandler.GROUPNAME);
        connHandler = SocketHandler.getInstance();
        connHandler.setWhoIsActive(DISPLAY_MESSAGE_ACTIVITY);
        name = connHandler.getName();
        setTitle(logName);
        loadFromLog();
        MsgPrinter sockReader = new MsgPrinter();
        Thread readFromSoc = new Thread(sockReader);
        readFromSoc.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        connHandler.setWhoIsActive(DISPLAY_MESSAGE_ACTIVITY);
        Thread readToLog = new Thread(new MsgPrinter());
        readToLog.start();
        //TODO: FIND A WAY TO RESTART THE READING THREAD
    }

    @Override
    protected void onPause() {
        super.onPause();
        connHandler.setWhoIsActive(SocketHandler.INVALID);
        //TODO: FIND A WAY TO STOP THE READING THREAD
    }

    public void sendMessage(View view) {
        EditText edText = (EditText) findViewById(R.id.msgHolder);
        String msg = edText.getText().toString();
        if (msg.equals("")) {
            return;
        }
        addMsg(name + ":\r\n " + msg, 1, 0);
        String msgToSend = name + ":\r\n " + msg + (char) 194;
        addToLog(logName + ":" + msgToSend);
        edText.setText(null);
        MsgHandler sendMsg = new MsgHandler(logName + ":" + msgToSend);
        Thread tmpThread = new Thread(sendMsg);
        tmpThread.start();
    }

    private class MsgHandler implements Runnable {
        private String msg;

        MsgHandler(String msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            connHandler.sendMsg(msg);
        }
    }

    private void logError(String msg) {
        errorMsg = msg;
        runOnUiThread(new Runnable() {
            public void run() {
                new AlertDialog.Builder(DisplayMessageActivity.this)
                        .setTitle(ERROR)
                        .setMessage(errorMsg)
                        .setCancelable(false)
                        .setPositiveButton(OK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        });
    }

    private class MsgPrinter implements Runnable {
        private String msg = null;
        private int counter = 0;

        public void run() {
            while (connHandler.getWhoIsActive().equals(DISPLAY_MESSAGE_ACTIVITY)) {
                if (msg == null) {
                    msg = connHandler.getMsg();
                    if (msg != null) {
                        synchronized (mutex) {
                            if (counter == 0) {
                                addToLog(msg + (char) SocketHandler.END_OF_MSG_CHAR);
                                counter++;
                            }
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    addMsg(msg, 0, 1);
                                } finally {
                                    synchronized (mutex) {
                                        msg = null;
                                        counter = 0;
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
    }

}
