package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfigIT;
import com.parkit.parkingsystem.integration.service.DataBasePrepareServiceIT;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfigIT dataBaseTestConfig = new DataBaseTestConfigIT();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareServiceIT dataBasePrepareService;
	private static final Logger logger = LogManager.getLogger("ParkingDataBaseIT");

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareServiceIT();
	}

	@BeforeEach
	private void setUpPerTest() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		dataBasePrepareService.clearDataBaseEntries();
	}

	@AfterAll
	private static void tearDown() {

	}

	@Test
	public void testParkingACar() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();

		Ticket ticket = ticketDAO.getTicket("ABCDEF");
		ParkingSpot parkingSpot = ticket.getParkingSpot();
		boolean spotAvailability = parkingSpot.isAvailable();

		assertNotNull(ticket);
		assertNotNull(ticket.getInTime());

		assertFalse(spotAvailability);

	}

	@Test
	public void testParkingLotExit() {
		testParkingACar();

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		Ticket ticketIn = ticketDAO.getTicket("ABCDEF");
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		ticketIn.setInTime(inTime);
		try {
			con = dataBaseTestConfig.getConnection();
			ps = con.prepareStatement("update ticket set IN_TIME = ? where ID = ?");
			ps.setTimestamp(1, new Timestamp(ticketIn.getInTime().getTime()));
			ps.setInt(2, ticketIn.getId());
			ps.execute();
		} catch (Exception ex) {
			logger.error("Unable to process updating ticket's in time", ex);
		} finally {
			dataBaseTestConfig.closeResultSet(rs);
			dataBaseTestConfig.closePreparedStatement(ps);
			dataBaseTestConfig.closeConnection(con);
		}

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processExitingVehicle();

		Ticket ticketOut = new Ticket();
		try {
			con = dataBaseTestConfig.getConnection();
			ps = con.prepareStatement(
					"select ID, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME from ticket where VEHICLE_REG_NUMBER = 'ABCDEF'");
			rs = ps.executeQuery();
			if (rs.next()) {
				ticketOut.setId(rs.getInt(1));
				ticketOut.setVehicleRegNumber(rs.getString(2));
				ticketOut.setPrice(rs.getDouble(3));
				ticketOut.setInTime(rs.getTimestamp(4));
				ticketOut.setOutTime(rs.getTimestamp(5));
			}
		} catch (Exception ex) {
			logger.error("Unable to get ticket's data", ex);
		} finally {
			dataBaseTestConfig.closeResultSet(rs);
			dataBaseTestConfig.closePreparedStatement(ps);
			dataBaseTestConfig.closeConnection(con);
		}

		assertNotNull(ticketOut.getOutTime());
		assertTrue(ticketOut.getPrice() != 0);

	}

}
