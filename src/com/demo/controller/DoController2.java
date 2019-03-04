 package com.demo.controller;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.fileupload.util.Streams;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.demo.common.model.Approval;
import com.demo.common.model.T1doAttr;
import com.demo.common.model.T1doBase;
import com.demo.common.model.T1doFeedback;
import com.demo.common.model.T1doFw;
import com.demo.common.model.T1doLog;
import com.demo.common.model.T1doOrder;
import com.demo.common.model.T1doPstatus;
import com.demo.common.model.T1doSet;
import com.demo.common.model.T1doStatus;
import com.demo.common.model.T1doTemp;
import com.demo.common.model.T1doType;
import com.demo.common.model.T1doUser;
import com.demo.common.model.TRegUser;
import com.demo.service.DoService;
import com.demo.util.ExcelExportUtil;
import com.demo.util.HttpUtil;
import com.demo.util.IDUtil;
import com.demo.util.JsonUtil;
import com.demo.util.MsgUtil;
import com.demo.util.StrUtil;
import com.demo.util.TimeUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;


public class DoController2 extends Controller {
	private static String url="http://172.16.8.7:6002/Base-Module/Message";//测试环境通知接口
	//private static String url="http://xcgovapi.hzxc.gov.cn/Base-Module/Message";//正式环境通知接口
	/*
	 2018年12月4日 coco 注解：//获得用户最近一次访问
	*/
	public void action() {
		renderJson();
	}
	/*
	 2018年10月31日 coco 注解：获得附件
	*/
	public void getAttr() {
    	JSONObject json=JsonUtil.getJSONObject(getRequest()); 	
		renderJson(MsgUtil.successMsg(T1doAttr.getAttr(json.getString("SHOW_ID"))));
	}
	/**
     * 导出Excel
     */
    public void exportExcel() {    
    	//http://localhost:8080/1do/do/exportExcel?type=3
         int type = getParaToInt("type");
         
         List<Record> data = DoService.exportExcel(type);
 		 String fileName="1do"+TimeUtil.getCurrentDateTime("yyyyMMddhhmmss")+".xls";
         File file = ExcelExportUtil.createExcelFile(fileName, data);
 		renderFile(file);
    }
	/*
		 2018年8月14日下午5:32:24 coco  //修改参与人的身份（抄送人或受理人）
	 */
	
	public void chuangUserId() {
		JSONObject json=JsonUtil.getJSONObject(getRequest());
		JSONObject douser=getSessionAttr("1doUser");
		if(douser.getBooleanValue("isfw")){
		String[] users=json.getString("user").split(";");
		Db.update("update t_1do_pstatus set otherid=0 where"
				+ " otherid=? and SHOW_ID=? ",json.getIntValue("otherid"),json.getString("SHOW_ID"));
		for(String user:users){
			Db.update("update t_1do_pstatus set otherid=? where"
					+ " SHOW_ID=? and O_USER=?",json.getIntValue("otherid"),json.getString("SHOW_ID"),user);
		}
		      renderJson(JsonUtil.getMap(200, "修改成功"));
		}else {
			  renderJson(JsonUtil.getMap(202, "权限不足")); 

		}
		
	}
	    /*
		 2018年7月16日下午4:58:52 coco  //整理层审核（失效）
		*/
		public void audit() {
			JSONObject json=JsonUtil.getJSONObject(getRequest());
			//JSONObject json=getSessionAttr("1doUser");
			T1doBase t1doBase =T1doBase.dao.findFirst("select * from t_1do_base where SHOW_ID=?", json.getString("SHOW_ID"));
			//sendIdo(t1doBase);
			t1doBase.setISAUDIT(2).update();
			renderJson(JsonUtil.getMap(200, "审核成功"));
		}
	    /*
		 2018年7月5日上午9:56:46 coco   //看板搜索 （旧）
		*/

		public void search1() {
			JSONObject json1=JsonUtil.getJSONObject(getRequest());
			JSONObject douser=getSessionAttr("1doUser");
			String sql="";
			if(StrUtil.isNotEmpty(json1.getString("base"))){
				 sql="and b.O_DESCRIBE like CONCAT('%','"+json1.getString("base")+"','%')";
			}
			String sql1="";
			int i=3;
			if(json1.getString("method").equals("medo")){
				sql1="and O_EXECUTOR like CONCAT('%','"+douser.getString("loginName")+"','%')" ;
				i=1;
			}else if(json1.getString("method").equals("hedo")){
				sql1="and O_CUSTOMER like CONCAT('%','"+douser.getString("loginName")+"','%')";
				i=2;
			}
			int type=json1.getIntValue("type");
			int delete=1;
			if(type==6){
				delete=2;
				type=5;
			}
			/*String from ="select a.SHOW_ID,a.O_DESCRIBE,a.O_EXECUTOR_NAME,a.O_CUSTOMER_NAME,b.type TYPE,a.O_START_TIME,a.O_FINISH_TIME,a.star,a.evaluation,a.Real_FINISH_TIME,a.DELETE_TIME "
			 		+ " from (select b.SHOW_ID,b.O_DESCRIBE,b.O_EXECUTOR_NAME,b.O_CUSTOMER_NAME,b.AT,b.O_START_TIME,b.O_FINISH_TIME,b.star,b.evaluation,b.Real_FINISH_TIME,b.DELETE_TIME "
					+ "from t_1do_base b,t_1do_status s  where b.O_IS_DELETED="+delete+" and b.SHOW_ID=s.SHOW_ID "+sql1+" and s.o_status=? "+sql+" ORDER BY b.ID desc)a LEFT JOIN (select * from t_1do_order "
							+ "where useraccount=? and type="+i+" )b on a.show_id=b.show_id ORDER BY "
							+ "modifyTime desc LIMIT ?,10";
			
			String from1="select count(*) num from t_1do_base b,t_1do_status s  where b.O_IS_DELETED="+delete+" and b.SHOW_ID=s.SHOW_ID "+sql1+" and s.o_status=? "+sql;*/
			String from ="select a.SHOW_ID,a.O_DESCRIBE,a.O_EXECUTOR_NAME,a.O_CUSTOMER_NAME,b.type TYPE,a.O_START_TIME,a.O_FINISH_TIME,a.star,a.evaluation,a.Real_FINISH_TIME,a.DELETE_TIME "
					+ " from (select b.SHOW_ID,b.O_DESCRIBE,b.O_EXECUTOR_NAME,b.O_CUSTOMER_NAME,b.AT,b.O_START_TIME,b.O_FINISH_TIME,b.star,b.evaluation,b.Real_FINISH_TIME,b.DELETE_TIME "
					+ "from t_1do_base b,t_1do_status s  where b.O_IS_DELETED="+delete+" and b.SHOW_ID=s.SHOW_ID "+sql1+" and s.o_status=? "+sql+" ORDER BY b.ID desc)a LEFT JOIN (select * from t_1do_order "
					+ "where useraccount=? and type="+i+" )b on a.show_id=b.show_id ORDER BY "
					+ "modifyTime desc LIMIT ?,10";
			
			String from1="select count(*) num from t_1do_base b,t_1do_status s  where b.O_IS_DELETED="+delete+" and b.SHOW_ID=s.SHOW_ID "+sql1+" and s.o_status=? "+sql;
			List<T1doBase> t3=T1doBase.dao.find(from,type,douser.getString("loginName"),(json1.getIntValue("pageNumber")-1)*10);
			//Record r=Db.findFirst(from1,type,json.getString("loginName"));
			Record r=Db.findFirst(from1,type);
			for(T1doBase t:t3){
				t.set1doIsLook(douser.getString("loginName"));
				//t.setLIGHTNING(t.getIdoFeedbacks44().size());
			}
			JSONObject json2=new JSONObject();
			json2.put("base", t3);
			json2.put("allPage", r.getInt("num"));
				renderJson(json2);
        
		}
		//新
		public void search() {
			JSONObject json1=JsonUtil.getJSONObject(getRequest());
			JSONObject douser=getSessionAttr("1doUser");
			String sql="";
			if(StrUtil.isNotEmpty(json1.getString("base"))){
				sql="and O_DESCRIBE like CONCAT('%','"+json1.getString("base")+"','%')";
			}
			String sql1="";
			int i=3;
			if(json1.getString("method").equals("medo")){
				sql1=" O_EXECUTOR like CONCAT('%','"+douser.getString("loginName")+"','%')" ;
				i=1;
			}else if(json1.getString("method").equals("hedo")){
				sql1=" O_CUSTOMER like CONCAT('%','"+douser.getString("loginName")+"','%')";
				i=2;
			}
			int type=json1.getIntValue("type");
			/*int delete=1;
			if(type==6){
				delete=2;
				type=5;
			}*/
			
			String from ="select a.SHOW_ID,a.O_DESCRIBE,a.O_EXECUTOR_NAME,a.O_CUSTOMER_NAME,b.type TYPE,a.O_START_TIME,a.O_FINISH_TIME,a.star,a.evaluation,a.Real_FINISH_TIME,a.DELETE_TIME "
					+ " from (select SHOW_ID,O_DESCRIBE,O_EXECUTOR_NAME,O_CUSTOMER_NAME,AT,O_START_TIME,O_FINISH_TIME,star,evaluation,Real_FINISH_TIME,DELETE_TIME "
					+ "from t_1do_base where  "+sql1+" and o_status=? "+sql+" ORDER BY ID desc)a LEFT JOIN (select * from t_1do_order "
					+ "where useraccount=? and type="+i+" )b on a.show_id=b.show_id ORDER BY "
					+ "modifyTime desc LIMIT ?,10";
			
			String from1="select count(*) num from t_1do_base  where "+sql1+" and o_status=? "+sql;
			List<T1doBase> t3=T1doBase.dao.find(from,type,douser.getString("loginName"),(json1.getIntValue("pageNumber")-1)*10);
			Record r=Db.findFirst(from1,type);
			for(T1doBase t:t3){
				t.set1doIsLook(douser.getString("loginName"));
			}
			JSONObject json2=new JSONObject();
			json2.put("base", t3);
			json2.put("allPage", r.getInt("num"));
			renderJson(json2);
			
		}
		/*
		 2018年7月5日上午9:56:46 coco   //APP看板搜索
		 */
		
		public void appSearch() {
		//	String loginName1=getPara("loginName");
			JSONObject json1=JsonUtil.getJSONObject(getRequest());
			JSONObject douser=getSessionAttr("1doUser");
			String loginName=douser==null?json1.getString("loginName"):douser.getString("loginName");
			String sql="";
			if(StrUtil.isNotEmpty(json1.getString("relate"))){
				sql=" and (O_EXECUTOR like CONCAT('%','"+json1.getString("relate")+"','%') or O_CUSTOMER like CONCAT('%','"+json1.getString("relate")+"','%') )";
			}
			String sql1=" and (O_EXECUTOR like CONCAT('%','"+loginName+"','%') or O_CUSTOMER like CONCAT('%','"+loginName+"','%') )"+sql;
			
			if(json1.getString("method").equals("medo")){
				sql1=" and O_EXECUTOR like CONCAT('%','"+loginName+"','%')" +sql;
				
			}else if(json1.getString("method").equals("hedo")){
				sql1=" and O_CUSTOMER like CONCAT('%','"+loginName+"','%')"+sql;
				
			}
			int type=json1.getIntValue("type");
			String type1="(s.o_status=3 or s.o_status=4 or s.o_status=5)";
			if(type!=0){
				 type1=" s.o_status="+type;
			}
			int isLook=json1.getIntValue("isLook");
			String look="";
			if(isLook!=0){
				 look=" and isSend="+isLook;
			}
			String from ="select a.SHOW_ID,a.O_DESCRIBE,a.O_CUSTOMER_NAME,a.O_CUSTOMER,a.AT,a.O_EXECUTOR,a.O_EXECUTOR_NAME,a.SEND_TIME,unix_timestamp(a.O_CREATE_TIME)*1000 O_CREATE_TIME,"
+"unix_timestamp(a.O_FINISH_TIME)*1000 O_FINISH_TIME,unix_timestamp(a.Real_FINISH_TIME)*1000 Real_FINISH_TIME,unix_timestamp(a.DELETE_TIME)*1000 DELETE_TIME,a.O_IS_DELETED ,"
+ "ifnull(b.LIGHTNING,0) LIGHTNING,ifnull(c.LOOKNUM,0) LOOKNUM,ifnull(d.FBNUM,0) FBNUM,s.O_STATUS,f.USER_TYPE,f.isSend ISLOOK "
					+"from t_1do_base a LEFT JOIN (select count(*) LIGHTNING,SHOW_ID from t_1do_feedback where FB_TYPE=4 GROUP BY SHOW_ID)b on a.SHOW_ID=b.SHOW_ID "
					+"LEFT JOIN (select count(*) LOOKNUM,SHOW_ID  from t_1do_log where log_type=2 and  isoverdue=1 GROUP BY SHOW_ID)c on a.SHOW_ID=c.SHOW_ID "
					+"LEFT JOIN (select count(*) FBNUM,SHOW_ID  from t_1do_feedback where FB_TYPE!=4 and isoverdue=1 GROUP BY SHOW_ID)d on a.SHOW_ID=d.SHOW_ID "
					+"LEFT JOIN t_1do_status s on a.SHOW_ID=s.SHOW_ID "
					+"LEFT JOIN (select * from t_1do_pstatus where USER_TYPE!=2 " +look+" and isDelete=1 and O_USER='"+loginName+"' GROUP BY SHOW_ID)f on a.SHOW_ID=f.SHOW_ID "
					+"where "+type1+sql1+look;
			
			String from1=" ORDER BY SEND_TIME desc LIMIT ?,? ) g ORDER BY SEND_TIME ";
			if(StrUtil.isNotEmpty(json1.getString("source"))){
				 from1=" ORDER BY SEND_TIME desc LIMIT ?,? ) g ";
			}
			List<Record> r3=Db.find("select * from ("+from+from1,json1.getIntValue("pageNumber"),json1.getIntValue("onePageNumber"));
			List<Record> r4=Db.find(from);
			for(Record r:r3){
				r.set("O_STATUS", StrUtil.getSql(r.getInt("O_IS_DELETED"), r.getInt("USER_TYPE"),r.getInt("O_STATUS")));

			}
			JSONObject json2=new JSONObject();
			json2.put("base", r3);
			json2.put("allPage", r4.size());
			renderJson(MsgUtil.successMsg(json2));
			
		}
		
		/*
		 2018年7月5日上午9:56:46 coco   //看板搜索
		 */
		
