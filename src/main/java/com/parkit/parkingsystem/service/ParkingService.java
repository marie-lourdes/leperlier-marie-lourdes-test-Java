package com.parkit.parkingsystem.service;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;

public class ParkingService {
	private static final Logger logger = LogManager.getLogger("ParkingService");
	private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

	private InputReaderUtil inputReaderUtil;
	private ParkingSpotDAO parkingSpotDAO;
	private TicketDAO ticketDAO;
	private double shortDoubleTicketPrice;
	private double duration;
	private int ticketsPerVehicle;

	public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
		this.inputReaderUtil = inputReaderUtil;
		this.parkingSpotDAO = parkingSpotDAO;
		this.ticketDAO = ticketDAO;
	}

	public void processIncomingVehicle() {
		try {
			ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
			if (parkingSpot != null && parkingSpot.getId() > 0) {
				String vehicleRegNumber = getVehicleRegNumber();
				parkingSpot.setAvailable(false);
				parkingSpotDAO.updateParking(parkingSpot);// allot this parking space and mark it's availability as
															// false

				Date inTime = new Date();
				Ticket ticket = new Ticket();
				// ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
				ticket.setParkingSpot(parkingSpot);
				ticket.setVehicleRegNumber(vehicleRegNumber);
				ticket.setPrice(0);
				ticket.setInTime(inTime);
				ticket.setOutTime(null);
				ticketDAO.saveTicket(ticket);

				// Get number of ticket and display message if the vehicle is already registered
				ticketsPerVehicle = ticketDAO.getNbTicket(vehicleRegNumber);
				logger.debug("ticketsPerVehicle process incoming vehicle {} ", ticketsPerVehicle);
				duration = fareCalculatorService.getDurationOfParking();
				if (ticketsPerVehicle > 1 && duration > 0.5) {
					logger.info("Heureux de vous revoir ! En tant qu’utilisateur régulier de\r\n"
							+ "notre parking, vous allez obtenir une remise de 5%");
				}

				logger.info("Generated Ticket and saved in DB");
				logger.info("Please park your vehicle in spot number: {} ", parkingSpot.getId());
				logger.info("Recorded in-time for vehicle number: {} is: {} ", vehicleRegNumber, inTime);
			}
		} catch (Exception e) {
			logger.error("Unable to process incoming vehicle", e);
		}
	}

	public String getVehicleRegNumber() {
		try {
			logger.info("Please type the vehicle registration number and press enter key");
			return inputReaderUtil.readVehicleRegistrationNumber();
		} catch (Exception e) {
			logger.error("Unable to get vehicle registration number", e);
		}
		return null;
	}

	public ParkingSpot getNextParkingNumberIfAvailable() {
		int parkingNumber = 0;
		ParkingSpot parkingSpot = null;
		try {
			ParkingType parkingType = getVehicleType();
			parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
			if (parkingNumber > 0) {
				parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
			} else {
				throw new NullPointerException("Error fetching parking number from DB. Parking slots might be full");
			}
		} catch (IllegalArgumentException ie) {
			logger.error("Error parsing user input for type of vehicle", ie);
		} catch (Exception e) {
			logger.error("Error fetching next available parking slot", e);
		}
		return parkingSpot;
	}

	public ParkingType getVehicleType() {
		logger.info("Please select vehicle type from menu");
		logger.info("1 CAR");
		logger.info("2 BIKE");
		int input = inputReaderUtil.readSelection();
		switch (input) {
		case 1 -> {
			return ParkingType.CAR;
		}
		case 2 -> {
			return ParkingType.BIKE;
		}
		default -> {
			logger.error("Incorrect input provided");
			throw new IllegalArgumentException("Entered input is invalid");
		}
		}
	}

	public void processExitingVehicle() {
		try {
			String vehicleRegNumber = getVehicleRegNumber();
			Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
			Date outTime = new Date();
			ticket.setOutTime(outTime);

			if (ticketDAO.updateTicket(ticket)) {
				ParkingSpot parkingSpot = ticket.getParkingSpot();
				parkingSpot.setAvailable(true);
				parkingSpotDAO.updateParking(parkingSpot);

				fareCalculatorService.calculateFare(ticket);

				// Apply price discount -5% if the vehicleregNumber is already registered with
				// method calculateFare( ticket, true)
				ticketsPerVehicle = ticketDAO.getNbTicket(vehicleRegNumber);
				logger.debug("ticketPerVehicle : {} ", ticketsPerVehicle);
				duration = fareCalculatorService.getDurationOfParking();
				if (ticketsPerVehicle > 1 && duration > 0.5) {
					fareCalculatorService.calculateFare(ticket, true);
				}

				// round the decimal number of ticketPrice with a maximum of two digits after
				// the decimal point
				// Math.round to round and display in interactive shell the ticket price type
				// Double with 2 number after coma
				double ticketPrice = ticket.getPrice();
				shortDoubleTicketPrice = Math.round(ticketPrice * 100.0) / 100.0;
				ticket.setPrice(shortDoubleTicketPrice);
				ticketDAO.updateTicket(ticket);
				logger.info("Please pay the parking fare: {} ", shortDoubleTicketPrice);
				logger.info("Recorded out-time for vehicle number: {} is: {} ", ticket.getVehicleRegNumber(), outTime);
			} else {
				logger.info("Unable to update ticket information. Error occurred");
			}

		} catch (Exception e) {
			logger.error("Unable to process exiting vehicle", e);
		}
	}
}