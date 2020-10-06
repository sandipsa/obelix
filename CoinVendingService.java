package com.adp.test;

import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

@Service
public class CoinVendingService{

	@Value("${coin-type.qty-mapping}")
	private String coinTypeMapping;

	 private static final String COIN_QUARTERS = "QUARTERS";
	 private static final String COIN_DIMES = "DIMES";
	 private static final String COIN_NICKELS = "NICKELS";
	 private static final String COIN_CENTS = "CENTS";
	
	 private static final  int DOLLARS = 100;             // number of cents in dollar
	 private static final  int  QUARTERS = 25;             // number of cents in quarter
	 private static final  int DIMES = 10;                // number of cents in dime
	 private static final  int  NICKELS = 5;               // number of cents in nickel
	
	 private ConcurrentMap<String, Integer> coinDispenseMap;
	 
	 @PostConstruct
	 public void loadCoins(){
		coinDispenseMap = new ConcurrentHashMap<>();
		String[] coinType = coinTypeMapping.split(";", 0);
		for(String coinAttr: coinType){
			String[] strCoinAttr = coinAttr.split(":");
			coinDispenseMap.put(strCoinAttr[0], Integer.parseInt(strCoinAttr[1]));
		}
	 }
	 
	 public Integer getAvailableCoins(String coinType){
		return coinDispenseMap.get(coinType);
	 }
	 
	 //ad-hoc method to add coins into the machine
	 public Integer loadCoinsForType(String coinType, Integer addQty){
		coinDispenseMap.computeIfPresent(coinType, (key, oldValue) -> oldValue + addQty);
		return coinDispenseMap.get(coinType);
	 }
	 	 	 
	 public int getAvailableBalance(){
		int runningTotal = 0;
		Integer coins = coinDispenseMap.get(COIN_QUARTERS);
		if(coins != null){
			runningTotal = coins.intValue()*QUARTERS;
		}
		coins = coinDispenseMap.get(COIN_DIMES);
		if(coins != null){
			runningTotal = runningTotal + coins.intValue()*DIMES;
		}
		coins = coinDispenseMap.get(COIN_NICKELS);
		
		if(coins != null){
			runningTotal = runningTotal + coins.intValue()*NICKELS;
		}
		
		coins = coinDispenseMap.get(COIN_CENTS);
		
		if(coins != null){
			runningTotal = runningTotal + coins.intValue();
		}
		
		return runningTotal;
	 }
	 
	 public String dispenseCoinsForBills(String dollar){
	 
	    int cents = 0;
		int numQuarters =0,             // number of dollars, quarters
          numDimes =0, numNickels = 0;                // number of dimes, nickels
        int centsLeft = 0;
		Integer qrtr, dim, nickl = null;
		
	    System.out.println("Dollar(s) to Coins Change");
				
		try{
		  cents = Integer.parseInt(dollar);
		}catch(NumberFormatException nfEx){
			 return "Please enter a numeric dollar amount. The value entered : " + dollar;
		}
		
		cents = cents * DOLLARS;
		
		int availableBal = getAvailableBalance();
		System.out.println("Available Balance: " + availableBal);
		
		if(cents <= availableBal){
			qrtr = coinDispenseMap.get(COIN_QUARTERS);
			if(qrtr != null){
				 // compute total quantities of quarter, dimes, nickels, and cents
				 numQuarters = cents/QUARTERS;
				 if(numQuarters > qrtr){
					numQuarters = qrtr;
				 }
				 centsLeft = cents - numQuarters*QUARTERS;
				 
			}
			
			if(centsLeft > 0 ){
				dim = coinDispenseMap.get(COIN_DIMES);
				if(dim != null){
					 
					 numDimes = centsLeft/DIMES;
					 if(numDimes > dim){
						numDimes = dim;
					 }
					 centsLeft = centsLeft - numDimes*DIMES;
				}
				
				if(centsLeft > 0 ){
					nickl = coinDispenseMap.get(COIN_NICKELS);
					if(nickl != null){
						 numNickels = centsLeft/NICKELS;
						 if(numNickels > nickl){
							numNickels = nickl;
						 }
						 centsLeft = centsLeft - numNickels*NICKELS;
					}
				}
			}//End of Quater
			
			//Adjust the coin balance - atomic transaction
			boolean hasCoinDispensed = adjustCoinQuantity(numQuarters, numDimes, numNickels, centsLeft);
			if(!hasCoinDispensed){
				System.out.println("Coin Machine doesn't have sufficient balance to dispense");
				return "Coin Machine doesn't have sufficient balance to dispense";
			}
			
			 // Log resulting number of coins
			  System.out.print("For total dollar of  $" + dollar);
			  System.out.println(" coins dispensed:");
			  
			  System.out.println("#quarters = " + numQuarters);
			  System.out.println("#dimes = " + numDimes);
			  System.out.println("#nickels = " + numNickels);
			  System.out.println("#pennies = " + centsLeft);
		}else{
			 System.out.println("Coin Machine doesn't have sufficient balance to dispense");
		}
		
		StringBuilder sb = new StringBuilder();
		if(numQuarters >0){
			sb.append(" Quarters = " + numQuarters);
			
		}
		
		if(numDimes >0){
			sb.append(", Dimes = " + numDimes);
		}
		if(numNickels >0){
			sb.append(", Nickels = " + numNickels);
		}
		if(centsLeft >0){
			sb.append(", Pennies = " + centsLeft);
		}
		
		return sb.toString();
	 }
	 
