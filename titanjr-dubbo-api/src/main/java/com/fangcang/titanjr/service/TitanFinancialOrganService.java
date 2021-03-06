package com.fangcang.titanjr.service;


import java.util.List;

import com.fangcang.titanjr.common.exception.GlobalServiceException;
import com.fangcang.titanjr.common.exception.MessageServiceException;
import com.fangcang.titanjr.dto.BaseResponseDTO;
import com.fangcang.titanjr.dto.bean.OrgBindInfo;
import com.fangcang.titanjr.dto.bean.OrgBindInfoDTO;
import com.fangcang.titanjr.dto.bean.OrgDTO;
import com.fangcang.titanjr.dto.bean.TitanOpenOrgDTO;
import com.fangcang.titanjr.dto.request.CancelOrgBindRequest;
import com.fangcang.titanjr.dto.request.FinancialOrganQueryRequest;
import com.fangcang.titanjr.dto.request.GetCheckCodeRequest;
import com.fangcang.titanjr.dto.request.OrgBaseInfoRequest;
import com.fangcang.titanjr.dto.request.OrgRegisterValidateRequest;
import com.fangcang.titanjr.dto.request.OrgSubRequest;
import com.fangcang.titanjr.dto.request.OrgUpdateRequest;
import com.fangcang.titanjr.dto.request.OrganBindRequest;
import com.fangcang.titanjr.dto.request.OrganCheckRequest;
import com.fangcang.titanjr.dto.request.OrganFreezeRequest;
import com.fangcang.titanjr.dto.request.OrganImageRequest;
import com.fangcang.titanjr.dto.request.OrganImageUploadRequest;
import com.fangcang.titanjr.dto.request.OrganRegisterRequest;
import com.fangcang.titanjr.dto.request.OrganRegisterUpdateRequest;
import com.fangcang.titanjr.dto.request.RegOrgSubRequest;
import com.fangcang.titanjr.dto.request.UpdateCheckCodeRequest;
import com.fangcang.titanjr.dto.request.VerifyCheckCodeRequest;
import com.fangcang.titanjr.dto.response.FinancialOrganResponse;
import com.fangcang.titanjr.dto.response.GetCheckCodeResponse;
import com.fangcang.titanjr.dto.response.OrgRegisterValidateResponse;
import com.fangcang.titanjr.dto.response.OrganBindResponse;
import com.fangcang.titanjr.dto.response.OrganBriefResponse;
import com.fangcang.titanjr.dto.response.OrganCheckResponse;
import com.fangcang.titanjr.dto.response.OrganFreezeResponse;
import com.fangcang.titanjr.dto.response.OrganImageResponse;
import com.fangcang.titanjr.dto.response.OrganImageUploadResponse;
import com.fangcang.titanjr.dto.response.OrganQueryCheckResponse;
import com.fangcang.titanjr.dto.response.OrganRegisterResponse;
import com.fangcang.titanjr.dto.response.OrganRegisterUpdateResponse;
import com.fangcang.titanjr.dto.response.VerifyCheckCodeResponse;
import com.fangcang.titanjr.entity.TitanOrgSub;

/**
 * 金服平台机构服务
 * 查询注册机构
 * 审核机构
 * 上传机构所需图片等
 * Created by zhaoshan on 2016/3/30.
 */
public interface TitanFinancialOrganService {

    /**
     * 查询机构信息,结果返回封装机构信息列表（有绑定关系）
     * 可以只返回一条记录，个人和企业机构根据类型来查询，
     * 以房仓本地数据库数据为准,查询条件需有审核状态。
     * 查询出的数据应该包括机构基础信息，审核状态，机构图片地址，机构绑定状态四部分信息
     * @param titanOrgQueryDTO
     * @deprecated Insteaded, use the queryBaseFinancialOrgan(FinancialOrganQueryRequest)
     * @see queryBaseFinancialOrgan(FinancialOrganQueryRequest)
     * @return
     */
	@Deprecated
    public FinancialOrganResponse queryFinancialOrgan(FinancialOrganQueryRequest organQueryRequest);
    /***
     * 查询机构信息
     * @param organQueryRequest
     * @return
     */
    public FinancialOrganResponse queryBaseFinancialOrgan(FinancialOrganQueryRequest organQueryRequest);
    
