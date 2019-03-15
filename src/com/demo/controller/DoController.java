 package com.demo.controller;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.fileupload.util.Streams;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.demo.common.model.T1doAttr;
import com.demo.common.model.T1doBase;
import com.demo.common.model.T1doFeedback;
import com.demo.common.model.T1doFw;
import com.demo.common.model.T1doLabel;
import com.demo.common.model.T1doLog;
import com.demo.common.model.T1doOrder;
import com.demo.common.model.T1doPstatus;
import com.demo.common.model.T1doRelation;
import com.demo.common.model.T1doType;
import com.demo.common.model.TRegUser;
import com.demo.common.model.Temp;
import com.demo.interceptor.AddLabel;
import com.demo.interceptor.SendIdo;
import com.demo.service.DoService;
import com.demo.util.DbUtil;
import com.demo.util.ExcelExportUtil;
import com.demo.util.HttpUtil;
import com.demo.util.IDUtil;
import com.demo.util.JsonUtil;
import com.demo.util.MsgUtil;
import com.demo.util.ShortMessageUtil;
import com.demo.util.StrUtil;
import com.demo.util.TimeUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;


public class DoController extends Controller {
	/*
	 2019年3月6日 coco 注解：
	*/
	public void deleteRelation() {
		JSONObject json=JsonUtil.getJSONObject(getRequest());
		JSONObject douser=getSessionAttr("1doUser");	
		if(douser.getBooleanValue("isfw"))
		renderJson(MsgUtil.successMsg(DbUtil.update(json.getString("SHOW_ID"),json.getString("RELATION_SHOW_ID"),1,0,"","")));
		else
		renderJson(MsgUtil.errorMsg("无权限删除"));
	}
	/*
	 2019年2月13日 coco 注解：//批量添加关联
	*/
	public void batchAddRelation() {
    	JSONObject json=JsonUtil.getJSONObject(getRequest());
		JSONArray array=json.getJSONArray("list");
		for (int i = 0; i < array.size(); i++) {			
			DbUtil.update(json.getString("SHOW_ID"),array.getString(i),2,0,"","");
		}
		renderJson(MsgUtil.successMsg("添加成功"));

	}
	/*
	 2019年2月13日 coco 注解：//传送门关键字查询1do
	*/
	public void selectBybase() {		
		renderJson(MsgUtil.successMsg(T1doBase.selectBybase(JsonUtil.getJSONObject(getRequest()))));
	}
	/*
	 2019年2月13日 coco 注解：传送门获取相关联1do
	*/
	public void getRelation() {
    	JSONObject json=JsonUtil.getJSONObject(getRequest());
    	JSONObject douser=getSessionAttr("1doUser");
    	String sql=douser.getBooleanValue("isfw")?"":" and (O_CUSTOMER like CONCAT('%','"+douser.getString("loginName")+"','%') or O_EXECUTOR like CONCAT('%','"+douser.getString("loginName")+"','%')) and b.O_STATUS!=6";			
		renderJson(MsgUtil.successMsg(T1doRelation.selectRelation(json.getString("SHOW_ID"),sql)));
	}
	//关联排序
	public void relationSort() {
		JSONArray array=JsonUtil.getJSONObject(getRequest()).getJSONArray("list");
    	//JSONObject douser=getSessionAttr("1doUser");
		for (int i = 0; i < array.size(); i++) {			
			Db.update("update t_1do_relation set sort=? where id=?",i+1,array.getInteger(i));
		}
		renderJson(MsgUtil.successMsg("排序完成"));
	}
	/*
	 2018年12月4日 coco 注解：//添加或删除标签
	*/
	public void addOrDeleteLabel() {
    	JSONObject json=JsonUtil.getJSONObject(getRequest());
    	JSONObject douser=getSessionAttr("1doUser");
    	T1doLabel l=null;
		if(douser.getBooleanValue("isfw")){
	    	if(json.getString("method").equals("add")){	    		
	    		if(T1doLabel.getT1doLabel(json)==null){
	    		    l =json.toJavaObject(T1doLabel.class);
	    			l.setTYPE(2).save();
	    			new Thread(new Runnable() {	
						@Override
						public void run() {T1doRelation.updateSimilarity(json.getString("SHOW_ID"),"",1);}
					}).start();
	    		}else{
	    			renderJson(MsgUtil.errorMsg("标签已存在"));
	    			return;
	    		}
	    	}else{	
	    		l=json.toJavaObject(T1doLabel.class);
	    		l.delete();	    		
	    		new Thread(new Runnable() {
					@Override
					public void run() {T1doRelation.updateSimilarity(json.getString("SHOW_ID"),"and SIMILARITY>0",1);}
				}).start();
	    	}
			renderJson(MsgUtil.successMsg(l));
		}else{
			renderJson(MsgUtil.errorMsg("无操作权限"));
		}
		
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
		DbUtil.update(json.getString("SHOW_ID"),"",3,json.getIntValue("otherid"),"","");		
		for(String user:users){
		DbUtil.update(json.getString("SHOW_ID"),user,4,json.getIntValue("otherid"),"","");
		}
		      renderJson(JsonUtil.getMap(200, "修改成功"));
		}else {
			  renderJson(JsonUtil.getMap(202, "权限不足")); 

		}
		
	}
	    
	    /*
		 2018年7月5日上午9:56:46 coco   //看板搜索 (新)
		*/
		public void search() {
			JSONObject json1=JsonUtil.getJSONObject(getRequest());
			JSONObject douser=getSessionAttr("1doUser");
			int i=3;
			int type=json1.getIntValue("type");
			String sql=type==7?"(o_status=3 or o_status=4) and LIGHTNING>0":"o_status="+type;
			if(StrUtil.isNotEmpty(json1.getString("base"))){
				sql+=" and O_DESCRIBE like CONCAT('%','"+json1.getString("base")+"','%')";
			}
			//String sql1="";
			if(json1.getString("method").equals("medo")){
				sql+=" and O_EXECUTOR like CONCAT('%','"+douser.getString("loginName")+"','%')" ;
				i=1;
			}else if(json1.getString("method").equals("hedo")){
				sql+=" and O_CUSTOMER like CONCAT('%','"+douser.getString("loginName")+"','%')";
				i=2;
			}
			    if(StrUtil.isNotEmpty(json1.getString("O_CUSTOMER_NAME"))){
					sql+=" and O_CUSTOMER_NAME like CONCAT('%','"+json1.getString("O_CUSTOMER_NAME")+"','%')";
				}
				if(StrUtil.isNotEmpty(json1.getString("O_EXECUTOR_NAME"))){
					sql+=" and O_EXECUTOR_NAME like CONCAT('%','"+json1.getString("O_EXECUTOR_NAME")+"','%')";
				}
			
			
			String from ="select a.*,b.type TYPE,IFNULL(c.log_type-1,2)ISLOOK"
					+ " from (select SHOW_ID,O_DESCRIBE,O_EXECUTOR_NAME,O_CUSTOMER_NAME,O_START_TIME,O_FINISH_TIME,star,evaluation,Real_FINISH_TIME,DELETE_TIME "
					+ " from t_1do_base where "+sql+" ORDER BY ID desc)a LEFT JOIN (select * from t_1do_order "
					+ " where useraccount=? and type="+i+" )b on a.show_id=b.show_id "
					+ " LEFT JOIN (SELECT SHOW_ID,log_type from t_1do_log where O_USER=? "
					+ " and log_type=2 GROUP BY SHOW_ID)c on a.SHOW_ID=c.SHOW_ID"
					+ " ORDER BY modifyTime desc LIMIT ?,10";
			
			String from1="select count(*) num from t_1do_base  where "+sql;
			List<T1doBase> t3=T1doBase.dao.find(from,douser.getString("loginName"),douser.getString("loginName"),(json1.getIntValue("pageNumber")-1)*10);
			Record r=Db.findFirst(from1);
			/*for(T1doBase t:t3){
				t.set1doIsLook(douser.getString("loginName"));//可以用表关联（未完成）
			}*/
			JSONObject json2=new JSONObject();
			json2.put("base", t3);
			json2.put("allPage", r.getInt("num"));
			renderJson(json2);
			
		}
	
		/*
		 2018年7月5日上午9:56:46 coco   //APP看板搜索
		 */
		
		public void appSearch() {
			JSONObject json1=JsonUtil.getJSONObject(getRequest());
			JSONObject douser=getSessionAttr("1doUser");
			String loginName=douser==null?json1.getString("loginName"):douser.getString("loginName");
			
			String sql="(O_EXECUTOR like CONCAT('%','"+loginName+"','%') or O_CUSTOMER like CONCAT('%','"+loginName+"','%') )";			
			if(json1.getString("method").equals("medo")){
				sql="O_EXECUTOR like CONCAT('%','"+loginName+"','%')" ;				
			}else if(json1.getString("method").equals("hedo")){
				sql="O_CUSTOMER like CONCAT('%','"+loginName+"','%')";				
			}
			if(StrUtil.isNotEmpty(json1.getString("relate"))){
				sql+=" and (O_EXECUTOR like CONCAT('%','"+json1.getString("relate")+"','%') or O_CUSTOMER like CONCAT('%','"+json1.getString("relate")+"','%') )";
			}
			int type=json1.getIntValue("type");
			if(type==3||type==4||type==5){
				sql+=" and a.o_status="+type;
			}else if(type==7){
				sql+=" and (a.o_status=3 or a.o_status=4) and LIGHTNING>0";
			}
			//sql+=type!=0?" and a.o_status="+type:"";
			int isLook=json1.getIntValue("isLook");
			String look=isLook!=0?" and isSend="+isLook:"";
			//优化(2019.3.5未完成)
			String from ="select a.SHOW_ID,a.O_DESCRIBE,a.O_CUSTOMER_NAME,a.O_CUSTOMER,a.AT,a.O_EXECUTOR,a.O_EXECUTOR_NAME,a.SEND_TIME,unix_timestamp(a.O_CREATE_TIME)*1000 O_CREATE_TIME,"
+"unix_timestamp(a.O_FINISH_TIME)*1000 O_FINISH_TIME,unix_timestamp(a.Real_FINISH_TIME)*1000 Real_FINISH_TIME,unix_timestamp(a.DELETE_TIME)*1000 DELETE_TIME,a.O_IS_DELETED ,"
+ " a.LIGHTNING,a.LOOKNUM,a.FBNUM,a.O_STATUS,f.USER_TYPE,f.isSend ISLOOK "
					+"from t_1do_base a  "  
					+"LEFT JOIN (select * from t_1do_pstatus where USER_TYPE!=2 " +look+" and isDelete=1 and O_USER='"+loginName+"' GROUP BY SHOW_ID)f on a.SHOW_ID=f.SHOW_ID "
					+"where "+sql+look;
			/*String from11 ="select a.ID,a.O_CREATE_TIME,a.SEND_TIME,a.O_STATUS,f.USER_TYPE,f.isSend ISLOOK,a.O_IS_DELETED "
					+"from t_1do_base a  "
					+"LEFT JOIN (select * from t_1do_pstatus where USER_TYPE!=2 " +look+" and isDelete=1 and O_USER='"+loginName+"' GROUP BY SHOW_ID)f on a.SHOW_ID=f.SHOW_ID "
					+"where "+sql+look;*/
			
			String from1=type==7?" ORDER BY SEND_TIME desc LIMIT ?,? ) g ORDER BY SEND_TIME ":" ORDER BY O_CREATE_TIME desc LIMIT ?,? ) g ORDER BY O_CREATE_TIME ";
			if(StrUtil.isNotEmpty(json1.getString("source"))){
				 from1=" ORDER BY SEND_TIME desc LIMIT ?,? ) g ";
			}
			List<Record> r3=Db.find("select * from ("+from+from1,json1.getIntValue("pageNumber"),json1.getIntValue("onePageNumber"));
			List<Record> r4=Db.find(from);
			for(Record r:r3){
				r.set("O_STATUS", StrUtil.getSql(r.getInt("O_IS_DELETED"), r.getInt("USER_TYPE"),r.getInt("O_STATUS")));
				//可以用表关联（未完成）
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
			     Record r=Db.findFirst("select count(*) num from t_1do_base where o_status=? "+sql1,j);
			     json2.put(""+j, r.getInt("num"));                                                                                                                                                                                                                                                                                      			     
			}
			 Record r=Db.findFirst("select count(*) num from t_1do_base where (o_status=3 or o_status=4) "+sql1+" and LIGHTNING>0");
		     json2.put("urge", r.getInt("num")); //催办数  
			String usql="";
			if(json1.getString("method").equals("medo")){
				usql="USER_TYPE=3 and";
			}else if(json1.getString("method").equals("hedo")){
				usql="USER_TYPE=1 and";
			}else if(json1.getString("method").equals("all")){
				usql="USER_TYPE!=2 and";
			}
			
			String att="select count(*) num from t_1do_base b, (select * from t_1do_pstatus where  "+usql+" isDelete=1 and isSend=? and O_USER='"+loginName+"' GROUP BY SHOW_ID)f where b.SHOW_ID=f.SHOW_ID "+sql1+" ";
			Record r1=Db.findFirst(att,1);
			Record r2=Db.findFirst(att,2);
			json2.put("Y", r1.getInt("num"));
			json2.put("N", r2.getInt("num"));			
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
				sql="and O_DESCRIBE like CONCAT('%','"+json1.getString("base")+"','%')";
			}
			String sql1="";
			
			if(json1.getString("method").equals("medo")){
				sql1=" O_EXECUTOR like CONCAT('%','"+douser.getString("loginName")+"','%')" ;
				
			}else if(json1.getString("method").equals("hedo")){
				sql1=" O_CUSTOMER like CONCAT('%','"+douser.getString("loginName")+"','%')";
				
			}
			int type=json1.getIntValue("type");
			/*int delete=1;
			if(type==6){
				delete=2;
				type=5;
			}*/
			
			String from ="select SHOW_ID,O_DESCRIBE,O_EXECUTOR_NAME,O_CUSTOMER_NAME,O_START_TIME,O_FINISH_TIME,star,evaluation,Real_FINISH_TIME,DELETE_TIME "
					+ "from t_1do_base  where "+sql1+" and o_status=? "+sql+"  ORDER BY "
					+ ""+json1.getString("sorting")+" LIMIT ?,10";
			String from1="select count(*) num  "
					+ "from t_1do_base b  where "+sql1+" and o_status=? "+sql;
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
				
		
		//通讯录获取最近联系人
		public void GetContact() {
			JSONObject douser=getSessionAttr("1doUser");
			String str=HttpUtil.getParameter(douser, "/Base-Module/CompanyUser/GetContact");
			System.out.println(str);
			String result   =   HttpUtil.doPost1("http://xcgovapi.hzxc.gov.cn/Base-Module/CompanyUser/GetContact", str);
			renderJson(result);
		}
		//通讯录获取部门和部门人员列表
		public void GetList() {
			JSONObject douser=getSessionAttr("1doUser");
			JSONObject json2=JsonUtil.getJSONObject(getRequest());
			String str=HttpUtil.getParameter(douser, "/Base-Module/CompanyDept/GetList",json2.getIntValue("isContainChildDeptMember"),json2.getString("parentId"));
			String result   =   HttpUtil.doPost1("http://xcgovapi.hzxc.gov.cn/Base-Module/CompanyDept/GetList", str);		
			renderJson(result);
		}
		public void GetListUser() {
			JSONObject json2=JsonUtil.getJSONObject(getRequest());
			JSONObject douser=getSessionAttr("1doUser");
			String str=HttpUtil.getParameter1(douser, "/Base-Module/CompanyUser/GetList",json2.getIntValue("isContainChildDeptMember"),json2.getString("deptId"));
			String result   =   HttpUtil.doPost1("http://xcgovapi.hzxc.gov.cn/Base-Module/CompanyUser/GetList", str);
			renderJson(result);
		}
		public void searchUser() {
			JSONObject json2=JsonUtil.getJSONObject(getRequest());
			JSONObject douser=getSessionAttr("1doUser");
			String str=HttpUtil.getParameter1(douser, "/Base-Module/CompanyUser/GetList",json2.getIntValue("isContainChildDeptMember"),json2.getIntValue("createPage"),json2.getIntValue("pageSize"),json2.getString("searchKey"));
			String result   =   HttpUtil.doPost1("http://xcgovapi.hzxc.gov.cn/Base-Module/CompanyUser/GetList", str);
			renderJson(result);
		}
		
		//通讯录获取用户信息
		public void CompanyUser() {
			JSONObject json2=JsonUtil.getJSONObject(getRequest());
			JSONObject douser=getSessionAttr("1doUser");
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
				return;
			}
			final String loginName=douser1==null?json.getString("loginName"):douser1.getString("loginName");
			if((t1doBase.getOIsDeleted()==2&&StrUtil.isNotEmpty(json.getString("loginName")))||(t1doBase.getOIsDeleted()==2&&!douser1.getBooleanValue("isfw"))){
			
				DbUtil.update(t1doBase.getShowId(),loginName, 5, 0,"","");
				DbUtil.update(t1doBase.getShowId(),loginName, 6, 0,"","");
				renderJson(JsonUtil.getMap(200, "该1do已删除"));
				return;
			}
			final String username=douser1.getString("username");
			//修改
			int i=DbUtil.update(t1doBase.getShowId(),loginName, 5, 0,"","");	
			int j=DbUtil.update(t1doBase.getShowId(),loginName, 6, 0,"","");
			//在线已读
			DbUtil.update(t1doBase.getShowId(),loginName, 7, 0,"","");
			
			//修改创建用户
			DbUtil.update(t1doBase.getShowId(),loginName, 8, 0,username,"");			
			T1doPstatus t2=T1doPstatus.getCustomerOrExecutor(json.getString("SHOW_ID"),loginName,3);
			//查询该1do参与人是否查看过
			Record r=Db.findFirst("select * from t_1do_log a,t_1do_pstatus b where a.SHOW_ID=b.SHOW_ID and a.SHOW_ID=? and b.USER_TYPE=3 and a.O_USER=b.O_USER",json.getString("SHOW_ID"));
			new T1doLog().setShowId(json.getString("SHOW_ID")).setOUser(loginName)
			.setOUserName(username).setOpTime(new Date()).
			setLog(username+"查看此1do").setLogType(2).save();
           if(t2!=null&&r==null){
        	   t1doBase.setSendTime(new Date().getTime()).update();            
           	new Thread(new SendIdo(t1doBase,2,loginName,"",1)).start();
			}else if(i>=1){
	           	new Thread(new SendIdo(t1doBase,2,loginName,username,2)).start();
			}else if(j>=1){
	           	new Thread(new SendIdo(t1doBase,2,loginName,username,3)).start();
			}
			//t1doBase.put("O_STATUS", t1doBase.getIdoStatus().getOStatus());
			t1doBase.put("O_STATUS", t1doBase.getOStatus());
			t1doBase.put("ccp", t1doBase.getUser(2));
			t1doBase.put("executor", t1doBase.getUser(1));
			t1doBase.put("O_LABEL", t1doBase.getLabel());
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
			 T1doPstatus t1=T1doPstatus.getUser(json.getString("SHOW_ID"),douser.getString("loginName"));
			 if(douser.getBoolean("isfw")||StrUtil.getflag(t1.getUserType(), 1)){
				int i= DbUtil.update(json.getString("target"), json.getString("content"), 7, 0,json.getString("AT"),json.getString("SHOW_ID"));				if(i==1){
					T1doBase t=T1doBase.getIdoBase(json.getString("SHOW_ID"));
					 //T1doLog.saveLog(json.getString("SHOW_ID"),douser.getString("loginName"),douser.getString("username"), douser.getString("username")+"修改此1do", 14,t.getODescribe());
					 T1doLog.saveLog(json.getString("SHOW_ID"),douser.getString("loginName"),douser.getString("username"), douser.getString("username")+"修改此1do", 14,new Temp(t.getODescribe(),json.getString("content")).toString());
					 if(json.getString("target").equals("O_DESCRIBE")){			 
						new Thread(new AddLabel(t.getODescribe(), json.getString("SHOW_ID"),1)).start();//批量添加标签
						StrUtil.getQTR(t);
						/*new Thread(new Runnable() {
			   				@Override
			   				public void run() {
			   					 sendIdo(t,2,loginName);//群发通知
			   				}
			   			}).start();*/   
					 }
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
				DbUtil.update(s, t1doBase.getShowId(), 10, i1, "", "");
				for(int j = 0; j < temp.length; j++){
					T1doPstatus t12=T1doPstatus.dao.findFirst("select * from t_1do_pstatus where isDelete=1 and SHOW_ID=? and O_USER=? and USER_TYPE=?",t1doBase.getShowId(),temp[j],i1);
					if(t12!=null){
						continue;
					}				
					int i=DbUtil.update(t1doBase.getShowId(),temp[j],11,i1, "", "");
					 if(i==0){
						 
						t.setShowId(json.getString("SHOW_ID")).setOUser(temp[j]).setOUserName(temp1[j]).save();
	
							DoService.sendOneIdo(t1doBase,1,temp[j],temp1[j]);//单独发通知i 1加入2查看3反馈
				           
						t.remove("ID");
					 }
					T1doLog.saveLog(json.getString("SHOW_ID"), douser.getString("loginName"), douser.getString("username"), douser.getString("username")+"邀请"+temp1[j]+"进入此1do", 9,temp1[j]);

				}
				
			}else if(json.getString("method").equals("remove")){
				DbUtil.update(t1doBase.getShowId(),json.getString("useraccount"),12,i1, "", "");
				T1doLog.saveLog(t1doBase.getShowId(), douser.getString("loginName"), douser.getString("username"), douser.getString("username")+"移除"+json.getString("username")+"出此1do", 8,json.getString("username"));
			}else{
				String[] temp =json.getString("useraccount").split(";");
				String[] temp1 =json.getString("username").split(";");
				for (int j = 0; j < temp.length; j++) {
					int i=DbUtil.update(t1doBase.getShowId(),temp[j],13,i1, "", "");
				 if(i==0){
					 
					t.setShowId(json.getString("SHOW_ID")).setOUser(temp[j]).setOUserName(temp1[j]).setOtherid(i1==3?2:0).save();
					T1doLog.saveLog(json.getString("SHOW_ID"), douser.getString("loginName"), douser.getString("username"), douser.getString("username")+"邀请"+temp1[j]+"进入此1do", 9,temp1[j]);

						DoService.sendOneIdo(t1doBase,1,temp[j],temp1[j]);//单独发通知i 1加入2查看3反馈
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
			DbUtil.update(json.getString("SHOW_ID"),douser.getString("loginName"), 14, 0, "", "");
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
					DbUtil.update(t1doFeedback.getShowId(),douser.getString("loginName"), 15, 0, "", "");
					
		           	new Thread(new SendIdo(t1doBase,3,douser.getString("loginName"),"",1)).start();
				}else{
					t1doBase.setSendTime(new Date().getTime()).setFBNUM(t1doBase.getFBNUM()+1).update();
					DbUtil.update(t1doFeedback.getShowId(),douser.getString("loginName"), 16, 0, "", "");
					final int temp=t1doBase.getOStatus()==3?1:t1doBase.getOStatus();									
		           	new Thread(new SendIdo(t1doBase,temp,douser.getString("loginName"),"",1)).start();

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
		//批量删除
		@Before(Tx.class)
		public void deleteAlldo(){
			//JSONObject json=JsonUtil.getJSONObject(getRequest()); 	
			JSONArray doList=JsonUtil.getJSONObject(getRequest()).getJSONArray("list");
			JSONObject douser=getSessionAttr("1doUser");
			String loginName=douser.getString("loginName");
			String username=douser.getString("username");
		    for (int j = 0; j < doList.size(); j++) {
				String showID=doList.getString(j);
			T1doPstatus t1=T1doPstatus.getUser(showID,loginName);
			final T1doBase t1doBase=T1doBase.getIdoBase(showID);
			
				if(douser.getBoolean("isfw")||StrUtil.getflag(t1.getUserType(), 9)){
					//T1doStatus t=T1doStatus.dao.findFirst("select * from t_1do_status where SHOW_ID=?",showID);
					if(t1doBase.getOStatus()==5){
						int i=Db.update("update t_1do_base set O_IS_DELETED=2 ,DELETE_TIME=now(),O_STATUS=6 where SHOW_ID=?",showID);	  
						if(i==1){
							T1doLog.saveLog(showID, loginName, username, username+"删除此1do", 12,"");	
				        	   t1doBase.setSendTime(new Date().getTime()).update();
							
				           	new Thread(new SendIdo(t1doBase,7,"","",1)).start();
				           	
				           	Db.update("update t_1do_relation set TYPE=6 where  RELATION_SHOW_ID=?",showID);
				           	Db.update("update t_1do_pstatus set O_STATUS=6,STATUS='已删除' where SHOW_ID=?",showID);
							System.out.println("删除成功");
						}else{
							System.out.println("删除失败");
						}
					}else{
						renderJson(JsonUtil.getMap(202, "任务进行中不能删除"));
						return;
					}
					
				}else{
					System.out.println("权限不足");

					renderJson(JsonUtil.getMap(202, "权限不足"));
					return;
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
					Db.update("update t_1do_pstatus set isSend=2 where SHOW_ID=? and O_USER=? and isDelete=1 and USER_TYPE!=2 and (online=2 or gmt_modified<CURDATE())",t1doBase.getShowId(),douser.getString("loginName"));
					Db.update("update t_1do_fwpstatus set isSend=2 where SHOW_ID=? and O_USER=? and (online=2 or gmt_modified<CURDATE())",t1doBase.getShowId(),douser.getString("loginName"));
					int i=Db.update("update t_1do_base set O_IS_DELETED=2 ,DELETE_TIME=now(),O_STATUS=6 where SHOW_ID=?",json.getString("SHOW_ID"));	  
					if(i==1){
						T1doLog.saveLog(json.getString("SHOW_ID"), douser.getString("loginName"), douser.getString("username"), douser.getString("username")+"删除此1do", 12,"");
			        	   t1doBase.setSendTime(new Date().getTime()).update();
						 
			           	new Thread(new SendIdo(t1doBase,7,"","",1)).start();
			           	Db.update("update t_1do_pstatus set O_STATUS=6,STATUS='已删除' where SHOW_ID=?",json.getString("SHOW_ID"));
						Db.update("update t_1do_relation set TYPE=6 where  RELATION_SHOW_ID=?",json.getString("SHOW_ID"));
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
				 
	           	new Thread(new SendIdo(t1doBase,8,"","",1)).start();
	           	Db.update("update t_1do_pstatus set O_STATUS=5,STATUS='已完成' where SHOW_ID=?",json.getString("SHOW_ID"));
				Db.update("update t_1do_relation set TYPE=5 where  RELATION_SHOW_ID=?",json.getString("SHOW_ID"));
				renderJson(JsonUtil.getMap(200, "恢复成功"));
			  }else{
				renderJson(JsonUtil.getMap(201, "恢复失败"));
			  }
			}else{
				renderJson(JsonUtil.getMap(202, "权限不足"));
			}
		}else{//重做
			if(douser.getBoolean("isfw")){
			Db.update("update t_1do_base set O_IS_DELETED=1 ,SEND_TIME=?,O_STATUS=3,LOOKNUM=0,FBNUM=0,LIGHTNING=0 where SHOW_ID=?",new Date().getTime(),json.getString("SHOW_ID"));	
			Db.update("update t_1do_pstatus set O_STATUS=USER_TYPE,isSend=2 where SHOW_ID=?",json.getString("SHOW_ID"));
			//Db.update("update t_1do_status set O_STATUS=3 where SHOW_ID=?",json.getString("SHOW_ID"));
			Db.update("update t_1do_feedback set isoverdue=2 where SHOW_ID=?",json.getString("SHOW_ID"));
			Db.update("update t_1do_log set isoverdue=2 where SHOW_ID=?",json.getString("SHOW_ID"));
			Db.update("update t_1do_fwpstatus set isSend=2 where SHOW_ID=? ",t1doBase.getShowId());	
			T1doLog.saveLog(json.getString("SHOW_ID"), douser.getString("loginName"), douser.getString("username"), douser.getString("username")+"要求重做此1do", 15,"");
			 
           	new Thread(new SendIdo(t1doBase,1,"","",1)).start();
           	Db.update("update t_1do_pstatus set O_STATUS=1,STATUS='已送达' where SHOW_ID=? and USER_TYPE=1",json.getString("SHOW_ID"));
           	Db.update("update t_1do_pstatus set O_STATUS=3,STATUS='待接单' where SHOW_ID=? and USER_TYPE=3",json.getString("SHOW_ID"));
			Db.update("update t_1do_relation set TYPE=3 where  RELATION_SHOW_ID=?",json.getString("SHOW_ID"));
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
			String showId=IDUtil.getUid();
			t1doBase.setOStartTime(new Date()).setOCreateTime(new Date()).setCreateUser(douser.getString("loginName"))
			.setCreateUserName(douser.getString("username")).setShowId(showId).setSendTime(new Date().getTime()).setOStatus(3)
			.setFBNUM(uploadFiles.size()).save(); //.setORange(StrUtil.getOnly(t1doBase.getOCustomer(),t1doBase.getCreateUser(),t1doBase.getOExecutor())).setORangeName(StrUtil.getOnly(t1doBase.getOCustomerName(),t1doBase.getCreateUserName(),t1doBase.getOExecutorName()))
			t1doBase.savefw();//保存整理层为查看通知做准备。
			/*String[] users={t1doBase.getOCustomer(),t1doBase.getCreateUser(),t1doBase.getOExecutor()};
			String[] usernames={t1doBase.getOCustomerName(),t1doBase.getCreateUserName(),t1doBase.getOExecutorName()};
			T1doPstatus.saveIdoPstatus1(t1doBase.getShowId(),users,usernames);*/
			T1doPstatus.saveIdoPstatus2(t1doBase);
			StrUtil.getQTR(t1doBase);//设置处理人和抄送人
			T1doAttr t1doAttr=t1doBase.newIdoAttr();	
			T1doLog.saveLog(t1doBase.getShowId(), t1doBase.getCreateUser(), t1doBase.getCreateUserName(), t1doBase.getCreateUserName()+"创建了此1do", 1, "");
			for (int i = 0; i < uploadFiles.size(); i++) {		
			    t1doAttr.setAttrPath("https://tyhy.hzxc.gov.cn:8443/1do/upload/"+uploadFiles.get(i));
				t1doAttr.setAttrName(uploadFiles1.get(i));
				t1doAttr.save();
				new T1doFeedback().setShowId(t1doBase.getShowId()).setOUser(t1doAttr.getUploadUser())
				.setOUserName(t1doAttr.getUploadUserName()).setFbTime(t1doAttr.getUploadTime())
				.setFbType(3).setATTRID(t1doAttr.getID()+"").setAttrPath(t1doAttr.getAttrPath()).setFBCONTENT(t1doAttr.getAttrName()).save();
				
				t1doAttr.remove("ID");
				T1doLog.saveLog(t1doBase.getShowId(),t1doAttr.getUploadUser(), t1doAttr.getUploadUserName(), t1doAttr.getUploadUserName()+"上传"+t1doAttr.getAttrName(), 3, t1doAttr.getAttrName()); 	
			}
			
			
			
	        new Thread(new SendIdo(t1doBase,1,"","",1)).start();
	        new Thread(new AddLabel(t1doBase.getODescribe(), showId,2)).start();//批量添加标签

			renderJson(JsonUtil.getMap(200, "创建1do成功！"));
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
					new Thread(new SendIdo(t1doBase,4,t1doFeedback.getFBCONTENT())).start();
					//三实库
					new Thread(new SendIdo(t1doBase,5,t1doFeedback.getFBCONTENT())).start();
				}
				
				if(t1doPstatus!=null&&t1doBase.getOStatus()==3){
					//第一个参与人反馈修改工单状态
					//t1doBase.update();
					t1doPstatus.setOStatus(4).setSTATUS("已接单").update();
					t1doBase.setOStatus(4).setSendTime(new Date().getTime()).setFBNUM(t1doBase.getFBNUM()+1).update();	
					DbUtil.update(t1doFeedback.getShowId(),douser.getString("loginName"), 15, 0, "", ""); 
		           	new Thread(new SendIdo(t1doBase,3,douser.getString("loginName"),"",1)).start();
		           	
				}else{
					t1doBase.setSendTime(new Date().getTime()).setFBNUM(t1doBase.getFBNUM()+1).update();
					//final int temp=t1doBase.getOStatus()==3?1:t1doBase.getOStatus();
					//反馈设置用户未读
					DbUtil.update(t1doFeedback.getShowId(),douser.getString("loginName"), 16, 0, "", ""); 
		           	new Thread(new SendIdo(t1doBase,9,douser.getString("loginName"),"",1)).start();

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
						
						if(t1doBase.getLIGHTNING()+1>5){
							t1doFeedback.setShortMessage(ShortMessageUtil.sendShortMessage(t1doBase.getShowId()));
						}
						t1doBase.setSendTime(new Date().getTime()).setLIGHTNING(t1doBase.getLIGHTNING()+1).update();
						str+="催办此1do";						
						t1doFeedback.setFBCONTENT(str).save();
						Db.update("update t_1do_pstatus set isSend=2,urge_isLook=0 where SHOW_ID=? and isDelete=1 and (online=2 or gmt_modified<CURDATE())",t1doBase.getShowId());
						//Db.update("update t_1do_pstatus set urge_isLook=0 where SHOW_ID=?",t1doBase.getShowId());
						Db.update("update t_1do_base b,(select SHOW_ID,GROUP_CONCAT(O_USER_NAME) O_USER_NAME,GROUP_CONCAT(O_USER) O_USER from(select SHOW_ID,O_USER_NAME,O_USER from t_1do_feedback where fb_type=4 group by SHOW_ID,O_USER_NAME)a group by SHOW_ID) c" 
                        +" set b.URGENAME=c.O_USER_NAME,b.URGESHOWID=c.O_USER where b.SHOW_ID=c.SHOW_ID and b.SHOW_ID=?",t1doBase.getShowId());
						
						new UrgeController().sendMessage(t1doBase.getShowId());//发送催报给连接websoct的客户端						 
			           	new Thread(new SendIdo(t1doBase,4,"","",1)).start();

					}else{
						//renderJson(JsonUtil.getMap(202, "权限不足"));
						renderJson(MsgUtil.errorMsg("权限不足"));
						return;
					}
					
				}else if(t1doLog.getLogType()==5){
					if(douser.getBoolean("isfw")||StrUtil.getflag(t1.getUserType(), 7)){
						
						//int i=Db.update("update t_1do_status set O_STATUS=5 where SHOW_ID=?",t1doFeedback.getShowId());
						if(t1doBase.getOStatus()>=5){
							renderJson(JsonUtil.getMap(200, "该1do已经办结"));
							return;
						}
						Db.update("update t_1do_pstatus set O_STATUS=5,STATUS='已完成'  where SHOW_ID=? ",t1doFeedback.getShowId());
						Db.update("update t_1do_pstatus set isSend=2 where SHOW_ID=? and (online=2 or gmt_modified<CURDATE())",t1doFeedback.getShowId());
						Db.update("update t_1do_fwpstatus set isSend=2 where SHOW_ID=? and (online=2 or gmt_modified<CURDATE())",t1doBase.getShowId());
						str+="确认办结";
						t1doFeedback.setFBCONTENT("确认办结").save();
						t1doBase.setRealFinishTime(new Date()).setSendTime(new Date().getTime()).setFBNUM(t1doBase.getFBNUM()+1).setOStatus(5).update();						
						 
			           	new Thread(new SendIdo(t1doBase,5,"","",1)).start();
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
						
						 
			           	new Thread(new SendIdo(t1doBase,6,"","",1)).start();

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
		 2018年6月21日 coco //创建保存1do（1call转1do时）
		*/
	    @Before(Tx.class)
		public void createIdo() {
			JSONObject json=JsonUtil.getJSONObject(getRequest());
			System.out.println(json.toString());
			final T1doBase t1doBase =json.getObject("BASE", T1doBase.class);
			JSONArray arr=json.getJSONArray("ATTR");
			String showId=IDUtil.getUid();
			t1doBase.setOStartTime(new Date()).setOCreateTime(new Date()).setCreateUser("1call").setCreateUserName("1call").setOStatus(3)
			.setShowId(showId).setSendTime(new Date().getTime()).setFBNUM(arr==null?0:arr.size()).save(); 
			//.setORangeName(StrUtil.getOnly(t1doBase.getOCustomerName(),t1doBase.getCreateUserName(),t1doBase.getOExecutorName())).setORange(StrUtil.getOnly(t1doBase.getOCustomer(),t1doBase.getCreateUser(),t1doBase.getOExecutor()))
			t1doBase.savefw();//保存整理层为查看通知做准备。
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
			/*String[] users={t1doBase.getOCustomer(),t1doBase.getCreateUser(),t1doBase.getOExecutor()};
			String[] usernames={t1doBase.getOCustomerName(),t1doBase.getCreateUserName(),t1doBase.getOExecutorName()};
			T1doPstatus.saveIdoPstatus1(t1doBase.getShowId(),users,usernames);*/
			new Thread(new AddLabel(t1doBase.getODescribe(), showId,2)).start();//批量添加标签
			T1doPstatus.saveIdoPstatus2(t1doBase);
			StrUtil.getQTR(t1doBase);//设置处理人和抄送人
			T1doLog.saveLog(t1doBase.getShowId(), t1doBase.getCreateUser(), t1doBase.getCreateUserName(), t1doBase.getCreateUserName()+"创建了此1do", 1, "");
			
           	new Thread(new SendIdo(t1doBase,1,"","",1)).start();

			renderJson(JsonUtil.getMap(200, t1doBase.getShowId()));
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
	     
	      
		

}
