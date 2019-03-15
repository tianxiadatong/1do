package com.demo.util;

import com.jfinal.plugin.activerecord.Db;

public class DbUtil {
	/*
	 2019年3月8日 coco 注解：
	*/
	public static int update(String showid,String showid1,int type,int temp,String showid2,String showid3) {
		switch (type) {
		case 1:			
			//删除1do关联
			return Db.update("update t_1do_relation a,t_1do_base b set a.sort=0,a.TYPE=b.o_status "
					+ "where ((a.SHOW_ID=? and a.RELATION_SHOW_ID=?) or (a.SHOW_ID=? and a.RELATION_SHOW_ID=?)) and b.SHOW_ID=a.RELATION_SHOW_ID and a.TYPE=0 "
					,showid,showid1,showid1,showid);
		case 2:		
			//批量添加1do关联
			return Db.update("update t_1do_relation set sort=-1,TYPE=0 "
					+ "where (SHOW_ID=? and RELATION_SHOW_ID=?) or (SHOW_ID=? and RELATION_SHOW_ID=?)"
					,showid,showid1,showid1,showid);
		case 3:
			//修改参与人的身份（抄送人或受理人）
			return Db.update("update t_1do_pstatus set otherid=0 where"
					+ " otherid=? and SHOW_ID=? ",temp,showid);
			
		case 4:
			//修改参与人的身份（抄送人或受理人）
			return Db.update("update t_1do_pstatus set otherid=? where"
					+ " SHOW_ID=? and O_USER=?",temp,showid,showid1);
		case 5:
			return Db.update("update t_1do_pstatus set isSend=1 where SHOW_ID=? and O_USER=? and isDelete=1 and USER_TYPE!=2 and (online=2 or gmt_modified<CURDATE())",showid,showid1);
		case 6:	
			return Db.update("update t_1do_fwpstatus set isSend=1 where SHOW_ID=? and O_USER=? and (online=2 or gmt_modified<CURDATE())",showid,showid1);
		case 7:
			//在线已读
			Db.update("update t_1do_pstatus set online=1,isSend=1 where SHOW_ID=? and O_USER=?",showid,showid1);
			Db.update("update t_1do_fwpstatus set online=1,isSend=1 where SHOW_ID=? and O_USER=?",showid,showid1);
			return 1;
		case 8:		
			//修改创建用户
			Db.update("update t_1do_base set CREATE_USER=?,CREATE_USER_NAME=? where CREATE_USER='1call' and SHOW_ID=?",showid1,showid2,showid);
			return 1;
		case 9:
			//修改标题或者内容
			return Db.update("UPDATE t_1do_base set "+showid+"=? ,AT=? where SHOW_ID=?",showid1,showid2,showid3);
		case 10:
			Db.update("update t_1do_pstatus set isDelete=2  where SHOW_ID=? and O_USER not in("+showid1+") and USER_TYPE=?",showid1,temp);
			return 1;
		case 11:			
			return Db.update("update t_1do_pstatus set isDelete=1 where SHOW_ID=? and O_USER=? and USER_TYPE=?",showid,showid1,temp);
		case 12:	
			Db.update("update t_1do_pstatus set isDelete=2  where SHOW_ID=? and O_USER=? and USER_TYPE=?",showid,showid1,temp);
			return 1;
		case 13:			
			return Db.update("update t_1do_pstatus set isDelete=1 where SHOW_ID=? and O_USER=? and USER_TYPE=?",showid,showid1,temp);

		case 14:	
			Db.update("update t_1do_pstatus set online=2 where SHOW_ID=? and O_USER=?",showid,showid1);
			Db.update("update t_1do_fwpstatus set online=2 where SHOW_ID=? and O_USER=?",showid,showid1);
			return 1;
		case 15:
			//第一个参与人反馈修改人员状态
			Db.update("update t_1do_pstatus set O_STATUS=4,STATUS='已接单' where SHOW_ID=? and USER_TYPE!=3",showid);
			//Db.update("update t_1do_pstatus set isSend=2  where  (online=2 or gmt_modified<CURDATE()) and SHOW_ID=? and (USER_TYPE=1 or O_USER=? ) and isDelete=1",showid,showid1);
			Db.update("update t_1do_pstatus set isSend=2  where  (online=2 or gmt_modified<CURDATE()) and SHOW_ID=? and (USER_TYPE=1 or O_USER=? ) and isDelete=1",showid,showid1);
			Db.update("update t_1do_fwpstatus set isSend=2 where SHOW_ID=? and (online=2 or gmt_modified<CURDATE()) ",showid);
			
			return 1;
		case 16:	
			//反馈设置用户未读
			Db.update("update t_1do_pstatus set O_STATUS=4,STATUS='已接单' where SHOW_ID=? and O_USER=? and O_STATUS=3",showid,showid1);
			Db.update("update t_1do_pstatus set isSend=2 where SHOW_ID=? and isDelete=1 and (online=2 or gmt_modified<CURDATE())",showid);
			Db.update("update t_1do_fwpstatus set isSend=2 where SHOW_ID=? and (online=2 or gmt_modified<CURDATE())",showid);
			return 1;
		case 17:			
			return 1;
		case 18:			
			return 1;
		case 19:			
			return 1;
		default:
			return 1;
		}
	}
}
