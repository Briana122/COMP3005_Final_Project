import java.math.BigDecimal;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import static java.lang.System.exit;

public class Member {

    private Connection connection;
    private int member_id;
    private String  first_name,
                    last_name,
                    email,
                    password,
                    sex;
    private Date    date_of_birth;
    private double  height,
                    weight;

    private int     heart_rate;

    private ArrayList<HealthGoal> health_goals;
    private ArrayList<HealthAchievement> health_achievements;
    private ArrayList<FitnessRoutine> fitness_routines;

    private Scanner sc = new Scanner(System.in);

    public Member(Connection conn, int id, String fname, String lname, String e, Date dob,
                  String p, String s, double h, double w, int hr){
        connection = conn;
        member_id = id;
        first_name = fname;
        last_name = lname;
        email = e;
        date_of_birth = dob;
        password = p;
        sex = s;
        height = h;
        weight = w;
        heart_rate = hr;
        getAllHealthAchievements();
        getAllHealthGoals();
        getAllFitnessRountines();
    }

    public void menu(){
        int choice = -1;
        System.out.println("\n\n --- HEALTH AND FITNESS CLUB--- \nWELCOME " + first_name + " " + last_name);
        while(choice == -1){
            System.out.println("1. Manage Profile");
            System.out.println("2. View Dashboard");
            System.out.println("3. View Group Classes");
            System.out.println("4. Book Private Class");
            System.out.println("5. Log Out");
            System.out.print("Choice: ");
            choice = Integer.parseInt(sc.nextLine());
            System.out.println("\n");
            if(choice == 1) manageProfile();
            else if (choice == 2) dashboard();
            else if (choice == 3) exploreSession();
            else if (choice == 4) bookPrivateSession();
            else if (choice == 5) exit(-1);
            else choice = -1;
        }
    }

