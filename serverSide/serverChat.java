package serverChat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

public class serverChat {

	private static final String LOGIN_URL_ADDRESS = "http://lemida.biu.ac.il/login/index.php";
	private static Vector<Client> client_list = new Vector<Client>();
	private static Object mutex = new Object();
	private static Object fileMutex = new Object();
	private static final String FILE_PATH = "/home/ronen/Desktop/Project/Clients";
	private static final String DIR_PATH = "/home/ronen/Desktop/Project/";
	static final String SEND_ACK = (char) 195 + "SEND_ACK" + (char) 195;
	private static final String ACK_STRING = (char) 195 + "ACK" + (char) 195;

	public StudentInfo checkWeb(String userId, String userPw) {
		WebDriver driver = new PhantomJSDriver();
		String studName;
		driver.get(LOGIN_URL_ADDRESS);
		WebElement id = driver.findElement(By.id("username"));
		WebElement pw = driver.findElement(By.id("password"));
		id.sendKeys(userId);
		pw.sendKeys(userPw);
		WebElement button = driver.findElement(By.id("loginbtn"));
		if (button.isEnabled()) {
			button.click();
		}
		if (driver.getCurrentUrl().equals(LOGIN_URL_ADDRESS)) {
			driver.close();
			return (new StudentInfo("", false));
		}
		List<WebElement> courses = driver.findElements(By
				.className("coursename"));
		String delims = "[ ]";
		Vector<String> courseList = new Vector<String>();
		String curCourse;
		for (int i = 0; i < courses.size(); ++i) {
			String[] tokens = courses.get(i).getText().split(delims);
			curCourse = "";
			for (int j = 1; j < tokens.length; ++j) {
				curCourse += tokens[j] + " ";
			}
			if (!courseList.contains(curCourse)) {
				courseList.add(curCourse);
			}
		}
		WebElement name = driver.findElement(By.className("fullname"));
		studName = name.getText();

		driver.close();
		// add to clients
		//
		synchronized (fileMutex) {
			if (!manageFiles(userId, userPw, studName, courseList)) {
				return new StudentInfo("", false);
			}
		}
		return new StudentInfo(studName, true);
	}

