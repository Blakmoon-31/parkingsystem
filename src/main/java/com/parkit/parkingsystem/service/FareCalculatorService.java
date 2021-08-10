package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	public void calculateFare(Ticket ticket) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		double inHour = ticket.getInTime().getTime();
		double outHour = ticket.getOutTime().getTime();
		boolean isRegularUser = ticket.isRecurrentUser();

		double duration = (outHour - inHour) / 1000 / 60 / 60;
		duration = Math.round(duration * 100.0) / 100.0;

		if (duration > 0.5) { // parking is due only for more than 30 minutes/half an hour
			switch (ticket.getParkingSpot().getParkingType()) {
			case CAR: {
				if (isRegularUser) { // if user is regular he gets a discount
					double price = (duration * Fare.CAR_RATE_PER_HOUR) * (1 - (Fare.CAR_RECURRENT_DISCOUNT / 100));
					ticket.setPrice(Math.round(price * 100.0) / 100.0);
				} else {
					double price = (duration * Fare.CAR_RATE_PER_HOUR);
					ticket.setPrice(Math.round(price * 100.0) / 100.0);
				}
				break;
			}
			case BIKE: {
				if (isRegularUser) { // if user is regular he gets a discount
					double price = (duration * Fare.BIKE_RATE_PER_HOUR) * (1 - (Fare.BIKE_RECURRENT_DISCOUNT / 100));
					ticket.setPrice(Math.round(price * 100.0) / 100.0);
				} else {
					double price = (duration * Fare.BIKE_RATE_PER_HOUR);
					ticket.setPrice(Math.round(price * 100.0) / 100.0);
				}
				break;
			}
			default:
				throw new IllegalArgumentException("Unkown Parking Type");
			}
		}
	}
}
