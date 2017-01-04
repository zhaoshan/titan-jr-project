package com.fangcang.titanjr.dao;

import com.fangcang.corenut.dao.PaginationSupport;
import com.fangcang.exception.DaoException;
import com.fangcang.titanjr.entity.TitanBankcard;
import com.fangcang.titanjr.entity.parameter.TitanBankcardParam;

public interface TitanBankcardDao {
	boolean selectForPage(TitanBankcardParam condition, PaginationSupport<TitanBankcard> paginationSupport) throws DaoException;
	int insert(TitanBankcard entity) throws DaoException;
	int update(TitanBankcard entity) throws DaoException;
}