package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
class ParkingDataBaseIT {
	private static final Logger logger = LogManager.getLogger("ParkingDataBaseIT");
	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static DataBasePrepareService dataBasePrepareService;
	private static ParkingSpot parkingSpot;
	private static Ticket ticketSaved;
	private static FareCalculatorService fareCalculatorService;

	@Spy
	private static ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();

	@Spy
	private static TicketDAO ticketDAO = new TicketDAO();

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	// free up the resource used with the classes needed to connect to the database
	@AfterAll
	private static void tearDown() {
		parkingSpotDAO.dataBaseConfig = null;
		ticketDAO.dataBaseConfig = null;
		dataBasePrepareService = null;
	}

	@Test
	void testParkingACar() throws SQLException {
		try {
			when(inputReaderUtil.readSelection()).thenReturn(1);
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
			ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
			parkingSpot = parkingService.getNextParkingNumberIfAvailable();

			parkingService.processIncomingVehicle();

			ticketSaved = ticketDAO.getTicket("ABCDEF");
			// Ticket ticketSavedOutime = ticketDAO.getTicket("ABCDEF");
			// check the inTime of saving ticket to ensure it's the same ticket
			logger.debug("ticket saved with inTime {} ", ticketSaved.getInTime());
			logger.debug("ticket saved with outTime {} ", ticketSaved.getOutTime());
			int nextParkingNumberMinAvailableForCar_ShouldBeSuperieurToParkingNumberPreviouslyAvailable = parkingSpotDAO
					.getNextAvailableSlot(ParkingType.CAR);
			logger.debug(
					"nextParkingNumberAvailable after registering the ticket and the parking number in DB 'test' is : {}",
					nextParkingNumberMinAvailableForCar_ShouldBeSuperieurToParkingNumberPreviouslyAvailable);
			// TODO: check that a ticket is actualy saved in DB and Parking table is updated
			// with availability
			verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
			verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
			// check the connection is not null
			assertNotNull(ticketDAO.dataBaseConfig.getConnection());
			// check if the ticket saved with vehicleregnumber, requesting the DB 'test"
			// with method getTicket(vehicleRegnumber)
			// and request SQL prepared and stocked in constant GET_TICKET
			assertNotNull(ticketSaved, "error saving ticket in DB 'test' test");
			assertTrue(
					nextParkingNumberMinAvailableForCar_ShouldBeSuperieurToParkingNumberPreviouslyAvailable > ticketSaved
							.getParkingSpot().getId(),
					"error updating and registering in DB 'test' availabilty false of parking number in method process incoming");
			// check if parkingSpotDAO.updateparking return true with call method
			// isAvailable() in the method
			assertTrue(parkingSpotDAO.updateParking(parkingSpot), "updateParking should return true ");

			assertTrue(
					nextParkingNumberMinAvailableForCar_ShouldBeSuperieurToParkingNumberPreviouslyAvailable > parkingSpot
							.getId(),
					"error in updating availability with value false  in parking number already saved in DB should  be superior to ParkingNumber Previously Available and saved in DB with the ticket ");

			System.out.println("ticket saved with availability " + ticketSaved.getParkingSpot().isAvailable()
					+ " in DB 'test' after registring the incoming vehicle");

		} catch (Exception e) {
			logger.trace(e);
			throw new RuntimeException("Failed to set up per test mock object inputReaderUtil in testParkingACar");
		} catch (AssertionError ex) {
			fail(ex);
		}
	}

