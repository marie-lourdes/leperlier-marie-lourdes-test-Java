package com.parkit.parkingsystem.integration.service;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Date;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;

public class DataBasePrepareService {

    DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    public void clearDataBaseEntries(){
        Connection connection = null;
        
        try{
            connection = dataBaseTestConfig.getConnection();
           
            //set parking entries to available
           connection.prepareStatement("update parking set available = true").execute();
          // connection.prepareStatement("insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(1,'ABCDEF',0,'2023-07-25T13:00:00',null)").execute();
           
            connection.prepareStatement("truncate table ticket").execute();
          
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }
   public void simulateInTimeDataBaseEntries(){
        Connection connection = null;
        try{
            connection = dataBaseTestConfig.getConnection();
Date dateInTime = new Date(System.currentTimeMillis() - 60*60*1000);
            Timestamp dateTimeStamps = new Timestamp(dateInTime.getTime());
         /*   Date dateInTime = new Date(System.currentTimeMillis() - 60*60*1000);
            Timestamp dateTimeStamps = new Timestamp(dateInTime.getTime());*/
            
        	connection.prepareStatement("insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, IN_TIME, OUT_TIME) values(2,'ABCDEF','"+dateTimeStamps+"',null)").execute();
          
           // connection.prepareStatement("update ticket set OUT_TIME='"+dateTimeStamps+"'").execute();

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }


}
