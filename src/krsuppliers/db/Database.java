package krsuppliers.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.TimeZone;

public class Database {
    private static Database database = null;
    private static Connection connection = null;

    private Database(){
        try{
            Map<String, String> configuration = Configuration.getConfiguration().getConfig();
            String connectionString = "jdbc:mysql://" + configuration.get("host") + ":" + configuration.get("port") + "/" + configuration.get("database") + "?useTimezone=true&serverTimezone=" + TimeZone.getDefault().getID();
            connection = DriverManager.getConnection(connectionString,configuration.get("username"),configuration.get("password"));
        }catch (SQLException | IOException e){
            System.out.println(e.getMessage());
        }
    }

    public static Connection getConnection(){
        if(database == null){
            database = new Database();
        }
        return connection;
    }

    public static void resetConnection(){
        database = null;
    }

}
