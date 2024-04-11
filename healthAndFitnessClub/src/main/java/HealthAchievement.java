import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Scanner;

public class HealthAchievement {

    private Connection connection;
    private int achievement_id,
        member_id;
    private Date date;
    private String title,
           description;
    private Scanner sc = new Scanner(System.in);

    public HealthAchievement(Connection conn, int a, int m, String t, Date d, String des){
        connection = conn;
        achievement_id = a;
        member_id = m;
        title = t;
        date = d;
        description = des;
    }

    public void delete(){
        try{
            PreparedStatement pstmt = connection.prepareStatement("DELETE FROM health_achievements WHERE achievement_id = ?");
            pstmt.setInt(1, achievement_id);

            // execute the query to delete entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Successful deletion");
            }
            else{
                System.out.println("Unable to delete Health Achievement. Please try again");
            }
        }
        // catch any exceptions
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void edit(){
        int choice = -1;

        System.out.println("\nEDIT ACHIEVEMENT " + '"' + title + '"');
        while(choice == -1){
            System.out.println("What would you like to edit?");
            System.out.println("1. Title");
            System.out.println("2. Description");
            System.out.println("3. Date");
            System.out.print("\nChoice: ");
            choice = Integer.parseInt(sc.nextLine());
            if(choice == 1) setTitle();
            else if (choice == 2) setDescription();
            else if (choice == 3) setDate();
            else choice = -1;
        }
    }

    private void setDate(){
        String newDate;
        System.out.print("New Date (yyyy-mm-dd): ");
        newDate = sc.nextLine();
        try{
            // create query to update the entry who has member id = member_id with first_name = newFName
            PreparedStatement pstmt = connection.prepareStatement("UPDATE health_achievements SET date = ? WHERE achievement_id = ?");

            // populate query with the provided member_id and newFName
            Date date = Date.valueOf(newDate);
            pstmt.setDate(1, date);
            pstmt.setInt(2, achievement_id);

            // execute the query to update entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Update date: " + newDate);
            }
            else{
                System.out.println("Unable to update date. Please try again");
            }
        }
        // catch any exceptions
        catch(Exception e){
            System.out.println("Unable to update date. Please try again");
        }
    }

    private void setDescription(){
        String newDes;
        System.out.print("New Description: ");
        newDes = sc.nextLine();
        try{
            // create query to update the entry who has member id = member_id with first_name = newFName
            PreparedStatement pstmt = connection.prepareStatement("UPDATE health_achievements SET description = ? WHERE achievement_id = ?");

            // populate query with the provided member_id and newFName
            pstmt.setString(1, newDes);
            pstmt.setInt(2, achievement_id);

            // execute the query to update entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Update description: " + newDes);
            }
            else{
                System.out.println("Unable to update description. Please try again");
            }
        }
        // catch any exceptions
        catch(Exception e){
            System.out.println("Unable to update description. Please try again");
        }
    }

    private void setTitle(){
        String newTitle;
        System.out.print("New Title: ");
        newTitle = sc.nextLine();
        try{
            // create query to update the entry who has member id = member_id with first_name = newFName
            PreparedStatement pstmt = connection.prepareStatement("UPDATE health_achievements SET title = ? WHERE achievement_id = ?");

            // populate query with the provided member_id and newFName
            pstmt.setString(1, newTitle);
            pstmt.setInt(2, achievement_id);

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
        return (title + "\t" + date + "\n\t" + description);
    }


    public int getAchievement_id() {
        return achievement_id;
    }

    public int getMember_id() {
        return member_id;
    }

    public Date getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}

