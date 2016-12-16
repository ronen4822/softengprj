package com.example.ronen.firstprojtry;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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

public class DisplayMessageActivity extends AppCompatActivity {
    public BufferedReader reader;
    private Socket clientSoc;
    private String name;
    public PrintWriter out;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        Intent intent= getIntent();
        String ip=intent.getStringExtra("IP");
        name=intent.getStringExtra("USERNAME");
        String port=intent.getStringExtra("PORT");

     /*   ConnectivityManager connMgr=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInf=connMgr.getActiveNetworkInfo();*/
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
        EditText edText=(EditText) findViewById(R.id.sendText);
        String msg=edText.getText().toString();
        MsgHandler sendMsg=new MsgHandler(name+": "+msg);
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
        private TextView thisText;
        private String ip;
        private String output;

        public MsgPrinter(String ip,String port) throws IOException {
            this.ip=ip;
            this.port=port;
            thisText=(TextView) findViewById(R.id.sendText);
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
                        thisText.setText(output);
                    }
                 });
            }
        }

    }


}
