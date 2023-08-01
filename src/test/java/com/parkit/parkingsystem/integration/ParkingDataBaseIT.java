package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static DataBasePrepareService dataBasePrepareService;
	private static ParkingSpot parkingSpot;
	private static Ticket ticketSaved;
	private static TicketDAO ticketDAO;
	private static FareCalculatorService fareCalculatorService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	public static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
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

	/*
	 * @BeforeEach private void setUpPerTest() throws Exception { }
	 */

	@Test
	public void testParkingACar() throws SQLException {
		try {
			when(inputReaderUtil.readSelection()).thenReturn(1);
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
			ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
			parkingSpot = parkingService.getNextParkingNumberIfAvailable();

			parkingService.processIncomingVehicle();

			Ticket ticketSavedOutime = ticketDAO.getTicket("ABCDEF");
			// Log the outTime null during saving ticket to ensure it's the same ticket

			System.out.println("ticket saved with outTime  " + ticketSavedOutime.getOutTime());

			// TODO: check that a ticket is actualy saved in DB and Parking table is updated
			// with availability
			ticketSaved = ticketDAO.getTicket("ABCDEF");

			// assertTrue(ticketDAO.saveTicket(ticket), "saveTicket should be retrun true
			// ");
			// check if the ticket saved with vehicleregnumber, requesting the DB 'test"
			// with method getTicket(vehicleRegnumber)
			// and request SQL prepared and stocked in constant GET_TICKET
			assertNotNull(ticketSaved, "error saving ticket in DB 'test' test");
			// check the time of saving ticket to ensure it's the same ticket
			System.out.println("ticket saved with intime " + ticketSaved.getInTime());
			int nextParkingNumberMinAvailableForCar_ShouldBeSuperieurToParkingNumberPreviouslyAvailable = parkingSpotDAO
					.getNextAvailableSlot(ParkingType.CAR);
			System.out.println(
					"nextParkingNumberAvailable after registering the ticket and the parking number in DB 'test' is : "
							+ nextParkingNumberMinAvailableForCar_ShouldBeSuperieurToParkingNumberPreviouslyAvailable);
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
			e.printStackTrace();
			throw new RuntimeException("Failed to set up per test mock object inputReaderUtil in testParkingACar");
		} catch (AssertionError ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	public void testParkingLotExit() throws InterruptedException, SQLException {
		try {
			// long startedAt = System.currentTimeMillis();
			testParkingACar();
			ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
			Thread.sleep(5000);
			parkingService.processExitingVehicle();
			// long endedAt = System.currentTimeMillis();

			System.out.println("INTIME testparking" + ticketDAO.getTicket("ABCDEF").getInTime());
			// Time converted in rate of Hour
			/*
			 * double timeElapsedOfMethodsMilliSeconds = endedAt - startedAt; double
			 * timeElapsedOfMethodsInRateHour = timeElapsedOfMethodsMilliSeconds / 1000 / 60
			 * / 60; System.out.println("time elapsed of methods in rate Hour " +
			 * timeElapsedOfMethodsInRateHour);
			 */

			// TODO: check that the fare generated and out time are populated correctly in
			// the database

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
			System.out.println("DURATION testparking " + duration);

			// check if the outTime updated in Db during the process exiting vehicle don't
			// return null
			assertNotNull(ticketDAO.getTicket("ABCDEF").getOutTime(),
					"error updating in DB the outTime of ticket saved should return a date TimeStamp from DB 'test', not Null");

		} catch (AssertionError ex) {
			fail(ex.getMessage());
		}

		System.out.println("ticket updated with fare " + ticketDAO.getTicket("ABCDEF").getPrice() + "and outime "
				+ ticketDAO.getTicket("ABCDEF").getOutTime() + " of ticket in DB 'test'with availability in DB 'test'");
	}

	@Test
	public void testParkingLotExitRecurringUser() throws InterruptedException, SQLException {
		try {
			dataBasePrepareService.clearDataBaseEntries();
			Thread.sleep(5000);
			when(inputReaderUtil.readSelection()).thenReturn(1);
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("GHIJK");
			ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
			parkingService.processIncomingVehicle();
			Thread.sleep(5000);
			parkingService.processExitingVehicle();
			dataBasePrepareService.simulateInTimeDataBaseEntries();

			// ParkingService parkingService = new ParkingService(inputReaderUtil,
			// parkingSpotDAO, ticketDAO);

			long startedAt = System.currentTimeMillis();
			parkingService.processIncomingVehicle();
			parkingService.processExitingVehicle();
			long endedAt = System.currentTimeMillis();

			fareCalculatorService = new FareCalculatorService();
			long inTimeRecurringUser = ticketDAO.getTicket("GHIJK").getInTime().getTime();
			long outTimeRecurringUser = ticketDAO.getTicket("GHIJK").getOutTime().getTime();
			fareCalculatorService.calculateDurationOfParking(inTimeRecurringUser, outTimeRecurringUser);
			double duration = fareCalculatorService.getDurationOfParking();
			System.out.println("DURATION testparkingrecurringuser " + duration);
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
			System.out.println("price testparkingrecurringuser" + ticketDAO.getTicket("GHIJK").getPrice());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Failed to set up per test mock object inputReaderUtil in testParkingLotExitRecurringUser");
		} catch (AssertionError ex) {
			fail(ex.getMessage());
		}
	}
}