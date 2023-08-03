package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

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
	private static ParkingService parkingService;
	private static Ticket ticket;
	private static ParkingSpot parkingSpot;

	@Mock
	private static InputReaderUtil inputReaderUtil = new InputReaderUtil();
	@Mock
	private static ParkingSpotDAO parkingSpotDAO;
	@Mock
	private static TicketDAO ticketDAO;

	@BeforeEach
	public void setUpPerTest() {
		try {
			parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
			ticket = new Ticket();
			ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
			ticket.setParkingSpot(parkingSpot);
			ticket.setVehicleRegNumber("ABCDEF");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up per test the mock object : inputReadUtil");
		}
	}

	@Test
	public void testProcessIncomingVehicle() {
		try {
			when(inputReaderUtil.readSelection()).thenReturn(1);
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
			when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
			when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
			when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
			when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);

			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

			parkingService.processIncomingVehicle();
			verify(inputReaderUtil, Mockito.times(1)).readSelection();
			verify(inputReaderUtil, Mockito.times(1)).readVehicleRegistrationNumber();
			verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
			verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
			verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
			verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
			assertEquals(1, inputReaderUtil.readSelection());
			assertEquals("ABCDEF", inputReaderUtil.readVehicleRegistrationNumber());
			assertEquals(1, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
			assertTrue(parkingSpotDAO.updateParking(parkingSpot));
			assertFalse(parkingSpot.isAvailable());
			assertTrue(ticketDAO.saveTicket(ticket));
			assertEquals(2, ticketDAO.getNbTicket("ABCDEF"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up per test mock objects in testProcessIncomingVehicle");
		} catch (AssertionError ex) {
			fail(ex);
		}
	}

	@Test
	public void testProcessExitingVehicle() {
		try {
			lenient().when(inputReaderUtil.readSelection()).thenReturn(2);
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
			when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
			when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
			when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
			when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);

			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

			parkingService.processExitingVehicle();
			verify(inputReaderUtil, Mockito.times(1)).readVehicleRegistrationNumber();
			verify(ticketDAO, Mockito.times(1)).getTicket("ABCDEF");
			verify(ticketDAO, Mockito.times(2)).updateTicket(any(Ticket.class));
			verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
			verify(ticketDAO, Mockito.times(1)).getNbTicket("ABCDEF");
			assertEquals(2, inputReaderUtil.readSelection());
			assertEquals("ABCDEF", inputReaderUtil.readVehicleRegistrationNumber());
			assertEquals(ticket, ticketDAO.getTicket("ABCDEF"));
			assertTrue(ticketDAO.updateTicket(ticket));
			assertTrue(parkingSpotDAO.updateParking(parkingSpot));
			assertTrue(parkingSpot.isAvailable());
			assertEquals(2, ticketDAO.getNbTicket("ABCDEF"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up per test mock objects in processExitingVehicleTest");
		} catch (AssertionError ex) {
			fail(ex);
		}
	}

	@Test
	public void testProcessExitingVehicleUnableUpdate() {
		try {
			
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
			when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
			when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

			parkingService.processExitingVehicle();
			verify(ticketDAO, Mockito.times(1)).getTicket("ABCDEF");
			verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
			verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
			//assertEquals(2, inputReaderUtil.readSelection());
			assertEquals("ABCDEF", inputReaderUtil.readVehicleRegistrationNumber());
			assertEquals(ticket, ticketDAO.getTicket("ABCDEF"));
			assertFalse(ticketDAO.updateTicket(ticket),
					"error updating ticket for exiting vehicle,should be return false not true");
			assertFalse(parkingSpotDAO.updateParking(parkingSpot));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up per test mock objects in processExitingVehicleTest");
		} catch (AssertionError ex) {
			fail(ex);
		}
	}

	@Test
	public void testGetNextParkingNumberIfAvailable() {
		try {
			when(inputReaderUtil.readSelection()).thenReturn(1);
			when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
			int parkingNumber = 1;
			boolean isAvailable = true;
			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

			parkingService.getNextParkingNumberIfAvailable();
			ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
			verify(inputReaderUtil, Mockito.times(2)).readSelection();
			verify(parkingSpotDAO, Mockito.times(2)).getNextAvailableSlot(any(ParkingType.class));
			assertEquals(1, inputReaderUtil.readSelection());
			assertAll(() -> assertEquals(parkingNumber, parkingSpot.getId()),
					() -> assertEquals(isAvailable, parkingSpot.isAvailable()));

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up per test mock objects in processExitingVehicleTest");
		} catch (AssertionError ex) {
			fail(ex);
		}
	}

	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
		try {
			when(inputReaderUtil.readSelection()).thenReturn(1);
			when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);
			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

			parkingService.getNextParkingNumberIfAvailable();
			verify(inputReaderUtil, Mockito.times(1)).readSelection();
			verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
			assertEquals(1, inputReaderUtil.readSelection());
			assertTrue(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR) <= 0);
			assertNull(parkingService.getNextParkingNumberIfAvailable());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Failed to set up per test mock objects in testGetNextParkingNumberIfAvailableParkingNumberNotFound");
		} catch (AssertionError ex) {
			fail(ex);
		}
	}

	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
		try {
			int selectionUser = 0;
			when(inputReaderUtil.readSelection()).thenReturn(3);
			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

			parkingService.getNextParkingNumberIfAvailable();
			selectionUser = inputReaderUtil.readSelection();
			verify(inputReaderUtil, Mockito.times(2)).readSelection();
			assertEquals(3, inputReaderUtil.readSelection());
			assertFalse(selectionUser > 0 && selectionUser <= 2, "wrong argument: " + selectionUser
					+ " should return false,argument parking type must be 1 or 2, assertion");
			assertNull(parkingService.getNextParkingNumberIfAvailable());
			assertThrows(IllegalArgumentException.class, () -> parkingService.getVehicleType());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Failed to set up per test mock objects in testGetNextParkingNumberIfAvailableParkingNumberNotFound");
		} catch (AssertionError ex) {
			fail(ex);
		}

	}
}