package com.luqi.timer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.luqi.common.model.Notice;
import com.luqi.common.model.T1doBase;
import com.luqi.common.model.T1doYscg;
import com.luqi.util.HttpUtil;
import com.luqi.util.StrUtil;
import com.luqi.util.TimeUtil;
import com.luqi.util.UrlUtil;
/**
 * 
 * @author 39805
 *	云上城管
 */
public class YscgTask implements Runnable {
	private T1doBase t;

	private int type;//1新建2办结

	@Override
	public void run() {
	
		String result = null;
		try {
			//新增同步到云上城管
			if(type==3) {
				JSONObject json=JSON.parseObject(t.getODescribe());
				json.remove("id");
				json.put("taskNo", t.getShowId());
				json.put("problemSource",T1doYscg.getT1doYscg(json.getString("problemSource"), 2));
				json.put("problemType",T1doYscg.getT1doYscg(json.getString("problemType").split("-")[0], 1));
				json.put("sitePhotos",json.getString("sitePhotos"));
				json.put("reportingTime",json.getString("reportingTime").length()==10?json.getString("reportingTime")+" "+TimeUtil.getHmsSDF():json.getString("reportingTime").matches("\\d+")?TimeUtil.stringToDate(json.getString("reportingTime")):json.getString("reportingTime"));
				if(json.getString("rectificationPhotos")!=null) {
					if(json.getJSONArray("rectificationPhotos").size()==0) {
						json.put("rectificationStatus","0");
					}
					json.put("rectificationPhotos",json.getString("rectificationPhotos"));
				}
				boolean flag=true;
				int i=0;
				while (flag) {
					result=HttpUtil.doPost1(UrlUtil.yscg_add, json.toString());
						if(result!="") {
						new Notice().setTest(json.toString()).setResult(result).save();
						JSONObject r=JSON.parseObject(result);
						if(r.getIntValue("code")==1) {
							t.setAPARAMETER(r.getJSONObject("data").getJSONObject("result").getIntValue("id")).update();
							flag=false;
						}
						
					}	
					if(flag) {
						try {
							
							Thread.sleep(10000L+i*10000);
							i++;
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				if(json.getIntValue("rectificationStatus")==1) {
					boolean flag1=true;
					while (flag1) {
							String re=HttpUtil.doPost1(UrlUtil.cg_update+"?taskNo="+t.getAPARAMETER(), "{\"taskNo\":\""+t.getAPARAMETER()+"\"}");
						if(re!="") {
							new Notice().setTest(UrlUtil.cg_update+"?taskNo="+t.getAPARAMETER()).setResult(re).save();
							JSONObject r=JSON.parseObject(result);
							if(r.getIntValue("code")==1) 
							flag1=false;
						}
						if(flag1) {
							try {
								Thread.sleep(10000L);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			//删除同步到云上城管
			}else if(type==4){
				result=HttpUtil.doPost1(UrlUtil.yscg_delete+"?taskNo="+t.getShowId(), "");
				new Notice().setTest(UrlUtil.yscg_delete+"?taskNo="+t.getShowId()).setResult(result).save();
			}else {
				result = getResult(type==1?UrlUtil.yscg:UrlUtil.yscg_update,t,type);
				//城管推送过来的工单如果办结了回调接口
				if(type==2&&StrUtil.isNotEmpty(t.getAPARAMETER()+"")) {
					boolean flag1=true;
					while (flag1) {
							String re=HttpUtil.doPost1(UrlUtil.cg_update+"?taskNo="+t.getAPARAMETER(), "{\"taskNo\":\""+t.getAPARAMETER()+"\"}");
						if(re!="") {
							new Notice().setTest(UrlUtil.cg_update+"?taskNo="+t.getAPARAMETER()).setResult(re).save();
							JSONObject r=JSON.parseObject(result);
							if(r.getIntValue("code")==1) 
							flag1=false;
						}
						if(flag1) {
							try {
								Thread.sleep(10000L);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
			
		} catch (URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		//new Notice().setTest(result).save();
		
	}

	public YscgTask(T1doBase t, int type) {
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
	    //不是城管推送过来的数据才会进行下面的推送过来的数据
	    if(StrUtil.isEmpty(""+t.getAPARAMETER())) {
		    response = httpclient.execute(httpPost);
		    result = IOUtils.toString( response.getEntity().getContent(), "UTF-8" );
		    System.out.println("result:" + result);
	    }
	    return result;
	   
   }

	public static void main(String[] args) {
		String result="";
		System.out.println(result=="");
		/*JSONObject json=new JSONObject();
		json.put("reportingTime","1010-10-10 10:10:10");
		json.put("reportingTime",json.getString("reportingTime").length()==10?json.getString("reportingTime")+" "+TimeUtil.getHmsSDF():json.getString("reportingTime").matches("\\d+")?TimeUtil.stringToDate(json.getString("reportingTime")):json.getString("reportingTime"));
		json.remove("reportingTime");
		json.remove("id");
		System.out.println(new Date().getTime());
		System.out.println("1992144980744".matches("\\d+"));
		System.out.println(TimeUtil.stringToDate("1992144980744"));
		System.out.println("1010-10-10".matches("\\d+"));
		System.out.println("1010-10-10 10:10:10".length());*/
	}
	
	
}
