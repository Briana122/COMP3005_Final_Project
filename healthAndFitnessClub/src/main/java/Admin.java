import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import static java.lang.System.exit;

public class Admin {
    private Connection connection;
    private int admin_id;
    private String  first_name,
            last_name,
            email,
            password;

    private Scanner sc = new Scanner(System.in);
    public Admin(Connection conn, int id, String fname, String lname, String e, String p){
        connection = conn;
        admin_id = id;
        first_name = fname;
        last_name = lname;
        email = e;
        password = p;
    }

    public void menu(){
        int choice = -1;
        System.out.println("\n\n --- HEALTH AND FITNESS CLUB--- \nWELCOME ADMIN " + first_name.toUpperCase() +
                " " + last_name.toUpperCase());
        while(choice == -1){
            System.out.println("1. Add Session");
            System.out.println("2. Edit Session");
            System.out.println("3. Manage Equipment");
            System.out.println("4. View Bills");
            System.out.println("5. Log Out");
            System.out.print("Choice: ");
            choice = Integer.parseInt(sc.nextLine());
            System.out.println("\n");
            if(choice == 1) {
                createNewSession();
                break;
            }
            else if (choice == 2) {
                editSession();
                break;
            }
            else if (choice == 3) {
                manageEquipment();
                break;
            }
            else if (choice == 4) {
                manageBills();
                break;
            }
            else if (choice == 5) exit(-1);
            else {
                choice = -1;
            }
        }
    }

    // manage bills
    private void manageBills(){
        Set<Integer> billID;
        int choice;

        while(true) {
            billID = displayBills();
            System.out.print("View bill (-1 to return to menu): ");
            choice = Integer.parseInt(sc.nextLine());
            if(choice == -1){
                menu();
                return;
            }
            if(billID.contains(choice)){
                payBill(choice);
                break;
            }
        }
    }

    // function to pay bill
    private void payBill(int billID){
        int choice;
        while(true) {
            System.out.println("1. Pay");
            System.out.println("0. Go back");
            choice = Integer.parseInt(sc.nextLine());
            if(choice == 0){
                menu();
                return;
            }
            else if (choice == 1){
                try{
                    PreparedStatement pstmt = connection.prepareStatement("UPDATE bills SET admin_id = ? WHERE bill_id = ?");

                    pstmt.setInt(1, admin_id);
                    pstmt.setInt(2, billID);

                    if(pstmt.executeUpdate() > 0){
                        System.out.println("Bill Paid");
                    }
                    else{
                        System.out.println("Unable to pay bill. Please try again");
                        menu();
                        return;
                    }
                }
                // catch any exceptions
                catch(Exception e){
                    e.printStackTrace();
                }
                menu();
                break;
            }
        }
    }

    // display all bills that need to be paid
    private Set<Integer> displayBills(){
        Set<Integer> billID = new HashSet<Integer>();
        try{
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM bills WHERE admin_id IS NULL");
            ResultSet resultSet = pstmt.executeQuery();

            System.out.println("Bill ID \t\t Amount \t\t Date");
            while(resultSet.next()){
                billID.add(resultSet.getInt("bill_id"));
                System.out.println(resultSet.getInt("bill_id") + "\t\t\t\t " +
                        resultSet.getString("amount") + "\t\t\t " +
                        resultSet.getString("date"));
            }
        }
        // catch any exceptions
        catch(Exception e){
            e.printStackTrace();
        }
        return billID;
    }