	@Test
	void testParkingLotExit() throws InterruptedException, SQLException, ClassNotFoundException {
		try {
			testParkingACar();
			lenient().when(inputReaderUtil.readSelection()).thenReturn(2);
			ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

			parkingService.processExitingVehicle();

			logger.debug("INTIME testparking {}", ticketDAO.getTicket("ABCDEF").getInTime());
			// TODO: check that the fare generated and out time are populated correctly in
			// the database
			verify(ticketDAO, Mockito.times(3)).getTicket(anyString());
			verify(parkingSpotDAO, Mockito.times(3)).updateParking(any(ParkingSpot.class));
			// check the connection is not null
			assertNotNull(ticketDAO.dataBaseConfig.getConnection());

			// check if price of ticket in DB 'test' is correctly calculated according
			// duration and fare for parking type CAR and saved
			fareCalculatorService = new FareCalculatorService();
			long inTimeRecurringUser = ticketDAO.getTicket("ABCDEF").getInTime().getTime();
			long outTimeRecurringUser = ticketDAO.getTicket("ABCDEF").getOutTime().getTime();
			fareCalculatorService.calculateDurationOfParking(inTimeRecurringUser, outTimeRecurringUser);
			double duration = fareCalculatorService.getDurationOfParking();
			double priceExpected = 0.0 * Fare.CAR_RATE_PER_HOUR;
			double priceActual = ticketDAO.getTicket("ABCDEF").getPrice();
			assertEquals(priceExpected, priceActual);
			logger.debug("duration parking {} ", duration);

			// check if the outTime updated in Db during the process exiting vehicle don't
			// return null
			assertNotNull(ticketDAO.getTicket("ABCDEF").getOutTime(),
					"error updating in DB the outTime of ticket saved should return a date TimeStamp from DB 'test', not Null");

		} catch (AssertionError ex) {
			fail(ex);
		}

		logger.debug("ticket updated with fare {} and outime {}  of ticket in DB 'test'with availability in DB 'test'",
				ticketDAO.getTicket("ABCDEF").getPrice(), ticketDAO.getTicket("ABCDEF").getOutTime());
	}

	@Test
	void testParkingLotExitRecurringUser() throws InterruptedException, SQLException {
		try {
			dataBasePrepareService.clearDataBaseEntries();
			when(inputReaderUtil.readSelection()).thenReturn(1);
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("GHIJK");
			ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
			parkingService.processIncomingVehicle();
			parkingService.processExitingVehicle();

			dataBasePrepareService.simulateInTimeDataBaseEntries();
			parkingService.processIncomingVehicle();
			parkingService.processExitingVehicle();

			verify(inputReaderUtil, Mockito.times(2)).readSelection();
			verify(inputReaderUtil, Mockito.times(4)).readVehicleRegistrationNumber();
			// check the connection is not null
			assertNotNull(ticketDAO.dataBaseConfig.getConnection());

			fareCalculatorService = new FareCalculatorService();
			long inTimeRecurringUser = ticketDAO.getTicket("GHIJK").getInTime().getTime();
			long outTimeRecurringUser = ticketDAO.getTicket("GHIJK").getOutTime().getTime();
			fareCalculatorService.calculateDurationOfParking(inTimeRecurringUser, outTimeRecurringUser);
			double duration = fareCalculatorService.getDurationOfParking();
			logger.debug("duration parking recurring user {} ", duration);
			double priceExpected = Math.round((0.95 * (duration * Fare.CAR_RATE_PER_HOUR)));
			double priceActual = Math.round((ticketDAO.getTicket("GHIJK").getPrice()));
			// check the calcul of price discount
			assertEquals(priceExpected, priceActual);

			double rateHourOf30minutes = 0.50;
			// check the duration is more than 30 minutes
			assertTrue(duration > rateHourOf30minutes,
					"the duration should be more than 30 minutes to get price discount");

			// check if the number ticket is more than one ticket
			int nbTicketOfRecurringUser = ticketDAO.getNbTicket("GHIJK");
			assertTrue(nbTicketOfRecurringUser > 1,
					"the number of ticket ohf user should be more than one ticket,this user is not recurring user");
			logger.debug("price testparkingrecurringuser {} ", ticketDAO.getTicket("GHIJK").getPrice());
		} catch (Exception e) {
			logger.trace(e);
			throw new RuntimeException(
					"Failed to set up per test mock object inputReaderUtil in testParkingLotExitRecurringUser");
		} catch (AssertionError ex) {
			fail(ex);
		}
	}
}