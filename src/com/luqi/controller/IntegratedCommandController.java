package com.luqi.controller;

import java.util.Date;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.luqi.common.model.T1doBase;
import com.luqi.common.model.T1doFeedback;
import com.luqi.common.model.T1doFw;
import com.luqi.common.model.T1doLabelFeedback;
import com.luqi.common.model.T1doLog;
import com.luqi.common.model.T1doPstatus;
import com.luqi.common.model.TRegUser;
import com.luqi.service.DoService;
import com.luqi.timer.SendIdo;
import com.luqi.util.DbUtil;
import com.luqi.util.JsonUtil;
import com.luqi.util.MsgUtil;
import com.luqi.util.ShortMessageUtil;
import com.luqi.util.StrUtil;

/**
* @author coco
* @date 2019年8月30日 上午11:05:02
*  @Description 综合指挥平台
*/
public class IntegratedCommandController extends Controller {
	
	/**
     * @Author coco
     * @Description 设置session		
     * @Date 
    */
	public  JSONObject setSession(String loginName){
			
		T1doFw t1doFw =T1doFw.getIdoFwForLoginName(loginName);
		boolean isfw=t1doFw==null?false:true;
		JSONObject json1=TRegUser.getUserForShowId(loginName);
		json1.put("isfw", isfw);
		json1.put("code", 200);		
		setSessionAttr("user", json1);
		return json1;
		//renderJson(json1);
	}
	
	/**
     * @Author coco
     * @Description 普通反馈
     * @Date  2018年6月25日下午3:59:19
    */
	@Before(Tx.class)
	public void feedback() {
		JSONObject json=JsonUtil.getJSONObject(getRequest());
		 JSONObject user1=getSessionAttr("user");
		if(json.getString("loginName")==null&&user1==null){
			renderJson(MsgUtil.errorMsg("用户未登入"));
			return;
		}else if(json.getString("loginName")!=null&&user1==null){
			user1=setSession(json.getString("loginName"));				
		}
		final JSONObject user=user1;
		final T1doFeedback t1doFeedback =json.toJavaObject(T1doFeedback.class);
		//查询是否是参与人
		T1doPstatus t1doPstatus=T1doPstatus.getCustomerOrExecutor(t1doFeedback.getShowId(),user.getString("loginName"),3);
		//T1doStatus t1doStatus=t1doFeedback.getIdoStatus();
		final T1doBase t1doBase=t1doFeedback.getT1doBase();
		t1doFeedback.setFbTime(new Date()).setTimeStamp(new Date().getTime()).setOUser(user.getString("loginName")).setOUserName(user.getString("username"));
		T1doLog t1doLog=json.toJavaObject(T1doLog.class);
		t1doLog.setOpTime(t1doFeedback.getFbTime()).setOUser(user.getString("loginName")).setOUserName(user.getString("username"));	
		T1doPstatus t1=T1doPstatus.getUser(json.getString("SHOW_ID"),user.getString("loginName"));
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
				t1doBase.setOStatus(4).setSendTime(new Date().getTime()).setFBNUM(t1doBase.getFBNUM()+1).update();	
				DbUtil.update(t1doFeedback.getShowId(),user.getString("loginName"), 15, 0, "", ""); 
	           	new Thread(new SendIdo(t1doBase,3,user.getString("loginName"),"",1)).start();
	           	
			}else{
				t1doBase.setSendTime(new Date().getTime()).setFBNUM(t1doBase.getFBNUM()+1).update();
				final int temp=t1doBase.getOStatus()==3?1:t1doBase.getOStatus();
				//反馈设置用户未读
				DbUtil.update(t1doFeedback.getShowId(),user.getString("loginName"), 16, 0, "", ""); 
	           	new Thread(new SendIdo(t1doBase,temp,user.getString("loginName"),"",1)).start();

	           	
			}
			
