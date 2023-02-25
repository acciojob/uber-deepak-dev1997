package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		Customer customer = customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);

	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		TripBooking tripBooking = new TripBooking();
		Driver driver = null;

		List<Driver> allDrivers = driverRepository2.findAll();

		for(Driver d : allDrivers){

			if(d.getCab().getAvailable()==Boolean.TRUE){
				if((driver ==null) || (d.getDriverId() < driver.getDriverId())){
					driver = d;
				}
			}
		}
		if(driver==null){
			throw new Exception("No cab available!");
		}

		Customer customer = customerRepository2.findById(customerId).get();
		tripBooking.setCustomer(customer);
		tripBooking.setDriver(driver);
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);

		int ratePerKm = driver.getCab().getPerKmRate();
		tripBooking.setBill(distanceInKm*10);
		tripBooking.setStatus(TripStatus.CONFIRMED);

		driver.getCab().setAvailable(false);

		customer.getTripBookingList().add(tripBooking);
		customerRepository2.save(customer);

		driver.getTripBookingList().add(tripBooking);
		driverRepository2.save(driver);


		return tripBooking;


	}

	@Override
	public void cancelTrip(Integer tripId){

		TripBooking tripBooking =  tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBooking.setBill(0);
		tripBooking.getDriver().getCab().setAvailable(true);

		tripBookingRepository2.save(tripBooking);


	}

	@Override
	public void completeTrip(Integer tripId){

		TripBooking tripBooking =  tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);

		int bill = tripBooking.getDriver().getCab().getPerKmRate()*tripBooking.getDistanceInKm();
		tripBooking.setBill(bill);
		tripBooking.getDriver().getCab().setAvailable(true);

		tripBookingRepository2.save(tripBooking);

	}
}