package com.example.ronen.firstprojtry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;


public class SocketHandler {

    public static final String HOST = "192.168.1.26";
    public static final String INVALID = "NULL";
    public static final String GROUPNAME = "GROUPNAME";
    public static final int PORT = 55555;
    public static final String DONE = "Done";
    public static final String ACCESS_DENIED = "Access denied";
    public static final int CONN_ERROR=1;
    public static final int SUCCESS=0;
    public static final int END_OF_MSG_CHAR = 194;

    private String id;
    private String pw;
    private static SocketHandler instance = null;

    private Vector<String> msgVector;
    private Object mutex=new Object();
    private Socket clientSoc;
    private BufferedReader reader;
    private PrintWriter out;
    private String name;
    private InetAddress serverAdd;
    private String whoIsActive;

    protected SocketHandler() {
        msgVector=new Vector<>();
        clientSoc = null;
        name = null;
        whoIsActive = null;
        reader = null;
        out = null;
    }
    public String getWhoIsActive() {
        return whoIsActive;
    }

    public void setWhoIsActive(String whoIsActive) {
        this.whoIsActive = whoIsActive;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getName() {
        return name;
    }

    public static SocketHandler getInstance() {
        if (instance == null) {
            instance = new SocketHandler();
        }
        return instance;
    }

    public int handleGroupMsgs() {
        try {
            serverAdd = InetAddress.getByName(HOST);
            if (!serverAdd.isReachable(300)) {
                //connError();
            }
            clientSoc = new Socket(serverAdd, PORT);
            reader = new BufferedReader(new InputStreamReader(
                    clientSoc.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(clientSoc.getOutputStream()), true);
            out.println(id);
            out.println(pw);
            String answer = reader.readLine();
            if (answer.equals(ACCESS_DENIED)) {
                out.close();
                reader.close();
                clientSoc.close();
                return CONN_ERROR; //MEANS THAT LOGIN HAS FAILED- RETURN TO FIRST ACTIVITY WITH ERROR MSG
                //TODO:
            }
            else
            {
                name=answer;
                String tmp;
                while (!(tmp = reader.readLine()).equals(DONE)) {
                    GroupList.addGroup(tmp);
                }
                return SUCCESS;
            }
        }
        catch (IOException e)
        {
            return -1;
        }
    }
    public void groupMsgReader()
    {
        try {
            String output="";
            String tmpOutput;
            char tmpChar;
            while (true) {
                output="";
                while ((tmpChar = (char) reader.read()) != (char) END_OF_MSG_CHAR) {
                    output += tmpChar;
                }
                tmpOutput=output;
                synchronized (mutex)
                {
                    msgVector.add(tmpOutput);
                }
            }
        }
        catch (Exception e)
        {
            //TODO:
        }
    }
    public String getMsg()
    {
        String firstMsg=null;
        synchronized (mutex)
        {
            if (msgVector.size()!=0) {
                firstMsg=msgVector.firstElement();
                msgVector.remove(0);
            }
        }
        return firstMsg;
    }
    public void sendMsg(String msg)
    {
        out.print(msg);
        out.flush();
    }





}
