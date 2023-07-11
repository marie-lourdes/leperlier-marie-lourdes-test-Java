package com.parkit.parkingsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

public class TicketDAO {

	private static final Logger logger = LogManager.getLogger("TicketDAO");

	public DataBaseConfig dataBaseConfig = new DataBaseConfig();

	Map<String, Integer> MapOfNumberOfTicketPerVehicle = new HashMap<String, Integer>();

	public boolean saveTicket(Ticket ticket) {
		Connection con = null;
		try {
			con = dataBaseConfig.getConnection();
			PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET);
			// ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
			// ps.setInt(1,ticket.getId());
			ps.setInt(1, ticket.getParkingSpot().getId());
			ps.setString(2, ticket.getVehicleRegNumber());
			ps.setDouble(3, ticket.getPrice());
			ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
			ps.setTimestamp(5, (ticket.getOutTime() == null) ? null : (new Timestamp(ticket.getOutTime().getTime())));
			return ps.execute();
		} catch (Exception ex) {
			logger.error("Error fetching next available slot", ex);
		} finally {
			dataBaseConfig.closeConnection(con);
			return false;
		}
	}

	public Ticket getTicket(String vehicleRegNumber) {
		Connection con = null;
		Ticket ticket = null;
		try {
			con = dataBaseConfig.getConnection();
			PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET);
			// ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
			ps.setString(1, vehicleRegNumber);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ticket = new Ticket();
				ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)), false);
				ticket.setParkingSpot(parkingSpot);
				ticket.setId(rs.getInt(2));
				ticket.setVehicleRegNumber(vehicleRegNumber);
				ticket.setPrice(rs.getDouble(3));
				ticket.setInTime(rs.getTimestamp(4));
				ticket.setOutTime(rs.getTimestamp(5));
			}
			dataBaseConfig.closeResultSet(rs);
			dataBaseConfig.closePreparedStatement(ps);
		} catch (Exception ex) {
			logger.error("Error fetching next available slot", ex);
		} finally {
			dataBaseConfig.closeConnection(con);
			return ticket;
		}
	}

	public boolean updateTicket(Ticket ticket) {
		Connection con = null;
		try {
			con = dataBaseConfig.getConnection();
			PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
			ps.setDouble(1, ticket.getPrice());
			ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
			ps.setInt(3, ticket.getId());
			ps.execute();
			return true;
		} catch (Exception ex) {
			logger.error("Error saving ticket info", ex);
		} finally {
			dataBaseConfig.closeConnection(con);
		}
		return false;
	}

	// Count number of ticket per vehicle registered by its numberplate registered
	public Map<String, Integer> getNbTicket(Ticket ticket, String vehicleRegNumber) {
		ticket = new Ticket();
		Ticket vehicleNumber = getTicket(vehicleRegNumber);
		vehicleNumber.getVehicleRegNumber();
		if (vehicleNumber != null) {
			try {
				for (Map.Entry<String, Integer> vehicleNb : MapOfNumberOfTicketPerVehicle.entrySet()) {
					if (!MapOfNumberOfTicketPerVehicle.containsKey(vehicleRegNumber)) {
						MapOfNumberOfTicketPerVehicle.put(vehicleRegNumber, 1);
					} else {
						Integer val = MapOfNumberOfTicketPerVehicle.get(vehicleRegNumber);
						MapOfNumberOfTicketPerVehicle.put(vehicleRegNumber, val + 1);
					}

				}

				/*
				 * List<Ticket> listOfNumberOfTicketPerVehicle = new ArrayList<Ticket>();
				 * for(Ticket vehicleNb : listOfNumberOfTicketPerVehicle ) {
				 * 
				 * listOfNumberOfTicketPerVehicle.add(vehicleNb);
				 * 
				 * } return listOfNumberOfTicketPerVehicle;
				 */

				

			} catch (NullPointerException e) {
				logger.error(" no ticket registered", e);

			}	
		}
		System.out.println("MapOfNumberOfTicketPerVehicle" + MapOfNumberOfTicketPerVehicle);
		return MapOfNumberOfTicketPerVehicle;
	}
}
