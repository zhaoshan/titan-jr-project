package test.fangcang.account;

import org.junit.Before;
import org.junit.Test;

import test.fangcang.GenericTest;

import com.fangcang.titanjr.dto.request.BankCardRequest;
import com.fangcang.titanjr.service.TitanFinancialAccountService;
import com.fangcang.titanjr.service.TitanFinancialBankCardService;
import com.fangcang.titanjr.service.TitanFinancialRateService;
import com.fangcang.titanjr.service.TitanFinancialTradeService;
import com.fangcang.titanjr.service.TitanFinancialUserService;
import com.fangcang.titanjr.web.util.WebConstant;

public class TitanAccountTest extends GenericTest{

	private TitanFinancialAccountService titanFinancialAccountService = null;
	
	private TitanFinancialBankCardService titanFinancialBankCardService= null;

	private TitanFinancialRateService titanFinancialRateService=null;
    @Before
    public void init(){
    	titanFinancialAccountService = (TitanFinancialAccountService)cfx.getBean("titanFinancialAccountService");
    	titanFinancialBankCardService=(TitanFinancialBankCardService)cfx.getBean("titanFinancialBankCardService");
    	titanFinancialRateService = (TitanFinancialRateService)cfx.getBean("titanFinancialRateService");
    }
    
//    @Test
    public void testAccountBankCard(){
    	try{
    		BankCardRequest bankCardRequest = new BankCardRequest();
        	bankCardRequest.setUserId("TJM10000108");
        	bankCardRequest.setStatus(WebConstant.BANKCARD_SUCCESS);
        	bankCardRequest.setAccountproperty(WebConstant.ACCOUNT_PUBLIC);
        	titanFinancialBankCardService.queryBankCardDTO(bankCardRequest);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    // @Test
    public void test(){
    	titanFinancialRateService.initRateData();
    }
    
    
    @Test
    public void testAccountBankCardBatch(){
    	unFreezeAllTransOrder();
    	
    }
    
    
	private void unFreezeAllTransOrder(){
		try {
			int offset=0;
			int row =100;
			do{
				 row = titanFinancialAccountService.unFreezeOrder(offset,row);
				 offset = (offset+1)*row;
			}while(row>0);
			
		} catch (Exception e) {
		}
	}
	
	
    
}
