package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;
import java.util.Calendar;

public class FareCalculatorService {
	private Ticket ticket;
	private long inHour;
	private long outHour;
	private double duration;
	private double rateHourOf30minutes;
	private boolean isDurationLessThan30minutes;

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        // get time in milliseconds for vehicle entrance and vehicle exit 
        inHour = ticket.getInTime().getTime(); 
        outHour = ticket.getOutTime().getTime();
        duration = calculateDurationOfParking(inHour, outHour, duration);    
         
        // define rate of hour for 30 minutes
        rateHourOf30minutes = 0.5;
        // check if the duration is less or equal to 30 minutes and return boolean type true or false
        isDurationLessThan30minutes = duration <= rateHourOf30minutes; 
        System.out.println("duration------------ " + duration);
        /* if the duration is less or equal to 30 minutes: 
        rounds down the duration the rate of hour of the duration , so 0.0 */
        if (isDurationLessThan30minutes) {
        	 duration = Math.floor(duration);  
             System.out.println("duration less than 30 min bike and car reduced to " + duration);     
        }
       
     
        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                System.out.println("ticket price car " + ticket.getPrice());
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                System.out.println("ticket price bike " + ticket.getPrice());
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
        
        if (!isDurationLessThan30minutes) {
        	calculateFare(ticket, false);
        }
        	   	
        
       
    }
    
    public double calculateDurationOfParking(long inHour, long outHour, double duration) {
    	// TODO: Some tests are failing here. Need to check if this logic is correct
        duration = outHour - inHour;
        // convert duration milliseconds in rate hour
        duration = duration / 1000 / 60 / 60;      
        return duration;    
    }

	public void calculateFare(Ticket ticket, boolean discount) {
		// TODO Auto-generated method stub
		boolean hasTicketDiscount = discount;
		double ticketDiscountPrice = hasTicketDiscount ? 0.95 * ticket.getPrice() : ticket.getPrice();
		ticket.setPrice(ticketDiscountPrice);
		System.out.println("ticket price discount" + ticket.getPrice());
		/*if (hasTicketDiscount) {
			0.95 * ticket.getPrice();
		}*/
		
		
	}  
}