package com.parkit.parkingsystem.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
	private static final Logger logger = LogManager.getLogger("FareCalculatorService");
	private long inHour;
	private long outHour;
	private double duration;
	private double rateHourOf30minutes;
	private boolean isDurationLessThan30minutes;

	public void calculateFare(Ticket ticket) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		// get time in milliseconds for vehicle entrance and vehicle exit
		inHour = ticket.getInTime().getTime();
		outHour = ticket.getOutTime().getTime();
		calculateDurationOfParking(inHour, outHour);
		duration = getDurationOfParking();

		rateHourOf30minutes = 0.50;
		isDurationLessThan30minutes = duration <= rateHourOf30minutes;
		logger.debug("duration------------ {} ", duration);

		// if the duration is less or equal to 30 minutes: rounds down the duration the
		// rate of hour of the duration , so 0.0
		if (isDurationLessThan30minutes) {
			duration = Math.floor(duration);
			logger.debug("duration less than 30 min bike and car reduced to {} ", duration);
		}

		switch (ticket.getParkingSpot().getParkingType()) {
		case CAR: {
			ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
			logger.debug("ticket price car {} ", ticket.getPrice());
			break;
		}
		case BIKE: {
			ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
			logger.debug("ticket price bike {} ", ticket.getPrice());
			break;
		}
		default:
			throw new IllegalArgumentException("Unkown Parking Type");
		}
		calculateFare(ticket, false);
	}

	public void calculateDurationOfParking(long inHour, long outHour) {
		double durationOfParking = (double) outHour - inHour;
		// convert duration milliseconds in rate hour
		durationOfParking = durationOfParking / 1000 / 60 / 60;
		this.duration = durationOfParking;
	}

	public double getDurationOfParking() {
		return duration;
	}

	public void calculateFare(Ticket ticket, boolean discount) {
		if (discount) {
			double ticketDiscountPrice = 0.95 * ticket.getPrice();
			ticket.setPrice(ticketDiscountPrice);
			logger.debug("ticket price discount {} ", ticket.getPrice());
		}
	}
}