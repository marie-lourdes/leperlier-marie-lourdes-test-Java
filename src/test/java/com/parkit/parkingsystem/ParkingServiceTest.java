package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

	// private static FareCalculatorService fareCalculatorService;
	private static ParkingService parkingService;
	public static Ticket ticket;

	@Mock
	private static InputReaderUtil inputReaderUtil;
	@Mock
	private static ParkingSpotDAO parkingSpotDAO;
	@Mock
	private static TicketDAO ticketDAO;

	@BeforeAll
	private static void setUp() { // fareCalculatorService = new

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		ticket = new Ticket();
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber("ABCDEF");

	}

	@BeforeEach
	public void setUpPerTest() {
		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up per test the mock object:inputReadUtil");
		}
	}

	@Test
	public void testProcessIncomingVehicle() {
		try {
			when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
			when(inputReaderUtil.readSelection()).thenReturn(1);
			when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);
			when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

			parkingService.processIncomingVehicle();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println(e.getMessage());
			throw new RuntimeException("Failed to set up per test mock objects in testProcessIncomingVehicle");

		}
		verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
		verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));

	}

	@Test
	public void processExitingVehicleTest() {
		try {
			when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
			when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
			when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);
			when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

			parkingService.processExitingVehicle();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println(e.getMessage());
			throw new RuntimeException("Failed to set up per test mock objects in processExitingVehicleTest");
		}

		verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
	}

	@Test
	public void processExitingVehicleTestUnableUpdate() {
		try {
			when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
			when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
			parkingService.processExitingVehicle();
			assertTrue(ticketDAO.updateTicket(ticket));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println(e.getMessage());
			throw new RuntimeException(
					"Failed to set up per test mock objects in processExitingVehicleTestUnableUpdate");
		} catch (AssertionError ex) {
			assertFalse(ticketDAO.updateTicket(null));
			fail("error updating ticket for exiting vehicle because ticket is null, \n result of updateTicket method " + ex.getMessage());
		}
	}
}
