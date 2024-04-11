import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Scanner;

public class HealthGoal {

    private Connection connection;
    private int goal_id;

    private int member_id;
    private Date creation_date;
    private String title;

    private Scanner sc = new Scanner(System.in);

    public HealthGoal(Connection conn, int g, int m, String t, Date d){
        connection = conn;
        goal_id = g;
        member_id = m;
        title = t;
        creation_date = d;
    }

    public void delete(){
        try{
            PreparedStatement pstmt = connection.prepareStatement("DELETE FROM health_goals WHERE goal_id = ?");
            pstmt.setInt(1, goal_id);

            // execute the query to delete entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Successful deletion");
            }
            else{
                System.out.println("Unable to delete Health Goal. Please try again");
            }
        }
        // catch any exceptions
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void edit(){
        System.out.println("\nEDIT ACHIEVEMENT " + '"' + title + '"');
        setTitle();
    }

    private void setTitle(){
        String newTitle;
        System.out.print("New Title: ");
        newTitle = sc.nextLine();
        try{
            PreparedStatement pstmt = connection.prepareStatement("UPDATE health_goals SET title = ? WHERE goal_id = ?");

            pstmt.setString(1, newTitle);
            pstmt.setInt(2, goal_id);

            // execute the query to update entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Update title: " + newTitle);
            }
            else{
                System.out.println("Unable to update title. Please try again");
            }
        }
        // catch any exceptions
        catch(Exception e){
            System.out.println("Unable to update title. Please try again");
        }
    }

    public String toString(){
        return (title + "\t" + creation_date);
    }

    public String toBasicString(){
        return (title + "\t" + creation_date);
    }

    public int getGoal_id(){
        return goal_id;
    }

    public Date getCreation_date() {
        return creation_date;
    }

    public String getTitle() {
        return title;
    }

}
