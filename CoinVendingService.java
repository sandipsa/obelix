package com.adp.test;

import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.strereotype.Service;
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
			coinDispenseMap.put(strCoinAttr[0], Integer.parseInt(strCoinAttr[1]);
		}
	 }
	 
	 public Integer getAvailableCoins(String coinType){
		return coinDispenseMap.get(coinType);
	 }
	 
	 public Integer reduceCoinQuantity(String coinType, Integer deductQty){
		coinDispenseMap.computeIfPresent(cointType, (key, oldValue) -> oldValue - deductQty);
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
			runningTotal = coins.intValue()*DIMES;
		}
		coins = coinDispenseMap.get(COIN_NICKELS);
		
		if(coins != null){
			runningTotal = coins.intValue()*NICKELS;
		}
		
		coins = coinDispenseMap.get(COIN_CENTS);
		
		if(coins != null){
			runningTotal = coins.intValue();
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
		
		int DollarCount = 0;
		if(!StringUtils.isNumeric(dollar)){
		  return "Please enter a numeric dollar amount";
		}
		
		cents = Interger.parseInt(dollar) * DOLLARS;
		
		int availableBal = getAvailableBalance();
		System.out.println("Available Balance: " + availableBal);
		
		if(cents <= availableBal){
			qtr = coinDispenseMap.get(COIN_QUARTERS);
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
					 
					 numDimes = cents/DIMES;
					 if(numDimes > dim){
						numDimes = dim;
					 }
					 centsLeft = cents - numDimes*DIMES;
				}
				
				if(centsLeft > 0 ){
					nickl = coinDispenseMap.get(COIN_NICKELS);
					if(nickl != null){
						 numNickels = cents/NICKELS;
						 if(numNickels > nickl){
							numNickels = nickl;
						 }
						 centsLeft = cents - numNickels*NICKELS;
					}
				}
			}//End of Quater
			
			reduceCoinQuantity(COIN_QUARTERS, numQuarters);
			reduceCoinQuantity(COIN_DIMES, numDimes);
			reduceCoinQuantity(COIN_NICKELS, numNickels);
			reduceCoinQuantity(COIN_CENTS, centsLeft);
			
			 // Log resulting number of coins
			  System.out.print("For total cents of  " + cents);
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
	 
	 public static void main(String[] args){
		  int cents = 0;
		int numQuarters =0,             // number of dollars, quarters
          numDimes =0, numNickels = 0;                // number of dimes, nickels
        int centsLeft = 0;
		Integer qrtr, dim, nickl = null;
		
		CoinVendingService coinMacine = new CoinVendingService();
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
			 cents = dollarCount * DOLLARS;
			
			int availableBal = getAvailableBalance();
		    System.out.println("Available Balance: " + availableBal);
			
			if(cents <= availableBal){
				qtr = coinDispenseMap.get(COIN_QUARTERS);
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
						 
						 numDimes = cents/DIMES;
						 if(numDimes > dim){
							numDimes = dim;
						 }
						 centsLeft = cents - numDimes*DIMES;
					}
					
					if(centsLeft > 0 ){
						nickl = coinDispenseMap.get(COIN_NICKELS);
						if(nickl != null){
							 numNickels = cents/NICKELS;
							 if(numNickels > nickl){
								numNickels = nickl;
							 }
							 centsLeft = cents - numNickels*NICKELS;
						}
					}
				}//End of Quater
				
				reduceCoinQuantity(COIN_QUARTERS, numQuarters);
				reduceCoinQuantity(COIN_DIMES, numDimes);
				reduceCoinQuantity(COIN_NICKELS, numNickels);
				reduceCoinQuantity(COIN_CENTS, centsLeft);
			
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