		public void searchNum() {
			JSONObject json1=JsonUtil.getJSONObject(getRequest());
			JSONObject douser=getSessionAttr("1doUser");
			String loginName=douser==null?json1.getString("loginName"):douser.getString("loginName");
			String sql1="";
			
			if(json1.getString("method").equals("medo")){
				sql1=" and O_EXECUTOR like CONCAT('%','"+loginName+"','%')" ;
				
			}else if(json1.getString("method").equals("hedo")){
				sql1=" and O_CUSTOMER like CONCAT('%','"+loginName+"','%')";
				
			}else if(json1.getString("method").equals("all")){
			sql1=" and (O_EXECUTOR like CONCAT('%','"+loginName+"','%') or O_CUSTOMER like CONCAT('%','"+loginName+"','%') ) ";
			
		}   
			JSONObject json2=new JSONObject();
			for (int j = 3; j < 7; j++) {	
			int type=j;
			int delete=1;
			int i=0;
			
			if(type==6){
				if(!json1.getString("method").equals("fwdo")){
					break;
				}
				delete=2;
				type=5;
				i=1;
			}
			String de="b.O_IS_DELETED="+delete+" and";
			if(type==5&&json1.getString("method").equals("medo")){
				//de="";
				String att="select count(*) num from t_1do_base b, (select * from t_1do_pstatus where  USER_TYPE=3 "
						+ "and isDelete=1 and isSend=? and O_USER='"+loginName+"' GROUP BY SHOW_ID)f where b.SHOW_ID=f.SHOW_ID "+sql1+" ";
				Record r1=Db.findFirst(att,1);
				Record r2=Db.findFirst(att,2);
				json2.put("Y", r1.getInt("num"));
				json2.put("N", r2.getInt("num"));
			}else if(type==5&&json1.getString("method").equals("hedo")){
				//de="";
				String att="select count(*) num from t_1do_base b, (select * from t_1do_pstatus where USER_TYPE=1 and isDelete=1 and isSend=? and O_USER='"+loginName+"' GROUP BY SHOW_ID)f where b.SHOW_ID=f.SHOW_ID "+sql1+" ";
				Record r1=Db.findFirst(att,1);
				Record r2=Db.findFirst(att,2);
				json2.put("Y", r1.getInt("num"));
				json2.put("N", r2.getInt("num"));
				
			}else if(type==5&&json1.getString("method").equals("all")){
				//de="";
				String att="select count(*) num from t_1do_base b, (select * from t_1do_pstatus where USER_TYPE!=2 and isDelete=1 and isSend=? and O_USER='"+loginName+"' GROUP BY SHOW_ID)f where b.SHOW_ID=f.SHOW_ID "+sql1+" ";
				Record r1=Db.findFirst(att,1);
				Record r2=Db.findFirst(att,2);
				json2.put("Y", r1.getInt("num"));
				json2.put("N", r2.getInt("num"));
				
			}
			String from1="select count(*) num from t_1do_base b,t_1do_status s  where "+de+" b.SHOW_ID=s.SHOW_ID "+sql1+" and s.o_status=? ";
			
			Record r=Db.findFirst(from1,type);

			json2.put(""+j, r.getInt("num"));
			if(i==1){
				break;
			}
			}
			renderJson(json2);
			
		}
		/*
		 2018年7月5日上午9:56:46 coco   //排序
		 */
		
		public void sorting() {
			JSONObject json1=JsonUtil.getJSONObject(getRequest());
			JSONObject douser=getSessionAttr("1doUser");
			String sql="";
			if(StrUtil.isNotEmpty(json1.getString("base"))){
				sql="and b.O_DESCRIBE like CONCAT('%','"+json1.getString("base")+"','%')";
			}
			String sql1="";
			
			if(json1.getString("method").equals("medo")){
				sql1="and O_EXECUTOR like CONCAT('%','"+douser.getString("loginName")+"','%')" ;
				
			}else if(json1.getString("method").equals("hedo")){
				sql1="and O_CUSTOMER like CONCAT('%','"+douser.getString("loginName")+"','%')";
				
			}
			int type=json1.getIntValue("type");
			int delete=1;
			if(type==6){
				delete=2;
				type=5;
			}
			
			String from ="select b.SHOW_ID,b.O_DESCRIBE,b.O_EXECUTOR_NAME,b.O_CUSTOMER_NAME,b.O_START_TIME,b.O_FINISH_TIME,b.star,b.evaluation,b.Real_FINISH_TIME,b.DELETE_TIME "
					+ "from t_1do_base b,t_1do_status s  where b.O_IS_DELETED="+delete+" and b.SHOW_ID=s.SHOW_ID "+sql1+" and s.o_status=? "+sql+"  ORDER BY "
					+ ""+json1.getString("sorting")+" LIMIT ?,10";
			String from1="select count(*) num  "
					+ "from t_1do_base b,t_1do_status s  where b.O_IS_DELETED="+delete+" and b.SHOW_ID=s.SHOW_ID "+sql1+" and s.o_status=? "+sql;
			List<T1doBase> t3=T1doBase.dao.find(from,type,(json1.getIntValue("pageNumber")-1)*10);
			Record r=Db.findFirst(from1,type);
			for(T1doBase t:t3){
				t.set1doIsLook(douser.getString("loginName"));
				//t.setLIGHTNING(t.getIdoFeedbacks44().size());
			}
			JSONObject json2=new JSONObject();
			json2.put("base", t3);
			json2.put("allPage", r.getInt("num"));
			renderJson(json2);
			
		}
	    /*
		 2018年7月3日下午4:29:42 coco   //通知设置（失效）
		*/
		 @Before(Tx.class)
		public void notice() {
			JSONObject json=JsonUtil.getJSONObject(getRequest());
			 JSONObject douser=getSessionAttr("1doUser");
			 T1doPstatus t1=T1doPstatus.dao.findFirst("select * from t_1do_pstatus where SHOW_ID=? and O_USER=? and USER_TYPE !=2",json.getString("SHOW_ID"),douser.getString("loginName"));
			if(douser.getBoolean("isfw")||StrUtil.getflag(t1.getUserType(), 1)){
				T1doSet t=T1doSet.dao.findFirst("select * from t_1do_set where SHOW_ID=?",json.getString("SHOW_ID"));
				HashSet<String> hs=new HashSet<String>();
				if(StrUtil.isNotEmpty(t.getEventType())){
					String[] temp = t.getEventType().split(";");
				   for(String tem:temp){
					hs.add(tem);
				   }
				}
				
				if(json.getString("method").equals("add")){
					hs.add(json.getString("EVENT_TYPE"));
				}else{
					hs.remove(json.getString("EVENT_TYPE"));
				}
				String result="";
				for(String str:hs){
					result+=str+";";
				}
				result=result.substring(0, result.length()-1);
				t.setEventType(result).update();
				renderJson(JsonUtil.getMap(200, "修改成功"));
			}else{
				renderJson(JsonUtil.getMap(202, "权限不足"));
			}
			
			
		
			
		}
		
	    /*
		 2018年6月28日上午11:18:08 coco  //1call转1do（失效）
		*/
		@Before(Tx.class)
		public void IcallToIdo() {
			String meg=HttpKit.readData(getRequest());
			//JSONObject json=JsonUtil.getJSONObject(getRequest());
			JSONObject json=JSONObject.parseObject(meg);
			long id=IDUtil.getUid1();
			new T1doTemp().setID(id).setBASE(meg).save();
			
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();  	
			map.put("ID", id+"");
			String str1=json.getString("BASE");
			JSONObject json1=JSONObject.parseObject(str1);
			String[] str=json1.getString("MESSAGE_ID").split(";");
			map.put("msg", str);	
			System.out.println(map.toString());
			renderJson(map);
		
			
				
			  
		}
		/*
		 2018年6月28日上午11:18:08 coco  //获得1call转1do消息（失效）
		*/
		public void getIcallToIdo() {
			JSONObject json=JsonUtil.getJSONObject(getRequest());
			long i=json.getLongValue("ID");
			T1doTemp t=T1doTemp.dao.findById(i);
			if(t==null){
				renderJson(JsonUtil.getMap(200, "数据不存在"));
				return;
			}
			//T1doTemp t=T1doTemp.dao.findByIdLoadColumns(json.getLongValue("ID"), "ID");
			JSONObject json1=JSONObject.parseObject(t.getBASE());
			renderJson(json1);
		}
		/*
		 2018年6月28日上午9:42:54 coco   //要我做（失效）
		*/
		public void medo() {
			JSONObject douser=getSessionAttr("1doUser");
			JSONObject json1=JsonUtil.getJSONObject(getRequest());
			List<T1doBase> t3=T1doBase.medo1(douser.getString("loginName"), 3);//待接单
			for(T1doBase t:t3){
				//t.set("LIGHTNING", t.getIdoFeedbacks44().size());
				t.set1doIsLook(douser.getString("loginName"));
				t.setLIGHTNING(t.getIdoFeedbacks44().size());
			}
			List<T1doBase> t4=T1doBase.medo1(douser.getString("loginName"), 4);//已接单
			for(T1doBase t:t4){
				t.setLIGHTNING(t.getIdoFeedbacks44().size());
				t.set1doIsLook(douser.getString("loginName"));
				//t.set("LIGHTNING", t.getIdoFeedbacks4().size());
			}
			List<T1doBase> t5=T1doBase.medo1(douser.getString("loginName"), 5,json1.getIntValue("num"));//已完成
			for(T1doBase t:t5){
				t.setLIGHTNING(t.getIdoFeedbacks44().size());
				t.set1doIsLook(douser.getString("loginName"));
				//t.set("LIGHTNING", t.getIdoFeedbacks4().size());
			}
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();  
			 map.put("1", t3);
			 map.put("2", t4);
			 map.put("3", t5);
			 renderJson(map);
		}
		/*
		 2018年6月28日上午9:42:54 coco   /要他做（失效）
		 */
		public void hedo() {
			JSONObject douser=getSessionAttr("1doUser");
			JSONObject json1=JsonUtil.getJSONObject(getRequest());
			List<T1doBase> t3=T1doBase.hedo1(douser.getString("loginName"), 3);//待接单
			for(T1doBase t:t3){
				t.setLIGHTNING(t.getIdoFeedbacks44().size()); 
				t.set1doIsLook(douser.getString("loginName"));
				
				//t.set("LIGHTNING", t.getIdoFeedbacks4().size());
			}
			List<T1doBase> t4=T1doBase.hedo1(douser.getString("loginName"), 4);//已接单
			for(T1doBase t:t4){
				t.setLIGHTNING(t.getIdoFeedbacks44().size());
				t.set1doIsLook(douser.getString("loginName"));
				//t.set("LIGHTNING", t.getIdoFeedbacks4().size());
			}
			//List<T1doBase> t5=T1doBase.hedo1(json.getString("loginName"), 5);//已完成
			List<T1doBase> t5=T1doBase.hedo1(douser.getString("loginName"), 5,json1.getIntValue("num"));//已完成
			for(T1doBase t:t5){
				t.setLIGHTNING(t.getIdoFeedbacks44().size());
				t.set1doIsLook(douser.getString("loginName"));
				//t.set("LIGHTNING", t.getIdoFeedbacks4().size());
			}
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();  
			 map.put("1", t3);
			 map.put("2", t4);
			 map.put("3", t5);
			 renderJson(map);
		}
		/*
		 2018年6月28日上午9:42:54 coco   /整理层看板（失效）
		 */
		public void fwdo() {
			JSONObject douser=getSessionAttr("1doUser");
			JSONObject json1=JsonUtil.getJSONObject(getRequest());
			List<T1doBase> t3=T1doBase.fwdo1(3,douser.getString("loginName"));//待接单
			for(T1doBase t:t3){
				t.setLIGHTNING(t.getIdoFeedbacks44().size());
				t.set1doIsLook(douser.getString("loginName"));
				
				//t.set("LIGHTNING", t.getIdoFeedbacks4().size());
			}
			List<T1doBase> t4=T1doBase.fwdo1(4,douser.getString("loginName"));//已接单
			for(T1doBase t:t4){
				t.setLIGHTNING(t.getIdoFeedbacks44().size());
				t.set1doIsLook(douser.getString("loginName"));
				
				//t.set("LIGHTNING", t.getIdoFeedbacks4().size());
			}
			List<T1doBase> t5=T1doBase.fwdo1(5,douser.getString("loginName"),json1.getIntValue("num"));//已完成
			for(T1doBase t:t5){
				t.setLIGHTNING(t.getIdoFeedbacks44().size());
				t.set1doIsLook(douser.getString("loginName"));
				
				//t.set("LIGHTNING", t.getIdoFeedbacks4().size());
			}
			List<T1doBase> t6=T1doBase.fwdo2(5,douser.getString("loginName"),json1.getIntValue("num"));//已删除
			for(T1doBase t:t6){
				t.setLIGHTNING(t.getIdoFeedbacks44().size());
				t.set1doIsLook(douser.getString("loginName"));
				
				//t.set("LIGHTNING", t.getIdoFeedbacks4().size());
			}
			
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();  
			map.put("1", t3);
			map.put("2", t4);
			map.put("3", t5);
			map.put("4", t6);
			renderJson(map);
		}
	    
		/*
		 2018年6月28日上午3:50:16 coco  //模拟1do登入
		 */
		public void fwdoshowid(){
			List<T1doFw> t=T1doFw.dao.find("select * from t_1do_fw where icallshowid is null");
			for (T1doFw t1doFw : t) {
				t1doFw.setIcallshowid(HttpUtil.loginIm(t1doFw.getOUser()).getString("loginName")).update();
			}
			renderJson(JsonUtil.getMap(200, "id成功生成"));
		}
		public void login1do() {
			
			JSONObject json=JsonUtil.getJSONObject(getRequest());	
			if(StrUtil.isEmpty(json.getString("useraccount"))){
				renderJson(JsonUtil.getMap(202, "账号错误"));
				return;
			}			
			T1doFw t1doFw =T1doFw.getIdoFw(json.getString("useraccount"));
			boolean isfw=t1doFw==null?false:true;
			JSONObject json1=TRegUser.getUser(json);
			if(json1==null){
			    json1=HttpUtil.loginIm(json.getString("useraccount"));
				System.out.println(json1);
				String str=HttpUtil.getParameter1(json1, "/Base-Module/CompanyUser",json1.getString("loginName"));			
				JSONObject json3= HttpUtil.doPost2("http://xcgovapi.hzxc.gov.cn/Base-Module/CompanyUser", str);
				System.out.println(json3);
				json1.put("D_NAME", json3.get("D_NAME"));
				json1.put("U_DEPT_ID", json3.get("U_DEPT_ID"));
				TRegUser.saveUser(json1);
			}
			json1.put("isfw", isfw);
			setSessionAttr("1doUser", json1);
			renderJson(json1);
				
			
		}
		
		public void login1do1() {
			
			JSONObject json=JsonUtil.getJSONObject(getRequest());	
			if(StrUtil.isEmpty(json.getString("useraccount"))){
				renderJson(JsonUtil.getMap(202, "账号错误"));
				return;
			}			
			T1doFw t1doFw =T1doFw.getIdoFw(json.getString("useraccount"));
			boolean isfw=t1doFw==null?false:true;
			JSONObject json1=HttpUtil.loginIm(json.getString("useraccount"));
			//JSONObject json2=HttpUtil.doGet1("http://xcgov.hzxc.gov.cn/Base-Module/CompanyUser?showId="+json1.getString("loginName"),json1.getString("LoginToken"),json1.getString("loginName"));
			T1doUser user=T1doUser.dao.findFirst("SELECT * from t_1do_user WHERE SHOW_ID=? ",json1.getString("loginName"));			
			if(user==null){				
				//JSONObject json2=HttpUtil.doGet1("http://xcgov.hzxc.gov.cn/Base-Module/CompanyUser?showId="+json1.getString("loginName"),json1.getString("LoginToken"),json1.getString("loginName"));
				String str=HttpUtil.getParameter1(json1, "/Base-Module/CompanyUser",json1.getString("loginName"));			
				JSONObject json2   =   HttpUtil.doPost2("http://xcgovapi.hzxc.gov.cn/Base-Module/CompanyUser", str);		
				T1doUser user1=json2.toJavaObject(T1doUser.class);
				@SuppressWarnings("unchecked")
				List<String> arr=(List<String>) json2.get("D_NAME");
				user1.setDName(arr.get(0));
				@SuppressWarnings("unchecked")
				List<String> arr1=(List<String>) json2.get("U_DEPT_ID");
				user1.setUDeptId(arr1.get(0));
				user1.save();
				json1.put("D_NAME", json2.get("D_NAME"));
				json1.put("U_DEPT_ID", json2.get("U_DEPT_ID"));
			}else{
				List<String> list=new ArrayList<String>();
				list.add(user.getDName());
				json1.put("D_NAME", list);
				List<String> list1=new ArrayList<String>();
				list1.add(user.getUDeptId());
				json1.put("U_DEPT_ID", list1);
			}
			json1.put("isfw", isfw);
			json1.put("useraccount", json.getString("useraccount"));
			getSession().setMaxInactiveInterval(28800);//单位秒
			setSessionAttr("1doUser", json1);
			renderJson(json1);
			
			
		}
		
