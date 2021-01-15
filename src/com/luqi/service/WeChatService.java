package com.luqi.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.luqi.common.model.T1doBase;
import com.luqi.common.model.T1doFeedback;
import com.luqi.common.model.T1doWechat;
import com.luqi.util.FileTypeJudge;
import com.luqi.util.HttpUtil;
import com.luqi.util.StrUtil;
import com.luqi.util.UrlUtil;

/**
* @author coco
* @date 2020年2月25日 下午3:35:25
* 
*/
public class WeChatService {
	//解绑微信群
	public static JSONObject untying(JSONObject json) throws UnsupportedEncodingException, IOException {
		//字段：IS_BINDING_WECHAT_GROUP是否绑定微信群:1是0否  
		Db.update("update t_1do_base set IS_BINDING_WECHAT_GROUP=0 where SHOW_ID=?",json.getString("SHOW_ID"));
		JSONObject p=new JSONObject();
		p.put("groupId",json.getString("SHOW_ID"));
		p.put("wxGroupId",json.getString("wxGroupId"));
		String result=HttpUtil.doPost(UrlUtil.weChat+"/Base-Module/Group/UnBindGroup", p);
		T1doWechat.saveT1doWechat(json.getString("SHOW_ID"), result, 11,p.toString());

		return JSON.parseObject(result);
	}
	//根据名称搜索微信群
	public static JSONObject searchWechatGroupByName(JSONObject json) throws UnsupportedEncodingException {

		String result=HttpUtil.doGet11(UrlUtil.weChat+"/Base-Module/Group/GetGroupList?"
				+ "keyword="+URLEncoder.encode(json.getString("keyword"),"UTF-8")
				+"&currentPage="+json.getIntValue("currentPage")+"&pageSize="+json.getIntValue("pageSize")) ;
		return JSON.parseObject(result);
	}
	//绑定微信群
	public static JSONObject bindingWechatGroup(JSONObject json) throws Exception {
		//创建微信群
		List<String> list=Db.query("SELECT O_USER FROM `t_1do_pstatus` where SHOW_ID=? and isDelete=1 and O_USER" + 
				"!='oEpW2eABeeHK2nB7'  GROUP BY O_USER ",json.getString("SHOW_ID"));
		//list.add("oEpW2eABeeHK2nB7");
		if(json.getIntValue("type")==1) {
			JSONObject p=new JSONObject();
			if(list.size()<2) {
				 p.put("IsSuccess", false);
				 p.put("Reason", "人数少于3人创建失败");
			     return p; 
			}
			p.put("memberList",list);
			String result=HttpUtil.doPost(UrlUtil.weChat+"/Base-Module/Group/CreateWXGroup", p);
			T1doWechat.saveT1doWechat(json.getString("SHOW_ID"), result, 7,p.toString());
			
			JSONObject r=JSON.parseObject(result);
			JSONObject r1=r.getJSONObject("Data");
			String taskName=r1.getString("taskName");
			boolean flag=true;
			String wxGroupId="";
			Thread.sleep(30000);
			int i=0;
			while(flag) {
				String result1=HttpUtil.doGet11(UrlUtil.weChat+"/Base-Module/Group/GetTaskState?"
						+ "taskName="+taskName) ;
				T1doWechat.saveT1doWechat(json.getString("SHOW_ID"), result1, 8,taskName);

				JSONObject r2=JSON.parseObject(result1);
				JSONObject r3=r2.getJSONObject("Data");
				if(r3.getBoolean("state")&&r3.getString("wxGroupId")!=null&&r3.getString("wxGroupId").contains("@chatroom")) {
					wxGroupId=r3.getString("wxGroupId");
					flag=!r3.getBoolean("state");
				}else {
					Thread.sleep(10000);
					i++;
				}
				
				if(i==10) {
					 p.put("IsSuccess", false);
					 p.put("Reason", "微信创建错误请联系管理员或重新新建");
				     return p; 
				}
			}
			JSONObject p1=new JSONObject();
			p1.put("groupId",json.getString("SHOW_ID"));
			p1.put("wxGroupId",wxGroupId);
			p1.put("groupName",StrUtil.bSubstring(json.getString("name"),32));
			String result2=HttpUtil.doPost(UrlUtil.weChat+"/Base-Module/Group/EditGroupNameAndBind", p1);
			T1doWechat.saveT1doWechat(json.getString("SHOW_ID"), result2, 9,p1.toString());

			JSONObject rt=JSON.parseObject(result2);
			if(rt.getBooleanValue("IsSuccess")) {
				Db.update("update t_1do_base set IS_BINDING_WECHAT_GROUP=1,WECHAT_GROUP_NAME=?,WECHAT_GROUP_ID=? where SHOW_ID=?"
					,json.getString("name"),wxGroupId,json.getString("SHOW_ID"));
				T1doBase t1doBase=T1doBase.getIdoBase(json.getString("SHOW_ID"));	
				//新建群发第一条消息
				T1doWechat.saveT1doWechat(json.getString("SHOW_ID"), SendMsg(wxGroupId,"1do小依",getMsg("新创建 1do ：",t1doBase)), 1,"");
				//把1do详情添加到微信群公告
				T1doWechat.saveT1doWechat(json.getString("SHOW_ID"), CreateGroupNotice(wxGroupId,t1doBase.getODescribe()), 3,"");
				
			}
			return rt;
			
		//绑定微信群	
		}else if(json.getIntValue("type")==2) {
			
			JSONObject p=new JSONObject();
			p.put("groupId",json.getString("SHOW_ID"));
			p.put("wxGroupId",json.getString("wxGroupId"));
			p.put("memberList",list);
			String result=HttpUtil.doPost(UrlUtil.weChat+"/Base-Module/Group/BindAndAddUsers", p);
			T1doWechat.saveT1doWechat(json.getString("SHOW_ID"), result, 10,p.toString());

			JSONObject rt=JSON.parseObject(result);
			if(rt.getBooleanValue("IsSuccess")) {
				
				/*try {
					JSONObject p1=new JSONObject();
					p1.put("wxGroupId",json.getString("wxGroupId"));
					p1.put("groupName",StrUtil.bSubstring(json.getString("name"),32));
					String result1 = HttpUtil.doPost(UrlUtil.weChat+"/Base-Module/Group/EditGroupName", p1);
					T1doWechat.saveT1doWechat(json.getString("SHOW_ID"), result1, 6,p1.toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				
				Db.update("update t_1do_base set IS_BINDING_WECHAT_GROUP=1,WECHAT_GROUP_NAME=?,WECHAT_GROUP_ID=? where SHOW_ID=?"
					,json.getString("name"),json.getString("wxGroupId"),json.getString("SHOW_ID"));
				T1doBase t1doBase=T1doBase.getIdoBase(json.getString("SHOW_ID"));	
				//新建群发第一条消息
				T1doWechat.saveT1doWechat(json.getString("SHOW_ID"), SendMsg(json.getString("wxGroupId"),"1do小依",getMsg("新创建 1Do ：",t1doBase)), 1,"");
				//把1do详情添加到微信群公告
				T1doWechat.saveT1doWechat(json.getString("SHOW_ID"), CreateGroupNotice(json.getString("wxGroupId"),t1doBase.getODescribe()), 3,"");
				
			}
			
			return JSON.parseObject(result);
		}
		return null;
		
	}
	/**   
	　* 描述：   
	　* 创建人：coco   
	　* 创建时间：2020年2月26日 下午1:19:42         
	*/
	public static String getMsg(String str,T1doBase t1doBase) {
		String msg=str+" \r\n" + 
				""+t1doBase.getOTitle()+"... ...\r\n" + 
				"发起人："+t1doBase.getOCustomerName()+"; \r\n" + 
				"参与人："+t1doBase.getOExecutorName()+";\r\n" + 
				"查看详情 -"+ UrlUtil.ip+"/1do/shortMessage/MyHtml.html?SHOW_ID="+t1doBase.getShowId();
		return msg;
	}
	/**   
	　* 描述：   发送微信消息
	　* 创建人：coco   
	　* 创建时间：2020年2月26日 下午12:25:42         
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	*/
	public static String SendMsg(String wxGroupId,String senderName,String msg) throws UnsupportedEncodingException, IOException {
		JSONObject p=new JSONObject();
		p.put("wxGroupId",wxGroupId);
		p.put("senderName",senderName);
		p.put("msg",msg);
		String result=HttpUtil.doPost(UrlUtil.weChat+"/Base-Module/Group/SendMsg", p);
		return result;
	}
	
	/**   
	　* 描述：   发送微信附件
	　* 创建人：coco   
	　* 创建时间：2020年2月26日 下午12:25:42         
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	*/
	public static void SendFile(String wxGroupId,T1doFeedback tf) throws UnsupportedEncodingException, IOException {
		JSONObject p=new JSONObject();
		p.put("wxGroupId",wxGroupId);
		p.put("senderName",tf.getOUser().equals("oEpW2eABeeHK2nB7")?"":tf.getOUserName());
		p.put("type",FileTypeJudge.getFileType(tf.getAttrPath()));
		p.put("url",tf.getAttrPath());
		p.put("fileName",tf.getFBCONTENT());
		String result=HttpUtil.doPost(UrlUtil.weChat+"/Base-Module/Group/SendFile", p);
		
		T1doWechat.saveT1doWechat(tf.getID()+"", result, 2,p.toString());
		//return result;
	}
	/**   
	　* 描述：   发送群公告
	　* 创建人：coco   
	　* 创建时间：2020年2月26日 下午12:25:42         
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	*/
	public static String CreateGroupNotice(String wxGroupId,String notice) throws UnsupportedEncodingException, IOException {
		JSONObject p=new JSONObject();
		p.put("wxGroupId",wxGroupId);
		p.put("notice",notice);
		String result=HttpUtil.doPost(UrlUtil.weChat+"/Base-Module/Group/CreateGroupNotice", p);
		return result;
	}
	//踢出群成员或者加入新成员  type 5是踢人，4是拉人
	public static void removeUsersOrAddUsers(String dataId,String wechatGroupId, List<String> oUser,int type) throws UnsupportedEncodingException, IOException {
		JSONObject p=new JSONObject();
		p.put("wxGroupId",wechatGroupId);
		p.put("memberList",oUser);
		String result="";
		if(type==4)
		   result=HttpUtil.doPost(UrlUtil.weChat+"/Base-Module/Group/AddUsers", p);
		else
		   result=HttpUtil.doPost(UrlUtil.weChat+"/Base-Module/Group/RemoveUsers", p);
		T1doWechat.saveT1doWechat(dataId, result, type,p.toString());
	  
		
	}	
}
