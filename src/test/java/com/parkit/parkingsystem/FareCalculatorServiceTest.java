package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

public class FareCalculatorServiceTest {
	private static FareCalculatorService fareCalculatorService;
	private Ticket ticket;

	@BeforeAll
	public static void setUp() {
		fareCalculatorService = new FareCalculatorService();
	}

	@BeforeEach
	public void setUpPerTest() {
		ticket = new Ticket();
	}

	@Test
	public void testCalculateFareCar() {
		try {
			Date inTime = new Date();
			inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
			Date outTime = new Date();
			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

			ticket.setInTime(inTime);
			ticket.setOutTime(outTime);
			ticket.setParkingSpot(parkingSpot);
			fareCalculatorService.calculateFare(ticket);
			assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
		} catch (AssertionError ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	public void testCalculateFareBike() {
		try {
			Date inTime = new Date();
			inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
			Date outTime = new Date();
			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

			ticket.setInTime(inTime);
			ticket.setOutTime(outTime);
			ticket.setParkingSpot(parkingSpot);
			fareCalculatorService.calculateFare(ticket);
			assertEquals(Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
		} catch (AssertionError ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	public void testCalculateFareUnkownType() {
		try {
			Date inTime = new Date();
			inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
			Date outTime = new Date();
			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.TRUCK, false);

			ticket.setInTime(inTime);
			ticket.setOutTime(outTime);
			ticket.setParkingSpot(parkingSpot);
			assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
		} catch (AssertionError ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	public void testCalculateFareBikeWithFutureInTime() {
		try {
			Date inTime = new Date();
			inTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
			Date outTime = new Date();
			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

			ticket.setInTime(inTime);
			ticket.setOutTime(outTime);
			ticket.setParkingSpot(parkingSpot);
			assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
		} catch (AssertionError ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	public void testCalculateFareBikeWithLessThanOneHourParkingTime() {
		try {
			Date inTime = new Date();
			// 45 minutes parking time should give 3/4th parking fare
			inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));
			Date outTime = new Date();
			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

			ticket.setInTime(inTime);
			ticket.setOutTime(outTime);
			ticket.setParkingSpot(parkingSpot);
			fareCalculatorService.calculateFare(ticket);
			assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
		} catch (AssertionError ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	public void testCalculateFareCarWithLessThanOneHourParkingTime() {
		try {
			Date inTime = new Date();
			// 45 minutes parking time should give 3/4th parking fare
			inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));
			Date outTime = new Date();
			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

			ticket.setInTime(inTime);
			ticket.setOutTime(outTime);
			ticket.setParkingSpot(parkingSpot);
			fareCalculatorService.calculateFare(ticket);
			assertEquals((0.75 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
		} catch (AssertionError ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	public void testCalculateFareCarWithLessThan30minutesParkingTime() {
		try {
			Date inTime = new Date();
			// 30 minutes parking time should give 1/2th parking fare
			inTime.setTime(System.currentTimeMillis() - (30 * 60 * 1000));
			Date outTime = new Date();
			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

			ticket.setInTime(inTime);
			ticket.setOutTime(outTime);
			ticket.setParkingSpot(parkingSpot);
			fareCalculatorService.calculateFare(ticket);
			assertEquals((0.0 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
		} catch (AssertionError ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	public void testCalculateFareBikeWithLessThan30minutesParkingTime() {
		try {
			Date inTime = new Date();
			// 30 minutes parking time should give 1/2th parking fare
			inTime.setTime(System.currentTimeMillis() - (30 * 60 * 1000));
			Date outTime = new Date();
			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

			ticket.setInTime(inTime);
			ticket.setOutTime(outTime);
			ticket.setParkingSpot(parkingSpot);
			fareCalculatorService.calculateFare(ticket);
			assertEquals((0.0 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
		} catch (AssertionError ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	public void testCalculateFareCarWithMoreThanADayParkingTime() {
		try {
			Date inTime = new Date();
			// 24 hours parking time should give 24 * parking fare per hour
			inTime.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000));
			Date outTime = new Date();
			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

			ticket.setInTime(inTime);
			ticket.setOutTime(outTime);
			ticket.setParkingSpot(parkingSpot);
			fareCalculatorService.calculateFare(ticket);
			assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
		} catch (AssertionError ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	public void testCalculateFareCarWithDiscount() {
		try {
			Date inTime = new Date();
			// 45 minutes parking time should give 3/4th parking fare
			inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));
			Date outTime = new Date();
			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
			double ticketCarDiscount;

			ticket.setInTime(inTime);
			ticket.setOutTime(outTime);
			ticket.setParkingSpot(parkingSpot);

			fareCalculatorService.calculateFare(ticket);
			ticketCarDiscount = 0.95 * (ticket.getPrice());
			fareCalculatorService.calculateFare(ticket, true);
			assertEquals(ticketCarDiscount, ticket.getPrice());
		} catch (AssertionError ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	public void testCalculateFareBikeWithDiscount() {
		try {
			Date inTime = new Date();
			// 45 minutes parking time should give 3/4th parking fare
			inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));
			Date outTime = new Date();
			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
			double ticketBikeDiscount;

			ticket.setInTime(inTime);
			ticket.setOutTime(outTime);
			ticket.setParkingSpot(parkingSpot);
			fareCalculatorService.calculateFare(ticket);
			ticketBikeDiscount = 0.95 * (ticket.getPrice());
			fareCalculatorService.calculateFare(ticket, true);
			assertEquals(ticketBikeDiscount, ticket.getPrice());
		} catch (AssertionError ex) {
			fail(ex.getMessage());
		}
	}
}