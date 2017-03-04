package serverChat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

public class Client {
	private String name;
	private int id;
	private boolean isActive;
	private String password;
	private Vector<String> groupList;
	private Socket clientSoc;
	private PrintWriter out;

	// will contain a list of groups later
	public Client() {
		this.name = "";
		this.id = -1;
		this.password = "";
		this.isActive=false;
		this.clientSoc=null;
		this.groupList = new Vector<String>();
	}
	public void setActive(Socket sockNum,PrintWriter out)
	{
		clientSoc=sockNum;
		isActive=true;
		this.out=out;
	}
	public Socket getSocket()
	{
		return this.clientSoc;
	}
	public PrintWriter getOut()
	{
		return this.out;
	}
	public boolean isActive()
	{
		return isActive;
	}
	public void add(String cur_val) {
		if (id == -1) {
			id = Integer.parseInt(cur_val);
			return;
		}
		if (name == "") {
			name = cur_val;
			return;
		}
		if (password == "") {
			password = cur_val;
		}
	}

	public void setList(String path) {
		try {
			FileReader in = new FileReader(path);
			BufferedReader File_buf = new BufferedReader(in);
			String tmpString;
			while ((tmpString = File_buf.readLine()) != null) {
				groupList.add(tmpString);
			}
			File_buf.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void printGroups(PrintWriter out)
	{
		for (int i=0;i<groupList.size();++i)
		{
			out.println(groupList.elementAt(i));
		}
		out.println("Done");
	}
	public boolean hasGroup(String groupName)
	{
		for (int i=0;i<groupList.size();++i)
		{
			if (groupList.elementAt(i).equals(groupName))
			{
				return true;
			}
		}
		return false;
	}
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String compareValues(int id, String password) {
		if (this.id != id || this.password != password) {
			return "ID or Password incorrect";
		}
		return "OK";
	}

	public String getName() {
		return name;
	}
}
