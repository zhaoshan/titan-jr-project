package com.titanjr.checkstand.request;

import com.titanjr.checkstand.dto.GateWayPayDTO;

/**
 * Created by zhaoshan on 2017/11/15.
 */
public class GateWayPayRequest extends BaseRequest {

    //网关支付对象
    private GateWayPayDTO gateWayPayDTO;

    public GateWayPayDTO getGateWayPayDTO() {
        return gateWayPayDTO;
    }

    public void setGateWayPayDTO(GateWayPayDTO gateWayPayDTO) {
        this.gateWayPayDTO = gateWayPayDTO;
    }

    @Override
    public void validate() {

    }
}