		//通讯录获取最近联系人
		public void GetContact() {
			JSONObject douser=getSessionAttr("1doUser");
			String str=HttpUtil.getParameter(douser, "/Base-Module/CompanyUser/GetContact");
			System.out.println(str);
			String result   =   HttpUtil.doPost1("http://xcgovapi.hzxc.gov.cn/Base-Module/CompanyUser/GetContact", str);
			//String str=HttpUtil.doGet("http://xcgov.hzxc.gov.cn/Base-Module/CompanyUser/GetContact?size=100",json.getString("LoginToken"),json.getString("loginName"));
			renderJson(result);
		}
		//通讯录获取部门和部门人员列表
		public void GetList() {
			JSONObject douser=getSessionAttr("1doUser");
			JSONObject json2=JsonUtil.getJSONObject(getRequest());
			String str=HttpUtil.getParameter(douser, "/Base-Module/CompanyDept/GetList",json2.getIntValue("isContainChildDeptMember"),json2.getString("parentId"));

			String result   =   HttpUtil.doPost1("http://xcgovapi.hzxc.gov.cn/Base-Module/CompanyDept/GetList", str);

			//String str=HttpUtil.doGet("http://xcgov.hzxc.gov.cn/Base-Module/CompanyDept/GetList?isContainChildDeptMember="+json2.getIntValue("isContainChildDeptMember")+"&parentId="+json2.getString("parentId"),json.getString("LoginToken"),json.getString("loginName"));
			
			renderJson(result);
		}
		public void GetListUser() {
			JSONObject json2=JsonUtil.getJSONObject(getRequest());
			JSONObject douser=getSessionAttr("1doUser");
			//String str=HttpUtil.doGet("http://xcgov.hzxc.gov.cn/Base-Module/CompanyUser/GetList?isContainChildDeptMember="+json2.getIntValue("isContainChildDeptMember")+"&sortColumn=U_DEPT_SORT&sortAscending=true&deptId="+json2.getString("deptId"),json.getString("LoginToken"),json.getString("loginName"));
			String str=HttpUtil.getParameter1(douser, "/Base-Module/CompanyUser/GetList",json2.getIntValue("isContainChildDeptMember"),json2.getString("deptId"));
			String result   =   HttpUtil.doPost1("http://xcgovapi.hzxc.gov.cn/Base-Module/CompanyUser/GetList", str);
			renderJson(result);
		}
		public void searchUser() {
			JSONObject json2=JsonUtil.getJSONObject(getRequest());
			JSONObject douser=getSessionAttr("1doUser");
	
			//String str=HttpUtil.doGet("http://xcgov.hzxc.gov.cn/Base-Module/CompanyUser/GetList?isContainChildDeptMember="+json2.getIntValue("isContainChildDeptMember")+"&sortColumn=U_DEPT_SORT"
			//		+ "&sortAscending=true&createPage="+json2.getIntValue("createPage")+"&pageSize="+json2.getIntValue("pageSize")+"&searchKey="+json2.getString("searchKey"),json.getString("LoginToken"),json.getString("loginName"));
			String str=HttpUtil.getParameter1(douser, "/Base-Module/CompanyUser/GetList",json2.getIntValue("isContainChildDeptMember"),json2.getIntValue("createPage"),json2.getIntValue("pageSize"),json2.getString("searchKey"));
			String result   =   HttpUtil.doPost1("http://xcgovapi.hzxc.gov.cn/Base-Module/CompanyUser/GetList", str);
			renderJson(result);
		}
		
		//通讯录获取用户信息
		public void CompanyUser() {
			JSONObject json2=JsonUtil.getJSONObject(getRequest());
			JSONObject douser=getSessionAttr("1doUser");
			//String str=HttpUtil.doGet("http://xcgov.hzxc.gov.cn/Base-Module/CompanyUser?showId="+json2.getString("SHOW_ID"),json.getString("LoginToken"),json.getString("loginName"));
			String str=HttpUtil.getParameter1(douser, "/Base-Module/CompanyUser",json2.getString("SHOW_ID"));

			String result   =   HttpUtil.doPost1("http://xcgovapi.hzxc.gov.cn/Base-Module/CompanyUser", str);
			renderJson(result);
		}
		/*
		 2018年6月28日上午3:50:16 coco  //模拟1do登出
		 */
		public void exit1do() {
			removeSessionAttr("1doUser");
			renderJson(JsonUtil.getMap(200, "退出成功"));
		}
		/*
		 2018年6月25日下午3:37:00 coco  //1do详情
		*/
		@Before(Tx.class)
		public void getIdoMessage() {
	  
	    
	    	JSONObject json=JsonUtil.getJSONObject(getRequest()); 	
			final T1doBase t1doBase=T1doBase.getIdoBase2(json.getString("SHOW_ID"));
			JSONObject douser1=getSessionAttr("1doUser");
			if(json.getString("loginName")==null&&douser1==null){
				renderJson(JsonUtil.getMap(400, "未登入"));
			}
			final String loginName=douser1==null?json.getString("loginName"):douser1.getString("loginName");
			if((t1doBase.getOIsDeleted()==2&&StrUtil.isNotEmpty(json.getString("loginName")))||(t1doBase.getOIsDeleted()==2&&!douser1.getBooleanValue("isfw"))){
				Db.update("update t_1do_pstatus set isSend=1 where SHOW_ID=? and O_USER=? and isDelete=1 and USER_TYPE!=2 and online=2",t1doBase.getShowId(),loginName);
				Db.update("update t_1do_fwpstatus set isSend=1 where SHOW_ID=? and O_USER=? and online=2",t1doBase.getShowId(),loginName);
				renderJson(JsonUtil.getMap(200, "该1do已删除"));
				return;
			}
			final String username=douser1.getString("username");
			t1doBase.setLIGHTNING(t1doBase.getIdoFeedbacks44().size());
			//修改
			int i=Db.update("update t_1do_pstatus set isSend=1 where SHOW_ID=? and O_USER=? and isDelete=1 and USER_TYPE!=2 and online=2",t1doBase.getShowId(),loginName);
			int j=Db.update("update t_1do_fwpstatus set isSend=1 where SHOW_ID=? and O_USER=? and online=2",t1doBase.getShowId(),loginName);
			//在线已读
			Db.update("update t_1do_pstatus set online=1,isSend=1 where SHOW_ID=? and O_USER=?",json.getString("SHOW_ID"),loginName);
			Db.update("update t_1do_fwpstatus set online=1,isSend=1 where SHOW_ID=? and O_USER=?",json.getString("SHOW_ID"),loginName);
			//修改创建用户
			Db.update("update t_1do_base set CREATE_USER=?,CREATE_USER_NAME=? where CREATE_USER='1call' and SHOW_ID=?",loginName,username,t1doBase.getShowId());
			
			T1doPstatus t2=T1doPstatus.getCustomerOrExecutor(json.getString("SHOW_ID"),loginName,3);
			//查询该1do参与人是否查看过
			Record r=Db.findFirst("select * from t_1do_log a,t_1do_pstatus b where a.SHOW_ID=b.SHOW_ID and a.SHOW_ID=? and b.USER_TYPE=3 and a.O_USER=b.O_USER",json.getString("SHOW_ID"));
			new T1doLog().setShowId(json.getString("SHOW_ID")).setOUser(loginName)
			.setOUserName(username).setOpTime(new Date()).
			setLog(username+"查看此1do").setLogType(2).save();
           if(t2!=null&&r==null){
        	   t1doBase.setSendTime(new Date().getTime()).update();
           	new Thread(new Runnable() {
   				@Override
   				public void run() {
   					 sendIdo(t1doBase,2,loginName);//群发通知
   				}
   			}).start();         	
			}else if(i>=1){
				new Thread(new Runnable() {
   				@Override
   				public void run() {
   					sendOneIdo(t1doBase,2,loginName,username);//单独发通知i 1加入2查看3反馈

   				}
   			}).start();
			}else if(j>=1){
				new Thread(new Runnable() {
   				@Override
   				public void run() {
   					fwsendOneIdo(t1doBase,2,loginName,username);//整理层单独发通知i 1加入2查看3反馈

   				}
   			}).start();
			}
			//t1doBase.put("O_STATUS", t1doBase.getIdoStatus().getOStatus());
			t1doBase.put("O_STATUS", t1doBase.getOStatus());
			t1doBase.put("ccp", t1doBase.getUser(2));
			t1doBase.put("executor", t1doBase.getUser(1));
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();  
			t1doBase.setLOOKNUM(t1doBase.getLOOKNUM()+1).update();
			map.put("BASE", t1doBase);
			renderJson(map);
		}
		
		/*
		 2018年6月27日下午3:07:59 coco  //1do详情修改发起时间/完成时间
		*/
		 @Before(Tx.class)
		public void changeTime() {
			JSONObject json=JsonUtil.getJSONObject(getRequest());
			JSONObject douser=getSessionAttr("1doUser");
	    	T1doPstatus t1=T1doPstatus.getUser(json.getString("SHOW_ID"),douser.getString("loginName"));   
			if(douser.getBooleanValue("isfw")||StrUtil.getflag(t1.getUserType(), 3)){
				   T1doBase t=json.toJavaObject(T1doBase.class);
					t.updateTime(json);
					renderJson(JsonUtil.getMap(200, "修改成功"));
				}else{
					renderJson(JsonUtil.getMap(202, "权限不足"));
				}
			
		}
		  //1do详情修改标题或者内容

		 @Before(Tx.class)
		 public void changeText() {
			 JSONObject json=JsonUtil.getJSONObject(getRequest());
			 JSONObject douser=getSessionAttr("1doUser");
			 T1doPstatus t1=T1doPstatus.dao.findFirst("select * from t_1do_pstatus where SHOW_ID=? and O_USER=? and USER_TYPE !=2",json.getString("SHOW_ID"),douser.getString("loginName"));
			 if(douser.getBoolean("isfw")||StrUtil.getflag(t1.getUserType(), 1)){
				T1doBase t=T1doBase.getIdoBase(json.getString("SHOW_ID"));
				int i= Db.update("UPDATE t_1do_base set "+json.getString("target")+"=? ,AT=? where SHOW_ID=?",json.getString("content"),json.getString("AT"),json.getString("SHOW_ID"));
				if(i==1){
					 T1doLog.saveLog(json.getString("SHOW_ID"),douser.getString("loginName"),douser.getString("username"), douser.getString("username")+"修改此1do", 14,t.getODescribe());
					 renderJson(JsonUtil.getMap(200, "修改成功"));
				 }else{
					 renderJson(JsonUtil.getMap(201, "修改失败"));
				 }
			}else{
				renderJson(JsonUtil.getMap(202, "权限不足"));
			}
			
			
		 }
		/*
		 2018年6月27日下午3:07:59 coco  //1do详情修改发起人/参与人/抄送人(2018.12.4需要修改重复添加删除人员是出现重复问题。)
		 */
		 @Before(Tx.class)
		public void changeUser() {
			JSONObject json=JsonUtil.getJSONObject(getRequest());
			//T1doBase t1doBase=T1doBase.getIdoBase(json.getString("SHOW_ID"));
			JSONObject douser=getSessionAttr("1doUser");
			 T1doPstatus t1=T1doPstatus.getUser(json.getString("SHOW_ID"),douser.getString("loginName"));
			boolean flag=json.getString("object").equals("参与人")?true:douser.getBoolean("isfw")||StrUtil.getflag(t1.getUserType(), 2);
			 if(flag){
			
			T1doBase t1doBase =T1doBase.getIdoBase(json.getString("SHOW_ID"));
			
			//T1doStatus t1doStatus=t1doBase.getIdoStatus();
			T1doPstatus t= new T1doPstatus();
			int i1=0;
			if(json.getString("object").equals("发起人")){
				i1=1;
				t1doBase.setOCustomer(StrUtil.getUser1(t1doBase.getOCustomer(), json.getString("useraccount"), json.getString("method")))
				.setOCustomerName(StrUtil.getUser1(t1doBase.getOCustomerName(), json.getString("username"), json.getString("method"))).update();
			    t.setUserType(1);
			    if(t1doBase.getOStatus()==3){
			    	t.setOStatus(1);
			    }else if(t1doBase.getOStatus()==4){
			    	t.setOStatus(4);
			    }else{
			    	t.setOStatus(5);
			    }
			}else if(json.getString("object").equals("参与人")){
				i1=3;
				t1doBase.setOExecutor(StrUtil.getUser1(t1doBase.getOExecutor(), json.getString("useraccount"), json.getString("method")))
				.setOExecutorName(StrUtil.getUser1(t1doBase.getOExecutorName(), json.getString("username"), json.getString("method"))).update();
				t.setUserType(3);
			    t.setOStatus(t1doBase.getOStatus());
			}else{
				i1=4;
				t1doBase.setCC(StrUtil.getUser1(t1doBase.getCC(), json.getString("useraccount"), json.getString("method")))
				.setCcName(StrUtil.getUser1(t1doBase.getCcName(), json.getString("username"),json.getString("method"))).update();
				t.setUserType(4);
				t.setOStatus(t1doBase.getOStatus());
			}
			if(json.getString("method").equals("cover")){
				String[] temp =json.getString("useraccount").split(";");
				String[] temp1 =json.getString("username").split(";");
				String s="";
				for (String string : temp) {
					s+="'"+string+"',"		;
				}
				s=s.substring(0, s.length()-1);
				List<T1doPstatus> ts=T1doPstatus.dao.find("select * from t_1do_pstatus where isDelete=1 and SHOW_ID=? and O_USER not in("+s+") and USER_TYPE=?",t1doBase.getShowId(),i1);
				for (int i = 0; i < ts.size(); i++) {
					//String string = ts[i];
					T1doLog.saveLog(json.getString("SHOW_ID"), douser.getString("loginName"), douser.getString("username"), douser.getString("username")+"移除"+ts.get(i).getOUserName()+"出此1do", 8,ts.get(i).getOUserName());
				
				}
				Db.update("update t_1do_pstatus set isDelete=2  where SHOW_ID=? and O_USER not in("+s+") and USER_TYPE=?",t1doBase.getShowId(),i1);
				for(int j = 0; j < temp.length; j++){
					T1doPstatus t12=T1doPstatus.dao.findFirst("select * from t_1do_pstatus where isDelete=1 and SHOW_ID=? and O_USER=? and USER_TYPE=?",t1doBase.getShowId(),temp[j],i1);
					if(t12!=null){
						continue;
					}				
					int i=Db.update("update t_1do_pstatus set isDelete=1 where SHOW_ID=? and O_USER=? and USER_TYPE=?",t1doBase.getShowId(),temp[j],i1);
					 if(i==0){
						 
						t.setShowId(json.getString("SHOW_ID")).setOUser(temp[j]).setOUserName(temp1[j]).save();
	
							sendOneIdo(t1doBase,1,temp[j],temp1[j]);//单独发通知i 1加入2查看3反馈
						t.remove("ID");
					 }
					T1doLog.saveLog(json.getString("SHOW_ID"), douser.getString("loginName"), douser.getString("username"), douser.getString("username")+"邀请"+temp1[j]+"进入此1do", 9,temp1[j]);

				}
				
			}else if(json.getString("method").equals("remove")){	
				Db.update("update t_1do_pstatus set isDelete=2  where SHOW_ID=? and O_USER=? and USER_TYPE=?",t1doBase.getShowId(),json.getString("useraccount"),i1);
				T1doLog.saveLog(t1doBase.getShowId(), douser.getString("loginName"), douser.getString("username"), douser.getString("username")+"移除"+json.getString("username")+"出此1do", 8,json.getString("username"));
			}else{
				String[] temp =json.getString("useraccount").split(";");
				String[] temp1 =json.getString("username").split(";");
				for (int j = 0; j < temp.length; j++) {
					int i=Db.update("update t_1do_pstatus set isDelete=1 where SHOW_ID=? and O_USER=? and USER_TYPE=?",t1doBase.getShowId(),temp[j],i1);
				 if(i==0){
					 
					t.setShowId(json.getString("SHOW_ID")).setOUser(temp[j]).setOUserName(temp1[j]).save();
					T1doLog.saveLog(json.getString("SHOW_ID"), douser.getString("loginName"), douser.getString("username"), douser.getString("username")+"邀请"+temp1[j]+"进入此1do", 9,temp1[j]);

						sendOneIdo(t1doBase,1,temp[j],temp1[j]);//单独发通知i 1加入2查看3反馈
					t.remove("ID");
				 }
				}
				
				
			}
			renderJson(JsonUtil.getMap(200, "修改成功"));
			}else{
				renderJson(JsonUtil.getMap(202, "权限不足"));
			}
		}
		//置顶
		 @Before(Tx.class)
		public void top() {
			JSONObject json=JsonUtil.getJSONObject(getRequest());
			JSONObject douser=getSessionAttr("1doUser"); 	
			T1doOrder t=json.toJavaObject(T1doOrder.class);
			T1doOrder t1=T1doOrder.getT1doOrder(json.getString("SHOW_ID"),douser.getString("loginName"),json.getIntValue("type"));
			if(t1==null){
				t.setUseraccount(douser.getString("loginName")).save();
				renderJson(JsonUtil.getMap(200, "置顶成功"));
			}else{
				t1.delete();
				//t1.setModifyTime(new Date()).update();
				renderJson(JsonUtil.getMap(200, "取消置顶成功"));

			}
		}
		
		
		
