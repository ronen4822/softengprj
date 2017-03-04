package serverChat;

public class StudentInfo {
	/*  Contains information about a student written in the database*/
	private String name;
	private Boolean exists;
	public StudentInfo(String name,Boolean exists)
	{
		this.name=name;
		this.exists=exists;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setExists(Boolean exists) {
		this.exists = exists;
	}
	public String getName() {
		return name;
	}
	public Boolean getExists() {
		return exists;
	}
	
}
