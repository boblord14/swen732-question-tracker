package user;

public class User {
    private int id;
    private String username;
    private String password;
    private boolean isTeacher;

    public int getId() { return id;}
    public String getUsername() { return username;}
    public String getPassword() { return password;}
    public boolean getIsTeacher() { return isTeacher;}

    public void setId(int id) { this.id = id;}
    public void setUsername(String username) { this.username = username;}
    public void setPassword(String password) { this.password = password;}
    public void setIsTeacher(boolean isTeacher) { this.isTeacher = isTeacher;}
}