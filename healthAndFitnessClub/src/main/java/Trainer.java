import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Scanner;

import static java.lang.System.exit;

public class Trainer {
    private Connection connection;
    private int trainer_id;
    private String  first_name,
            last_name,
            email,
            password,
            sex;

    private Scanner sc = new Scanner(System.in);

    public Trainer(Connection conn, int id, String fname, String lname, String e, String p, String s){
        connection = conn;
        trainer_id = id;
        first_name = fname;
        last_name = lname;
        email = e;
        password = p;
        sex = s;
    }

    public void menu(){
        int choice = -1;
        System.out.println("\n\n --- HEALTH AND FITNESS CLUB--- \nWELCOME TRAINER " + first_name.toUpperCase() +
                " " + last_name.toUpperCase());
        while(choice == -1){
            System.out.println("1. Manage Trainer Profile");
            System.out.println("2. Search Member by Name");
            System.out.println("3. View Availability");
            System.out.println("4. Log Out");
            System.out.print("Choice: ");
            choice = Integer.parseInt(sc.nextLine());
            System.out.println("\n");
            if(choice == 1) manageProfile();
            else if (choice == 2) searchMember();
            else if (choice == 3) availability();
            else if (choice == 4) exit(-1);
            else choice = -1;
        }
    }

    private void availability(){
        int choice = -1;
        while(choice == -1){
            System.out.println("--- AVAILABILITY ---");
            viewAvailability();
            System.out.println("\n\n1. Add Availability");
            System.out.println("0. Go back");
            System.out.print("Choice: ");
            choice = Integer.parseInt(sc.nextLine());
            System.out.println("\n");
            if(choice == 1) addAvailability();
            else if (choice == 2) addAvailability();
            else if (choice == 0) menu();
            else choice = -1;
        }
        if(choice < 4 && choice > 0){
            viewAvailability();
        }
        menu();
    }

    private void addAvailability(){
        System.out.println("--- ADD AVAILABILITY ---");
        System.out.print("\nEnter date (YYYY-MM-DD): ");
        LocalDate newDate = LocalDate.parse(sc.nextLine());

        System.out.print("\nEnter start time (HH:MM): ");
        LocalTime newStartTime = LocalTime.parse(sc.nextLine());

        System.out.print("\nEnter end time (HH:MM): ");
        LocalTime newEndTime = LocalTime.parse(sc.nextLine());
        if(!hasConflict(newDate, newStartTime, newEndTime,-1)){
            Time durationTime = calculateDuration(newStartTime, newEndTime);

            String query = "INSERT INTO trainer_schedule (trainer_id, date, start_time, end_time, duration) VALUES (?, ?, ?, ?, ?)";

            try{
                PreparedStatement pstmt = connection.prepareStatement(query);

                pstmt.setInt(1, trainer_id);
                pstmt.setDate(2, Date.valueOf(newDate));
                pstmt.setTime(3, Time.valueOf(newStartTime));
                pstmt.setTime(4, Time.valueOf(newEndTime));
                pstmt.setTime(5, durationTime);

                if(pstmt.executeUpdate() > 0){
                    System.out.println("Successfully added new availability");
                }
            }catch(Exception e){
                System.out.println("Something went wrong. Please try again");
                menu();
            }
        }
        else{
            System.out.println("There's conflict with an existing session. Please try again");
            menu();
        }
    }

    private void viewAvailability(){
        int count = 0;
        try{
            // create query get all the entries in the students table
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM trainer_schedule WHERE trainer_id =" + trainer_id);
            // execute the query
            ResultSet resultSet = pstmt.executeQuery();

            // print out the data returned by the query
            System.out.println("Schedule ID \t Date \t\t\t Start Time \t\t End Time \t\t Duration");
            while(resultSet.next()){
                System.out.println(resultSet.getInt("schedule_id") + "\t\t\t\t " +
                        resultSet.getString("date") + "\t\t " +
                        resultSet.getString("start_time") + "\t\t\t " +
                        resultSet.getString("end_time") + "\t\t " +
                        resultSet.getString("duration"));
                count++;
            }
        }
        // catch any exceptions
        catch(Exception e){
            menu();
        }
    }