	 public boolean adjustCoinQuantity(int numQuarters, int numDimes, int numNickels, int numCents){
		boolean isSuccessful = true;
		coinDispenseMap.computeIfPresent(COIN_QUARTERS, (key, oldValue) -> oldValue - numQuarters);
		coinDispenseMap.computeIfPresent(COIN_DIMES, (key, oldValue) -> oldValue - numDimes);
		coinDispenseMap.computeIfPresent(COIN_NICKELS, (key, oldValue) -> oldValue - numNickels);
		coinDispenseMap.computeIfPresent(COIN_CENTS, (key, oldValue) -> oldValue - numCents);
		
		if(coinDispenseMap.get(COIN_QUARTERS) < 0 || coinDispenseMap.get(COIN_DIMES) < 0 ||
			coinDispenseMap.get(COIN_NICKELS) < 0  || coinDispenseMap.get(COIN_CENTS) < 0 ){
			//Coin qty going in negative. Rollback the transaction
			coinDispenseMap.computeIfPresent(COIN_QUARTERS, (key, oldValue) -> oldValue + numQuarters);
			coinDispenseMap.computeIfPresent(COIN_DIMES, (key, oldValue) -> oldValue + numDimes);
			coinDispenseMap.computeIfPresent(COIN_NICKELS, (key, oldValue) -> oldValue + numNickels);
			coinDispenseMap.computeIfPresent(COIN_CENTS, (key, oldValue) -> oldValue + numCents);
			isSuccessful = false;
		}
		return isSuccessful;
	 }
	 
	 //Utility method to get max qty of coins
	 public String dispenseMaxCoinsForDollar(String dollar){
		  int cents = 0;
		int numQuarters =0,             // number of dollars, quarters
          numDimes =0, numNickels = 0;                // number of dimes, nickels
        int centsLeft = 0;
		Integer qrtr, dim, nickl, penny = null;
		
	    System.out.println("Dollar(s) to Coins Change");
				
		try{
		  cents = Integer.parseInt(dollar);
		}catch(NumberFormatException nfEx){
			 return "Please enter a numeric dollar amount. The value entered : " + dollar;
		}
		
		cents = cents * DOLLARS;
		int availableBal = getAvailableBalance();
		System.out.println("Available Balance: " + availableBal);
		
		if(cents <= availableBal){
			penny = coinDispenseMap.get(COIN_CENTS);
			if(penny != null){
		    	 centsLeft = cents - penny;
			}
			
			if(centsLeft > 0 ){
					nickl = coinDispenseMap.get(COIN_NICKELS);
					if(nickl != null){
						 numNickels = centsLeft/NICKELS;
						 if(numNickels > nickl){
							numNickels = nickl;
						 }
						 centsLeft = centsLeft - numNickels*NICKELS;
					}
				
				if(centsLeft > 0 ){				
					dim = coinDispenseMap.get(COIN_DIMES);
					if(dim != null){
						 
						 numDimes = centsLeft/DIMES;
						 if(numDimes > dim){
							numDimes = dim;
						 }
						 centsLeft = centsLeft - numDimes*DIMES;
					}
					
					qrtr = coinDispenseMap.get(COIN_QUARTERS);
					if(qrtr != null){
						 // compute total quantities of quarter, dimes, nickels, and cents
						 numQuarters = centsLeft/QUARTERS;
						 if(numQuarters > qrtr){
							numQuarters = qrtr;
						 }
						 centsLeft = centsLeft - numQuarters*QUARTERS;
						 
					}
					
					
				}	
			}
			
			if(centsLeft != 0){
					System.out.println("Dispensing excess coins... ABorting");
					return "Coin Machine doesn't have sufficient balance to dispense";
			}
			
			//Adjust the coin balance - atomic transaction
			boolean hasCoinDispensed = adjustCoinQuantity(numQuarters, numDimes, numNickels, penny);
		
			// Log resulting number of coins
			  System.out.print("For total dollar of  $" + dollar);
			  System.out.println(" coins dispensed:");
			  
			  System.out.println("#quarters = " + numQuarters);
			  System.out.println("#dimes = " + numDimes);
			  System.out.println("#nickels = " + numNickels);
			  System.out.println("#pennies = " + penny);
			  
				
		}else{
			 System.out.println("Coin Machine doesn't have sufficient balance to dispense");
		}
		
		StringBuilder sb = new StringBuilder();
		if(numQuarters >0){
			sb.append(" Quarters = " + numQuarters);
			
		}
		
		if(numDimes >0){
			sb.append(", Dimes = " + numDimes);
		}
		if(numNickels >0){
			sb.append(", Nickels = " + numNickels);
		}
		if(centsLeft >0){
			sb.append(", Pennies = " + penny);
		}
		
		return sb.toString();
	 }
	 
