import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;

public class FitnessRoutine {

    private Connection connection;
    private int routine_id,
        member_id;
    private String title,
            description,
            instructions;

    private Scanner sc = new Scanner(System.in);
    public FitnessRoutine(Connection conn, int r, int m, String t, String ins, String des){
        connection = conn;
        routine_id = r;
        member_id = m;
        title = t;
        instructions = ins;
        description = des;
    }

    public void delete(){
        try{
            // create query to delete student entry with student id = student_id
            PreparedStatement pstmt = connection.prepareStatement("DELETE FROM fitness_routines WHERE routine_id = ?");

            // populate query with the given student_id
            pstmt.setInt(1, routine_id);

            // execute the query to delete entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Successful deletion");
            }
            else{
                System.out.println("Unable to delete Fitness Routine. Please try again");
            }
        }
        // catch any exceptions
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void edit(){
        int choice = -1;

        System.out.println("\nEDIT ROUTINE " + '"' + title + '"');
        while(choice == -1){
            System.out.println("What would you like to edit?");
            System.out.println("1. Title");
            System.out.println("2. Description");
            System.out.println("3. Instructions");
            System.out.print("\nChoice: ");
            choice = Integer.parseInt(sc.nextLine());
            if(choice == 1) setTitle();
            else if (choice == 2) setDescription();
            else if (choice == 3) setInstructions();
            else choice = -1;
        }
    }

    private void setInstructions(){
        String newIns;
        System.out.print("New Instructions: ");
        newIns = sc.nextLine();
        try{
            // create query to update the entry who has member id = member_id with first_name = newFName
            PreparedStatement pstmt = connection.prepareStatement("UPDATE fitness_routines SET instructions = ? WHERE routine_id = ?");

            // populate query with the provided member_id and newFName
            pstmt.setString(1, newIns);
            pstmt.setInt(2, routine_id);

            // execute the query to update entry and print success message
            if(pstmt.executeUpdate() > 0){
                System.out.println("Update instructions: " + newIns);
            }
            else{
                System.out.println("Unable to update instructions. Please try again");
            }
        }
        // catch any exceptions
        catch(Exception e){
            System.out.println("Unable to update instructions. Please try again");
        }
    }

    private void setDescription(){
        String newDes;
        System.out.print("New Description: ");
        newDes = sc.nextLine();
        try{
            // create query to update the entry who has member id = member_id with first_name = newFName
            PreparedStatement pstmt = connection.prepareStatement("UPDATE fitness_routines SET description = ? WHERE routine_id = ?");

            // populate query with the provided member_id and newFName
            pstmt.setString(1, newDes);
            pstmt.setInt(2, routine_id);

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
            PreparedStatement pstmt = connection.prepareStatement("UPDATE fitness_routines SET title = ? WHERE routine_id = ?");

            // populate query with the provided member_id and newFName
            pstmt.setString(1, newTitle);
            pstmt.setInt(2, routine_id);

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
        return (title + "\nDescription: " + description + "\nInstruction: " + instructions);
    }

    public String toBasicString(){
        return (title);
    }

    public int getRoutine_id() {
        return routine_id;
    }

    public int getMember_id() {
        return member_id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getInstructions() {
        return instructions;
    }
}
