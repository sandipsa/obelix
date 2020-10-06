package com.adp.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJunitRunner;


@RunWith(MockitoJunitRunner.class)
public class CoinVendingServiceTest{
	@InjectMocks
	private CoinVendingService coinMachine;
	
	private ClassLoader classLoader = null;
	
	@Before
	public void setup() throws Exception{
	  String coinTypMapping = "QUARTERS:100;DIMES:100;NICKELS:100;CENTS:100";
	  coinMachine.cointTypeMapping = coinTypMapping;
	  coinMachine.loadCoins();
	}
	
	@Test
	public void testNormalDispense(){
		Scanner fileChunks = new Scanner(new File("src/test/resources/dollarInput1.txt"));
		//while(fileChunks.hasNextInt()){
			String msg = coinMachine.dispenseCoinsForBills(fileChunks.next());
			assertEquls(" Quarts = 40", msg);
			String msg = coinMachine.dispenseCoinsForBills(fileChunks.next());
			assertEquls(" Quarts = 44", msg);
		        String msg = coinMachine.dispenseCoinsForBills(fileChunks.next());
			assertEquls("Coin Machine doesn't have sufficient balance to dispense", msg);
			String msg = coinMachine.dispenseCoinsForBills(fileChunks.next());
			assertEquls(" Quarts = 8", msg);
		
		        String msg = coinMachine.dispenseCoinsForBills(fileChunks.next());
			assertEquls("Please enter a numeric dollar amount. The value entered : ws", msg);
 	}
}
