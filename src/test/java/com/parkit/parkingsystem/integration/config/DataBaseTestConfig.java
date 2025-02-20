package com.parkit.parkingsystem.integration.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConnectionConstants;

public class DataBaseTestConfig extends DataBaseConfig {

	private static final Logger logger = LogManager.getLogger("DataBaseTestConfig");

	public Connection getConnection() throws ClassNotFoundException, SQLException {
		try {
			logger.trace("Create DB connection");
			Class.forName("com.mysql.cj.jdbc.Driver");

		} catch (Exception e) {
			logger.error("erreur connexion");
			logger.trace(e);
		}
		return DriverManager.getConnection(DBConnectionConstants.URL_DBTESTCONNECTION,
				DBConnectionConstants.USER_DBCONNECTION, DBConnectionConstants.PASSWORD_DBCONNECTION);
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
