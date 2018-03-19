/** 
 * CopyRright ©2017 深圳市天下房仓科技有限公司 All Right Reserved.
 * 
 * @fileName AccountDownloadServiceImpl.java
 * @author Jerry
 * @date 2018年3月15日 下午5:17:15  
 */
package com.titanjr.checkstand.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipInputStream;

import javax.annotation.Resource;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.allinpay.ets.client.SecurityUtil;
import com.fangcang.titanjr.common.util.FtpUtil;
import com.fangcang.titanjr.common.util.MD5;
import com.fangcang.titanjr.common.util.httpclient.HttpClient;
import com.fangcang.titanjr.dto.response.FTPConfigResponse;
import com.fangcang.titanjr.service.TitanSysconfigService;
import com.fangcang.util.DateUtil;
import com.fangcang.util.JsonUtil;
import com.fangcang.util.StringUtil;
import com.titanjr.checkstand.constants.PayTypeEnum;
import com.titanjr.checkstand.constants.RSErrorCodeEnum;
import com.titanjr.checkstand.constants.SysConstant;
import com.titanjr.checkstand.dto.GateWayConfigDTO;
import com.titanjr.checkstand.request.TLGatewayPayDownloadRequest;
import com.titanjr.checkstand.request.TLQrCodeDownloadRequest;
import com.titanjr.checkstand.request.TLQrCodePayRequest;
import com.titanjr.checkstand.respnse.RSResponse;
import com.titanjr.checkstand.respnse.TLQrCodeDownloadResponse;
import com.titanjr.checkstand.respnse.TLQrCodePayResponse;
import com.titanjr.checkstand.service.AccountDownloadService;
import com.titanjr.checkstand.util.CommonUtil;
import com.titanjr.checkstand.util.MyHostnameVerifier;
import com.titanjr.checkstand.util.MyX509TrustManager;
import com.titanjr.checkstand.util.SignMsgBuilder;

/**
 * 对账文件下载服务实现
 * @author Jerry
 * @date 2018年3月15日 下午5:17:15  
 */
@Service("accountDownloadService")
public class AccountDownloadServiceImpl implements AccountDownloadService {
	
	private final static Logger logger = LoggerFactory.getLogger(AccountDownloadServiceImpl.class);
	
	private final String resUrl = this.getClass().getResource("/").getPath().replace("classes/", "");
	private final String tmpUrl = System.getProperty("java.io.tmpdir"); //临时目录
	
	@Resource
	private TitanSysconfigService titanSysconfigService;
	