    private static Time calculateDuration(LocalTime newStartTime, LocalTime newEndTime) {
        // Calculate duration between start time and end time
        Duration duration = Duration.between(newStartTime, newEndTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        Time durationTime = Time.valueOf(String.format("%02d:%02d:00", hours, minutes));
        return durationTime;
    }

    public boolean hasConflict(LocalDate date, LocalTime startTime, LocalTime endTime, int s_id) {
        String query = "SELECT COUNT(*) FROM trainer_schedule " +
                "WHERE trainer_id = ? AND date = ? " +
                "AND schedule_id != ? " +
                "AND ((start_time <= ? AND end_time >= ?) " +
                "OR (start_time >= ? AND start_time < ?) " +
                "OR (end_time > ? AND end_time <= ?))";

        try{
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, trainer_id);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            pstmt.setInt(3, s_id);
            pstmt.setTime(4, java.sql.Time.valueOf(startTime));
            pstmt.setTime(5, java.sql.Time.valueOf(endTime));
            pstmt.setTime(6, java.sql.Time.valueOf(startTime));
            pstmt.setTime(7, java.sql.Time.valueOf(endTime));
            pstmt.setTime(8, java.sql.Time.valueOf(startTime));
            pstmt.setTime(9, java.sql.Time.valueOf(endTime));

            ResultSet resultSet = pstmt.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);
            return count > 0; // If count > 0, there is a conflict

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    private void searchMember(){
        String name, query;
        ArrayList<Member> memberSearch = new ArrayList<Member>();

        System.out.print("First OR Last Name (press enter to search for all members): ");
        name = sc.nextLine();
        if(name.length() == 0){
            query = "SELECT * FROM Member";
        }
        else{
            query = "SELECT * FROM Member WHERE first_name = ? OR last_name = ?";
        }

        try{
            PreparedStatement pstmt = connection.prepareStatement(query);

            // populate query with the provided member_id and newFName
            if(name.length() != 0){
                pstmt.setString(1, name);
                pstmt.setString(2, name);
            }

            // execute the query
            ResultSet resultSet = pstmt.executeQuery();

            System.out.println("\n\nMember ID \t\t First Name \t\t Last Name \t\t Email");
            while(resultSet.next()){
                System.out.println(resultSet.getString("member_id") + "\t\t\t\t " +
                        resultSet.getString("first_name") + "\t\t\t\t " +
                        resultSet.getString("last_name") + "\t\t\t " +
                        resultSet.getString("email") + "\t\t ");

                memberSearch.add(new Member(connection,
                        resultSet.getInt("member_id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("email"),
                        resultSet.getDate("date_of_birth"),
                        resultSet.getString("password"),
                        resultSet.getString("sex"),
                        resultSet.getDouble("height"),
                        resultSet.getDouble("weight"),
                        resultSet.getInt("heart_rate")));
            }
            if(memberSearch.size() > 0) editMember(memberSearch);
        }
        // catch any exceptions
        catch(Exception e){
            e.printStackTrace();
            System.out.println("Error getting members... Please try again");
        }
        menu();
    }

    private void editMember(ArrayList<Member> memberList){
        int choice = -1;

        while(choice == -1){
            System.out.print("View info for member ID (0 for none): ");
            choice = Integer.parseInt(sc.nextLine());
            String memberQuery = "SELECT * FROM member WHERE member_id = ?";

            try {
                PreparedStatement stmt = connection.prepareStatement(memberQuery);
                stmt.setInt(1, choice);
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    Member m = new Member(connection,
                            resultSet.getInt("member_id"),
                            resultSet.getString("first_name"),
                            resultSet.getString("last_name"),
                            resultSet.getString("email"),
                            resultSet.getDate("date_of_birth"),
                            resultSet.getString("password"),
                            resultSet.getString("sex"),
                            resultSet.getDouble("height"),
                            resultSet.getDouble("weight"),
                            resultSet.getInt("heart_rate"));
                    m.trainerManageProfile();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void manageProfile(){
        int choice = -1;

        System.out.println("--- PROFILE ---");
        System.out.println("1. First Name: " + first_name);
        System.out.println("2. Last Name: " + last_name);
        System.out.println("3. Password: " + password);
        System.out.println("4. Email: " + email);
        System.out.println("\n0. Back to Menu \n");

        while(choice == -1){
            System.out.print("What would you like to change? ");
            choice = Integer.parseInt(sc.nextLine());
            System.out.println("\n");
            if(choice == 1) setFName();
            else if (choice == 2) setLName();
            else if (choice == 3) setPassword();
            else if (choice == 4) setEmail();
            else if (choice == 0) menu();
            else choice = -1;
        }
    }

    private void setFName(){
        String newFName;
        System.out.print("New First Name: ");
        newFName = sc.nextLine();
        try{
            // create query to update the entry who has member id = member_id with first_name = newFName
            PreparedStatement pstmt = connection.prepareStatement("UPDATE trainer SET first_name = ? WHERE trainer_id = ?");

            // populate query with the provided member_id and newFName
            pstmt.setString(1, newFName);
            pstmt.setInt(2, trainer_id);

            // execute the query to update entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Update first name: " + newFName);
            }
            else{
                System.out.println("Unable to update first name. Please try again");
            }
            updateCurrInfo();
            manageProfile();
        }
        // catch any exceptions
        catch(Exception e){
            System.out.println("Unable to update first name. Please try again");
            manageProfile();
        }
    }

    private void setLName(){
        String newLName;
        System.out.print("New Last Name: ");
        newLName = sc.nextLine();
        try{
            // create query to update the entry who has member id = member_id with last_name = newLName
            PreparedStatement pstmt = connection.prepareStatement("UPDATE trainer SET last_name = ? WHERE trainer_id = ?");

            // populate query with the provided member_id and newLName
            pstmt.setString(1, newLName);
            pstmt.setInt(2, trainer_id);

            // execute the query to update entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Update last name: " + newLName);
            }
            else{
                System.out.println("Unable to update last name. Please try again");
            }
            updateCurrInfo();
            manageProfile();
        }
        // catch any exceptions
        catch(Exception e){
            System.out.println("Unable to update last name. Please try again");
            manageProfile();
        }
    }

    private void setPassword(){
        String pswd;
        System.out.print("New Password: ");
        pswd = sc.nextLine();
        try{
            // create query to update the entry who has member id = member_id with password = pswd
            PreparedStatement pstmt = connection.prepareStatement("UPDATE trainer SET password = ? WHERE trainer_id = ?");

            // populate query with the provided member_id and pswd
            pstmt.setString(1, pswd);
            pstmt.setInt(2, trainer_id);

            // execute the query to update entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Update password: " + pswd);
            }
            else{
                System.out.println("Unable to update password. Please try again");
            }
            updateCurrInfo();
            manageProfile();
        }
        // catch any exceptions
        catch(Exception e){
            System.out.println("Unable to update password. Please try again");
            manageProfile();
        }
    }

    private void setEmail(){
        String e;
        System.out.print("New Email: ");
        e = sc.nextLine();
        try{
            // create query to update the entry who has member id = member_id with email = e
            PreparedStatement pstmt = connection.prepareStatement("UPDATE trainer SET email = ? WHERE trainer_id = ?");

            // populate query with the provided member_id and e
            pstmt.setString(1, e);
            pstmt.setInt(2, trainer_id);

            // execute the query to update entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Update email: " + e);
            }
            else{
                System.out.println("Unable to update email. Please try again");
            }
            updateCurrInfo();
            manageProfile();
        }
        // catch any exceptions
        catch(Exception ex){
            System.out.println("Unable to update email. Please try again");
            manageProfile();
        }
    }

    private void updateCurrInfo(){
        try{
            // create query get all the entries in the students table
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM trainer WHERE trainer_id = " + trainer_id);
            // execute the query
            ResultSet resultSet = pstmt.executeQuery();

            if(resultSet.next()){
                first_name = resultSet.getString("first_name");
                last_name = resultSet.getString("last_name");
                email = resultSet.getString("email");
                password = resultSet.getString("password");
            }
        }
        // catch any exceptions
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
