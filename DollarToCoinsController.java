package com.adp.test;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.AutoWired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.apache.commons.lang3.StringUtils;

@RestController
public class DollarToCoinsController {
	
	@AutoWired
	private CoinVendingService coinMachine;
	
	@GetMapping("/dispenseCoinsForBills")
	public String dispenseChangeForDollar(@RequestParam(value = "dollar") String dollarAmount) {
		if(!StringUtils.isNumeric(dollarAmount)){
		  return "Please enter a numeric dollar amount";
		}
	 	 String coinDispensed = coinMachine.dispenseCoinsForBills(dollarAmount);
		return "Coins dispensed: " + coinDispensed;
	}
}
