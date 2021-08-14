package com.parkit.parkingsystem.integration.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.config.DataBaseConfig;

public class DataBaseTestConfigIT extends DataBaseConfig {

	private static final Logger logger = LogManager.getLogger("DataBaseTestConfig");

	public Connection getConnection() throws ClassNotFoundException, SQLException {
		logger.info("Create DB connection");
		Class.forName("com.mysql.cj.jdbc.Driver");

		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("src/main/resources/DB.properties")) {
			prop.load(input);
		} catch (FileNotFoundException e) {
			logger.error("DB configuration file not found", e);
		} catch (IOException e) {
			logger.error("Error while reading DB configuration file", e);
		}

		String DBPassWord = prop.getProperty("DBpassword");

		return DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/test?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
				"root", DBPassWord);
	}

	public void closeConnection(Connection con) {
		if (con != null) {
			try {
				con.close();
				logger.info("Closing DB connection");
			} catch (SQLException e) {
				logger.error("Error while closing connection", e);
			}
		}
	}

	public void closePreparedStatement(PreparedStatement ps) {
		if (ps != null) {
			try {
				ps.close();
				logger.info("Closing Prepared Statement");
			} catch (SQLException e) {
				logger.error("Error while closing prepared statement", e);
			}
		}
	}

	public void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
				logger.info("Closing Result Set");
			} catch (SQLException e) {
				logger.error("Error while closing result set", e);
			}
		}
	}
}
