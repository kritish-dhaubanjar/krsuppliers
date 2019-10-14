package krsuppliers.db;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Configuration {

    private static Map<String, String> config = new HashMap<>();
    private static Configuration configuration = null;

    private Configuration() throws IOException {
        File file = new File(".env");
        if(!file.exists()) {
            if (!file.createNewFile()) {
                throw new Error("Can't create the necessary configuration files!");
            }
            FileOutputStream ostream = new FileOutputStream(file);
            String init = "{ 'username': 'root' , 'password': 'toor', 'database': 'krsuppliers', 'host': '127.0.0.1', 'port': '3306' }";
            ostream.write(init.getBytes());
            ostream.close();
        }
        else{
            byte [] bytes = new byte[1024];
            FileInputStream istream = new FileInputStream(file);
            int num = istream.read(bytes);
            String string = new String(bytes, 0, num);
            string = string.replace("{", "").replace("}", "");
            String [] list = string.split(",");

            String [] lines;

            for (String s : list){
                lines = s.split(":");
                config.put(lines[0].replaceAll("'", "").trim(), lines[1].replaceAll("'", "").trim() );
            }
            istream.close();
        }
    }

    public boolean updateConfiguration(String username, String password, String database, String host, String port) throws IOException{
        File file = new File(".env");
        if(file.exists()) {
            if(file.delete()) {
                if (!file.createNewFile()) {
                    throw new Error("Can't create the necessary configuration files!");
                }
                FileOutputStream ostream = new FileOutputStream(file);
                String init = "{ 'username': '" + username + "' , 'password': '"+  password +" ', 'database': '"+ database +"', 'host': '"+ host + "', 'port': '" + port +"' }";
                config.put("username", username);
                config.put("password", password);
                config.put("database", database);
                config.put("host", host);
                config.put("port", port);
                ostream.write(init.getBytes());
                ostream.close();
                Database.resetConnection();
                return true;
            }
        }
        return false;
    }

    public static Configuration getConfiguration() throws IOException{
        if(configuration == null)
            configuration = new Configuration();
        return configuration;
    }

    public Map<String, String> getConfig() {
        return config;
    }
}