			t1doLog.setLogType(10).setLog(user.getString("username")+"反馈一条信息").setContent(t1doFeedback.getID()+"").save();
			new Thread( new Runnable() {	
				@Override
				public void run() {	
					new WebSocketController().sendMessage(t1doBase.getShowId());
				}				
			}).start();
			new Thread( new Runnable() {	
				@Override
				public void run() {	
					T1doLabelFeedback.saveAllLabel(t1doFeedback);
				}				
			}).start();
			//批量添加1do通知数据不发通知
			new Thread( new Runnable() {	
				@Override
				public void run() {	
					DoService.sendIdoBatchPut(t1doBase,user,t1doFeedback.getFBCONTENT());
				}				
			}).start();
			renderJson(MsgUtil.successMsg(t1doFeedback));
			return;
		}else{
			t1doLog.setLogType(t1doFeedback.getFbType());
			String str=t1doLog.getOUserName();
			if(t1doLog.getLogType()==4){
				if(user.getBooleanValue("isfw")||StrUtil.getflag(t1.getUserType(), 6)){
					
					if(t1doBase.getLIGHTNING()+1>5){
						String[] result =ShortMessageUtil.sendShortMessage(t1doBase.getShowId(),t1doBase.getOTitle(),t1doFeedback.getOUserName());
						t1doFeedback.setShortMessage(result[0]);
						t1doFeedback.setCallMessage(result[1]);
						}
					t1doBase.setSendTime(new Date().getTime()).setLIGHTNING(t1doBase.getLIGHTNING()+1).update();
					str+="催办此1do";						
					t1doFeedback.setFBCONTENT(str).save();
					Db.update("update t_1do_pstatus set isRead=2,urge_isLook=0 where SHOW_ID=? and isDelete=1 ",t1doBase.getShowId());//and (online=2 or gmt_modified<CURDATE())
					//Db.update("update t_1do_pstatus set urge_isLook=0 where SHOW_ID=?",t1doBase.getShowId());
					Db.update("update t_1do_base b,(select SHOW_ID,GROUP_CONCAT(O_USER_NAME) O_USER_NAME,GROUP_CONCAT(O_USER) O_USER from(select SHOW_ID,O_USER_NAME,O_USER from t_1do_feedback where fb_type=4 group by SHOW_ID,O_USER_NAME)a group by SHOW_ID) c" 
                    +" set b.URGENAME=c.O_USER_NAME,b.URGESHOWID=c.O_USER where b.SHOW_ID=c.SHOW_ID and b.SHOW_ID=?",t1doBase.getShowId());
					new Thread( new Runnable() {	
						@Override
						public void run() {	
					           new UrgeController().sendMessage(t1doBase.getShowId());//发送催报给连接websoct的客户端						 
						}				
					}).start();
					new Thread(new SendIdo(t1doBase,4,user.getString("loginName"),"",1)).start();

				}else{
					renderJson(MsgUtil.errorMsg("权限不足"));
					return;
				}
				
			}else if(t1doLog.getLogType()==5){
				if(user.getBoolean("isfw")||StrUtil.getflag(t1.getUserType(), 7)){
					
					//int i=Db.update("update t_1do_status set O_STATUS=5 where SHOW_ID=?",t1doFeedback.getShowId());
					if(t1doBase.getOStatus()>=5){
						renderJson(MsgUtil.errorMsg("该1do已经办结"));
						return;
					}
					Db.update("update t_1do_pstatus set O_STATUS=5,STATUS='已完成'  where SHOW_ID=? ",t1doFeedback.getShowId());
					Db.update("update t_1do_pstatus set isRead=2 where SHOW_ID=? and (online=2 or gmt_modified<CURDATE())",t1doFeedback.getShowId());
					Db.update("update t_1do_fwpstatus set isRead=2 where SHOW_ID=? and (online=2 or gmt_modified<CURDATE())",t1doBase.getShowId());
					str+="确认办结";
					t1doFeedback.setFBCONTENT("确认办结").save();
					t1doBase.setRealFinishTime(new Date()).setSendTime(new Date().getTime()).setFBNUM(t1doBase.getFBNUM()+1).setOStatus(5).update();						
					 
		           	new Thread(new SendIdo(t1doBase,5,user.getString("loginName"),"",1)).start();
				}else{
					renderJson(MsgUtil.errorMsg("权限不足"));
					
					return;
				}
				
			}else if(t1doLog.getLogType()==6){
				if(user.getBoolean("isfw")||StrUtil.getflag(t1.getUserType(),8)){
					t1doFeedback.setFBCONTENT("评价："+t1doFeedback.getFBCONTENT()).save();
					str+="评价了此1do";
					t1doBase.setStar(t1doFeedback.getStar()).setEvaluation(t1doFeedback.getFBCONTENT()).setSendTime(new Date().getTime()).setFBNUM(t1doBase.getFBNUM()+1).update();
					
					new Thread( new Runnable() {	
						@Override
						public void run() {	
							T1doLabelFeedback.saveAllLabel(t1doFeedback);
						}				
					}).start();
		           	new Thread(new SendIdo(t1doBase,6,user.getString("loginName"),"",1)).start();

				}else{
					renderJson(MsgUtil.errorMsg("权限不足"));
					
					return;
				}
			}		
			t1doLog.setLog(str).save();
			//t1doLog.save();
			
			new Thread( new Runnable() {	
				@Override
				public void run() {	
			new WebSocketController().sendMessage(t1doBase.getShowId());//发送反馈给连接websoct的客户端.
		    }				
	        }).start();
			//批量添加1do通知数据不发通知
			new Thread( new Runnable() {	
				@Override
				public void run() {	
					DoService.sendIdoBatchPut(t1doBase,user,"评价："+t1doFeedback.getFBCONTENT());
				}				
			}).start();
			renderJson(MsgUtil.successMsg(t1doFeedback));
			return;
		}
		
	}
	
	
	
	
	

	

}