	@Override
	public RSResponse gatewayPayDownload(TLGatewayPayDownloadRequest tlGatewayPayDownloadRequest) {

		RSResponse response = new RSResponse();
		response.setMerchantNo(SysConstant.RS_MERCHANT_NO);
		response.setVersion(SysConstant.RS_VERSION);
		response.setSignType(SysConstant.RS_SIGN_TYPE);
		//String responseStr = "";
		FileWriter fwriter = null;  
		
		try{
			
			String configKey = tlGatewayPayDownloadRequest.getMchtCd() +"_" + PayTypeEnum.AGENT_TRADE.combPayType + 
					"_" + SysConstant.TL_CHANNEL_CODE + "_" + tlGatewayPayDownloadRequest.getRequestType();
			GateWayConfigDTO gateWayConfigDTO = SysConstant.gateWayConfigMap.get(configKey);
			if(gateWayConfigDTO == null){
				logger.error("【通联-网关支付对账文件下载】失败，获取网关配置为空，configKey={}", configKey);
				response.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
				return response;
			}
			tlGatewayPayDownloadRequest.setSignMsg(SignMsgBuilder.getSignMsgForGatewayDoanload(
					tlGatewayPayDownloadRequest, gateWayConfigDTO.getSecretKey()));
			
			String fileAsString = ""; // 签名信息前的对账文件内容
			String fileSignMsg = ""; // 文件签名信息
			boolean isVerified = false; // 验证签名结果
			// 建立连接
			URL url = new URL(gateWayConfigDTO.getGateWayUrl() 
					+ "?mchtCd=" + tlGatewayPayDownloadRequest.getMchtCd() 
					+ "&settleDate=" + tlGatewayPayDownloadRequest.getSettleDate() 
					+ "&signMsg=" + tlGatewayPayDownloadRequest.getSignMsg());
			HttpURLConnection httpConn = this.getHttpsURLConnection(url);
			httpConn.connect();
			// 读取交易结果
			BufferedReader fileReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
			StringBuffer fileBuf = new StringBuffer(); 
			// 签名信息前的字符串
			String lines;
			while ((lines = fileReader.readLine()) != null) {
				if (lines.length() > 0) {
					// 按行读，每行都有换行符
					fileBuf.append(lines + "\r\n");
				} else {
					// 文件中读到空行，则读取下一行为签名信息
					fileSignMsg = fileReader.readLine();
				}
			}
			fileReader.close();
			fileAsString = fileBuf.toString();
			
			// 验证签名：先对文件内容计算 MD5 摘要，再将 MD5 摘要作为明文进行签名验证
			String fileMd5 = SecurityUtil.MD5Encode(fileAsString);
			isVerified = SecurityUtil.verifyByRSA(resUrl+SysConstant.MD5_CER_PATH, fileMd5.getBytes(), Base64.decode(fileSignMsg));
			/*if (isVerified) {
				logger.info("【通联-网关支付对账文件下载】签名验证通过，上传并保存文件数据...");*/
				String fileName = tlGatewayPayDownloadRequest.getSettleDate()+".txt";
				File accountLocal = new File(tmpUrl+SysConstant.TL_GATEWAY_DIR+"/");
				accountLocal.mkdir();
				
				fwriter = new FileWriter(accountLocal.getPath()+"\\"+fileName);  
				fwriter.write(fileAsString);
				fwriter.flush();
				fwriter.close();
				logger.info("文件临时保存为："+accountLocal.getPath()+"\\"+fileName);
				
				//登录ftp并上传文件
				FtpUtil util = null;
				FTPConfigResponse configResponse = titanSysconfigService.getFTPConfig();
				util = new FtpUtil(configResponse.getFtpServerIp(),
						configResponse.getFtpServerPort(),
						configResponse.getFtpServerUser(),
						configResponse.getFtpServerPassword());
				util.ftpLogin();
				logger.info("login ftp success");
				File file = new File(accountLocal.getPath()+"\\"+fileName);
				InputStream inputStream = new FileInputStream(file);
				util.uploadStream(fileName, inputStream, FtpUtil.UPLOAD_PATH_TL_AGENT_CHECKING+SysConstant.TL_GATEWAY_DIR);
				logger.info("upload to ftp success fileName=" + tlGatewayPayDownloadRequest.getSettleDate()+".txt");
				util.ftpLogOut();
				
				logger.info("网关支付对账文件下载并上传成功");
				
			/*} else {
				logger.error("【通联-网关支付对账文件下载】签名验证失败，丢弃该文件，日期：{}", tlGatewayPayDownloadRequest.getSettleDate());
				response.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
			}*/
			
			return response;
			
			//---------------------------下面的写法也可以，上面是通联的写法
			/*//构建参数，发送请求
			HttpPost httpPost = new HttpPost(gateWayConfigDTO.getGateWayUrl());
			List<NameValuePair> params = BeanConvertor.beanToList(tlGatewayPayDownloadRequest);
			logger.info("【通联-网关支付对账文件下载】请求参数：{}", tlGatewayPayDownloadRequest.toString());
			
			//发送请求
			HttpResponse httpRes = HttpClient.httpRequest(params, httpPost);
			if (null != httpRes) {
				
				HttpEntity entity = httpRes.getEntity();
				responseStr = EntityUtils.toString(entity, "UTF-8");
				logger.info("【通联-网关支付对账文件下载】返回信息：{}", responseStr);
				// 读取交易结果
				BufferedReader fileReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
						responseStr.getBytes(Charset.forName("UTF-8"))), Charset.forName("UTF-8")));
				StringBuffer fileBuf = new StringBuffer(); 
				// 签名信息前的字符串
				String lines;
				while ((lines = fileReader.readLine()) != null) {
					if (lines.length() > 0) {
						// 按行读，每行都有换行符
						fileBuf.append(lines + "\r\n");
					} else {
						// 文件中读到空行，则读取下一行为签名信息
						fileSignMsg = fileReader.readLine();
					}
				}
				fileReader.close();
				fileAsString = fileBuf.toString();
				
				// 验证签名：先对文件内容计算 MD5 摘要，再将 MD5 摘要作为明文进行签名验证
				String fileMd5 = SecurityUtil.MD5Encode(fileAsString);
				isVerified = SecurityUtil.verifyByRSA(resUrl+SysConstant.CER_PATH, fileMd5.getBytes(), Base64.decode(fileSignMsg));
				if (isVerified) {
					logger.info("【通联-网关支付对账文件下载】签名验证通过");
					
				} else {
					logger.error("【通联-网关支付对账文件下载】签名验证失败，丢弃该文件，日期：{}", tlGatewayPayDownloadRequest.getSettleDate());
				}
				
				return response;
				
			}else{
				
				logger.error("【通联-网关支付对账文件下载】失败 httpRes为空");
				response.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
				return response;
			}*/
			
		}catch(Exception e){
			
			logger.error("【通联-网关支付对账文件下载】异常：", e);
			response.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
			return response;
			
		}/*finally {
			try {
				fwriter.flush();
				fwriter.close();
			} catch (IOException e) {
				logger.error("【通联-网关支付对账文件下载】异常：", e);
			}
		}*/
		
	}
	
	
	@SuppressWarnings({ "unused", "resource" })
	@Override
	public RSResponse qrCodePayDownload(TLQrCodeDownloadRequest tlQrCodeDownloadRequest) {

		TLQrCodeDownloadResponse tlQrCodeDownloadResponse = new TLQrCodeDownloadResponse();
		RSResponse response = new RSResponse();
		response.setMerchantNo(SysConstant.RS_MERCHANT_NO);
		response.setVersion(SysConstant.RS_VERSION);
		response.setSignType(SysConstant.RS_SIGN_TYPE);
		String responseStr = "";
		
		try {
			
			//获取网关配置
			String configKey = tlQrCodeDownloadRequest.getCusid() +"_" + PayTypeEnum.QR_WECHAT_URL.combPayType + 
					"_" + SysConstant.TL_CHANNEL_CODE + "_" + tlQrCodeDownloadRequest.getRequestType();
			GateWayConfigDTO gateWayConfigDTO = SysConstant.gateWayConfigMap.get(configKey);
			if(gateWayConfigDTO == null){
				logger.error("【通联-扫码/公众号支付对账文件下载】失败，获取网关配置为空，configKey={}", configKey);
				response.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
				return response;
			}
			
			//构建参数，发送请求
			String params = buildRequestParam(tlQrCodeDownloadRequest, gateWayConfigDTO.getAppId(), 
					gateWayConfigDTO.getSecretKey());
			logger.info("【通联-扫码/公众号支付对账文件下载】请求参数：{}", CommonUtil.toLineString(params));
			HttpPost httpPost = new HttpPost(gateWayConfigDTO.getGateWayUrl() + "?" + params.toString());
			HttpResponse httpRes = HttpClient.httpRequest(new ArrayList<NameValuePair>(), httpPost);
			
			if (null != httpRes) {
				HttpEntity entity = httpRes.getEntity();
				responseStr = EntityUtils.toString(entity, "UTF-8");
				logger.info("【通联-扫码/公众号支付对账文件下载】返回信息：\n{}", responseStr);
				
				tlQrCodeDownloadResponse = (TLQrCodeDownloadResponse)JsonUtil.jsonToBean(responseStr, TLQrCodeDownloadResponse.class);
				if(!"SUCCESS".equals(tlQrCodeDownloadResponse.getRetcode()) 
						|| !StringUtil.isValidString(tlQrCodeDownloadResponse.getUrl())){
					logger.error("【通联-扫码/公众号支付对账文件下载】获取zip文件地址失败");
					
				}else {
					
					logger.info("【通联-扫码/公众号支付对账文件下载】获取zip文件地址成功，开始保存...");
					String fileName = DateUtil.dateToString(DateUtil.stringToDate(tlQrCodeDownloadRequest.
							getDate(), "yyyyMMdd"), "yyyy-MM-dd");
					String localZipPath = tmpUrl+SysConstant.TL_QECODE_DIR+"/";
					//String localZipPath = "F:/test/";
					int byteread = 0;
					int bytesum = 0;
					InputStream inStream=null;
					FileOutputStream fs =null;
					URL url = new URL(tlQrCodeDownloadResponse.getUrl());
					URLConnection conn = url.openConnection();
					inStream = conn.getInputStream();
					fs = new FileOutputStream(localZipPath+fileName+".zip");
					byte[] buffer = new byte[4028];
					while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread;
					fs.write(buffer, 0, byteread);
					}
					logger.info("【通联-扫码/公众号支付对账文件下载】保存成功开始解压，地址：{}", localZipPath+fileName+".zip");
					//解压
					ZipInputStream zin=new ZipInputStream(new FileInputStream(new File(localZipPath+fileName+".zip")));
					while (zin.getNextEntry() != null) {
						 FileOutputStream os = new FileOutputStream(localZipPath+fileName+".xlsx");  
			             // Transfer bytes from the ZIP file to the output file  
			             byte[] buf = new byte[1024];  
			             int len;  
			             while ((len = zin.read(buf)) > 0) {  
			                 os.write(buf, 0, len);  
			             }  
			             os.close();  
			             zin.closeEntry();
					}
					
					
				}
				
				return response;
				
			}else{
				
				logger.error("【通联-扫码/公众号支付对账文件下载】失败 httpRes为空");
				response.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);;
				return response;
			}
			
		} catch (Exception e) {
			logger.error("【通联-扫码/公众号支付对账文件下载】发生异常：", e);
			response.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
			return response;
		}

	}
	
	
	/**
	 * 得到 HttpURLConnection 对象
	 * @author Jerry
	 * @date 2018年3月14日 下午3:45:34
	 */
	public HttpURLConnection getHttpsURLConnection(URL url) {
		try {
			HttpURLConnection httpConnection = (HttpURLConnection) 
			url.openConnection();
			if ("https".equals(url.getProtocol())) {// 如果是 https 协议
				HttpsURLConnection httpsConn = (HttpsURLConnection) 
				httpConnection;
				TrustManager[] managers = { new MyX509TrustManager() };// 证书过滤
				SSLContext sslContext;
				sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, managers, new SecureRandom());
				
				SSLSocketFactory ssf = sslContext.getSocketFactory();
				httpsConn.setSSLSocketFactory(ssf);
				httpsConn.setHostnameVerifier(new MyHostnameVerifier());
				//主机名过滤
				return httpsConn;
			}
			return httpConnection;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	
	private String buildRequestParam(TLQrCodeDownloadRequest tlQrCodeDownloadRequest, String appId, 
			String secretKey) throws UnsupportedEncodingException{
		
		//TreeMap会按字段名的ASCLL码从小到大排序
		TreeMap<String,String> params = new TreeMap<String,String>();
		params.put("cusid", tlQrCodeDownloadRequest.getCusid());
		params.put("appid", appId);
		params.put("date", tlQrCodeDownloadRequest.getDate());
		params.put("randomstr", tlQrCodeDownloadRequest.getRandomstr());
		params.put("key", secretKey);
		
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, String> entry:params.entrySet()){
			if(entry.getValue()!=null&&entry.getValue().length()>0){
				sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
			}
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}
		logger.info("【通联-扫码/公众号对账文件下载】加密前排序为：{}", sb.toString());
		String md5Msg = MD5.MD5Encode(sb.toString(), "UTF-8").toUpperCase();
		logger.info("【通联-扫码/公众号对账文件下载】加密后sign为：{}", md5Msg);
		params.put("sign", md5Msg);
		params.remove("key");
		
		StringBuilder paramBuf = new StringBuilder();
    	boolean isNotFirst = false;
    	for (Map.Entry<String, String> entry: params.entrySet()){
    		if(entry.getValue()!=null&&entry.getValue().length()>0){
	    		if (isNotFirst)
	    			paramBuf.append('&');
	    		isNotFirst = true;
	    		paramBuf
	    			.append(entry.getKey())
	    			.append('=')
	    			.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
    		}
    	}
    	
    	logger.info("【通联-扫码/公众号对账文件下载】上送参数：{}", paramBuf.toString());
		return paramBuf.toString();
		
	}
	
	
	/**
	 * poi读取-xlsx
	 * @author Jerry
	 * @date 2018年3月16日 下午3:39:41
	 */
	private static void excelToString(String realPath,String fileName){
        try{
            File file = new File(realPath);
            InputStream str = new FileInputStream(file);
            XSSFWorkbook xwb = new XSSFWorkbook(str);  //利用poi读取excel文件流
            XSSFSheet st = xwb.getSheetAt(0);  //读取sheet的第一个工作表
            int rows = st.getLastRowNum();//总行数
            int cols;//总列数
            logger.info("总行数："+rows);
            //读取第三行第6列-交易时间
            XSSFRow row = st.getRow(3);
            if(row!=null){
            	XSSFCell cell = row.getCell(6);
            }
            
            /*for(int i=0;i<rows;i++){
                XSSFRow row=st.getRow(i);//读取某一行数据
                if(row!=null){
                    //获取行中所有列数据
                    cols=row.getLastCellNum();
                    logger.info("========行========"+rows+"=====列========"+cols);
	                for(int j=0;j<cols;j++){
	                    XSSFCell cell=row.getCell(j);
	                    if(cell==null){
	                        System.out.print("   "); 
	                    }else{
	                    //判断单元格的数据类型
	                    switch (cell.getCellType()) { 
	                        case XSSFCell.CELL_TYPE_NUMERIC: // 数字 
	                            System.out.print(cell.getNumericCellValue() + "   "); 
	                            break; 
	                        case XSSFCell.CELL_TYPE_STRING: // 字符串 
	                            System.out.print(cell.getStringCellValue() + "   "); 
	                            break; 
	                        case XSSFCell.CELL_TYPE_BOOLEAN: // Boolean 
	                            System.out.println(cell.getBooleanCellValue() + "   "); 
	                            break; 
	                        case XSSFCell.CELL_TYPE_FORMULA: // 公式 
	                            System.out.print(cell.getCellFormula() + "   "); 
	                            break; 
	                        case XSSFCell.CELL_TYPE_BLANK: // 空值 
	                            System.out.println(""); 
	                            break; 
	                        case XSSFCell.CELL_TYPE_ERROR: // 故障 
	                            System.out.println("故障"); 
	                            break; 
	                        default: 
	                            System.out.print("未知类型   "); 
	                            break; 
	                        } 
	                    }
	                }
                }
            }*/
        }catch(IOException e){
            e.printStackTrace();  
        }
         
    }

}