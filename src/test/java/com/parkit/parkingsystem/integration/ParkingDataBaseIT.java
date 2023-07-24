package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;
	private static Ticket ticket;
	private static ParkingSpot parkingSpot;
	private static Ticket ticketSaved;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();

	}

	/*
	 * @BeforeEach private void setUpPerTest() throws Exception { }
	 * 
	 * @AfterEach private void undefUpPerTest() throws Exception {
	 */

	@Test
	public void testParkingACar() {
		try {
			when(inputReaderUtil.readSelection()).thenReturn(1);
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
			ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
			parkingSpot = parkingService.getNextParkingNumberIfAvailable();
			parkingService.processIncomingVehicle();

			Ticket ticketSavedOutime = ticketDAO.getTicket("ABCDEF");
			// check the time of saving ticket to ensure it's the same ticket
			System.out.println("ticket saved with outtime " + ticketSavedOutime.getOutTime());

			// TODO: check that a ticket is actualy saved in DB and Parking table is updated
			// with availability
			ticketSaved = ticketDAO.getTicket("ABCDEF");
			// check the time of saving ticket to ensure it's the same ticket
			System.out.println("ticket saved with intime " + ticketSaved.getInTime());
			int nextParkingNumberMinAvailableForCar_ShouldBeSuperieurToParkingNumberPreviouslyAvailable = parkingSpotDAO
					.getNextAvailableSlot(ParkingType.CAR);
			System.out.println(
					"nextParkingNumberAvailable after registering the ticket and the parking number in DB 'test' is : "
							+ nextParkingNumberMinAvailableForCar_ShouldBeSuperieurToParkingNumberPreviouslyAvailable);
			// check if parkingSpotDAO.updateparking return true with call method
			// isAvailable() in the method
			assertTrue(parkingSpotDAO.updateParking(parkingSpot), "updateParking should return true ");
			// check if the ticket saved with vehicleregnumber, requesting the DB 'test"
			// with method getTicket(vehicleRegnumber)
			// and request SQL prepared and stocked in constant GET_TICKET
			assertNotNull(ticketSaved, "error saving ticket in DB 'test' test");
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
	public void testParkingLotExit() throws InterruptedException {
		dataBasePrepareService.clearDataBaseEntries();
		try {
			long startedAt = System.currentTimeMillis();
			testParkingACar();
			// check if the outime during the process incoming vehicle return null
			assertNull(ticketSaved.getOutTime(), "outTime registered in DB 'test' should be null");
			/*
			 * int ticket = ticketDAO.getTicket("ABCDEF"); System.out.println("nb ticket" +
			 * nbTicket);
			 */
			ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
			Thread.sleep(5000);
			parkingService.processExitingVehicle();
			long endedAt = System.currentTimeMillis();

			// Time converted in rate of Hour
			double timeElapsedOfMethodsMilliSeconds = endedAt - startedAt;
			double timeElapsedOfMethodsInRateHour = timeElapsedOfMethodsMilliSeconds / 1000 / 60 / 60;
			System.out.println("time elapsed of methods in rate Hour " + timeElapsedOfMethodsInRateHour);

			// check if price of ticket in Db 'test' is correctly calculated according
			// duration and fare for parking type CAR and saved
			if (timeElapsedOfMethodsInRateHour < 0.5) {
				assertEquals(0.0 * Fare.CAR_RATE_PER_HOUR, ticketDAO.getTicket("ABCDEF").getPrice());
			} else {
				assertEquals(timeElapsedOfMethodsInRateHour * Fare.CAR_RATE_PER_HOUR,
						ticketDAO.getTicket("ABCDEF").getPrice());
			}

			// TODO: check that the fare generated and out time are populated correctly in
			// the database

			// check if the outTime updated in Db during the process exiting vehicle don't
			// return null
			assertNotNull(ticketDAO.getTicket("ABCDEF").getOutTime(),
					"error updating in DB the outTime of ticket saved should return a date TimeStamp from DB 'test', not Null");
		} catch (AssertionError e) {
			fail(e.getMessage());
		}

		System.out.println("ticket updated with fare " + ticketDAO.getTicket("ABCDEF").getPrice() + "and outime "
				+ ticketDAO.getTicket("ABCDEF").getOutTime() + " of ticket in DB 'test'with availability in DB 'test'");
		// add delay with the second call of processIncomingVehicle
		Thread.sleep(5000);
	}
}