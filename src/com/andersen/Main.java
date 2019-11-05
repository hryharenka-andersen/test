package com.andersen;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        try (InputStream input = new FileInputStream("resources/db.properties")) {
            Properties prop = new Properties();
            // load a properties file
            prop.load(input);
            String url = prop.getProperty("url");
            String pass = prop.getProperty("pass");
            String username = prop.getProperty("username");
            Integer number = Integer.parseInt(prop.getProperty("number"));
            Handler handler = new Handler(url, username, pass, number);
            handler.generateXML();
            System.out.println("Sum is " + handler.calculateSum());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
