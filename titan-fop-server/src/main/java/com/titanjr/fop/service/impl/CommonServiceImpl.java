package com.titanjr.fop.service.impl;


import com.fangcang.exception.ServiceException;
import com.titanjr.fop.request.ExternalSessionGetRequest;
import com.titanjr.fop.response.ExternalSessionGetResponse;
import com.titanjr.fop.service.CommonService;
import org.springframework.stereotype.Component;

/**
 * Created by zhaoshan on 2017/12/22.
 */
@Component
public class CommonServiceImpl implements CommonService {

    @Override
    public ExternalSessionGetResponse getRequestSession(ExternalSessionGetRequest sessionGetRequest) throws ServiceException {

        //参数校验

        //

        return null;
    }
}