    /**
     * 分页查询结构信息
     * 待确认分页实现方式
     * @param titanOrgQueryDTO
     * @return
     */
    public OrganQueryCheckResponse queryFinancialOrganForPage(FinancialOrganQueryRequest organQueryRequest);


    /**
     * 注册泰坦金服机构信息，需要初审，是一个保存并更新类型的接口（新注册，或者初审失败修改资料均使用此接口）
     * 当前角色会放在session中，需根据session中的角色判定是否有开通权限
     * 01.SaaS已注册机构用户使用金服账号登录：不支持
     * 02.SaaS已注册机构用户使用SaaS账号登录：session中需写入当前商家，读取绑定关系并写入session，首页，添加用户页面需根据当前身份来展示。
     * 03.官网已注册机构用户使用金服账号登录：session中需写入当前商家，并不读取绑定关系信息，首页，添加用户页面需根据当前身份来展示。
     * 包含的最基本步骤：
     * 1.注册员工,调用TitanFinancialUserService中registerFinancialUser
     * 2.图片上传
     * 3.金服添加机构
     * 4.金服添加机构绑定关系
     * 5.进行审核，直接审核通过
     * 分三种情况：
     * 第一种：SaaS页面注册机构以及新增员工时候使用1（第一种）,2,3,4
     * 第二种：金服官网注册机构以及新增员工时候使用1（第二种）,2,3
     * 第三种：后台自动注册机构时添加员工使用1（第三种）,3,5
     * @param organRegisterRequest
     * @return
     */
    public OrganRegisterResponse registerFinancialOrgan(OrganRegisterRequest organRegisterRequest) throws MessageServiceException, GlobalServiceException;

    /***
     * 注册真实机构并添加机构关联关系
     * @param regOrgSubRequest
     * @param relateOrgCode 虚拟机构编码
     * @return
     * @throws GlobalServiceException
     */
    public OrganRegisterResponse registerOrgSub(RegOrgSubRequest regOrgSubRequest,String relateOrgCode) throws GlobalServiceException,MessageServiceException;

    /**
     * 重新修改机构注册信息，后台初审失败，客户修改后再次发起申请时的接口,修改真实证件资料
     * 1.重新上传图片并修改图片路径
     * 2.金服机构信息修改
     * 3.修改审核记录中审核状态(运营人员修改时不用改变审核状态)
     * @param organRegisterUpdateRequest
     * @return
     */
    public OrganRegisterUpdateResponse reRegisterOrgan(OrganRegisterUpdateRequest organRegisterUpdateRequest) throws GlobalServiceException;
    
//    /**
//     * 修改机构信息，不需要修改机构状态
//     * @param updateOrganRequest
//     * 1.重新上传图片并修改图片路径
//     * 2.金服机构信息修改
//     * @throws GlobalServiceException
//     */
//    public UpdateOrgaResponse updateOrg(UpdateOrgaRequest updateOrgaRequest) throws GlobalServiceException;
    /**
     * 检查机构信息是否已经注册
     * @param request
     * @return  code = 500 ,该证件已经注册过
     * @throws MessageServiceException
     */
    public OrgRegisterValidateResponse validateOrg(OrgRegisterValidateRequest request) ;
   /**
     * 审核机构账户，此接口相当于审核状态修改接口
     * 后台运营员的审核====不存在时插入////////初审失败合作方修改后重新发起审核====存在时更新
     * 1.插入或更新审核记录（存在时更新，不存在时插入）
     * 2.若审核通过，调用融数接口创建账户
     * 3.房仓系统添加账户（account表）
     * 4.查询同步更新账户余额
     * 手动注册：金服官网和SaaS页面注册的使用1,2,3,4
     * 自动注册：分销商付款之前的注册使用1,2,3,4
     * @param organCheckRequest
     * @return
     */
    public OrganCheckResponse checkFinancialOrgan(OrganCheckRequest organCheckRequest) throws GlobalServiceException,MessageServiceException;

    /**
     * 上传机构账户所需图片信息
     * 1.图片划分成多种尺寸
     * 2.将1中各尺寸图片保存于FTP或者FastDFS中
     * 3.数据库中保存对应的路径信息
     * @param organImageUploadRequest
     * @return
     */
    public OrganImageUploadResponse uploadFinancialOrganImages(OrganImageUploadRequest organImageUploadRequest) throws GlobalServiceException;
    /**
     * 机构照片
     * @param request
     * @return
     */
    public OrganImageResponse getOrganImage(OrganImageRequest request); 