	private Boolean manageFiles(String userId, String userPw, String realName,
			Vector<String> courseList) {
		File clientFile = new File(FILE_PATH);
		if (!clientFile.exists()) {
			try {
				clientFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("FAILED TO CREATE CLIENT FILE!!");
				e.printStackTrace();
				return false;
			}
		}
		try {
			FileWriter writer = new FileWriter(clientFile, true);
			writer.append(userId + "," + realName + "," + userPw + "\n");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("FAILED TO OPEN CLIENT FILE!!");
			e.printStackTrace();
			return false;
		}

		File groupFile = new File(DIR_PATH + userId);
		if (!groupFile.exists()) {
			try {
				groupFile.createNewFile();
			} catch (IOException e) {
				System.out.println("FAILED TO CREATE GROUP FILE FOR CLIENT!!");
				e.printStackTrace();
				return false;
			}
		}
		try {
			FileWriter writer = new FileWriter(groupFile, true);
			for (int i = 0; i < courseList.size(); ++i) {
				writer.append(courseList.elementAt(i) + '\n');
				writer.flush();
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("FAILED TO OPEN CLIENT FILE!!");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		try {
			InetAddress addr = InetAddress.getByName("192.168.1.26");
			ServerSocket listener = new ServerSocket(55555, 50, addr); // address

			// file reading
			String sCurrentLine;
			int size_of_list = 0;
			if (new File(FILE_PATH).exists()) {
				FileReader in = new FileReader(FILE_PATH);

				BufferedReader File_buf = new BufferedReader(in);

				while ((sCurrentLine = File_buf.readLine()) != null) {
					String sub_string = "";
					int i = 0;
					sCurrentLine += '\n';
					client_list.add(new Client());
					while (sCurrentLine.charAt(i) != '\n') {
						int j = i;
						while (sCurrentLine.charAt(j) != ','
								&& sCurrentLine.charAt(j) != '\n') {
							sub_string += sCurrentLine.charAt(j);
							j++;
						}
						if (sCurrentLine.charAt(j) != '\n') {
							j++; // to avoid ","
						}
						i = j;
						client_list.elementAt(size_of_list).add(sub_string);
						sub_string = "";
					}
					client_list.elementAt(size_of_list).setList(
							"/home/ronen/Desktop/Project/"
									+ client_list.elementAt(size_of_list)
											.getId());
					size_of_list++;
				}
				File_buf.close();
			}

			// finished reading file

			Socket tmp;
			int vecSize = 0;
			serverChat srv = new serverChat();
			Thread tmpThread;
			while (true) {
				tmp = listener.accept();
				System.out.println("Client: " + vecSize + " connected");
				synchronized (mutex) {
					tmpThread = new Thread(srv.new newClient(tmp));
					tmpThread.start();
				}
				vecSize++;
			}
		} finally {
			System.exit(0);
		}
	}

	public int isUserValid(String id, String pw) {
		synchronized (mutex) {
			for (int i = 0; i < client_list.size(); ++i) {
				if (client_list.elementAt(i).getId() == Integer.parseInt(id)
						&& client_list.elementAt(i).getPassword().equals(pw)) {
					return i;
				}
			}
		}
		return -1;
	}

	private class newClient implements Runnable {
		private Socket sockNum;

		public newClient(Socket sockNum) {
			this.sockNum = sockNum;
		}

		@Override
		public void run() {
			BufferedReader in;
			PrintWriter out;
			try {
				in = new BufferedReader(new InputStreamReader(
						sockNum.getInputStream()));
				out = new PrintWriter(new OutputStreamWriter(
						sockNum.getOutputStream()), true);
				// all reads and writes initialized
				String id, pw;
				id = in.readLine();
				pw = in.readLine();
				StudentInfo currentStudent;
				int locInClients = isUserValid(id, pw);
				if (locInClients == -1) {
					currentStudent = checkWeb(id, pw);
					if (currentStudent.getExists()) {
						client_list.add(new Client());
						int i = client_list.size() - 1;
						client_list.elementAt(i).add(id);
						client_list.elementAt(i).add(currentStudent.getName());
						System.out.println(currentStudent.getName());
						client_list.elementAt(i).add(pw);
						synchronized (fileMutex) {
							client_list.elementAt(i).setList(DIR_PATH + id);
						}
						locInClients = i;
					} else {
						locInClients = -1;
					}
				}
				synchronized (mutex) {
					if (locInClients == -1) {
						out.println("Access denied");
						out.close();
						in.close();
						sockNum.close();
						// TODO: close the connection;
					} else {
						client_list.elementAt(locInClients).setActive(sockNum,
								out);
						out.println(client_list.elementAt(locInClients)
								.getName());
						client_list.elementAt(locInClients).printGroups(out);
					}
				}
				if (locInClients == -1) {
					return;
				}
				String input = "";
				String groupName = "";
				String msg;
				int isGroup;
				char tmp;
				int weirdChar = 194;
				while (true) {
					input = "";
					groupName = "";
					msg = "";
					isGroup = 1;
					while ((tmp = (char) in.read()) != (char) weirdChar) {
						if ((int) tmp == -1) {
							// set client to off
							in.close();
							out.close();
							sockNum.close();
							return;
						}
						input += tmp;
					}
					if (input.equals(SEND_ACK)) {
						synchronized (mutex) {
							out.print(ACK_STRING + (char) weirdChar);
							out.flush();
						}
					} else {
						input += (char) 194;
						for (int i = 0; i < input.length(); ++i) {
							if (isGroup == 1 && input.charAt(i) == ':') {
								isGroup = 0;
								continue;
							}
							if (isGroup == 1) {
								groupName += input.charAt(i);
								continue;
							}
							if (isGroup == 0) {
								msg += input.charAt(i);
							}
						}

						PrintWriter tmpWriter;
						synchronized (mutex) {
							for (int i = 0; i < client_list.size(); ++i) {
								if (client_list.elementAt(i).getSocket() != sockNum
										&& client_list.elementAt(i).hasGroup(
												groupName)
										&& client_list.elementAt(i).isActive()) {
									System.out.println(input);
									tmpWriter = client_list.elementAt(i)
											.getOut();
									tmpWriter.print(input);
									tmpWriter.flush();
								}
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}