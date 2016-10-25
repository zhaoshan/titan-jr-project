package com.fangcang.titanjr.job.executors;

import java.text.ParseException;
import java.util.Date;

import net.sf.json.JSONSerializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fangcang.titanjr.common.util.DateUtil;
import com.fangcang.titanjr.dto.request.UnFreeBalanceBatchRequest;
import com.fangcang.titanjr.dto.request.UnFreezeRequest;
import com.fangcang.titanjr.dto.response.UnFreezeResponse;
import com.fangcang.titanjr.service.TitanFinancialAccountService;

/**
 * Created by zhaoshan on 2016/6/20.
 */
public class UnFreezeTransOrderRunner implements Runnable {
	
	private static final Log log = LogFactory.getLog(UnFreezeTransOrderRunner.class);
	
	private TitanFinancialAccountService titanFinancialAccountService;
	
	public UnFreezeTransOrderRunner(TitanFinancialAccountService titanFinancialAccountService){
		this.titanFinancialAccountService = titanFinancialAccountService;
	}

    @Override
    public void run() {
    	this.unFreezeAllTransOrder();
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
