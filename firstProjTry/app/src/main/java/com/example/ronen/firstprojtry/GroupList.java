package com.example.ronen.firstprojtry;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.security.acl.Group;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

public class GroupList extends AppCompatActivity {
    public static final String GROUPS = "Groups";
    public static final String USERID = "USERID";
    public static final String PASSWORD = "PASSWORD";
    public static final String GROUP_LIST = "GroupList";
    private final Object mutex = new Object();
    private Calendar curCalendar = Calendar.getInstance();
    private SocketHandler clientConn;
    private SimpleDateFormat formatting = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss");
    private ListView myList;
    private String id;
    private String pw;
    private static Vector<String> groups = null;

    public String getDate() {
        return formatting.format(curCalendar.getTime());
    }

    public static void addGroup(String group) {
        if (groups == null) {
            groups = new Vector<>();
        }
        groups.add(group);
    }

    private void connError() {
        runOnUiThread(new Runnable() {
            public void run() {
                new AlertDialog.Builder(GroupList.this)
                        .setTitle("ERROR")
                        .setMessage("Connection failed!")
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

    synchronized private void addToLog(String locNMsg) throws IOException {
        int isGroup;
        isGroup = 1;
        String thisMsg = "";
        String groupName = "";
        for (int i = 0; i < locNMsg.length(); ++i) {
            if (isGroup == 1 && locNMsg.charAt(i) == ':') {
                isGroup = 0;
                continue;
            }
            if (isGroup == 1) {
                groupName += locNMsg.charAt(i);
                continue;
            }
            if (isGroup == 0) {
                thisMsg += locNMsg.charAt(i);
            }
        }
        File logFile = new File(Environment.getExternalStorageDirectory(), "Groups");
        if (!logFile.exists()) {
            logFile.mkdirs();
        }
        File filepath = new File(Environment.getExternalStorageDirectory() + File.separator + "Groups" + File.separator + groupName + ".txt");
        if (!filepath.exists()) {
            filepath.createNewFile();
        }
        FileWriter writer = new FileWriter(filepath, true);
        String curTime = getDate();
        String tmp = "[ " + curTime + " ]-" + thisMsg;
        writer.append(tmp);
        writer.flush();
        writer.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        setTitle(GROUPS);
        clientConn = SocketHandler.getInstance();
        clientConn.setWhoIsActive(GROUP_LIST);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.groupBar);
        setSupportActionBar(myToolbar);
        myToolbar.setBackgroundResource(R.color.colorPrimaryDark);
        Intent intent = getIntent();
        id = intent.getStringExtra(USERID);
        pw = intent.getStringExtra(PASSWORD);
        createConnection sockReader = new createConnection();
        Thread readFromSoc = new Thread(sockReader);
        readFromSoc.start();


    }

    @Override
    protected void onResume() {
        super.onResume();
        clientConn.setWhoIsActive(GROUP_LIST);
        Thread readToLog = new Thread(new logMsgs());
        readToLog.start();
        //TODO: FIND A WAY TO RESTART THE READING THREAD
    }

    @Override
    protected void onPause() {
        super.onPause();
        clientConn.setWhoIsActive(SocketHandler.INVALID);
        //TODO: FIND A WAY TO STOP THE READING THREAD
    }

    private class logMsgs implements Runnable {
        private String msg = null;

        public void run() {
            while (clientConn.getWhoIsActive().equals(GROUP_LIST)) {
                if (msg == null) {
                    msg = clientConn.getMsg();
                }
                if (msg != null) {
                    try {
                        addToLog(msg + (char) SocketHandler.END_OF_MSG_CHAR);
                        msg = null;
                    }
                    catch(Exception e)
                    {

                    }

                }

            }
        }

    }

    private class createConnection implements Runnable {
        public void run() {
            try {
                clientConn.setId(id);
                clientConn.setPw(pw);
                int response = clientConn.handleGroupMsgs();
                if (response == SocketHandler.CONN_ERROR) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            new AlertDialog.Builder(GroupList.this)
                                    .setTitle("ERROR")
                                    .setMessage("Invalid username or password... returning to login screen!")
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

                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            myList = (ListView) findViewById(R.id.list);

                            String[] values = new String[groups.size()];
                            for (int i = 0; i < groups.size(); ++i) {
                                values[i] = groups.elementAt(i);
                            }
                            groups.clear();
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(GroupList.this,
                                    android.R.layout.simple_list_item_1, android.R.id.text1, values);
                            myList.setAdapter(adapter);
                            myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view,
                                                        int position, long id) {
                                    int itemPosition = position;
                                    String itemValue = (String) myList.getItemAtPosition(position);
                                    Intent intent = new Intent(GroupList.this, DisplayMessageActivity.class);
                                    intent.putExtra(SocketHandler.GROUPNAME, itemValue);
                                    startActivity(intent);
                                }
                            });
                        }
                    });
                    Thread readToLog = new Thread(new logMsgs());
                    readToLog.start();
                    clientConn.groupMsgReader(); //TODO: ERROR HANDLING
                }
            } catch (Exception e) {
            }
        }
    }
}
