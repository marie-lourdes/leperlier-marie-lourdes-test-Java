package com.parkit.parkingsystem.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.constants.DBConnectionConstants;

import java.sql.*;

public class DataBaseConfig {

	private static final Logger logger = LogManager.getLogger("DataBaseConfig");

	public Connection getConnection() throws ClassNotFoundException, SQLException {
		logger.trace("Create DB connection");
		Class.forName("com.mysql.cj.jdbc.Driver");
		return DriverManager.getConnection(DBConnectionConstants.URL_DBCONNECTION,DBConnectionConstants.USER_DBCONNECTION, DBConnectionConstants.PASSWORD_DBCONNECTION);
		//return DriverManager.getConnection("jdbc:mysql://localhost:3306/prod", "root", "rootroot");
	}

	public void closeConnection(Connection con) {
		if (con != null) {
			try {
				con.close();
				logger.trace("Closing DB connection");
			} catch (SQLException e) {
				logger.error("Error while closing connection", e);
			}
		}
	}

	public void closePreparedStatement(PreparedStatement ps) {
		if (ps != null) {
			try {
				ps.close();
				logger.trace("Closing Prepared Statement");
			} catch (SQLException e) {
				logger.error("Error while closing prepared statement", e);
			}
		}
	}

	public void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
				logger.trace("Closing Result Set");
			} catch (SQLException e) {
				logger.error("Error while closing result set", e);
			}
		}
	}
}