	    /*
		 2018年6月27日下午2:26:01 coco   //判断是否是整理层
		*/
		public void isfw() {
			JSONObject douser=getSessionAttr("1doUser"); 	
			T1doFw t1doFw =T1doFw.getIdoFw(douser.getString("loginName"));
			if(t1doFw==null){
				renderJson(JsonUtil.getMap(201, false));
			}else{
				renderJson(JsonUtil.getMap(200, true));
			}
			
		}
	    /*
		 2018年6月26日下午11:20:20 coco  //获得流程
		*/
		public void getProcess() {
			JSONObject json=JsonUtil.getJSONObject(getRequest()); 	
			//T1doBase t1doBase =T1doBase.getIdoBase(json.getString("SHOW_ID"));
			T1doBase t1doBase =T1doBase.dao.findFirst("select * from t_1do_base where SHOW_ID=?", json.getString("SHOW_ID"));
			List<T1doFeedback> t1doFeedbacks=t1doBase.getIdoFeedbacks2();//反馈
			String str=StrUtil.getStr(t1doFeedbacks);
			List<T1doFeedback> t1doFeedbacks4=t1doBase.getIdoFeedbacks4();//催办
			List<T1doFeedback> t1doFeedbacks44=t1doBase.getIdoFeedbacks44();//催办
			String str4=StrUtil.getStr(t1doFeedbacks4);
			T1doFeedback t1doFeedbacks5=t1doBase.getIdoFeedbacks5();//办结
			
			List<T1doFeedback> t1doFeedbacks6=t1doBase.getIdoFeedbacks6();//评价
			String str6=StrUtil.getStr(t1doFeedbacks6);
			List<T1doBase> sonT1doBase=t1doBase.getSonIdoBase1();
		//	LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(); 
			 String[] oc=t1doBase.getOCustomerName().split(";");
			 List<Record> list = new ArrayList<Record>();
			 List<Record> list1 = new ArrayList<Record>();
			 for(String co1:oc){
				 Record r=new Record();
				 r.set("user", co1);
				 r.set("time", t1doBase.getOStartTime());
				 list.add(r);
			 }
			 List<LinkedHashMap<String, Object>> all = new ArrayList<LinkedHashMap<String, Object>>();
			 LinkedHashMap<String, Object> map1 = new LinkedHashMap<String, Object>();
			 map1.put("title", list);
			 map1.put("type", 1);
			 all.add(map1);
			 LinkedHashMap<String, Object> map2 = new LinkedHashMap<String, Object>();
			 Record r=new Record();
			 r.set("user", t1doBase.getCreateUserName());
			 r.set("time", t1doBase.getOCreateTime());
			 list1.add(r);
			 map2.put("title", list1);
			 map2.put("type", 2);
			 all.add(map2);
			 T1doFeedback t1=T1doFeedback.dao.findFirst("select * from t_1do_feedback where SHOW_ID=?",t1doBase.getShowId());
			 T1doFeedback t2=T1doFeedback.dao.findFirst("select * from t_1do_feedback where SHOW_ID=? and FB_TYPE !=4",t1doBase.getShowId());//反馈
			 T1doFeedback t3=T1doFeedback.dao.findFirst("select * from t_1do_feedback where SHOW_ID=? and FB_TYPE=4",t1doBase.getShowId());//催报
			// T1doBase sonT1doBase2=t1doBase.getSonIdoBase2();
			 T1doBase sonT1doBase3=t1doBase.getSonIdoBase3();//拆项
			    int[] i=new int[]{3,4,5};
				if(t2==null&&t3==null&&sonT1doBase3==null){
					i=new int[]{3,4,5};
				}else if(t2==null&&t3!=null&&sonT1doBase3==null){
					i=new int[]{4,3,5};
				}else if(t2==null&&t3==null&&sonT1doBase3!=null){
					i=new int[]{5,3,4};
				}else if(t2!=null&&t3==null&&sonT1doBase3==null){
					i=new int[]{3,4,5};
				}else if(t2!=null&&t3!=null&&sonT1doBase3==null){
					if(t1.getFbType()==4){
						i=new int[]{4,3,5};
					}else{
					    i=new int[]{3,4,5};
					}
				}else if(t2!=null&&t3==null&&sonT1doBase3!=null){
					if(t2.getFbTime().before(sonT1doBase3.getOCreateTime())){
						 i=new int[]{3,5,4};
					}else{
						i=new int[]{5,3,4};
					}
					
				} else if(t2==null&&t3!=null&&sonT1doBase3!=null){
					if(t3.getFbTime().before(sonT1doBase3.getOCreateTime())){
						 i=new int[]{4,5,3};
					}else{
						i=new int[]{5,4,3};
					}
					
				}else{
					if(t1.getFbType()==4){
						if(t2.getFbTime().before(sonT1doBase3.getOCreateTime())){
							i=new int[]{4,3,5};
						}else if(t3.getFbTime().before(sonT1doBase3.getOCreateTime())){
							i=new int[]{4,5,3};
						}else{
							i=new int[]{5,4,3};
						}
						
					}else{
						if(t3.getFbTime().before(sonT1doBase3.getOCreateTime())){
							i=new int[]{3,4,5};
						}else if(t2.getFbTime().before(sonT1doBase3.getOCreateTime())){
							i=new int[]{3,5,4};
						}else{
							i=new int[]{5,3,4};
						}
					}
				}
			for(int num:i){
				if(num==3){
					LinkedHashMap<String, Object> map3 = new LinkedHashMap<String, Object>();
					 map3.put("title", str);
					 map3.put("type", 3);
					 all.add(map3);
				}else if(num==4){
					LinkedHashMap<String, Object> map4 = new LinkedHashMap<String, Object>();
					 map4.put("title", str4);
					 map4.put("type", 4);
					 map4.put("num", t1doFeedbacks44.size());
					 all.add(map4);
				}else{
					LinkedHashMap<String, Object> map5 = new LinkedHashMap<String, Object>();
					 map5.put("son", sonT1doBase);
					 T1doBase sonT1doBase23=t1doBase.getSonIdoBase23();
					 List<Record> list11 = new ArrayList<Record>();
					 if(sonT1doBase23!=null){
						 Record r1=new Record();
						 r1.set("user", sonT1doBase23.getCreateUserName());
						 r1.set("time", sonT1doBase23.getOCreateTime());
						 list11.add(r1);
					 }
		
					 map5.put("title", list11);
					 
					 map5.put("type", 5);
					 all.add(map5);
				}
			}
			 //map.put("3", str);
			// map.put("4", str4);
			// map.put("5", array);
			LinkedHashMap<String, Object> map6 = new LinkedHashMap<String, Object>();
			
		     map6.put("title",t1doFeedbacks5==null?"":t1doFeedbacks5.getFBCONTENT());
			 
			 map6.put("type", 6);
			 all.add(map6);
			 LinkedHashMap<String, Object> map7 = new LinkedHashMap<String, Object>();
			 map7.put("title", str6);
			 map7.put("type", 7);
			 all.add(map7);
			
			renderJson(all); 
		}
		/*
		 2018年6月26日下午11:20:20 coco  //获得操作日志
		 */
		public void getLog() {
			JSONObject json=JsonUtil.getJSONObject(getRequest()); 	
			T1doBase t1doBase =json.toJavaObject(T1doBase.class);
			List<T1doLog> t1doLogs=t1doBase.getIdoLogs1();
			renderJson(t1doLogs); 
		}
	    /*
		 2018年6月26日下午10:50:37 coco  //获得反馈消息。
		*/
		public void getFeedback() { 
			JSONObject json=JsonUtil.getJSONObject(getRequest()); 	
			T1doBase t1doBase =json.toJavaObject(T1doBase.class);
			List<T1doFeedback> t1doFeedbacks=t1doBase.getIdoFeedbacks1(json.getIntValue("id"),json.getIntValue("num"));
		  //  List<T1doFeedback> getIdoFeedbacks11= T1doFeedback.dao.find("select  O_USER_NAME from t_1do_feedback where SHOW_ID=? and FB_TYPE!=4",json.getString("SHOW_ID"));
		   // int count=T1doFeedback.getNum(json.getString("SHOW_ID"));
		    setSessionAttr(json.getString("SHOW_ID"), T1doFeedback.getNum(json.getString("SHOW_ID")));
			renderJson(t1doFeedbacks);
		}
		/*
		 2018年7月1日下午8:34:11 coco  //轮询
		*/
		public void polling() throws InterruptedException {
	         JSONObject json=JsonUtil.getJSONObject(getRequest());
	         Long time=new Date().getTime();
	         String str=json.getString("SHOW_ID");
	         //if(json.getBooleanValue(key))
	         setSessionAttr(str+"A", json.getBooleanValue("flag"));
		     // 死循环 查询有无数据变化
        	 List<T1doFeedback> getIdoFeedbacks12=new ArrayList<T1doFeedback>();
		     while ((boolean) getSessionAttr(str+"A")) {
		      List<T1doFeedback> getIdoFeedbacks11= T1doFeedback.dao.find("select O_USER_NAME from t_1do_feedback where SHOW_ID=? and FB_TYPE!=4 and isoverdue=1",str);
				int i=(int)getSessionAttr(str);
		      int n=getIdoFeedbacks11.size()-i;
		      if(n!=0){
				     getIdoFeedbacks12=T1doFeedback.dao.find("select ID,O_USER_NAME,TIME_STAMP,FB_TIME,O_USER,FBCONTENT,FB_TYPE,FB_USER_NAME,FB_USER,ATTR_PATH,star,AT from t_1do_feedback where SHOW_ID=? and FB_TYPE!=4  and isoverdue=1 LIMIT ?,?",str,i,n);;
				   
				     renderJson(getIdoFeedbacks12); 	 	
		             setSessionAttr(str, getIdoFeedbacks11.size());
		             return; // 跳出循环，返回数据
		         } // 模拟没有数据变化，将休眠 hold住连接
		             Thread.sleep(1000);
		             Long time1=new Date().getTime();
		             if(time1-time>40000){
		            	 renderJson(getIdoFeedbacks12);
		            	 return;
		             }
		             
		         
		     }
		     renderJson(getIdoFeedbacks12);
		}
		public void closeIdo() {
			JSONObject json=JsonUtil.getJSONObject(getRequest()); 	
			JSONObject douser=getSessionAttr("1doUser");
			Db.update("update t_1do_pstatus set online=2 where SHOW_ID=? and O_USER=?",json.getString("SHOW_ID"),douser.getString("loginName"));
			Db.update("update t_1do_fwpstatus set online=2 where SHOW_ID=? and O_USER=?",json.getString("SHOW_ID"),douser.getString("loginName"));
			renderJson(JsonUtil.getMap(200, "下线成功"));
	
		}
	    
		//附件反馈

		@Before(Tx.class)
		public void feedbackUpload() throws IOException {
			MultipartParser mp = new MultipartParser(this.getRequest(), 52428800, false, false,"UTF-8");//52428800=50*1024*1024
		//	List<UploadFile> uploadFiles=this.getFiles("FILE");//获取前台上传文件对象
			List<String> uploadFiles=new ArrayList<String>();
			List<String> uploadFiles1=new ArrayList<String>();
			
			//String base = null;
			Part part;
			// Map<String, String > map =  new HashMap<String, String>();
			 JSONObject json=new JSONObject();
			while((part=mp.readNextPart())!=null){
				String name = part.getName();
				if(part.isParam()){
					ParamPart param=(ParamPart) part;
					// base=param.getStringValue();
					 String value=param.getStringValue();
					 json.put(name, value);
				}else if(part.isFile()){
					FilePart filePart = (FilePart) part;
				//	uploadFiles.add(filePart);
					String fileName=IDUtil.getUid()//UUID.randomUUID().toString()
							+ filePart.getFileName().substring(filePart.getFileName().lastIndexOf("."));
					File t1 = new File("D:\\1do\\upload\\");//设置本地上传文件对象（并重命名）
					File t = new File(t1,fileName);//设置本地上传文件对象（并重命名）
					FileOutputStream out = new FileOutputStream(t);
	        		Streams.copy(filePart.getInputStream(), out, true);
	        		uploadFiles.add(fileName);
	        		uploadFiles1.add(filePart.getFileName());
				}
			}
		
			JSONObject douser=getSessionAttr("1doUser");
			//JSONObject json1=JSON.parseObject("{\"useraccount\": \"fangshengqun\",\"username\": \"coco\"}");
			T1doFeedback t1doFeedback =json.toJavaObject(T1doFeedback.class);
			t1doFeedback.setFbTime(new Date()).setTimeStamp(new Date().getTime()).setOUser(douser.getString("loginName")).setOUserName(douser.getString("username"));
			T1doAttr t1doAttr=t1doFeedback.getIdoAttr();
			String attrId="";
			String fn="";
			String ATTR_PATH="";
			//for(HashMap<String,String> uploadFile:uploadFiles){
		    for (int i = 0; i < uploadFiles.size(); i++) {
		
	
				
				t1doAttr.setAttrPath("https://tyhy.hzxc.gov.cn:8443/1do/upload/"+uploadFiles.get(i));
				t1doAttr.setAttrName(uploadFiles1.get(i));
				fn+=uploadFiles1.get(i);
				ATTR_PATH+="https://tyhy.hzxc.gov.cn:8443/1do/upload/"+uploadFiles.get(i);
				t1doAttr.save();
				//t1doAttr.setAttrOrder(t1doAttr.getID()).update();
				attrId+=t1doAttr.getID();
				t1doAttr.remove("ID");
				T1doLog.saveLog(t1doFeedback.getShowId(), douser.getString("loginName"), douser.getString("username"), douser.getString("username")+"上传"+uploadFiles1.get(i), 3, uploadFiles1.get(i));
			}
			t1doFeedback.setATTRID(attrId).setAttrPath(ATTR_PATH).setFBCONTENT(fn).save();
			//T1doLog t1doLog=t1doFeedback.getIdoLog();
			if(t1doFeedback.getFbType()==2){
				t1doFeedback.setFBCONTENT("回复"+t1doFeedback.getFbUserName()+" "+fn).update();
			}
			feedback1(t1doFeedback,douser);
			//t1doLog.save();
			renderJson(MsgUtil.successMsg(t1doFeedback));
			//renderJson(JsonUtil.getMap(200, "反馈成功"));
		}
		
