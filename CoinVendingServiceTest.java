package com.adp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJunitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJunitRunner.class)
public class CoinVendingServiceTest{
	@InjectMocks
	private CoinVendingService coinMachine;
	
	private ClassLoader classLoader = null;
	
	@Before
	public void setup() throws Exception{
	  String coinTypeMapping = "QUARTERS:100;DIMES:100;NICKELS:100;CENTS:100";
	  ReflectionTestUtils.setField(coinMachine, "coin-type.qty-mapping", coinTypeMapping);
	  
	  
	  
	}
	
	@Test
	public void testNormalDispense(){
		Scanner fileChunks = new Scanner(new File("src/test/resources/dollarInput1.txt"));
		while(fileChunks.hasNextInt()){
			String msg = coinMachine.dispenseCoinsForBills(fileChunks.next());
			assertEquls("Quarts 80, Dimes 10")
		}
 	}
}