    /**
     * 冻结或解冻机构的接口
     * 冻结机构时：
     * 1.将房仓此机构状态在数据库修改为失效
     * 2.修改此机构和房仓商家的绑定关系状态为失效（若存在）
     * 3.调用融数接口，将机构状态设置为失效
     * 解冻时执行完全相反的操作
     * @param organFreezeRequest
     * @return
     */
    public OrganFreezeResponse freezeFinancialOrgan(OrganFreezeRequest organFreezeRequest) throws GlobalServiceException;

    /**
     * 将房仓商家和机构绑定或解绑的接口
     * 绑定之前需判定是否允许操作
     * 1.查找到金服机构超级管理员
     * 2.金服添加超级管理员用户绑定关系
     * 3.SaaS被绑定的员工绑定权限
     * 4.金服添加机构绑定关系
     * @param organBindRequest
     * @return
     */
    public OrganBindResponse bindFinancialOrgan(OrganBindRequest organBindRequest) throws MessageServiceException,GlobalServiceException;


    /**
     * 查询商家绑定信息
     * @param orgBindInfo
     * @return
     */
    public OrgBindInfo queryOrgBindInfoByUserid(OrgBindInfo orgBindInfo);
    
    
    /**
     * 查询有效账户的绑定信息
     * @param orgBindInfo
     * @return
     */
    public OrgBindInfo queryActiveOrgBindInfo(OrgBindInfo orgBindInfo);
    
    /**
     * 根据给出的任意条件查询商家绑定信息
     * @author Jerry
     * @date 2017年8月3日 上午10:00:27
     * @param orgBindInfo
     * @return
     */
    public OrgBindInfo queryOrgBindInfo(OrgBindInfo orgBindInfo);
    
    /**
     * 获取或者生成验证码
     * @param getCheckCodeRequest
     * @return
     */
    public GetCheckCodeResponse getCheckCode(GetCheckCodeRequest getCheckCodeRequest) throws GlobalServiceException;
    
    /**
     * 校验验证码
     * @param verifyCheckCodeRequest
     * @return
     */
    public VerifyCheckCodeResponse verifyCheckCode(VerifyCheckCodeRequest verifyCheckCodeRequest);
    
    /**
     * 使用验证码
     * @param updateCheckCodeRequest
     * @return
     */
    public BaseResponseDTO useCheckCode(UpdateCheckCodeRequest updateCheckCodeRequest) throws  GlobalServiceException;
    /**
     * 查询虚拟机构信息
     * @param orgDTO
     * @return
     */
    public OrgDTO queryOrg(OrgDTO orgDTO);
    
    /**
     * 获取
     * @return
     */
    public TitanOrgSub getOrgSub(OrgSubRequest orgSubRequest);
    /**
     * 机构总数
     * @return
     */
    public int countOrg(FinancialOrganQueryRequest financialOrganQueryRequest);
    /**
     * 修改机构信息
     * @param orgUpdateRequest
     * @return
     * @throws GlobalServiceException
     */
    public BaseResponseDTO updateOrg(OrgUpdateRequest orgUpdateRequest)throws GlobalServiceException;
    
    
   	/**
   	 * 修改机构信息,如果是证件信息，就同时修改融数的
   	 * @param orgBaseInfoRequest
   	 * @return
   	 * @throws GlobalServiceException
   	 */
    public BaseResponseDTO updateOrgBaseInfo(OrgBaseInfoRequest orgBaseInfoRequest)throws MessageServiceException;
    
    
    /**
     * 查询对方
     * @param orgUpdateRequest
     * @return
     * @throws GlobalServiceException
     */
    public OrganBriefResponse queryOrganBriefByUserId(FinancialOrganQueryRequest organQueryRequest);
    
    /**
     * 查询秘钥
     * @param userId
     * @return
     */
    public TitanOpenOrgDTO queryTitanOpenOrgDTO(String userId); 
    
    /**
     * 获取绑定的机构信息
     * @param orgBindDTO
     * @return
     */
    public List<OrgBindInfoDTO> queryOrgBindInfoDTO(OrgBindInfoDTO orgBindDTO);
    
    /***
     * 解除金融机构关联
     */
    public BaseResponseDTO cancelOrgBind(CancelOrgBindRequest cancelOrgBindRequest) throws MessageServiceException;;
    
    /**
     * 初始化旧的机构，生成新的真实机构
     */
    public void fixOldOrg();
}
