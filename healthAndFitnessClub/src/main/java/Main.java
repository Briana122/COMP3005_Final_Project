import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        // information to establish connection with postgreSQL database
        String url = "jdbc:postgresql://localhost:5432/FinalProject";
        String user = "postgres";
        String password = "password";

        // try to establish a connection to the database with the information above
        try{
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, user, password);
            // connection established
            if(connection != null){
                System.out.println("Connected to the database\n\n");

                startClub(connection);
                connection.close();
            }
            // connection failed to establish
            else{
                System.out.println("Failed to connect to the database");
            }
            // catch any exceptions that occur during the execution
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void startClub(Connection connection){
        System.out.println("--- HEALTH AND FITNESS CLUB ---");

        start(connection);

    }

    public static void start(Connection connection){
        int loginSuccess = -1;
        Scanner sc = new Scanner(System.in);
        String selection = "0";
        while(true){
            System.out.println("1. Sign up");
            System.out.println("2. Sign in");
            System.out.println("3. Exit\n");
            System.out.print("Selection: ");

            selection = sc.nextLine();

            if(selection.equals("1")){
                signUp(connection);
                break;
            }
            else if (selection.equals("2")){
                loginSuccess = logIn(connection);
                while(loginSuccess == -1){
                    System.out.println("Invalid. Please try again.");
                    loginSuccess = logIn(connection);
                }
                break;
            }
            else if (selection.equals("3")){
                System.out.println("Goodbye :)");
                break;
            }
            else{
                System.out.println("Invalid choice, please pick again.");
            }
        }
    }

    public static void signUp(Connection connection){
        String fname, lname, password, email, sex, DoB = "00-00-0000";
        double height, weight;
        int heart_rate;
        Scanner sc = new Scanner(System.in);
        clearScreen();

        System.out.println("\n\n--- SIGN UP ---");
        System.out.println("Please enter the following information\n");
        System.out.print("First Name: ");
        fname = sc.nextLine();
        System.out.print("Last Name: ");
        lname = sc.nextLine();

        while(true){
            System.out.print("Email: ");
            email = sc.nextLine();
            if(!emailExists(connection, email)) break;
            else System.out.println("Email taken, please use another email");
        }

        while(true){
            System.out.print("\nSex - Female (F), Male (M), Other (O): ");
            sex = sc.nextLine().toUpperCase();
            if(sex.equals("M") || sex.equals("F") || sex.equals("O")) break;
        }

        while(true){
            System.out.print("Date of Birth (YYYY-MM-DD): ");
            DoB = sc.nextLine();
            if (isValidDate(DoB)) break;
        }

        System.out.print("Height (cm): ");
        height = Double.parseDouble(sc.nextLine());
        System.out.print("Weight (lbs): ");
        weight = Double.parseDouble(sc.nextLine());
        System.out.print("Heart Rate (beats/min): ");
        heart_rate = Integer.parseInt(sc.nextLine());

        System.out.print("\n Please set a password: ");
        password = sc.nextLine();

        addMember(connection, fname, lname, email, password, sex, DoB, height, weight, heart_rate);
    }

    public static int logIn(Connection connection){
        String password, email;
        Scanner sc = new Scanner(System.in);

        System.out.println("\n\n--- SIGN IN ---");

        System.out.print("Email: ");
        email = sc.nextLine();
        System.out.print("Password: ");
        password = sc.nextLine();

        if(authMember(connection, email, password) || authTrainer(connection, email, password) || authAdmin(connection, email, password)){
            return 1;
        }
        return -1;
    }

    public static boolean authMember(Connection connection, String email, String password){
        String memberQuery = "SELECT * FROM member WHERE email = ? AND password = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(memberQuery);

            stmt.setString(1, email);
            stmt.setString(2, password); // In a real-world scenario, use a hashed password here

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
                m.menu();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean authTrainer(Connection connection, String email, String password){
        String trainerQuery = "SELECT * FROM trainer WHERE email = ? AND password = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(trainerQuery);

            stmt.setString(1, email);
            stmt.setString(2, password); // In a real-world scenario, use a hashed password here

            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                Trainer t = new Trainer(connection,
                        resultSet.getInt("trainer_id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("email"),
                        resultSet.getString("password"),
                        resultSet.getString("sex"));
                t.menu();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean authAdmin(Connection connection, String email, String password){
        String adminQuery = "SELECT * FROM admin WHERE email = ? AND password = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(adminQuery);

            stmt.setString(1, email);
            stmt.setString(2, password); // In a real-world scenario, use a hashed password here

            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                Admin a = new Admin(connection,
                        resultSet.getInt("admin_id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("email"),
                        resultSet.getString("password"));
                a.menu();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void addMember(Connection connection, String first_name, String last_name, String email,
                                 String password, String sex, String DoB, double height, double weight, int heart_rate){
        try{
            // create query to insert a new row to the table
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO Member (first_name, last_name, email," +
                    " date_of_birth, heart_rate, height, weight, password, sex) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");

            // populate query with the provided member information
            pstmt.setString(1, first_name);
            pstmt.setString(2, last_name);
            pstmt.setString(3, email);
            // parsing date_of_birth to be of the correct Date type before sending the query
            Date date_of_birth = Date.valueOf(DoB);
            pstmt.setDate(4, date_of_birth);
            pstmt.setInt(5, heart_rate);
            pstmt.setBigDecimal(6, BigDecimal.valueOf(height));
            pstmt.setBigDecimal(7, BigDecimal.valueOf(weight));
            pstmt.setString(8, password);
            pstmt.setString(9, sex);

            // execute the query to add new member and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Welcome on board " + first_name + " " + last_name);
                authMember(connection, email, password);
            }
            else{
                System.out.println("Unable to add Member. Please try again");
                startClub(connection);
            }
        }
        // catch any exceptions
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static boolean emailExists(Connection connection, String email) {
        String query = "SELECT COUNT(*) FROM Member WHERE email = ?";
        int count = 0;
        try {
            PreparedStatement stmt = connection.prepareStatement(query);

            stmt.setString(1, email);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count > 0;
    }

    private static boolean isValidDate(String date) {
        String[] parts = date.split("-");

        // Check if there are exactly 3 parts
        if (parts.length != 3) {
            return false;
        }

        try {
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            // Validate year, month, and day
            if (year < 1 || year > 9999) return false;
            if (month < 1 || month > 12) return false;

            // Days in each month
            int[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

            // Adjust for leap year
            if (month == 2 && isLeapYear(year)) {
                daysInMonth[1] = 29;
            }

            return day >= 1 && day <= daysInMonth[month - 1];

        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isLeapYear(int year) {
        return ((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0);
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
