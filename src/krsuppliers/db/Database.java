package krsuppliers.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static Database database = null;
    private static Connection connection = null;

    private Database(){
        try{
            connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1/krsuppliers","root","toor");
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public static Connection getConnection(){
        if(database == null){
            database = new Database();
        }
        return connection;
    }

}