    // function to manage equipment
    private void manageEquipment(){
        int choice = -1;
        Set<Integer> equipID = new HashSet<Integer>();

        System.out.println("--- EQUIPMENT MANAGEMENT ---");

        try{
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM equipment");
            // execute the query
            ResultSet resultSet = pstmt.executeQuery();

            // print out the data returned by the query
            System.out.println("Equipment ID \t Last Maintenance Date \t\t Next Maintenance Date \t Name");
            while(resultSet.next()){
                equipID.add(resultSet.getInt("equip_id"));
                System.out.println(resultSet.getString("equip_id") + "\t\t\t\t " +
                        resultSet.getString("last_maintenance_date") + "\t\t\t\t\t " +
                        resultSet.getString("next_maintenance_date") + "\t\t\t\t " +
                        resultSet.getString("name") + "\t\t ");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        // what to do with the equipment
        while(choice == -1){
            System.out.println("\n0. Go back");
            System.out.println("1. Do maintence on equipment");
            System.out.print("Choice: ");
            choice = Integer.parseInt(sc.nextLine());
            if(choice == 0) {
                menu();
                break;
            }
            else if (choice == 1) {
                maintenanceOnEquip(equipID);
                break;
            }
            else {
                choice = -1;
            }
        }
    }

    // carry maintenance on equipment
    private void maintenanceOnEquip(Set<Integer> equipID){
        int choice = -1, id;
        String nextDate;

        while(true){
            System.out.print("Equipment ID: ");
            id = Integer.parseInt(sc.nextLine());
            if(equipID.contains(id)) break;
        }

        System.out.print("Next Maintenance Date (YYYY/MM/DD): ");
        nextDate = sc.nextLine();

        try{
            PreparedStatement pstmt = connection.prepareStatement("UPDATE equipment SET last_maintenance_date = CURRENT_DATE, next_maintenance_date = ? WHERE equip_id = ?");

            pstmt.setDate(1, Date.valueOf(nextDate));
            pstmt.setInt(2, id);

            // execute the query to update entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Maintenance complete. Update done." );
            }
            else{
                System.out.println("Unable to do maintenance. Please try again");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        menu();
    }

    // edit info for existing session
    private void editSession(){

        int sessionChosen, choice = -1;

        Set<Integer> sessionID = getAllSessions();
        if(sessionID.size() == 0) {
            System.out.println("No sessions currently.");
            menu();
            return;
        }
        while(true) {
            System.out.print("\nPlease select session ID you want to modify: ");
            sessionChosen = Integer.parseInt(sc.nextLine());
            if(sessionID.contains(sessionChosen)) break;
        }

        while(choice == -1) {
            System.out.println("1. Date/Time/Trainer");
            System.out.println("2. Room");
            System.out.println("3. Delete");
            System.out.print("What would you like to change?");

            choice = Integer.parseInt(sc.nextLine());
            if(choice == 1) {
                changeTimeOrTrainer(sessionChosen);
                break;
            }
            if(choice == 2) {
                changeRoom(sessionChosen);
                break;
            }
            if(choice == 3) {
                deleteSession(sessionChosen);
                break;
            }
            else {
                choice = -1;
            }
        }
        menu();
    }

    // delete existing session
    private void deleteSession(int sessionID){
        try{
            // create query to delete session entry
            PreparedStatement pstmt = connection.prepareStatement("DELETE FROM session WHERE session_id = ?");
            pstmt.setInt(1, sessionID);

            // execute the query to delete entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Successful deletion");
            }
            else{
                System.out.println("Unable to delete session. Please try again");
            }
        }
        // catch any exceptions
        catch(Exception e){
            e.printStackTrace();
        }
    }

    // change room for existing session
    private void changeRoom(int sessionID){
        int roomChosen;
        Date date;
        Time startTime, endTime;
        String changeRoomQuery;
        String getSessionQuery = "Select * FROM session WHERE session_id = " + sessionID;

        try{
            PreparedStatement pstmt = connection.prepareStatement(getSessionQuery);

            ResultSet resultSet = pstmt.executeQuery();

            if(resultSet.next()){
                date = resultSet.getDate("date");
                startTime = resultSet.getTime("start_time");
                endTime = resultSet.getTime("end_time");

                Set<Integer> roomID = getAvailableRooms(date, startTime, endTime);

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

                changeRoomQuery = "UPDATE session SET room_id = ? WHERE session_id = ?";
                PreparedStatement statement = connection.prepareStatement(changeRoomQuery);
                statement.setInt(1, roomChosen);
                statement.setInt(2, sessionID);

                if(statement.executeUpdate() > 0){
                    System.out.println("Update Session Room Successfully");
                }
                else{
                    System.out.println("Unable to update room. Please try again");
                }
            }
            else{
                System.out.println("No rooms available. Please try again");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    // change time/trainer for existing session
    private void changeTimeOrTrainer(int sessionID){
        LocalDate newDate;
        LocalTime newStartTime, newEndTime;
        Time duration;
        String query;
        int trainerChosen;

        try {
            // new date/time
            System.out.print("Date (YYYY-MM-DD): ");
            newDate = LocalDate.parse(sc.nextLine());

            System.out.print("Start time (HH:MM): ");
            newStartTime = LocalTime.parse(sc.nextLine());

            System.out.print("End time (HH:MM): ");
            newEndTime = LocalTime.parse(sc.nextLine());

            duration = calculateDuration(newStartTime, newEndTime);

            // find available trainers
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

            query = "UPDATE session SET date = ?, start_time = ?, end_time = ?, duration = ?, trainer_id = ? WHERE session_id = ?";

            PreparedStatement pstmt = connection.prepareStatement(query);

            pstmt.setDate(1, Date.valueOf(newDate));
            pstmt.setTime(2, Time.valueOf(newStartTime));
            pstmt.setTime(3, Time.valueOf(newEndTime));
            pstmt.setTime(4, duration);
            pstmt.setInt(5, trainerChosen);
            pstmt.setInt(6, sessionID);


            if(pstmt.executeUpdate() > 0){
                System.out.println("Update session successful");
                return;
            }
            else{
                System.out.println("Unable to update session. Please try again");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    // get all existing sessions
    private Set<Integer> getAllSessions(){
        Set<Integer> sessionID = new HashSet<Integer>();
        try{
            String query = "SELECT * FROM session ORDER BY session_id ASC";
            PreparedStatement pstmt = connection.prepareStatement(query);

            ResultSet resultSet = pstmt.executeQuery();

            // print out the data returned by the query
            System.out.println("Session ID \t\t Date \t\t\t Start Time \t\t End Time \t\t Duration \t\t Trainer");
            while(resultSet.next()){
                sessionID.add(resultSet.getInt("session_id"));
                System.out.println(resultSet.getInt("session_id") + "\t\t\t\t " +
                        resultSet.getString("date") + "\t\t " +
                        resultSet.getString("start_time") + "\t\t\t " +
                        resultSet.getString("end_time") + "\t\t " +
                        resultSet.getString("duration") + "\t\t " +
                        resultSet.getString("trainer_id"));
            }
        }
        catch(Exception e){
            System.out.println("Error getting session");
        }
        return sessionID;
    }

    // create new session
    private void createNewSession(){
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

            System.out.print("Cost: ");
            cost = Double.parseDouble(sc.nextLine());

            while(true) {
                System.out.print("Type of session (PRIVATE - P, GROUP - G): ");
                type = sc.nextLine();
                if (type.toUpperCase().equals("P")) {
                    type = "PRIVATE";
                    break;
                }
                else if (type.toUpperCase().equals("G")) {
                    type = "GROUP";
                    break;
                }
            }

            duration = calculateDuration(newStartTime, newEndTime);
        }
        catch(Exception e){
            System.out.println("Something went wrong. Please try again.");
            return;
        }

        // get available trainers
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
        // get available rooms
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
        // create the new session (insert into db)
        try{
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO session (date, cost, start_time, " +
                    "end_time, duration, type, title, description, trainer_id, room_id) VALUES(?, ?, ?, ?, " +
                    "?, ?, ?, ?, ?, ?)");

            pstmt.setDate(1, Date.valueOf(newDate));
            pstmt.setDouble(2, cost);
            pstmt.setTime(3, Time.valueOf(newStartTime));
            pstmt.setTime(4, Time.valueOf(newEndTime));
            pstmt.setTime(5, duration);
            pstmt.setString(6, type);
            pstmt.setString(7, title);
            pstmt.setString(8, description);
            pstmt.setInt(9, trainerChosen);
            pstmt.setInt(10, roomChosen);

            // execute the query to update entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Added session successfully.");
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

    // get available rooms during specified time
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

    // get available trainers during specified time
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

    // calculate duration between start and end times
    private static Time calculateDuration(LocalTime newStartTime, LocalTime newEndTime) {
        // Calculate duration between start time and end time
        Duration duration = Duration.between(newStartTime, newEndTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        Time durationTime = Time.valueOf(String.format("%02d:%02d:00", hours, minutes));
        return durationTime;
    }
}
