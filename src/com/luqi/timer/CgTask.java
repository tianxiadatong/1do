package com.luqi.timer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.alibaba.fastjson.JSONObject;
import com.luqi.common.model.Notice;
import com.luqi.common.model.T1doBase;
import com.luqi.util.HttpUtil;
import com.luqi.util.TimeUtil;
import com.luqi.util.UrlUtil;
/**
 * 
 * @author 39805
 *	云上城管
 */
public class CgTask implements Runnable {
	private T1doBase t;

	private int type;//1新建2办结

	@Override
	public void run() {
	
		String result = null;
		try {
			result = getResult(type==1?UrlUtil.yscg:UrlUtil.yscg_update,t,type);
		} catch (URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Notice().setTest(result).save();
		
	}

	public CgTask(T1doBase t, int type) {
		super();
		this.t = t;
		this.type = type;
	}

   public static String getResult(String url,T1doBase t,int type) throws URISyntaxException, ClientProtocolException, IOException {
	   CloseableHttpClient httpclient = HttpClients.createDefault();
	    String result = "";
	    CloseableHttpResponse response = null;
	    URIBuilder builder = new URIBuilder(url);
	    builder.addParameter("eventId", t.getShowId());//工单id 必填  
	    if(type==1) {
	    	builder.addParameter("workOrderContent",t.getODescribe());//工单内容 必填
	  	    builder.addParameter("createdBy",t.getCreateUserName());//创建人 必填
	  	    builder.addParameter("createdDate", TimeUtil.getyMdhmsSDF());//创建时间
	    }
	  
	    URI uri = builder.build();
	    HttpPost httpPost = new HttpPost( uri );
	    RequestConfig requestConfig = RequestConfig.custom()
	            .setSocketTimeout(2000)
	            .setConnectTimeout(5000)
	            .build();
	    httpPost.setConfig(requestConfig);
	    response = httpclient.execute(httpPost);
	    result = IOUtils.toString( response.getEntity().getContent(), "UTF-8" );
	    System.out.println("result:" + result);

	    return result;
	   
   }

	
	
}
