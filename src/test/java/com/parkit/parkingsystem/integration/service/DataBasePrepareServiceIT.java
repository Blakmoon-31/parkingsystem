package com.parkit.parkingsystem.integration.service;

import java.sql.Connection;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfigIT;

public class DataBasePrepareServiceIT {

	DataBaseTestConfigIT dataBaseTestConfig = new DataBaseTestConfigIT();

	public void clearDataBaseEntries() {
		Connection connection = null;
		try {
			connection = dataBaseTestConfig.getConnection();

			// set parking entries to available
			connection.prepareStatement("update parking set available = true").execute();
			connection.prepareStatement("update parking set available = true").close();
			// clear ticket entries;
			connection.prepareStatement("truncate table ticket").execute();
			connection.prepareStatement("truncate table ticket").close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dataBaseTestConfig.closeConnection(connection);
		}
	}

}
