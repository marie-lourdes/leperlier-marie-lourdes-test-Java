package com.parkit.parkingsystem.constants;

public class DBConnectionConstants {
	public static final String URL_DBCONNECTION = "jdbc:mysql://localhost:3306/prod";
	public static final String URL_DBTESTCONNECTION = "jdbc:mysql://localhost:3306/test?serverTimezone=Europe/Paris";
	public static final String USER_DBCONNECTION = "root";
	public static final String PASSWORD_DBCONNECTION = "rootroot";

	// constructor private for utility class
	private DBConnectionConstants() {

	}
}