		public static void feedback1(final T1doFeedback t1doFeedback,final JSONObject douser) {
			//JSONObject json=JsonUtil.getJSONObject(getRequest());
			//final JSONObject douser=getSessionAttr("1doUser");
			//final T1doFeedback t1doFeedback =json.toJavaObject(T1doFeedback.class);
			//查询是否是参与人
			final T1doBase t1doBase=t1doFeedback.getT1doBase();
			T1doPstatus t1doPstatus=T1doPstatus.getCustomerOrExecutor(t1doFeedback.getShowId(),douser.getString("loginName"),3);
			//T1doStatus t1doStatus=t1doFeedback.getIdoStatus();
			//T1doPstatus t1=T1doPstatus.getUser(t1doFeedback.getShowId(),douser.getString("loginName"));
			//t1doFeedback.save();
				if(t1doPstatus!=null&&t1doBase.getOStatus()==3){
					t1doBase.setOStatus(4).update();
					t1doPstatus.setOStatus(4).update();
					t1doBase.setSendTime(new Date().getTime()).setFBNUM(t1doBase.getFBNUM()+1).update();
					Db.update("update t_1do_pstatus set O_STATUS=4 where SHOW_ID=? and USER_TYPE!=3",t1doFeedback.getShowId());
					Db.update("update t_1do_pstatus set isSend=2  where  online=2 and SHOW_ID=? and (USER_TYPE=1 or O_USER=? ) and isDelete=1",t1doFeedback.getShowId(),douser.getString("loginName"));
					Db.update("update t_1do_fwpstatus set isSend=2 where SHOW_ID=? and online=2 ",t1doFeedback.getShowId());
					new Thread(new Runnable() {
						@Override
						public void run() {
							sendIdo(t1doBase,3,douser.getString("loginName"));
						}
					}).start(); 
					
				}else{
					t1doBase.setSendTime(new Date().getTime()).setFBNUM(t1doBase.getFBNUM()+1).update();
					Db.update("update t_1do_pstatus set isSend=2 where SHOW_ID=? and isDelete=1 and online=2",t1doFeedback.getShowId());
					Db.update("update t_1do_fwpstatus set isSend=2 where SHOW_ID=? and online=2",t1doFeedback.getShowId());
					final int temp=t1doBase.getOStatus()==3?1:t1doBase.getOStatus();									
					new Thread(new Runnable() {
						@Override
						public void run() {
							sendIdo(t1doBase,temp,douser.getString("loginName"));
						}
					}).start(); 
				}

				//renderJson(MsgUtil.successMsg(t1doFeedback));
			
			
		}
		//附件单独删除
				@Before(Tx.class)
				public void deleteAttr(){
			    	JSONObject json=JsonUtil.getJSONObject(getRequest()); 	
			    	int i=Db.delete("DELETE from t_1do_attr where attr_path=?",json.getString("ATTR_PATH"));
			    	if(i==1){
						renderJson(JsonUtil.getMap(200, "删除成功"));
			    	}else{
						renderJson(JsonUtil.getMap(201, "删除失败"));
			    	}
					
				}
		//删除或恢复或者重做
		@Before(Tx.class)
		public void deleteAlldo(){
			JSONObject json=JsonUtil.getJSONObject(getRequest()); 	
			JSONObject douser=getSessionAttr("1doUser");
			JSONArray doList=json.getJSONArray("list");
			String loginName=douser.getString("loginName");
			String username=douser.getString("username");
		    for (int j = 0; j < doList.size(); j++) {
				String showID=doList.getString(j);
			T1doPstatus t1=T1doPstatus.getUser(showID,loginName);
			final T1doBase t1doBase=T1doBase.getIdoBase(showID);
			
				if(douser.getBoolean("isfw")||StrUtil.getflag(t1.getUserType(), 9)){
					T1doStatus t=T1doStatus.dao.findFirst("select * from t_1do_status where SHOW_ID=?",showID);
					if(t.getOStatus()==5){
						int i=Db.update("update t_1do_base set O_IS_DELETED=2 ,DELETE_TIME=now() where SHOW_ID=?",showID);	  
						if(i==1){
							T1doLog.saveLog(showID, loginName, username, username+"删除此1do", 12,"");	
				        	   t1doBase.setSendTime(new Date().getTime()).update();
							new Thread(new Runnable() {
								@Override
								public void run() {
									sendIdo(t1doBase,7,"");
								}
							}).start(); 
							System.out.println("删除成功");
						}else{
							System.out.println("删除失败");
						}
					}else{
						System.out.println("任务进行中不能删除");
					}
					
				}else{
					System.out.println("权限不足");

					renderJson(JsonUtil.getMap(202, "权限不足"));
				}
		     }
			renderJson(JsonUtil.getMap(200, "删除成功"));
			}
		//删除或恢复或者重做
		@Before(Tx.class)
		public void deleteIdoOrRestoreIdoOrRedo(){
			JSONObject json=JsonUtil.getJSONObject(getRequest()); 	
			 JSONObject douser=getSessionAttr("1doUser");
			 T1doPstatus t1=T1doPstatus.getUser(json.getString("SHOW_ID"),douser.getString("loginName"));
				final T1doBase t1doBase=T1doBase.getIdoBase(json.getString("SHOW_ID"));
			if(json.getString("result").equals("delete")){//删除
			 if(douser.getBoolean("isfw")||StrUtil.getflag(t1.getUserType(), 9)){
				//T1doStatus t=T1doStatus.dao.findFirst("select * from t_1do_status where SHOW_ID=?",json.getString("SHOW_ID"));
				if(t1doBase.getOStatus()==5){
					//修改
					Db.update("update t_1do_pstatus set isSend=2 where SHOW_ID=? and O_USER=? and isDelete=1 and USER_TYPE!=2 and online=2",t1doBase.getShowId(),douser.getString("loginName"));
					Db.update("update t_1do_fwpstatus set isSend=2 where SHOW_ID=? and O_USER=? and online=2",t1doBase.getShowId(),douser.getString("loginName"));
					int i=Db.update("update t_1do_base set O_IS_DELETED=2 ,DELETE_TIME=now(),O_STATUS=6 where SHOW_ID=?",json.getString("SHOW_ID"));	  
					if(i==1){
						T1doLog.saveLog(json.getString("SHOW_ID"), douser.getString("loginName"), douser.getString("username"), douser.getString("username")+"删除此1do", 12,"");
			        	   t1doBase.setSendTime(new Date().getTime()).update();
						new Thread(new Runnable() {
			 				@Override
			 				public void run() {
			 					sendIdo(t1doBase,7,"");
			 				}
			 			}).start(); 
						renderJson(JsonUtil.getMap(200, "删除成功"));
					  }else{
						renderJson(JsonUtil.getMap(201, "删除失败"));
					  }
				}else{
					renderJson(JsonUtil.getMap(203, "任务进行中不能删除"));
				}
			  
			}else{
				renderJson(JsonUtil.getMap(202, "权限不足"));
			}
		}else if(json.getString("result").equals("Restore")) {//恢复
			if(douser.getBoolean("isfw")){
			int i=Db.update("update t_1do_base set O_IS_DELETED=1 ,DELETE_TIME=null,O_STATUS=5 where SHOW_ID=?",json.getString("SHOW_ID"));
			if(i==1){
				T1doLog.saveLog(json.getString("SHOW_ID"), douser.getString("loginName"), douser.getString("username"), douser.getString("username")+"恢复此1do", 13,"");
	        	   t1doBase.setSendTime(new Date().getTime()).update();
				new Thread(new Runnable() {
	 				@Override
	 				public void run() {
	 					sendIdo(t1doBase,8,"");
	 				}
	 			}).start(); 
				renderJson(JsonUtil.getMap(200, "恢复成功"));
			  }else{
				renderJson(JsonUtil.getMap(201, "恢复失败"));
			  }
			}else{
				renderJson(JsonUtil.getMap(202, "权限不足"));
			}
		}else{//重做
			if(douser.getBoolean("isfw")){
			Db.update("update t_1do_base set O_IS_DELETED=1 ,SEND_TIME=?,O_STATUS=3 where SHOW_ID=?",new Date().getTime(),json.getString("SHOW_ID"));	
			Db.update("update t_1do_pstatus set O_STATUS=USER_TYPE,isSend=2 where SHOW_ID=?",json.getString("SHOW_ID"));
			//Db.update("update t_1do_status set O_STATUS=3 where SHOW_ID=?",json.getString("SHOW_ID"));
			Db.update("update t_1do_feedback set isoverdue=2 where SHOW_ID=?",json.getString("SHOW_ID"));
			Db.update("update t_1do_log set isoverdue=2 where SHOW_ID=?",json.getString("SHOW_ID"));
			Db.update("update t_1do_fwpstatus set isSend=2 where SHOW_ID=? ",t1doBase.getShowId());	
			T1doLog.saveLog(json.getString("SHOW_ID"), douser.getString("loginName"), douser.getString("username"), douser.getString("username")+"要求重做此1do", 15,"");
			new Thread(new Runnable() {
 				@Override
 				public void run() {
 					sendIdo(t1doBase,1,"");
 				}
 			}).start(); 
			renderJson(JsonUtil.getMap(200, "该1do已进入重做流程"));
			}else{
				renderJson(JsonUtil.getMap(202, "权限不足"));
			}
		}
			
		}
		//附件单独上传
		
