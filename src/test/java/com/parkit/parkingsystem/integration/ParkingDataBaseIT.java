package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
	// private ParkingService parkingService;

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

	@BeforeEach
    private void setUpPerTest() throws Exception {
		
		when(inputReaderUtil.readSelection()).thenReturn(1);
	        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	        //when(inputReaderUtil.readSelection()).thenReturn(2);
  		
    }
	/*
	 * @AfterEach private void undefUpPerTest() throws Exception {
	 * 
	 * //dataBasePrepareService.clearDataBaseEntries(); }
	 * 
	 * /* @AfterAll private static void tearDown() {
	 * 
	 * /* try {
	 * when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF"); }
	 * catch (Exception e) { // TODO Auto-generated catch block e.printStackTrace();
	 * } //dataBasePrepareService.clearDataBaseEntries(); }
	 */

	@Test
	public void testParkingACar() {
		try {
			ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
			parkingSpot = parkingService.getNextParkingNumberIfAvailable();
			parkingService.processIncomingVehicle();

			Ticket ticketSavedOutime = ticketDAO.getTicket("ABCDEF");
			// check the time of saving ticket to ensure it's the same ticket
			System.out.println("ticket saved with outtime " + ticketSavedOutime.getOutTime());

			// TODO: check that a ticket is actualy saved in DB and Parking table is updated
			// with availability
			Ticket ticketSaved = ticketDAO.getTicket("ABCDEF");
			// check the time of saving ticket to ensure it's the same ticket
			System.out.println("ticket saved with intime " + ticketSaved.getInTime());
			int nextParkingNumberMinAvailableForCar_ShouldBeSuperieurToparkingNumberAlreadySavedInDb = parkingSpotDAO
					.getNextAvailableSlot(ParkingType.CAR);
			System.out.println("nextParkingNumberAvailable after registering the ticket and the parking number is"
					+ nextParkingNumberMinAvailableForCar_ShouldBeSuperieurToparkingNumberAlreadySavedInDb);
			// check if parkingSpotDAO.updateparking return true with call method
			// isAvailable() in the method
			assertTrue(parkingSpotDAO.updateParking(parkingSpot), "updateParking return false");
			// check if the ticket saved with vehicleregnumber returned by the mock
			// inputreaderUtil , requesting the DB 'test" with method getTicket
			// and request SQL prepared and stocked in constant GET_TICKET
			assertNotNull(ticketSaved, "error saving ticket in DB test");
			assertTrue(
					nextParkingNumberMinAvailableForCar_ShouldBeSuperieurToparkingNumberAlreadySavedInDb > parkingSpot
							.getId(),
					"error in updating availability with value false  in parking number already saved in DB");
			System.out.println("ticket saved with availability");
		} catch (AssertionError ex) {
			fail(ex.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testParkingLotExit() throws InterruptedException {
		dataBasePrepareService.clearDataBaseEntries();
		try {
			long startedAt = System.currentTimeMillis();
			testParkingACar();

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

			if (timeElapsedOfMethodsInRateHour < 0.5) {
				assertEquals(0.0 * Fare.CAR_RATE_PER_HOUR, ticketDAO.getTicket("ABCDEF").getPrice());
			} else {
				assertEquals(timeElapsedOfMethodsInRateHour * Fare.CAR_RATE_PER_HOUR,
						ticketDAO.getTicket("ABCDEF").getPrice());
			}

			// TODO: check that the fare generated and out time are populated correctly in
			// the database
			assertNotNull(ticketDAO.getTicket("ABCDEF").getOutTime(),
					"error updating in DB the outTime of ticket saved");
		} catch (AssertionError e) {
			fail(e.getMessage());
		}

		System.out.println("out time updated ticket" + ticketDAO.getTicket("ABCDEF").getOutTime());
		// add delay with the second call of processIncomingVehicle 
		Thread.sleep(5000);
	}
}
