package me.notenoughskill.marketplace.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static Connection connection;
    private static final String URL = "jdbc:mysql://b7qyfevmyqjide9wjtyp-mysql.services.clever-cloud.com:3306/b7qyfevmyqjide9wjtyp";
    private static final String USER = "u2qvqedpvejitaft";
    private static final String PASSWORD = "tjwXboFkFzZPvgi2PA45";

    public static void initialize() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