		@Before(Tx.class)
		public void upload() throws IOException {
			MultipartParser mp = new MultipartParser(this.getRequest(), 52428800, false, false,"UTF-8");//52428800=50*1024*1024
			//	List<UploadFile> uploadFiles=this.getFiles("FILE");//获取前台上传文件对象
			List<String> uploadFiles=new ArrayList<String>();
			List<String> uploadFiles1=new ArrayList<String>();
			
			//String base = null;
			Part part;
			// Map<String, String > map =  new HashMap<String, String>();
			JSONObject json=new JSONObject();
			while((part=mp.readNextPart())!=null){
				String name = part.getName();
				if(part.isParam()){
					ParamPart param=(ParamPart) part;
					// base=param.getStringValue();
					String value=param.getStringValue();
					json.put(name, value);
				}else if(part.isFile()){
					FilePart filePart = (FilePart) part;
					//	uploadFiles.add(filePart);
					String fileName=IDUtil.getUid()//UUID.randomUUID().toString()
							+ filePart.getFileName().substring(filePart.getFileName().lastIndexOf("."));
					File t1 = new File("D:\\1do\\upload\\");//设置本地上传文件对象（并重命名）
					File t = new File(t1,fileName);//设置本地上传文件对象（并重命名）
					FileOutputStream out = new FileOutputStream(t);
					Streams.copy(filePart.getInputStream(), out, true);
					uploadFiles.add(fileName);
					uploadFiles1.add(filePart.getFileName());
				}
			}
			JSONObject douser=getSessionAttr("1doUser");
			T1doAttr t1doAttr=new T1doAttr().setShowId(json.getString("SHOW_ID")).setUploadUser(douser.getString("loginName")).setUploadTime(new Date())
					.setIsFb(1);
			//String attrId="";
			String fn="";
			List<T1doAttr> list = new ArrayList<T1doAttr>();
			for (int i = 0; i < uploadFiles.size(); i++) {
			
				t1doAttr.setAttrPath("https://tyhy.hzxc.gov.cn:8443/1do/upload/"+uploadFiles.get(i));
				t1doAttr.setAttrName(uploadFiles1.get(i));
				fn+=uploadFiles1.get(i)+" ";
				t1doAttr.save();
				//t1doAttr.setAttrOrder(t1doAttr.getID()).update();
				list.add(t1doAttr);
				//attrId+=t1doAttr.getID()+";";
				t1doAttr.remove("ID");
				T1doLog.saveLog(json.getString("SHOW_ID"),douser.getString("loginName"),douser.getString("username"),t1doAttr.getUploadUserName()+"上传"+fn, 3, fn); 	
			}
		
			/*T1doLog t1doLog=new T1doLog().setShowId(json.getString("SHOW_ID")).setOUser(douser.getString("loginName")).setOUserName(douser.getString("username"))
					.setOpTime(new Date()).setLogType(3).setLog(douser.getString("username")+"上传"+fn);
			t1doLog.save();*/

			renderJson(list);
		}
		 
		
		/*
		 2018年6月26日上午10:17:23 coco  新建1do保存1
		 */
		@Before(Tx.class)
		public void saveIdo() throws IOException {
			MultipartParser mp = new MultipartParser(this.getRequest(), 52428800, false, false,"UTF-8");//52428800=50*1024*1024
				List<String> uploadFiles=new ArrayList<String>();
				List<String> uploadFiles1=new ArrayList<String>();
				
				//String base = null;
				Part part;
				JSONObject json=new JSONObject();
					while((part=mp.readNextPart())!=null){
						String name = part.getName();
						if(part.isParam()){
							ParamPart param=(ParamPart) part;
							// base=param.getStringValue();
							 String value=param.getStringValue();
							 json.put(name, value);
				    }else if(part.isFile()){
						FilePart filePart = (FilePart) part;
						String fileName=IDUtil.getUid()//UUID.randomUUID().toString()
								+ filePart.getFileName().substring(filePart.getFileName().lastIndexOf("."));
						File t1 = new File("D:\\1do\\upload\\");//设置本地上传文件对象（并重命名）
						File t = new File(t1,fileName);//设置本地上传文件对象（并重命名）
						FileOutputStream out = new FileOutputStream(t);
		        		Streams.copy(filePart.getInputStream(), out, true);
		        		uploadFiles.add(fileName);
		        		uploadFiles1.add(filePart.getFileName());
					}
				}
			JSONObject douser=getSessionAttr("1doUser");
            if(douser==null){
            	renderJson(JsonUtil.getMap(400, "用户未登入"));
            	return;
            }
			final T1doBase t1doBase =json.toJavaObject(T1doBase.class);
			t1doBase.setOStartTime(new Date()).setOCreateTime(new Date()).setCreateUser(douser.getString("loginName"))
			.setCreateUserName(douser.getString("username")).setShowId(IDUtil.getUid()).
			setORange(StrUtil.getOnly(t1doBase.getOCustomer(),t1doBase.getCreateUser(),t1doBase.getOExecutor())).setSendTime(new Date().getTime())
			.setORangeName(StrUtil.getOnly(t1doBase.getOCustomerName(),t1doBase.getCreateUserName(),t1doBase.getOExecutorName()))
			.save(); 
			t1doBase.savefw();//保存整理层为查看通知做准备。

			//new T1doStatus().setShowId(t1doBase.getShowId()).save();//1do状态表
			//new T1doSet().setShowId(t1doBase.getShowId()).setEventType(json.getString("EVENT_TYPE")).save();
			String[] users={t1doBase.getOCustomer(),t1doBase.getCreateUser(),t1doBase.getOExecutor()};
			String[] usernames={t1doBase.getOCustomerName(),t1doBase.getCreateUserName(),t1doBase.getOExecutorName()};
			T1doPstatus.saveIdoPstatus1(t1doBase.getShowId(),users,usernames);
			t1doBase.setUserotherid(json);//设置参与人其他身份
			T1doAttr t1doAttr=t1doBase.newIdoAttr();	
			//new T1doLog().setShowId(t1doBase.getShowId()).setOUser(t1doBase.getCreateUser()).setOUserName(t1doBase.getCreateUserName()).setOpTime(t1doBase.getOCreateTime()).setLog(t1doBase.getCreateUserName()+"创建了此1do").setLogType(1).save();                                              			
			T1doLog.saveLog(t1doBase.getShowId(), t1doBase.getCreateUser(), t1doBase.getCreateUserName(), t1doBase.getCreateUserName()+"创建了此1do", 1, "");
			for (int i = 0; i < uploadFiles.size(); i++) {		
			    t1doAttr.setAttrPath("https://tyhy.hzxc.gov.cn:8443/1do/upload/"+uploadFiles.get(i));
				t1doAttr.setAttrName(uploadFiles1.get(i));
				t1doAttr.save();
				//t1doAttr.setAttrOrder(t1doAttr.getID()).update();
				new T1doFeedback().setShowId(t1doBase.getShowId()).setOUser(t1doAttr.getUploadUser())
				.setOUserName(t1doAttr.getUploadUserName()).setFbTime(t1doAttr.getUploadTime())
				.setFbType(3).setATTRID(t1doAttr.getID()+"").setAttrPath(t1doAttr.getAttrPath()).setFBCONTENT(t1doAttr.getAttrName()).save();
				
				t1doAttr.remove("ID");
				T1doLog.saveLog(t1doBase.getShowId(),t1doAttr.getUploadUser(), t1doAttr.getUploadUserName(), t1doAttr.getUploadUserName()+"上传"+t1doAttr.getAttrName(), 3, t1doAttr.getAttrName()); 	
			}
			 new Thread(new Runnable() {
 				@Override
 				public void run() {
 					 sendIdo(t1doBase,1,"");
 				}
 			}).start(); 
			
			
			renderJson(JsonUtil.getMap(200, "创建1do成功！"));
		}
		/*
		 2018年6月25日下午3:59:19 coco  //普通反馈 （旧）
		*/
		@Before(Tx.class)
		public void feedback1() {
			JSONObject json=JsonUtil.getJSONObject(getRequest());
			final JSONObject douser=getSessionAttr("1doUser");
			final T1doFeedback t1doFeedback =json.toJavaObject(T1doFeedback.class);
			//查询是否是参与人
			T1doPstatus t1doPstatus=T1doPstatus.getCustomerOrExecutor(t1doFeedback.getShowId(),douser.getString("loginName"),3);
			//T1doStatus t1doStatus=t1doFeedback.getIdoStatus();
			final T1doBase t1doBase=t1doFeedback.getT1doBase();
			t1doFeedback.setFbTime(new Date()).setTimeStamp(new Date().getTime()).setOUser(douser.getString("loginName")).setOUserName(douser.getString("username"));
			T1doLog t1doLog=json.toJavaObject(T1doLog.class);
			t1doLog.setOpTime(t1doFeedback.getFbTime()).setOUser(douser.getString("loginName")).setOUserName(douser.getString("username"));	
			T1doPstatus t1=T1doPstatus.getUser(json.getString("SHOW_ID"),douser.getString("loginName"));
			if(t1doFeedback.getFbType()==1||t1doFeedback.getFbType()==2){
				if(StrUtil.isNotEmpty(t1doFeedback.getAT())&&
						t1doFeedback.getAT().charAt(t1doFeedback.getAT().length()-2)==','){
					renderJson(MsgUtil.errorMsg("AT参数错误"));
					return;
				}
				t1doFeedback.save();
					if(t1doPstatus!=null){
						//主动办
						new Thread(new Runnable() {
			 				@Override
			 				public void run() {
			 				if(t1doBase.getSOURCE()==2&&t1doBase.getDPARAMETER()==2&&t1doBase.getCPARAMETER()==2&&!t1doBase.getISAPPROVAL()){
			 							
			 					Approval arr=Approval.dao.findFirst("select * from approval where source=2 and name=?",t1doFeedback.getFBCONTENT());
			 					/*boolean flag=false;
			 					boolean flag1=false;
			 					for (Approval app : arr) {
			 						if(t1doFeedback.getFBCONTENT().indexOf(app.getName())>=0){
			 							flag=app.getType();
			 							flag1=true;
			 							break;
			 						}
								}*/
			 					if(arr!=null){
			 					 if(arr.getType()==1){
			 							String result1 = null;
									
										try {
											System.out.println("http://172.16.8.18:8080/1call/getSchemeStart?id="+t1doBase.getAPARAMETER()+"&schemeStart=3&examineTime="+TimeUtil.getDateTime1());
											result1 = HttpUtil.doPost11("http://172.16.8.18:8080/1call/getSchemeStart?id="+t1doBase.getAPARAMETER()+"&schemeStart=3&examineTime="+TimeUtil.getDateTime1());

										} catch (UnsupportedEncodingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										System.out.println("------------"+result1);
										
									
				 					JSONObject json=JSON.parseObject(result1);
				 					System.out.println("json-----"+json.toString());
				 					if(json.getInteger("code")==200){
				 						t1doBase.setCPARAMETER(3).setISAPPROVAL(true).update();
				 					}
			 					  }else{
			 						t1doBase.setISAPPROVAL(true).update();
			 					  }
			 				    }else{
			 						System.out.println("不审批");
			 					}
			 					
			 				}
			 				}
			 			}).start(); 
						//三实库
						new Thread(new Runnable() {
							@Override
							public void run() {
								if(t1doBase.getSOURCE()==3&&t1doBase.getCPARAMETER()==1&&!t1doBase.getISAPPROVAL()){
									
									Approval arr=Approval.dao.findFirst("select * from approval where source=3 and name=?",t1doFeedback.getFBCONTENT());
									
									if(arr!=null){
										JSONObject json=new JSONObject();
										json.put("id", t1doBase.getAPARAMETER());
										json.put("type", arr.getType());
									   JSONObject result1 = HttpUtil.doPost3("http://59.202.68.28:8080/ssk/qs/approval",json.toString());
										
										if(result1.getInteger("code")==200){
											t1doBase.setCPARAMETER(arr.getType()).setISAPPROVAL(true).update();
										}
				
									}else{
										System.out.println("不审批");
									}
									
								}
							}
						}).start(); 
					}
					
						
					
				if(t1doPstatus!=null&&t1doBase.getOStatus()==3){
					t1doBase.setOStatus(4).update();
					t1doPstatus.setOStatus(4).update();
					t1doBase.setSendTime(new Date().getTime()).update();
					Db.update("update t_1do_pstatus set O_STATUS=4 where SHOW_ID=? and USER_TYPE!=3",t1doFeedback.getShowId());
					Db.update("update t_1do_pstatus set isSend=2  where  online=2 and SHOW_ID=? and (USER_TYPE=1 or O_USER=? ) and isDelete=1",t1doFeedback.getShowId(),douser.getString("loginName"));
					Db.update("update t_1do_fwpstatus set isSend=2 where SHOW_ID=? and online=2 ",t1doFeedback.getShowId());
					new Thread(new Runnable() {
		 				@Override
		 				public void run() {
		 					 sendIdo(t1doBase,3,douser.getString("loginName"));
		 				}
		 			}).start(); 
					
				}else{
					t1doBase.setSendTime(new Date().getTime()).update();
					Db.update("update t_1do_pstatus set isSend=2 where SHOW_ID=? and isDelete=1 and online=2",t1doFeedback.getShowId());
					Db.update("update t_1do_fwpstatus set isSend=2 where SHOW_ID=? and online=2",t1doFeedback.getShowId());
					final int temp=t1doBase.getOStatus()==3?1:t1doBase.getOStatus();									
					new Thread(new Runnable() {
		 				@Override
		 				public void run() {
		 					sendIdo(t1doBase,temp,douser.getString("loginName"));
		 				}
		 			}).start(); 
				}
				//String str=douser.getString("username")+"反馈:"+t1doFeedback.getFBCONTENT();
				t1doLog.setLogType(10).setLog(douser.getString("username")+"反馈一条信息").save();
				//renderJson(JsonUtil.getMap(200, "反馈成功"));
				renderJson(MsgUtil.successMsg(t1doFeedback));
				return;
			}else{
				t1doLog.setLogType(t1doFeedback.getFbType());
				String str=t1doLog.getOUserName();
				if(t1doLog.getLogType()==4){
				    if(douser.getBooleanValue("isfw")||StrUtil.getflag(t1.getUserType(), 6)){
						t1doBase.setSendTime(new Date().getTime()).update();
				    	str+="催办此1do";
						t1doFeedback.setFBCONTENT(str).save();
						new Thread(new Runnable() {
			 				@Override
			 				public void run() {
			 					sendIdo(t1doBase,4,"");
			 				}
			 			}).start(); 
				   }else{
					  //renderJson(JsonUtil.getMap(202, "权限不足"));
					   renderJson(MsgUtil.errorMsg("权限不足"));
					   return;
				   }
					
				}else if(t1doLog.getLogType()==5){
					if(douser.getBoolean("isfw")||StrUtil.getflag(t1.getUserType(), 7)){
						
						int i=Db.update("update t_1do_status set O_STATUS=5 where SHOW_ID=?",t1doFeedback.getShowId());
						if(i==0){
							renderJson(JsonUtil.getMap(200, "该1do已经办结"));
							return;
						}
						Db.update("update t_1do_pstatus set O_STATUS=5 and isSend=2 where SHOW_ID=? and online=2",t1doFeedback.getShowId());
						Db.update("update t_1do_fwpstatus set isSend=2 where SHOW_ID=? and online=2",t1doBase.getShowId());
						str+="确认办结";
						t1doFeedback.setFBCONTENT("确认办结").save();
						t1doBase.setRealFinishTime(new Date()).setSendTime(new Date().getTime()).update();
						
						
						
						new Thread(new Runnable() {
			 				@Override
			 				public void run() {
			 					 sendIdo(t1doBase,5,"");
			 				}
			 			}).start(); 
					}else{
						//renderJson(JsonUtil.getMap(202, "权限不足"));
						   renderJson(MsgUtil.errorMsg("权限不足"));

						return;
					}
					
				}else if(t1doLog.getLogType()==6){
					if(douser.getBoolean("isfw")||StrUtil.getflag(t1.getUserType(),8)){
					t1doFeedback.setFBCONTENT("评价："+t1doFeedback.getFBCONTENT()).save();
					str+="评价了此1do";
					t1doBase.setStar(t1doFeedback.getStar()).setEvaluation(t1doFeedback.getFBCONTENT()).setSendTime(new Date().getTime()).update();

					new Thread(new Runnable() {
		 				@Override
		 				public void run() {
		 					 sendIdo(t1doBase,6,"");
		 				}
		 			}).start(); 
					}else{
						   renderJson(MsgUtil.errorMsg("权限不足"));

						//renderJson(JsonUtil.getMap(202, "权限不足"));
						return;
					}
				}		
				t1doLog.setLog(str).save();
				//t1doLog.save();

				
				//renderJson(JsonUtil.getMap(200, "反馈成功"));
				renderJson(MsgUtil.successMsg(t1doFeedback));
				return;
			}
			
		}
		/*
		 2018年6月25日下午3:59:19 coco  //普通反馈 （新）2019.1.30
		 */
		@Before(Tx.class)
		public void feedback() {
			JSONObject json=JsonUtil.getJSONObject(getRequest());
			final JSONObject douser=getSessionAttr("1doUser");
			final T1doFeedback t1doFeedback =json.toJavaObject(T1doFeedback.class);
			//查询是否是参与人
			T1doPstatus t1doPstatus=T1doPstatus.getCustomerOrExecutor(t1doFeedback.getShowId(),douser.getString("loginName"),3);
			//T1doStatus t1doStatus=t1doFeedback.getIdoStatus();
			final T1doBase t1doBase=t1doFeedback.getT1doBase();
			t1doFeedback.setFbTime(new Date()).setTimeStamp(new Date().getTime()).setOUser(douser.getString("loginName")).setOUserName(douser.getString("username"));
			T1doLog t1doLog=json.toJavaObject(T1doLog.class);
			t1doLog.setOpTime(t1doFeedback.getFbTime()).setOUser(douser.getString("loginName")).setOUserName(douser.getString("username"));	
			T1doPstatus t1=T1doPstatus.getUser(json.getString("SHOW_ID"),douser.getString("loginName"));
			if(t1doFeedback.getFbType()==1||t1doFeedback.getFbType()==2){
				if(StrUtil.isNotEmpty(t1doFeedback.getAT())&&
						t1doFeedback.getAT().charAt(t1doFeedback.getAT().length()-2)==','){
					renderJson(MsgUtil.errorMsg("AT参数错误"));
					return;
				}
				t1doFeedback.save();
				if(t1doPstatus!=null){
					//主动办
					new Thread(new Runnable() {
						@Override
						public void run() {
							if(t1doBase.getSOURCE()==2&&t1doBase.getDPARAMETER()==2&&t1doBase.getCPARAMETER()==2&&!t1doBase.getISAPPROVAL()){
								
								Approval arr=Approval.dao.findFirst("select * from approval where source=2 and name=?",t1doFeedback.getFBCONTENT());
								/*boolean flag=false;
			 					boolean flag1=false;
			 					for (Approval app : arr) {
			 						if(t1doFeedback.getFBCONTENT().indexOf(app.getName())>=0){
			 							flag=app.getType();
			 							flag1=true;
			 							break;
			 						}
								}*/
								if(arr!=null){
									if(arr.getType()==1){
										String result1 = null;
										
										try {
											System.out.println("http://172.16.8.18:8080/1call/getSchemeStart?id="+t1doBase.getAPARAMETER()+"&schemeStart=3&examineTime="+TimeUtil.getDateTime1());
											result1 = HttpUtil.doPost11("http://172.16.8.18:8080/1call/getSchemeStart?id="+t1doBase.getAPARAMETER()+"&schemeStart=3&examineTime="+TimeUtil.getDateTime1());
											
										} catch (UnsupportedEncodingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										System.out.println("------------"+result1);
										
										
										JSONObject json=JSON.parseObject(result1);
										System.out.println("json-----"+json.toString());
										if(json.getInteger("code")==200){
											t1doBase.setCPARAMETER(3).setISAPPROVAL(true).update();
										}
									}else{
										t1doBase.setISAPPROVAL(true).update();
									}
								}else{
									System.out.println("不审批");
								}
								
							}
						}
					}).start(); 
					//三实库
					new Thread(new Runnable() {
						@Override
						public void run() {
							if(t1doBase.getSOURCE()==3&&t1doBase.getCPARAMETER()==1&&!t1doBase.getISAPPROVAL()){
								
								Approval arr=Approval.dao.findFirst("select * from approval where source=3 and name=?",t1doFeedback.getFBCONTENT());
								
								if(arr!=null){
									JSONObject json=new JSONObject();
									json.put("id", t1doBase.getAPARAMETER());
									json.put("type", arr.getType());
									JSONObject result1 = HttpUtil.doPost3("http://59.202.68.28:8080/ssk/qs/approval",json.toString());
									
									if(result1.getInteger("code")==200){
										t1doBase.setCPARAMETER(arr.getType()).setISAPPROVAL(true).update();
									}
									
								}else{
									System.out.println("不审批");
								}
								
							}
						}
					}).start(); 
				}
				
				
				
				if(t1doPstatus!=null&&t1doBase.getOStatus()==3){
					//t1doBase.update();
					t1doPstatus.setOStatus(4).update();
					t1doBase.setOStatus(4).setSendTime(new Date().getTime()).setFBNUM(t1doBase.getFBNUM()+1).update();
					Db.update("update t_1do_pstatus set O_STATUS=4 where SHOW_ID=? and USER_TYPE!=3",t1doFeedback.getShowId());
					Db.update("update t_1do_pstatus set isSend=2  where  online=2 and SHOW_ID=? and (USER_TYPE=1 or O_USER=? ) and isDelete=1",t1doFeedback.getShowId(),douser.getString("loginName"));
					Db.update("update t_1do_fwpstatus set isSend=2 where SHOW_ID=? and online=2 ",t1doFeedback.getShowId());
					new Thread(new Runnable() {
						@Override
						public void run() {
							sendIdo(t1doBase,3,douser.getString("loginName"));
						}
					}).start(); 
					
				}else{
					t1doBase.setSendTime(new Date().getTime()).setFBNUM(t1doBase.getFBNUM()+1).update();
					Db.update("update t_1do_pstatus set isSend=2 where SHOW_ID=? and isDelete=1 and online=2",t1doFeedback.getShowId());
					Db.update("update t_1do_fwpstatus set isSend=2 where SHOW_ID=? and online=2",t1doFeedback.getShowId());
					final int temp=t1doBase.getOStatus()==3?1:t1doBase.getOStatus();									
					new Thread(new Runnable() {
						@Override
						public void run() {
							sendIdo(t1doBase,temp,douser.getString("loginName"));
						}
					}).start(); 
				}
				//String str=douser.getString("username")+"反馈:"+t1doFeedback.getFBCONTENT();
				t1doLog.setLogType(10).setLog(douser.getString("username")+"反馈一条信息").save();
				//renderJson(JsonUtil.getMap(200, "反馈成功"));
				renderJson(MsgUtil.successMsg(t1doFeedback));
				return;
			}else{
				t1doLog.setLogType(t1doFeedback.getFbType());
				String str=t1doLog.getOUserName();
				if(t1doLog.getLogType()==4){
					if(douser.getBooleanValue("isfw")||StrUtil.getflag(t1.getUserType(), 6)){
						t1doBase.setSendTime(new Date().getTime()).setLIGHTNING(t1doBase.getLIGHTNING()+1).update();
						str+="催办此1do";
						t1doFeedback.setFBCONTENT(str).save();
						new Thread(new Runnable() {
							@Override
							public void run() {
								sendIdo(t1doBase,4,"");
							}
						}).start(); 
					}else{
						//renderJson(JsonUtil.getMap(202, "权限不足"));
						renderJson(MsgUtil.errorMsg("权限不足"));
						return;
					}
					
				}else if(t1doLog.getLogType()==5){
					if(douser.getBoolean("isfw")||StrUtil.getflag(t1.getUserType(), 7)){
						
						int i=Db.update("update t_1do_status set O_STATUS=5 where SHOW_ID=?",t1doFeedback.getShowId());
						if(i==0){
							renderJson(JsonUtil.getMap(200, "该1do已经办结"));
							return;
						}
						Db.update("update t_1do_pstatus set O_STATUS=5 and isSend=2 where SHOW_ID=? and online=2",t1doFeedback.getShowId());
						Db.update("update t_1do_fwpstatus set isSend=2 where SHOW_ID=? and online=2",t1doBase.getShowId());
						str+="确认办结";
						t1doFeedback.setFBCONTENT("确认办结").save();
						t1doBase.setRealFinishTime(new Date()).setSendTime(new Date().getTime()).setFBNUM(t1doBase.getFBNUM()+1).update();
						
						
						
						new Thread(new Runnable() {
							@Override
							public void run() {
								sendIdo(t1doBase,5,"");
							}
						}).start(); 
					}else{
						//renderJson(JsonUtil.getMap(202, "权限不足"));
						renderJson(MsgUtil.errorMsg("权限不足"));
						
						return;
					}
					
				}else if(t1doLog.getLogType()==6){
					if(douser.getBoolean("isfw")||StrUtil.getflag(t1.getUserType(),8)){
						t1doFeedback.setFBCONTENT("评价："+t1doFeedback.getFBCONTENT()).save();
						str+="评价了此1do";
						t1doBase.setStar(t1doFeedback.getStar()).setEvaluation(t1doFeedback.getFBCONTENT()).setSendTime(new Date().getTime()).setFBNUM(t1doBase.getFBNUM()+1).update();
						
						new Thread(new Runnable() {
							@Override
							public void run() {
								sendIdo(t1doBase,6,"");
							}
						}).start(); 
					}else{
						renderJson(MsgUtil.errorMsg("权限不足"));
						
						//renderJson(JsonUtil.getMap(202, "权限不足"));
						return;
					}
				}		
				t1doLog.setLog(str).save();
				//t1doLog.save();
				
				
				//renderJson(JsonUtil.getMap(200, "反馈成功"));
				renderJson(MsgUtil.successMsg(t1doFeedback));
				return;
			}
			
		}
		
		
		/*
		 2018年6月25日下午3:59:19 coco  //删除反馈
		 */
		@Before(Tx.class)
		public void deleteFeedback() {
			JSONObject json=JsonUtil.getJSONObject(getRequest());
			JSONObject douser=getSessionAttr("1doUser");
			T1doFeedback t=T1doFeedback.dao.findById(json.getIntValue("ID"));
			//int i=Db.update("update t_1do_feedback set FB_TYPE=7 where ID=? and O_USER=?",json.getIntValue("ID"),json1.getString("loginName"));
		    if(t.getOUser().equals(douser.getString("loginName"))){
		    	T1doLog.saveLog(t.getShowId(), douser.getString("loginName"), douser.getString("username"), t.getID()+"被删除", 11,"");
			    t.setFbType(7).update();
			    t.setFbType(8).remove("ID").save();
		    	renderJson(JsonUtil.getMap(200, "删除反馈成功"));
			}else{
				renderJson(JsonUtil.getMap(201, "删除反馈不成功"));

			}
			
		}
		
	    /*
		 2018年6月24日下午4:53:49 coco   //获得发起人/参与人1do状态为1已送达2/3.待接单/4.已接单并且   1do和人员订阅事件都包含1.送达  //通知
		*/
		/*@Before(Tx.class)
		public void getIdo() {
			JSONObject douser=getSessionAttr("1doUser");//{"O_USER":"fangshengqun"}
	    	T1doPstatus t1doPstatus=new T1doPstatus().setOUser(douser.getString("loginName"));
	        List<Record> records=t1doPstatus.getRecords();
	        for(Record record:records){
	        	record.set("O_STATUS", StrUtil.getStatus(record.getStr("O_STATUS")));
	        	record.set("USER_TYPE", StrUtil.getUserType(record.getStr("USER_TYPE")));
	        	List<T1doFeedback> l=T1doFeedback.dao.find("select O_USER_NAME from t_1do_feedback where SHOW_ID=? and FB_TYPE=4",record.getStr("SHOW_ID"));
	        	record.set("LIGHTNING", l.size());
	        }
			renderJson(records);
		}*/
		/*public void get1do() {
			JSONObject json=JsonUtil.getJSONObject(getRequest());
			//JSONObject json=getSessionAttr("1doUser");//{"O_USER":"fangshengqun"}
			//T1doPstatus t1doPstatus=new T1doPstatus().setOUser(json.getString("useraccount"));
			T1doPstatus t1doPstatus=json.toJavaObject(T1doPstatus.class);
			List<Record> records=t1doPstatus.getRecords();
			for(Record record:records){
				record.set("O_STATUS", StrUtil.getStatus(record.getStr("O_STATUS")));
				record.set("USER_TYPE", StrUtil.getUserType(record.getStr("USER_TYPE")));
				List<T1doFeedback> l=T1doFeedback.dao.find("select O_USER_NAME from t_1do_feedback where SHOW_ID=? and FB_TYPE=4",record.getStr("SHOW_ID"));
				record.set("LIGHTNING", l.size());
			}
			renderJson(records);
		}*/
		
		/*
		 2018年7月9日上午10:52:04 coco   群发送通知
		*/
		public static String sendIdo(T1doBase t1doBase,int i,String O_USER){
			//T1doType t1doType=t1doBase.getT1doType();
			String[] str=StrUtil.getSql(i,O_USER,t1doBase.getOStatus());
			List<T1doPstatus> t1=T1doPstatus.dao.find(str[1],t1doBase.getShowId());
			String result = null;
			int[] base=t1doBase.num();//0催办数1查看数2反馈数
			JSONObject object = new JSONObject();
			//object.put("O_TITLE", "1do");
			object.put("SHOW_ID", t1doBase.getShowId());
			object.put("O_DESCRIBE", t1doBase.getODescribe());
			object.put("O_CUSTOMER_NAME", t1doBase.getOCustomerName());
			object.put("O_CUSTOMER", t1doBase.getOCustomer());
			object.put("O_EXECUTOR", t1doBase.getOExecutor());
			object.put("O_EXECUTOR_NAME", t1doBase.getOExecutorName());
			object.put("AT", t1doBase.getAT());
			object.put("SEND_TIME", t1doBase.getSendTime());
			object.put("O_STATUS", str[0]);
			object.put("LOOKNUM", base[1]);//查看数量
			object.put("FBNUM", base[2]);//反馈数量	
			object.put("LIGHTNING", base[0]);
			//object.put("evaluation", i==6?"1":"2");//是否评价1评价2不评价
			object.put("O_CREATE_TIME",t1doBase.getOCreateTime());
			object.put("O_FINISH_TIME",t1doBase.getOFinishTime()==null?"":t1doBase.getOFinishTime());
			//T1doFeedback t=T1doFeedback.getT1doFeedback(5, t1doBase.getShowId());
			//object.put("Real_FINISH_TIME",t==null?"":t.getFbTime());
			object.put("Real_FINISH_TIME",t1doBase.getRealFinishTime()==null?"":t1doBase.getRealFinishTime());
			object.put("DELETE_TIME",t1doBase.getDeleteTime()==null?"":t1doBase.getDeleteTime());
			
			for(T1doPstatus tt:t1){
				String loginName=tt.getOUser();
				String trueName=tt.getOUserName();
				String att="select count(*) num from t_1do_base b, (select * from t_1do_pstatus where USER_TYPE!=2 and isDelete=1 and isSend=2 and O_USER='"+loginName+"' GROUP BY SHOW_ID)f where "
						+ "b.SHOW_ID=f.SHOW_ID and (O_EXECUTOR like CONCAT('%','"+loginName+"','%') or O_CUSTOMER like CONCAT('%','"+loginName+"','%') ) ";
				Record r2=Db.findFirst(att);
				object.put("UNREAD", r2.getInt("num"));//未读数
				object.put("ISLOOK", tt.getIsSend());//1是2否
				
				if(i==1){
				  if(tt.getUserType()==3){					
					object.put("O_STATUS", "待接单");
				  }else{
					object.put("O_STATUS", "已送达");
				  }
				}
				object.put("USER_TYPE", tt.getUserType());
				String str1=HttpUtil.getParameter2(t1doBase.getShowId(), loginName, trueName, object,t1doBase.getODescribe());	
			    result   =   HttpUtil.doPost1(url, str1);
			   // System.out.println(result);
			    //new T1doTemp().setBASE(result).setID(IDUtil.getUid1()).save();
			    //System.out.println(1);
			}
			if(i==1){
				object.put("O_STATUS", "已送达");
			}
			List<T1doFw> list=T1doFw.dao.find("SELECT a.*,b.isSend FROM t_1do_fw a LEFT JOIN t_1do_fwpstatus b on a.SHOW_ID=b.SHOW_ID and a.icallshowid=b.O_USER where a.type=1");
			for (T1doFw t1doFw : list) {
				String loginName=t1doFw.getIcallshowid();
				String trueName=t1doFw.getOUserName();
				object.put("USER_TYPE", 6);
				object.put("ISLOOK", t1doFw.getIsSend());//1是2否
				if(i==7){
					object.put("ISLOOK", 1);//1是2否
				}
				String att="select count(*)  from t_1do_fwpstatus where isSend=2 and O_USER='"+loginName+"' GROUP BY O_USER";
				Record r2=Db.findFirst(att);
				object.put("UNREAD", r2.getInt("num"));//未读数
				String str2=HttpUtil.getParameter2(t1doBase.getShowId(), loginName, trueName, object,t1doBase.getODescribe());				
			    result   =   HttpUtil.doPost1(url, str2);
			  
			}
			return result;
			
  
		}
		//单独发送通知
		public static String sendOneIdo(T1doBase t1doBase,int i,String loginName,String trueName){ //i 1加入2查看3反馈
			//T1doType t1doType=t1doBase.getT1doType();
			T1doPstatus user=T1doPstatus.getUser(t1doBase.getShowId(),loginName);
			T1doFw fw=T1doFw.getfw(loginName);
			//T1doStatus ts=t1doBase.getIdoStatus();			
			//T1doFeedback tf=T1doFeedback.getT1doFeedback(6,t1doBase.getShowId());//查询是否评价
			String result = null;
			int[] base=t1doBase.num();//0催办数1查看数2反馈数		
				JSONObject object = new JSONObject();
				//object.put("O_TITLE", "1do");
				object.put("SHOW_ID", t1doBase.getShowId());
				object.put("O_DESCRIBE", t1doBase.getODescribe());
				if(t1doBase.getOStatus()==3){
					if(i==2&&fw!=null){
						object.put("O_STATUS", "已送达");
					}else if(i==1&&user.getUserType()==1){
						object.put("O_STATUS", "已送达");
					}else{
						object.put("O_STATUS", "待接单");
					}
				}else if(t1doBase.getOStatus()==4){
					object.put("O_STATUS", "已接单");
				}else{
					 object.put("O_STATUS", "已完成");
				}
			    if(t1doBase.getOIsDeleted()==2){
			    	object.put("O_STATUS", "已删除");
			    }
				object.put("LOOKNUM", base[1]);//查看数量
				object.put("FBNUM", base[2]);//反馈数量	
				object.put("LIGHTNING", base[0]);
				if(i==1){
					object.put("ISLOOK", 2);//1是2否
				}else{
					object.put("ISLOOK", 1);//1是2否

				}
				String att="select count(*) num from t_1do_base b, (select * from t_1do_pstatus where USER_TYPE!=2 and isDelete=1 and isSend=2 and O_USER='"+loginName+"' GROUP BY SHOW_ID)f where "
						+ "b.SHOW_ID=f.SHOW_ID and (O_EXECUTOR like CONCAT('%','"+loginName+"','%') or O_CUSTOMER like CONCAT('%','"+loginName+"','%') ) ";
				Record r2=Db.findFirst(att);
				object.put("UNREAD", r2.getInt("num"));//未读数
				object.put("SEND_TIME", t1doBase.getSendTime());
				object.put("O_CUSTOMER_NAME", t1doBase.getOCustomerName());
				object.put("O_CUSTOMER", t1doBase.getOCustomer());
				object.put("O_EXECUTOR", t1doBase.getOExecutor());
				object.put("O_EXECUTOR_NAME", t1doBase.getOExecutorName());
				object.put("AT", t1doBase.getAT());

			//	object.put("evaluation", tf==null?"2":"1");//是否评价1评价2不评价
				//object.put("O_TYPE_NAME", "综合需求");
				object.put("USER_TYPE", user==null?6:user.getUserType());	
				object.put("O_CREATE_TIME",t1doBase.getOCreateTime());
				object.put("O_FINISH_TIME",t1doBase.getOFinishTime());
				//T1doFeedback t=T1doFeedback.getT1doFeedback(5, t1doBase.getShowId());
				//object.put("Real_FINISH_TIME",t==null?"":t.getFbTime());
				object.put("Real_FINISH_TIME",t1doBase.getRealFinishTime()==null?"":t1doBase.getRealFinishTime());
				object.put("DELETE_TIME",t1doBase.getDeleteTime()==null?"":t1doBase.getDeleteTime());
				String str1=HttpUtil.getParameter2(t1doBase.getShowId(), loginName, trueName, object,t1doBase.getODescribe());				
				result   =   HttpUtil.doPost1(url, str1);
				//new T1doTemp().setBASE(result).setID(IDUtil.getUid1()).setPid(tt.getID()).save();			   
			    return result; 
			 
			
		}
		//整理层单独发送通知
		public static String fwsendOneIdo(T1doBase t1doBase,int i,String loginName,String trueName){ //i 1加入2查看3反馈
			//T1doStatus ts=t1doBase.getIdoStatus();			
			T1doFeedback tf=T1doFeedback.getT1doFeedback(6,t1doBase.getShowId());//查询是否评价
			String result = null;
			int[] base=t1doBase.num();//0催办数1查看数2反馈数		
			JSONObject object = new JSONObject();
			//object.put("O_TITLE", "1do");
			object.put("SHOW_ID", t1doBase.getShowId());
			object.put("O_DESCRIBE", t1doBase.getODescribe());
			if(t1doBase.getOStatus()==3){
				object.put("O_STATUS", "已送达");
			}else if(t1doBase.getOStatus()==4){
				object.put("O_STATUS", "已接单");
			}else{
				object.put("O_STATUS", "已完成");
			}
			if(t1doBase.getOIsDeleted()==2){
		    	object.put("O_STATUS", "已删除");
		    }
			object.put("LOOKNUM", base[1]);//查看数量
			object.put("FBNUM", base[2]);//反馈数量	
			object.put("LIGHTNING", base[0]);
			if(i==1){
				object.put("ISLOOK", 2);//1是2否
			}else{
				object.put("ISLOOK", 1);//1是2否
				
			}
			String att="select count(*)  from t_1do_fwpstatus where isSend=2 and O_USER='"+loginName+"' GROUP BY O_USER";
			Record r2=Db.findFirst(att);
			object.put("UNREAD", r2.getInt("num"));//未读数
			object.put("SEND_TIME", t1doBase.getSendTime());
			object.put("O_CUSTOMER_NAME", t1doBase.getOCustomerName());
			object.put("O_CUSTOMER", t1doBase.getOCustomer());
			object.put("O_EXECUTOR", t1doBase.getOExecutor());
			object.put("O_EXECUTOR_NAME", t1doBase.getOExecutorName());
			object.put("AT", t1doBase.getAT());

			//object.put("evaluation", tf==null?"2":"1");//是否评价1评价2不评价
			//object.put("O_TYPE_NAME", "综合需求");
			object.put("USER_TYPE", 6);		
			object.put("O_CREATE_TIME",t1doBase.getOCreateTime());
			object.put("O_FINISH_TIME",t1doBase.getOFinishTime());
			//T1doFeedback t=T1doFeedback.getT1doFeedback(5, t1doBase.getShowId());
			//object.put("Real_FINISH_TIME",t==null?"":t.getFbTime());
			object.put("Real_FINISH_TIME",t1doBase.getRealFinishTime()==null?"":t1doBase.getRealFinishTime());
			object.put("DELETE_TIME",t1doBase.getDeleteTime()==null?"":t1doBase.getDeleteTime());
			String str1=HttpUtil.getParameter2(t1doBase.getShowId(), loginName, trueName, object,t1doBase.getODescribe());				
			result   =   HttpUtil.doPost1(url, str1);
			//new T1doTemp().setBASE(result).setID(IDUtil.getUid1()).setPid(tt.getID()).save();			   
			return result;
			
			
		}
	
		
	    /*
		 2018年6月23日下午6:12:15 coco   //拆项
		*/
        @Before(Tx.class)
		public void splitItem() {
        	JSONObject json=JsonUtil.getJSONObject(getRequest());
			T1doBase t1doBase =json.getObject("BASE", T1doBase.class);
			JSONObject douser=getSessionAttr("1doUser");
			t1doBase.setOStartTime(new Date()).setOCreateTime(new Date()).setCreateUser(douser.getString("loginName")).setCreateUserName(douser.getString("username"));
			t1doBase.setShowId(IDUtil.getUid());
			t1doBase.setORange(StrUtil.getOnly(t1doBase.getOCustomer(),t1doBase.getCreateUser(),t1doBase.getOExecutor()));
			t1doBase.setORangeName(StrUtil.getOnly(t1doBase.getOCustomerName(),t1doBase.getCreateUserName(),t1doBase.getOExecutorName()));	
			T1doBase pt1doBase=t1doBase.getParentIdoBase();//得到父1do；
			if(pt1doBase.getOStatus()==5){
				renderJson(JsonUtil.getMap(200, "父项已完成，不可拆项"));
				return;
			}
			t1doBase.setOTypeId(pt1doBase.getOTypeId());
			t1doBase.save();
			List<T1doAttr> t1doAttrs=pt1doBase.getIdoAttr();
			for(T1doAttr attr:t1doAttrs){
				attr.remove("ID");
				attr.remove("ATTR_ORDER");	
				attr.setShowId(t1doBase.getShowId());
				attr.save();
				//attr.setAttrOrder(attr.getID()).update();
			}
			new T1doStatus().setShowId(t1doBase.getShowId()).save();//1do状态表

			new T1doSet().setShowId(t1doBase.getShowId()).setEventType(json.getString("EVENT_TYPE")).save();
			String[] users={t1doBase.getOCustomer(),t1doBase.getCreateUser(),t1doBase.getOExecutor(),t1doBase.getCC()};
			String[] usernames={t1doBase.getOCustomerName(),t1doBase.getCreateUserName(),t1doBase.getOExecutorName(),t1doBase.getCcName()};
			T1doPstatus.saveIdoPstatus1(t1doBase.getShowId(),users,usernames);
			//T1doPstatus.saveIdoPstatus(t1doBase.getShowId(), t1doBase.getOCustomer(),t1doBase.getCreateUser(),t1doBase.getOExecutor());
			//new T1doLog().setShowId(t1doBase.getShowId()).setOUser(t1doBase.getCreateUser()).setOUserName(t1doBase.getCreateUserName()).setOpTime(t1doBase.getOCreateTime()).setLog(t1doBase.getCreateUserName()+"帮助"+t1doBase.getOCustomerName().replace(";","、")+"新建子项").setLogType(1).save();                                                   
			new T1doLog().setShowId(t1doBase.getShowId()).setOUser(t1doBase.getCreateUser()).setOUserName(t1doBase.getCreateUserName()).setOpTime(t1doBase.getOCreateTime()).setLog(t1doBase.getCreateUserName()+"新建子项").setLogType(1).save();                                                   
			new T1doLog().setShowId(pt1doBase.getShowId()).setOUser(t1doBase.getCreateUser()).setOUserName(t1doBase.getCreateUserName()).setOpTime(t1doBase.getOCreateTime()).setLog(t1doBase.getCreateUserName()+"进行拆项").setLogType(7).save();                                                   
			//new T1doLog().setShowId(pt1doBase.getShowId()).SET
			sendIdo(t1doBase,1,"");
			renderJson(JsonUtil.getMap(200, "新建子项成功！"));
		}
        /*
		 2018年6月21日 coco //创建保存1do（1call转1do时）
		*/
	    @Before(Tx.class)
		public void createIdo() {
			JSONObject json=JsonUtil.getJSONObject(getRequest());
			System.out.println(json.toString());
			final T1doBase t1doBase =json.getObject("BASE", T1doBase.class);
			t1doBase.setOStartTime(new Date()).setOCreateTime(new Date()).setCreateUser("1call").setCreateUserName("1call")
			.setShowId(IDUtil.getUid()).setORange(StrUtil.getOnly(t1doBase.getOCustomer(),t1doBase.getCreateUser(),t1doBase.getOExecutor()))
			.setORangeName(StrUtil.getOnly(t1doBase.getOCustomerName(),t1doBase.getCreateUserName(),t1doBase.getOExecutorName())).setSendTime(new Date().getTime()).save(); 
			t1doBase.savefw();//保存整理层为查看通知做准备。
			JSONArray arr=json.getJSONArray("ATTR");
			if(arr!=null){
			for (int i = 0; i < arr.size(); i++) {
				T1doAttr t1doAttr=arr.getObject(i, T1doAttr.class);
				t1doAttr.setShowId(t1doBase.getShowId());
				t1doAttr.save();
				new T1doFeedback().setShowId(t1doBase.getShowId()).setOUser(t1doAttr.getUploadUser())
				.setOUserName(t1doAttr.getUploadUserName()).setFbTime(t1doAttr.getUploadTime())
				.setFbType(3).setATTRID(t1doAttr.getID()+"").setAttrPath(t1doAttr.getAttrPath()).setFBCONTENT(t1doAttr.getAttrName()).save();
				T1doLog.saveLog(t1doBase.getShowId(),t1doAttr.getUploadUser(), t1doAttr.getUploadUserName(), t1doAttr.getUploadUserName()+"上传"+t1doAttr.getAttrName(), 3, t1doAttr.getAttrName()); 	
			}}
			//new T1doStatus().setShowId(t1doBase.getShowId()).save();//1do状态表
			
			//new T1doSet().setShowId(t1doBase.getShowId()).setEventType(json.getString("EVENT_TYPE")).save();
			String[] users={t1doBase.getOCustomer(),t1doBase.getCreateUser(),t1doBase.getOExecutor()};
			String[] usernames={t1doBase.getOCustomerName(),t1doBase.getCreateUserName(),t1doBase.getOExecutorName()};
			T1doPstatus.saveIdoPstatus1(t1doBase.getShowId(),users,usernames);
			T1doLog.saveLog(t1doBase.getShowId(), t1doBase.getCreateUser(), t1doBase.getCreateUserName(), t1doBase.getCreateUserName()+"创建了此1do", 1, "");
			new Thread(new Runnable() {
				@Override
				public void run() {sendIdo(t1doBase,1,"");}
			}).start();
			renderJson(JsonUtil.getMap(200, t1doBase.getShowId()));
		}
	   
	   
		/*
		 2018年6月21日下午2:36:27 coco  //新增1do分类（T1doType）
		*/
		public void test() {
			Record t=Db.findFirst("select * from t_1do_base");
			new Thread(new Runnable() {
				@Override
				public void run() {}
			}).start();
			renderJson(t);
		}
		public void saveIdoType() {
			JSONObject json=JsonUtil.getJSONObject(getRequest());
			T1doType t1doType=json.toJavaObject(T1doType.class);
			
			t1doType.setOTypeId(IDUtil.getUid());
			t1doType.save();
			renderJson(JsonUtil.getMap(200, "添加成功！"));
		}

	      public void msginfo() {
	    	JSONObject json=JsonUtil.getJSONObject(getRequest());	      
	    	JSONArray jsonArray = json.getJSONArray("msgId");
			@SuppressWarnings("rawtypes")
			List<LinkedHashMap> list=new ArrayList<LinkedHashMap>();
			for(int i=0;i<jsonArray.size();i++) {
				//T1doBase msg =T1doBase.dao.findFirst("select * from t_1do_base where MESSAGE_ID like ?", "%"+jsonArray.get(i)+"%");
				T1doBase msg =T1doBase.dao.findFirst("select * from t_1do_base where MESSAGE_ID like ?", "%"+jsonArray.get(i)+"%");
				//Msginfo msg = Msginfo.dao.findById(jsonArray.get(i));
				LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(); 
				if(msg!=null){
					map.put("msgId", jsonArray.get(i));
					map.put("exist", true);
					list.add(map);
				}
				
			}
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();  
			 map.put("code", 200);
			 map.put("message", list);
	         renderJson(map);
	    	  
	}
	      /*
		 2018年8月6日下午3:38:11 coco
		*/
		public void getsessionName() {
			JSONObject json1=JsonUtil.getJSONObject(getRequest());
			JSONObject json= new JSONObject();
			JSONObject json2= new JSONObject();
			JSONObject json3= new JSONObject();
			JSONObject json4= new JSONObject();
			json.put("appName", "launchr");
			json.put("appToken", "verify-code");
			json.put("userName", "NO6lZyJjYRCAKd9R");
			/*json.put("userToken", json1.getString("LoginToken"));
			json.put("userName", json1.getString("loginName"));*/
			json.put("sessionName", json1.getString("GROUPID"));
			json2.put("param", json);
			json3.put("body", json2);
			json4.put("async",false);
			json4.put("authToken","NhHCGqeKtkK0dFnznZxP9FxeTF8=");
			json4.put("companyCode","xcgov");
			json4.put("companyShowID","b5WJZ1bRLDCb7x2B");
			json4.put("language","zh-cn");
			json4.put("loginName","NO6lZyJjYRCAKd9R");
			json4.put("resourceUri","/Chat-Module/chat/session");
			json4.put("type","POST");
			json4.put("userName","boyd");
			json3.put("header",json4);
		   String str=HttpUtil.doPost1("http://xcgovapi.hzxc.gov.cn/Chat-Module/chat/session", json3.toString());
		   JSONObject json33 = JSON.parseObject(str);
			String Data1=json33.getString("Body");
			JSONObject json22 = JSON.parseObject(Data1);
			String Data2=json22.getString("response");
			JSONObject json11 = JSON.parseObject(Data2);
			String Data=json11.getString("Data");
			JSONObject json333 = JSON.parseObject(Data);
			String Data22=json333.getString("data");
			renderJson(Data22);
		}
	      public static void main(String[] args) {
	  		int i=10;
	  		if (i>1) {
				System.out.println(1);
			} else if(i>2) {
				System.out.println(2);

			}else{
				System.out.println(3);

			}
	  		
	  		/*HashMap<String,String> uploadFiles=new HashMap<String,String>();
	  		uploadFiles.put("1", "sssss");
	  		System.out.println(uploadFiles);

	  		System.out.println("1234.jsp".substring("1234.jsp".lastIndexOf(".")));*/
	  	}	
		

}