    private void bookPrivateSession(){
        int trainerChosen = 0, roomChosen = 0;
        LocalDate newDate;
        LocalTime newStartTime, newEndTime;
        double cost;
        String type, title, description;
        Time duration;

        System.out.println("--- NEW SESSION ---");

        try {
            System.out.print("\nTitle: ");
            title = sc.nextLine();

            System.out.print("Description: ");
            description = sc.nextLine();

            System.out.print("Date (YYYY-MM-DD): ");
            newDate = LocalDate.parse(sc.nextLine());

            System.out.print("Start time (HH:MM): ");
            newStartTime = LocalTime.parse(sc.nextLine());

            System.out.print("End time (HH:MM): ");
            newEndTime = LocalTime.parse(sc.nextLine());

            System.out.println("All private sessions cost $40/hour.");

            duration = calculateDuration(newStartTime, newEndTime);

            cost = 40 * duration.toLocalTime().getHour();
        }
        catch(Exception e){
            System.out.println("Something went wrong. Please try again.");
            return;
        }

        Set<Integer> trainerID = getAvailableTrainers(newDate, newStartTime, newEndTime);

        if(trainerID.size() != 0){
            while(true) {
                System.out.print("\nPlease select trainer ID: ");
                trainerChosen = Integer.parseInt(sc.nextLine());
                if(trainerID.contains(trainerChosen)) break;
            }
        }
        else{
            System.out.println("No trainers available at this time.");
            menu();
            return;
        }

        Set<Integer> roomID = getAvailableRooms(Date.valueOf(newDate), Time.valueOf(newStartTime), Time.valueOf(newEndTime));

        if (roomID.size() != 0){
            while(true) {
                System.out.print("\nPlease select room ID: ");
                roomChosen = Integer.parseInt(sc.nextLine());
                if(roomID.contains(roomChosen)) break;
            }
        }
        else{
            System.out.println("No rooms available at this time.");
            menu();
            return;
        }

        try{
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO session (date, cost, start_time, " +
                    "end_time, duration, type, title, description, trainer_id, room_id) VALUES(?, ?, ?, ?, " +
                    "?, ?, ?, ?, ?, ?)");

            pstmt.setDate(1, Date.valueOf(newDate));
            pstmt.setDouble(2, cost);
            pstmt.setTime(3, Time.valueOf(newStartTime));
            pstmt.setTime(4, Time.valueOf(newEndTime));
            pstmt.setTime(5, duration);
            pstmt.setString(6, "PRIVATE");
            pstmt.setString(7, title);
            pstmt.setString(8, description);
            pstmt.setInt(9, trainerChosen);
            pstmt.setInt(10, roomChosen);

            // execute the query to update entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Added session successfully.");
                createBill("PRIVATE", cost, Date.valueOf(newDate));
                menu();
                return;
            }
            else{
                System.out.println("Unable to add session. Please try again");
                menu();
                return;
            }
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("Could not add session. Please try again.");
            menu();
        }
    }

    private void createBill(String type, double cost, Date date){
        try{
            String query = "INSERT INTO Bills (member_id, amount, date, session_type) " +
                    "SELECT * FROM (SELECT ?, ?, CURRENT_DATE, ?) AS new_bill " +
                    "WHERE NOT EXISTS ( SELECT 1 FROM Bills " +
                    "WHERE member_id = ? AND amount = ? AND date = CURRENT_DATE AND session_type = ?)";
            // create query to insert a new row to the table
            PreparedStatement pstmt = connection.prepareStatement(query);


            // populate query with the provided student information
            pstmt.setInt(1, member_id);
            pstmt.setDouble(2, cost);
            pstmt.setString(3, type);
            pstmt.setInt(4, member_id);
            pstmt.setDouble(5, cost);
            pstmt.setString(6, type);

            // execute the query to add new student and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Bill Added");
            }
            else{
                System.out.println("Unable to add bill. Please try again");
            }
        }
        // catch any exceptions
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void exploreSession(){
        Set<Integer> groupID = new HashSet<Integer>();
        int sessionID;
        String query = "SELECT * " +
                "FROM session JOIN room on session.room_id = room.room_id " +
                "WHERE session.type = 'GROUP' " +
                "ORDER BY session.session_id ASC";
        try{
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet resultSet = pstmt.executeQuery();

            // print out the data returned by the query
            System.out.println("--- GROUP SESSIONS ---\n");
            System.out.println("ID");
            while(resultSet.next()){
                groupID.add(resultSet.getInt("session_id"));
                System.out.println(resultSet.getInt("session_id") + ". " +
                        resultSet.getString("title") + "\t" +
                        resultSet.getDate("date") + "\t" +
                        resultSet.getString("start_time") + "-" +
                        resultSet.getString("end_time"));
            }
            while(true) {
                System.out.print("Which session ID would you like to view? (-1 to go back) ");
                sessionID = Integer.parseInt(sc.nextLine());
                if(sessionID == -1){
                    menu();
                    return;
                }
                else if(groupID.contains(sessionID)) break;
            }
            displaySession(sessionID);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void displaySession(int sessionID){
        String query = "SELECT * FROM session WHERE session_id = " + sessionID;
        int choice = -1;

        try{
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet resultSet = pstmt.executeQuery();
            if(resultSet.next()){
                System.out.println(resultSet.getInt("session_id") + "\t" +
                        resultSet.getString("title") + "\n" +
                        resultSet.getString("description") + "\n\n" +
                        resultSet.getString("date") + "\t" +
                        resultSet.getString("start_time") + " - " +
                        resultSet.getString("end_time") + "\n\n");
                while(true) {
                    System.out.println("1. Register");
                    System.out.println("0. Go back");
                    choice = Integer.parseInt(sc.nextLine());
                    if(choice == 0){
                        exploreSession();
                        break;
                    }
                    else if (choice == 1){
                        registerSession(sessionID, resultSet.getInt("room_id"),
                                resultSet.getDouble("cost"),
                                resultSet.getDate("date"),
                                resultSet.getString("type"));
                        exploreSession();
                        break;
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void registerSession(int sessionID, int roomID, double cost, Date date, String type){
        // check if class is full
        if(isSessionFull(sessionID, getRoomCapcity(roomID))){
            System.out.println("Session is full. Please register for another session.");
            exploreSession();
            return;
        }
        // if not full, add member to class
        try{
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO register (session_id, member_id) VALUES(?, ?)");

            pstmt.setInt(1, sessionID);
            pstmt.setInt(2, member_id);

            if(pstmt.executeUpdate() > 0){
                createBill(type, cost, date);
                System.out.println("Registered for session success.");
            }
            else{
                System.out.println("Unable to register for session. Please try again");
                exploreSession();
                return;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private boolean isSessionFull(int sessionID, int maxCap){
        try{
            PreparedStatement pstmt = connection.prepareStatement("SELECT COUNT(DISTINCT member_id) AS total_entries " +
                    "FROM Register WHERE session_id = " + sessionID);
            ResultSet resultSet = pstmt.executeQuery();
            resultSet.next();
            // current registrants are more or equal to max capacity
            if(resultSet.getInt(1) >= maxCap) return true;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private int getRoomCapcity(int roomID){
        try{
            PreparedStatement pstmt = connection.prepareStatement("SELECT max_capacity FROM room WHERE room_id = " + roomID);
            ResultSet resultSet = pstmt.executeQuery();
            resultSet.next();
            return (resultSet.getInt(1));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    private void manageProfile(){
        int choice = -1;

        System.out.println("--- PROFILE ---");
        System.out.println("1. First Name: " + first_name);
        System.out.println("2. Last Name: " + last_name);
        System.out.println("3. Password: " + password);
        System.out.println("4. Email: " + email);
        System.out.println("5. Heart Rate: " + heart_rate);
        System.out.println("6. Height: " + height);
        System.out.println("7. Weight: " + weight);
        System.out.println("\n0. Back to Menu \n");

        while(choice == -1){
            System.out.print("What would you like to change? ");
            choice = Integer.parseInt(sc.nextLine());
            System.out.println("\n");
            if(choice == 1) setFName();
            else if (choice == 2) setLName();
            else if (choice == 3) setPassword();
            else if (choice == 4) setEmail();
            else if (choice == 5) setHeartRate();
            else if (choice == 6) setHeight();
            else if (choice == 7) setWeight();
            else if (choice == 0) menu();
            else choice = -1;
        }
        if(choice > 0 && choice < 8){
            updateCurrInfo();
            manageProfile();
        }
    }

    private void dashboard(){
        // view height, weight
        // modify fitness achievements, and fitness goals
        int choice = -1;

        getAllHealthAchievements();
        getAllFitnessRountines();
        getAllHealthGoals();

        System.out.println("--- DASHBOARD ---");
        System.out.println("First Name: " + first_name);
        System.out.println("Last Name: " + last_name);
        System.out.println("Heart Rate: " + heart_rate);
        System.out.println("Height: " + height);
        System.out.println("Weight: " + weight);
        System.out.println("Number of Health Achievements: " + health_achievements.size());
        System.out.println("Number of Health Goals: " + health_goals.size());
        System.out.println("Number of Fitness Routine: " + fitness_routines.size());

        System.out.println("\n1. View Health Achievements ");
        System.out.println("2. View Health Goals ");
        System.out.println("3. View Fitness Routines ");
        System.out.println("0. Back to Menu \n");

        while(choice == -1){
            System.out.print("What would you like to do? ");
            choice = Integer.parseInt(sc.nextLine());
            System.out.println("\n");
            if(choice == 1) healthAchievements();
            else if (choice == 2) healthGoals();
            else if (choice == 3) viewFitnessRoutines();
            else if (choice == 0) menu();
            else choice = -1;
        }
        menu();
    }

    private void healthGoals(){
        int choice = -1;
        System.out.println("--- HEALTH GOALS ---");

        viewHealthGoals();

        while(choice == -1){
            System.out.println("\nWould you like to");
            System.out.println("1. Edit a goal");
            System.out.println("2. Add a goal");
            System.out.println("3. Delete a goal");
            System.out.println("0. Go back to dashboard");
            System.out.print("Choice: ");

            choice = Integer.parseInt(sc.nextLine());
            System.out.println("\n");
            if(choice == 1) editGoal();
            else if (choice == 2) addGoal();
            else if (choice == 3) deleteGoal();
            else if (choice == 0) dashboard();
            else choice = -1;
        }
        dashboard();
    }
    private void healthAchievements(){
        int choice = -1;
        System.out.println("--- HEALTH ACHIEVEMENTS ---");

        viewHealthAchievements();

        while(choice == -1){
            System.out.println("\nWould you like to");
            System.out.println("1. Edit an achievement");
            System.out.println("2. Add an achievement");
            System.out.println("3. Delete an achievement");
            System.out.println("0. Go back to dashboard");
            System.out.print("Choice: ");

            choice = Integer.parseInt(sc.nextLine());
            System.out.println("\n");
            if(choice == 1) editAchievement();
            else if (choice == 2) addAchievement();
            else if (choice == 3) deleteAchievement();
            else if (choice == 0) dashboard();
            else choice = -1;
        }
        dashboard();
    }

    private void editGoal(){
        int choice = -1;
        while(choice == -1){
            System.out.println("Which goal would you like to edit?");
            viewHealthGoals();
            System.out.print("\nChoice: ");
            choice = Integer.parseInt(sc.nextLine());
            if(choice > 0 && choice <= health_goals.size()){
                health_goals.get(choice - 1).edit();
                viewHealthGoals();
            }
            else choice = -1;
        }
    }
    private void editAchievement(){
        int choice = -1;
        while(choice == -1){
            System.out.println("Which achievement would you like to edit?");
            viewHealthAchievements();
            System.out.print("\nChoice: ");
            choice = Integer.parseInt(sc.nextLine());
            if(choice > 0 && choice <= health_achievements.size()){
                health_achievements.get(choice - 1).edit();
                viewHealthAchievements();
            }
            else choice = -1;
        }
    }

    private void addGoal(){
        String title;
        System.out.println("ADD GOAL");
        System.out.print("Title: ");
        title = sc.nextLine();
        System.out.println();

        try{
            // create query to insert a new row to the table
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO health_goals " +
                    "(member_id, title, creation_date) VALUES(?, ?, CURRENT_DATE)");

            // populate query with the provided student information
            pstmt.setInt(1, member_id);
            pstmt.setString(2, title);

            // execute the query to add new student and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Added Health Goal " + title);
                viewHealthGoals();
            }
            else{
                System.out.println("Unable to add Health Goal. Please try again");
                healthGoals();
            }
        }
        // catch any exceptions
        catch(Exception e){
            System.out.println("Unable to add Health Goal. Please try again");
            healthGoals();
        }
    }

    private void addAchievement(){
        String title, description, date;
        System.out.println("ADD ACHIEVEMENT");
        System.out.print("Title: ");
        title = sc.nextLine();
        System.out.print("\nDescription: ");
        description = sc.nextLine();
        System.out.print("\nDate (yyyy-mm-dd): ");
        date = sc.nextLine();
        System.out.println();

        try{
            // create query to insert a new row to the table
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO health_achievements " +
                    "(member_id, title, date, description) VALUES(?, ?, ?, ?)");

            // populate query with the provided student information
            pstmt.setInt(1, member_id);
            pstmt.setString(2, title);
            // parsing enrollment_date to be of the correct Date type before sending the query
            Date d = Date.valueOf(date);
            pstmt.setDate(3, d);
            pstmt.setString(4, description);

            // execute the query to add new student and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Added Health Achievement " + title);
                viewHealthAchievements();
            }
            else{
                System.out.println("Unable to add Health Achievement. Please try again");
                healthAchievements();
            }
        }
        // catch any exceptions
        catch(Exception e){
            System.out.println("Unable to add Health Achievement. Please try again");
            healthAchievements();

        }
    }

    private void deleteGoal(){
        int choice = -1;
        if(health_goals.size() > 0){
            while(choice == -1){
                System.out.println("Which goal would you like to delete?");
                viewHealthGoals();
                System.out.print("\nChoice: ");
                choice = Integer.parseInt(sc.nextLine());
                if(choice > 0 && choice <= health_goals.size()){
                    health_goals.get(choice - 1).delete();
                    viewHealthGoals();
                }
                else choice = -1;
            }
        }
        else{
            System.out.println("No goals to delete");
        }
    }

    private void deleteAchievement(){
        int choice = -1;
        if(health_achievements.size() > 0){
            while(choice == -1){
                System.out.println("Which achievement would you like to delete?");
                viewHealthAchievements();
                System.out.print("\nChoice: ");
                choice = Integer.parseInt(sc.nextLine());
                if(choice > 0 && choice <= health_achievements.size()){
                    health_achievements.get(choice - 1).delete();
                    viewHealthAchievements();
                }
                else choice = -1;
            }
        }
        else{
            System.out.println("No achievements to delete");
        }
    }

    private void viewHealthAchievements(){
        int count = 1;
        getAllHealthAchievements();
        for(HealthAchievement h: health_achievements){
            System.out.println(count + ". " + h.toString());
            count++;
        }
    }

    private void viewHealthGoals(){
        int count = 1;
        getAllHealthGoals();
        for(HealthGoal g: health_goals){
            System.out.println(count + ". " + g.toString());
            count++;
        }
    }

    private void viewFitnessRoutines(){
        int count = 1;
        getAllFitnessRountines();
        System.out.println("--- FITNESS ROUTINES ---");
        for(FitnessRoutine r: fitness_routines){
            System.out.println(count + ". " + r.toString());
            count++;
        }
    }

    private void getAllHealthGoals(){
        String query = "SELECT * FROM Health_Goals WHERE member_id = ?";
        health_goals = new ArrayList<HealthGoal>();
        try {
            PreparedStatement stmt = connection.prepareStatement(query);

            stmt.setInt(1, member_id);

            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                HealthGoal g = new HealthGoal(connection,
                        resultSet.getInt("goal_id"),
                        resultSet.getInt("member_id"),
                        resultSet.getString("title"),
                        resultSet.getDate("creation_date"));
                health_goals.add(g);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getAllHealthAchievements(){
        String query = "SELECT * FROM Health_Achievements WHERE member_id = ?";
        health_achievements = new ArrayList<HealthAchievement>();
        try {
            PreparedStatement stmt = connection.prepareStatement(query);

            stmt.setInt(1, member_id);

            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                HealthAchievement a = new HealthAchievement(connection,
                        resultSet.getInt("achievement_id"),
                        resultSet.getInt("member_id"),
                        resultSet.getString("title"),
                        resultSet.getDate("date"),
                        resultSet.getString("description"));
                health_achievements.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getAllFitnessRountines(){
        String query = "SELECT * FROM Fitness_Routines WHERE member_id = ?";
        fitness_routines = new ArrayList<FitnessRoutine>();
        try {
            PreparedStatement stmt = connection.prepareStatement(query);

            stmt.setInt(1, member_id);

            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                FitnessRoutine r = new FitnessRoutine(connection,
                        resultSet.getInt("routine_id"),
                        resultSet.getInt("member_id"),
                        resultSet.getString("title"),
                        resultSet.getString("instructions"),
                        resultSet.getString("description"));
                fitness_routines.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setFName(){
        String newFName;
        System.out.print("New First Name: ");
        newFName = sc.nextLine();
        try{
            // create query to update the entry who has member id = member_id with first_name = newFName
            PreparedStatement pstmt = connection.prepareStatement("UPDATE member SET first_name = ? WHERE member_id = ?");

            // populate query with the provided member_id and newFName
            pstmt.setString(1, newFName);
            pstmt.setInt(2, member_id);

            // execute the query to update entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Update first name: " + newFName);
            }
            else{
                System.out.println("Unable to update first name. Please try again");
            }
        }
        // catch any exceptions
        catch(Exception e){
            System.out.println("Unable to update first name. Please try again");
        }
    }

    private void setLName(){
        String newLName;
        System.out.print("New Last Name: ");
        newLName = sc.nextLine();
        try{
            // create query to update the entry who has member id = member_id with last_name = newLName
            PreparedStatement pstmt = connection.prepareStatement("UPDATE member SET last_name = ? WHERE member_id = ?");

            // populate query with the provided member_id and newLName
            pstmt.setString(1, newLName);
            pstmt.setInt(2, member_id);

            // execute the query to update entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Update last name: " + newLName);
            }
            else{
                System.out.println("Unable to update last name. Please try again");
            }
        }
        // catch any exceptions
        catch(Exception e){
            System.out.println("Unable to update last name. Please try again");
        }
    }

    private void setPassword(){
        String pswd;
        System.out.print("New Password: ");
        pswd = sc.nextLine();
        try{
            // create query to update the entry who has member id = member_id with password = pswd
            PreparedStatement pstmt = connection.prepareStatement("UPDATE member SET password = ? WHERE member_id = ?");

            // populate query with the provided member_id and pswd
            pstmt.setString(1, pswd);
            pstmt.setInt(2, member_id);

            // execute the query to update entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Update password: " + pswd);
            }
            else{
                System.out.println("Unable to update password. Please try again");
            }
        }
        // catch any exceptions
        catch(Exception e){
            System.out.println("Unable to update password. Please try again");
        }
    }

    private void setEmail(){
        String e;
        System.out.print("New Email: ");
        e = sc.nextLine();
        try{
            // create query to update the entry who has member id = member_id with email = e
            PreparedStatement pstmt = connection.prepareStatement("UPDATE member SET email = ? WHERE member_id = ?");

            // populate query with the provided member_id and e
            pstmt.setString(1, e);
            pstmt.setInt(2, member_id);

            // execute the query to update entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Update email: " + e);
            }
            else{
                System.out.println("Unable to update email. Please try again");
            }
        }
        // catch any exceptions
        catch(Exception ex){
            System.out.println("Unable to update email. Please try again");
        }
    }

    private void setHeartRate(){
        String newHR;
        System.out.print("Updated Heart Rate (beats/min): ");
        newHR = sc.nextLine();
        try{
            // create query to update the entry who has member id = member_id with heart_rate = newHR
            PreparedStatement pstmt = connection.prepareStatement("UPDATE member SET heart_rate = ? WHERE member_id = ?");

            // populate query with the provided member_id and heart_rate
            pstmt.setInt(1, Integer.parseInt(newHR));
            pstmt.setInt(2, member_id);

            // execute the query to update entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Update heart rate: " + Integer.parseInt(newHR));
            }
            else{
                System.out.println("Unable to update heart rate. Please try again");
            }
        }
        // catch any exceptions
        catch(Exception e){
            System.out.println("Unable to update heart rate. Please input a valid integer heart rate.");
        }
    }

    private void setHeight(){
        String h;
        System.out.print("New Height: ");
        h = sc.nextLine();
        try{
            // create query to update the entry who has member id = member_id with height = h
            PreparedStatement pstmt = connection.prepareStatement("UPDATE member SET height = ? WHERE member_id = ?");

            // populate query with the provided member_id and h
            pstmt.setBigDecimal(1, BigDecimal.valueOf(Double.parseDouble(h)));
            pstmt.setInt(2, member_id);

            // execute the query to update entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Update height: " + Double.parseDouble(h));
            }
            else{
                System.out.println("Unable to update height. Please try again");
            }
        }
        // catch any exceptions
        catch(Exception e){
            System.out.println("Unable to update height. Please input a valid height.");
        }
    }

    private void setWeight(){
        String w;
        System.out.print("New Weight: ");
        w = sc.nextLine();
        try{
            // create query to update the entry who has member id = member_id with weight = w
            PreparedStatement pstmt = connection.prepareStatement("UPDATE member SET weight = ? WHERE member_id = ?");

            // populate query with the provided member_id and w
            pstmt.setBigDecimal(1, BigDecimal.valueOf(Double.parseDouble(w)));
            pstmt.setInt(2, member_id);

            // execute the query to update entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Update weight: " + Double.parseDouble(w));
            }
            else{
                System.out.println("Unable to update weight. Please try again");
            }
        }
        // catch any exceptions
        catch(Exception e){
            System.out.println("Unable to update weight. Please input a valid height.");
        }
    }

    private void updateCurrInfo(){
        try{
            // create query get all the entries in the students table
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM member WHERE member_id = " + member_id);
            // execute the query
            ResultSet resultSet = pstmt.executeQuery();

            if(resultSet.next()){
                first_name = resultSet.getString("first_name");
                last_name = resultSet.getString("last_name");
                email = resultSet.getString("email");
                password = resultSet.getString("password");
                height = resultSet.getDouble("height");
                weight = resultSet.getDouble("weight");
                heart_rate = resultSet.getInt("heart_rate");
            }
        }
        // catch any exceptions
        catch(Exception e){
            e.printStackTrace();
        }
    }
    private void editFitnessRoutine(){
        int choice = -1;
        while(choice == -1){
            System.out.println("Which fitness routine would you like to edit?");
            viewFitnessRoutines();
            System.out.print("\nChoice: ");
            choice = Integer.parseInt(sc.nextLine());
            if(choice > 0 && choice <= fitness_routines.size()){
                fitness_routines.get(choice - 1).edit();
                viewFitnessRoutines();
            }
            else choice = -1;
        }
    }

    private void addFitnessRoutine(){
        String title, description, instructions;
        System.out.println("ADD FITNESS ROUTINE");
        System.out.print("Title: ");
        title = sc.nextLine();
        System.out.print("\nDescription: ");
        description = sc.nextLine();
        System.out.print("\nInstructions: ");
        instructions = sc.nextLine();
        System.out.println();

        try{
            // create query to insert a new row to the table
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO fitness_routines " +
                    "(member_id, title, instructions, description) VALUES(?, ?, ?, ?)");

            // populate query with the provided student information
            pstmt.setInt(1, member_id);
            pstmt.setString(2, title);
            pstmt.setString(3, instructions);
            pstmt.setString(4, description);

            // execute the query to add new student and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Added Fitness Routine " + title);
                viewFitnessRoutines();
            }
            else{
                System.out.println("Unable to add Fitness Routine. Please try again");
                trainerFitnessRoutines();
            }
        }
        // catch any exceptions
        catch(Exception e){
            System.out.println("Unable to add Fitness Routine. Please try again");
            trainerFitnessRoutines();
        }
    }

    private void deleteFitnessRoutine(){
        int choice = -1;
        if(fitness_routines.size() > 0){
            while(choice == -1){
                System.out.println("Which fitness routine would you like to delete?");
                viewFitnessRoutines();
                System.out.print("\nChoice: ");
                choice = Integer.parseInt(sc.nextLine());
                if(choice > 0 && choice <= fitness_routines.size()){
                    fitness_routines.get(choice - 1).delete();
                    viewFitnessRoutines();
                }
                else choice = -1;
            }
        }
        else{
            System.out.println("No fitness routines to delete");
        }
    }
    private void trainerFitnessRoutines(){
        int choice = -1;
        viewFitnessRoutines();

        while(choice == -1){
            System.out.println("\nWould you like to");
            System.out.println("1. Edit a fitness routine");
            System.out.println("2. Add a fitness routine");
            System.out.println("3. Delete a fitness routine");
            System.out.println("0. Go back");
            System.out.print("Choice: ");

            choice = Integer.parseInt(sc.nextLine());
            System.out.println("\n");
            if(choice == 1) editFitnessRoutine();
            else if (choice == 2) addFitnessRoutine();
            else if (choice == 3) deleteFitnessRoutine();
            else if (choice == 0) trainerManageProfile();
            else choice = -1;
        }
    }

    public void trainerManageProfile(){
        int choice = -1;

        System.out.println("--- PROFILE ---");
        System.out.println("First Name: " + first_name);
        System.out.println("Last Name: " + last_name);
        System.out.println("Email: " + email);
        System.out.println("1. Heart Rate: " + heart_rate);
        System.out.println("2. Height: " + height);
        System.out.println("3. Weight: " + weight);
        System.out.println("4. View Achievements: " + health_achievements.size());
        System.out.println("5. View Goals: " + health_goals.size());
        System.out.println("6. View Fitness Routine: " + fitness_routines.size());
        System.out.println("\n0. Back to Member List \n");

        while(choice == -1){
            System.out.print("What would you like to change? ");
            choice = Integer.parseInt(sc.nextLine());
            System.out.println("\n");
            if(choice == 1) setHeartRate();
            else if (choice == 2) setHeight();
            else if (choice == 3) setWeight();
            else if (choice == 4) trainerHealthAchievements();
            else if (choice == 5) trainerHealthGoals();
            else if (choice == 6) trainerFitnessRoutines();
            else if (choice == 0) return;
            else choice = -1;
        }
        if(choice > 0 && choice < 7){
            updateCurrInfo();
            trainerManageProfile();
        }
    }

    private void trainerHealthGoals(){
        int choice = -1;
        System.out.println("--- HEALTH GOALS ---");

        viewHealthGoals();

        while(choice == -1){
            System.out.println("\nWould you like to");
            System.out.println("1. Edit a goal");
            System.out.println("2. Add a goal");
            System.out.println("3. Delete a goal");
            System.out.println("0. Go back");
            System.out.print("Choice: ");

            choice = Integer.parseInt(sc.nextLine());
            System.out.println("\n");
            if(choice == 1) editGoal();
            else if (choice == 2) addGoal();
            else if (choice == 3) deleteGoal();
            else if (choice == 0) trainerManageProfile();
            else choice = -1;
        }
    }
    private void trainerHealthAchievements(){
        int choice = -1;
        System.out.println("--- HEALTH ACHIEVEMENTS ---");

        viewHealthAchievements();

        while(choice == -1){
            System.out.println("\nWould you like to");
            System.out.println("1. Edit an achievement");
            System.out.println("2. Add an achievement");
            System.out.println("3. Delete an achievement");
            System.out.println("0. Go back");
            System.out.print("Choice: ");

            choice = Integer.parseInt(sc.nextLine());
            System.out.println("\n");
            if(choice == 1) editAchievement();
            else if (choice == 2) addAchievement();
            else if (choice == 3) deleteAchievement();
            else if (choice == 0) trainerManageProfile();
            else choice = -1;
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

    private Set<Integer> getAvailableRooms(Date date, Time startTime, Time endTime){
        Set<Integer> roomID = new HashSet<Integer>();

        String query = "SELECT room_id , room_number FROM room " +
                "EXCEPT(" +
                "SELECT room.room_id, room.room_number " +
                "FROM session JOIN room ON session.room_id = room.room_id " +
                "WHERE( session.room_id = room.room_id " +
                "AND session.date = ? " +
                "AND ((session.start_time <= ? AND session.end_time >= ?) OR " +
                "(session.start_time >= ? AND session.start_time < ?) OR " +
                "(session.end_time < ? AND session.end_time <= ?))));";
        try{
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setDate(1, date);
            pstmt.setTime(2, startTime);
            pstmt.setTime(3, endTime);
            pstmt.setTime(4, startTime);
            pstmt.setTime(5, endTime);
            pstmt.setTime(6, startTime);
            pstmt.setTime(7, endTime);

            ResultSet resultSet = pstmt.executeQuery();

            System.out.println("\nRoom ID \t\t Room Number");

            while (resultSet.next()) {
                roomID.add(resultSet.getInt("room_id"));
                System.out.println(resultSet.getInt("room_id") + "\t\t\t\t " +
                        resultSet.getString("room_number"));
            }
            return roomID;
        }
        catch(Exception e){
            System.out.println("Could not find any rooms.");
            return roomID;
        }
    }

    private Set<Integer> getAvailableTrainers(LocalDate date, LocalTime startTime, LocalTime endTime){
        Set<Integer> trainerID = new HashSet<Integer>();
        String query = "SELECT trainer.trainer_id, trainer_schedule.start_time, trainer_schedule.end_time, " +
                "trainer.first_name, trainer.last_name " +
                "FROM trainer_schedule JOIN trainer ON trainer.trainer_id = trainer_schedule.trainer_id " +
                "WHERE date = ? AND start_time <= ? AND end_time >= ? " +
                "EXCEPT SELECT ts.trainer_id, ts.start_time, ts.end_time, t.first_name, t.last_name " +
                "FROM trainer_schedule ts JOIN session s ON ts.trainer_id = s.trainer_id JOIN trainer t ON ts.trainer_id = t.trainer_id " +
                "WHERE s.date = ? AND (" +
                "(s.start_time <= ? AND s.end_time > ?) OR " +
                "(s.start_time < ? AND s.end_time >= ?));";
        try{
            PreparedStatement pstmt = connection.prepareStatement(query);

            pstmt.setDate(1, Date.valueOf(date));
            pstmt.setTime(2, Time.valueOf(startTime));
            pstmt.setTime(3, Time.valueOf(endTime));
            pstmt.setDate(4, Date.valueOf(date));
            pstmt.setTime(5, Time.valueOf(startTime));
            pstmt.setTime(6, Time.valueOf(startTime));
            pstmt.setTime(7, Time.valueOf(endTime));
            pstmt.setTime(8, Time.valueOf(endTime));

            ResultSet resultSet = pstmt.executeQuery();

            System.out.println("\nTrainer ID \t\t\t First Name \t\t\t Last Name \t\t\t Time at work");

            while (resultSet.next()) {
                trainerID.add(resultSet.getInt("trainer_id"));
                System.out.println(resultSet.getInt("trainer_id") + "\t\t\t\t\t " +
                        resultSet.getString("first_name") + "\t\t\t\t\t " +
                        resultSet.getString("last_name") + "\t\t\t\t " +
                        resultSet.getTime("start_time") + " to " +
                        resultSet.getTime("end_time"));
            }
            return trainerID;
        }
        catch(Exception e){
            System.out.println("Could not get trainers.");
            return trainerID;
        }
    }
}