	 public static void main(String[] args){
		  int cents = 0;
		int numQuarters =0,             // number of quarters
          numDimes =0, numNickels = 0;  // number of dimes, nickels
        int centsLeft = 0;
		Integer qrtr, dim, nickl = null;
		
		CoinVendingService coinMacine = new CoinVendingService();
		coinMacine.coinDispenseMap = new ConcurrentHashMap<>();
		coinMacine.coinDispenseMap.put("QUARTERS", 100);
		coinMacine.coinDispenseMap.put("DIMES", 100);
		coinMacine.coinDispenseMap.put("NICKELS", 100);
		coinMacine.coinDispenseMap.put("CENTS", 100);
		
		while(true){
			 System.out.println("Dollar(s) to Coins Change");
			  // prompt the user to enter a total number of cents
			 Scanner keyboard = new Scanner(System.in);
             System.out.println("Enter the dollar amount that needs to be changed for(positive integer): ");
			 String dollarCount = keyboard.next();
			 while(!StringUtils.isNumeric(dollarCount)){
				  System.out.println( "Please enter a numeric dollar amount");
				  dollarCount = keyboard.next();
			 }
			 
			 try{
				cents = Integer.parseInt(dollarCount); 
			 }catch(NumberFormatException nfe){
				  System.out.println("Please enter a numeric dollar amount.");
				  continue;
			 }
			 
			 cents = cents * DOLLARS;
			
			int availableBal = coinMacine.getAvailableBalance();
		    System.out.println("Available Balance: " + availableBal);
			
			if(cents <= availableBal){
				qrtr = coinMacine.coinDispenseMap.get(COIN_QUARTERS);
				if(qrtr != null){
					 // compute total quantities of quarter, dimes, nickels, and cents
					 numQuarters = cents/QUARTERS;
					 if(numQuarters > qrtr){
						numQuarters = qrtr;
					 }
					 centsLeft = cents - numQuarters*QUARTERS;
					 
				}
				
				if(centsLeft > 0 ){
					dim = coinMacine.coinDispenseMap.get(COIN_DIMES);
					if(dim != null){
						 
						 numDimes = cents/DIMES;
						 if(numDimes > dim){
							numDimes = dim;
						 }
						 centsLeft = cents - numDimes*DIMES;
					}
					
					if(centsLeft > 0 ){
						nickl = coinMacine.coinDispenseMap.get(COIN_NICKELS);
						if(nickl != null){
							 numNickels = cents/NICKELS;
							 if(numNickels > nickl){
								numNickels = nickl;
							 }
							 centsLeft = cents - numNickels*NICKELS;
						}
					}
				}//End of Quater
				
				coinMacine.adjustCoinQuantity(numQuarters, numDimes, numDimes, centsLeft);
				
			
			 // Log resulting number of coins
			  System.out.print("For total cents of  " + cents);
			  System.out.println(" coins dispensed:");
			  
			  System.out.println("#quarters = " + numQuarters);
			  System.out.println("#dimes = " + numDimes);
			  System.out.println("#nickels = " + numNickels);
			  System.out.println("#pennies = " + centsLeft);
			  if(coinMacine.getAvailableBalance() < 100){
				  break;
			  }
			}else{
				 System.out.println("Coin Machine doesn't have sufficient balance to dispense");
			}
		}
	   
	
		
	 }

}
