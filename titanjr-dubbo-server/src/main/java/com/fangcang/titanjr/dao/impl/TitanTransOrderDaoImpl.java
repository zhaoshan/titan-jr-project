package com.fangcang.titanjr.dao.impl;

import java.util.List;

import com.fangcang.corenut.dao.PaginationSupport;
import com.fangcang.corenut.dao.impl.GenericDAOMyBatisImpl;
import com.fangcang.exception.DaoException;
import com.fangcang.titanjr.dao.TitanTransOrderDao;
import com.fangcang.titanjr.dto.bean.RefundDTO;
import com.fangcang.titanjr.dto.bean.RepairTransferDTO;
import com.fangcang.titanjr.dto.bean.TransOrderDTO;
import com.fangcang.titanjr.dto.request.RepairTransferRequest;
import com.fangcang.titanjr.dto.request.TransOrderRequest;
import com.fangcang.titanjr.entity.TitanTransOrder;
import com.fangcang.titanjr.entity.parameter.TitanTransOrderParam;
import com.fangcang.util.StringUtil;

public class TitanTransOrderDaoImpl extends GenericDAOMyBatisImpl implements TitanTransOrderDao {
    @Override
    public boolean selectForPage(TitanTransOrderParam condition,
                                 PaginationSupport<TitanTransOrder> paginationSupport)
            throws DaoException {
        try {
            super.selectForPage("com.fangcang.titanjr.dao.TitanTransOrderDao.queryList", condition, paginationSupport);
        } catch (Exception e) {
            throw new DaoException(e);
        }
        return true;
    }


    @Override
    public boolean selectOrderForPage(TitanTransOrderParam condition, PaginationSupport<TitanTransOrder> paginationSupport)
            throws DaoException {
        try {
            if("1".equals(condition.getStatus())){//付款
        	
        		super.selectForPage("com.fangcang.titanjr.dao.TitanTransOrderDao.queryPayOrder", condition, paginationSupport);
        	
        	}else if("2".equals(condition.getStatus())){//收款
        	
        		super.selectForPage("com.fangcang.titanjr.dao.TitanTransOrderDao.queryGatheringOrder", condition, paginationSupport);
        	
        	}else if("3".equals(condition.getStatus())){//充值
        	
        		super.selectForPage("com.fangcang.titanjr.dao.TitanTransOrderDao.queryRechargeOrder", condition, paginationSupport);
        		
        	}else if("4".equals(condition.getStatus())){//提现
        		
        		super.selectForPage("com.fangcang.titanjr.dao.TitanTransOrderDao.queryWithdrawOrder", condition, paginationSupport);
        		
        	}else if("5".equals(condition.getStatus())){//冻结记录
        		
        		super.selectForPage("com.fangcang.titanjr.dao.TitanTransOrderDao.queryFreezeOrder", condition, paginationSupport);
        		
        	}else{
        		super.selectForPage("com.fangcang.titanjr.dao.TitanTransOrderDao.queryAllOrder", condition, paginationSupport);
        	}
           
        } catch (Exception e) {
            throw new DaoException(e);
        }
        return true;
    }

    @Override
    public boolean selectTransLoanForPage(TitanTransOrderParam condition, PaginationSupport<TitanTransOrder> paginationSupport)
            throws DaoException {
        try {
            super.selectForPage("com.fangcang.titanjr.dao.TitanTransOrderDao.queryTransLoanOrder", condition, paginationSupport);
        } catch (Exception e) {
            throw new DaoException(e);
        }
        return true;
    }

    @Override
    public int insert(TitanTransOrder entity) throws DaoException {
        try {
            return super.insertEntity("com.fangcang.titanjr.dao.TitanTransOrderDao.insertEntity", entity);
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    @Override
    public int update(TitanTransOrder entity) throws DaoException {
        try {
        	if(entity.getTransid()!=null || StringUtil.isValidString(entity.getUserorderid())||
        			StringUtil.isValidString(entity.getOrderid())){
        		 return super.updateEntity("com.fangcang.titanjr.dao.TitanTransOrderDao.updateEntity", entity);
        	}
        } catch (Exception e) {
            throw new DaoException(e);
        }
        return 0;
    }

    @Override
    public List<TransOrderDTO> selectTitanTransOrder(TransOrderRequest transOrderRequest) throws DaoException {
        try {
            return super.selectList("com.fangcang.titanjr.dao.TitanTransOrderDao.selectTitanTransOrder", transOrderRequest);
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    @Override
    public List<TransOrderDTO> selectTitanTransOrderLock(TransOrderRequest transOrderRequest) throws DaoException {
        try {
            return super.selectList("com.fangcang.titanjr.dao.TitanTransOrderDao.selectTitanTransOrderLock", transOrderRequest);
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

	@Override
	public List<RepairTransferDTO> queryTitanTransOrderByStatus(RepairTransferRequest repairTransferRequest){
		 try {
	            return super.selectList("com.fangcang.titanjr.dao.TitanTransOrderDao.queryOrderByTypeAndStatus", repairTransferRequest);
	        } catch (Exception e) {
	            throw new DaoException(e);
	        }
	}


	@Override
	public int updateTitanTransOrderByTransId(TitanTransOrder entity) throws DaoException {
		try {
        	 return super.updateEntity("com.fangcang.titanjr.dao.TitanTransOrderDao.updateTitanTransOrderByTransId", entity);
        } catch (Exception e) {
            throw new DaoException(e);
        }
	}


	@Override
	public List<RefundDTO> selectTransRefundOrder(
			TransOrderRequest transOrderRequest) throws DaoException {
		  try {
	           return super.selectList("com.fangcang.titanjr.dao.TitanTransOrderDao.selectTitanTransOrderAndRefund",transOrderRequest);
	        } catch (Exception e) {
	            throw new DaoException(e);
	        }
	}
	
	public List<String> queryTransOrderStatus(String orderNo){
		try {
       	 return super.selectList("com.fangcang.titanjr.dao.TitanTransOrderDao.confirmOrderStatus", orderNo);
       } catch (Exception e) {
           throw new DaoException(e);
       }
	}
}