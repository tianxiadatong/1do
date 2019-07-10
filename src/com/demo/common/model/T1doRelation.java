package com.demo.common.model;

import java.util.List;

import com.demo.common.model.base.BaseT1doRelation;
import com.demo.util.DbUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class T1doRelation extends BaseT1doRelation<T1doRelation> {
	public static final T1doRelation dao = new T1doRelation().dao();
	/*
	 2019年5月9日 coco 注解：
	*/
	public static List<T1doRelation> getList(String SHOW_ID) {
		return dao.find(" select a.SHOW_ID,b.SHOW_ID RELATION_SHOW_ID,(CASE WHEN count(*)/c.cnum*100>count(*)/d.dnum*100 THEN count(*)/d.dnum*100 ELSE count(*)/c.cnum*100 END )SIMILARITY"
				+" from t_1do_label a,(select * from t_1do_label GROUP BY SHOW_ID,LABEL) b,(select count(*) cnum,SHOW_ID from t_1do_label GROUP BY SHOW_ID) c,"
				+" (select count(*) dnum,SHOW_ID from t_1do_label  GROUP BY SHOW_ID) D"
				+" where a.LABEL=b.LABEL and a.SHOW_ID=? and b.SHOW_ID!=? and a.SHOW_ID=c.SHOW_ID and b.SHOW_ID=d.SHOW_ID"
				+" group by a.SHOW_ID,b.SHOW_ID ",SHOW_ID,SHOW_ID);
	}
	/*
	 2019年2月12日 coco 注解：保存关联1do
	*/
	public static void saveRelation(String showId,String relationShowId,Integer SIMILARITY,Integer TYPE) {
		new T1doRelation().setShowId(showId).setRelationShowId(relationShowId).setTYPE(TYPE)
		.setSIMILARITY(SIMILARITY).save();
	}
	/*
	 2019年2月12日 coco 注解：修改关联1do
	 */
	public static void updateRelation(String showId,String relationShowId,Integer SIMILARITY) {
		Db.update("update t_1do_relation set SIMILARITY=? where SHOW_ID=? and RELATION_SHOW_ID=?",SIMILARITY,showId,relationShowId);
		Db.update("update t_1do_relation set SIMILARITY=? where SHOW_ID=? and RELATION_SHOW_ID=?",SIMILARITY,relationShowId,showId);
	}
	/*
	 2019年2月12日 coco 注解：//查询关联1do
	 */
	public static List<Record> selectRelation(String showId,String sql) {
		return Db.find("select  a.ID,a.SIMILARITY,b.SHOW_ID,a.TYPE,b.O_TITLE O_DESCRIBE,unix_timestamp(b.O_CREATE_TIME)*1000 O_CREATE_TIME,unix_timestamp(b.O_FINISH_TIME)*1000 O_FINISH_TIME,b.FBNUM,b.LOOKNUM,"
				+ "(case b.O_STATUS when 3 then '待接单' when 4 then '已接单' when 5 then '已完成' else '已删除' end) O_STATUS"
				+ ",b.O_CUSTOMER_NAME,b.O_EXECUTOR_NAME,b.O_CUSTOMER,b.O_EXECUTOR from t_1do_relation a,t_1do_base b "
				+ "where (a.SIMILARITY>b.SIMILARITY or a.TYPE<=0) and a.RELATION_SHOW_ID=b.SHOW_ID and a.SHOW_ID=? "+sql+" ORDER BY a.SORT,a.SIMILARITY desc",showId);
	}
	/*
	 2019年2月14日 coco 注解：修改相似度
	*/
	public static void updateSimilarity(String SHOW_ID,String sql,Integer type) {
		 if(type==1){
			 
			/* List<T1doLabel> list=T1doLabel.dao.find("select * from t_1do_label where SHOW_ID=?",SHOW_ID);
			 List<T1doRelation> base1=dao.find("select * from t_1do_relation where SHOW_ID=? "+sql,SHOW_ID);
			 base1.forEach(t1->{
				 List<T1doLabel> list1=T1doLabel.dao.find("select * from t_1do_label where SHOW_ID=? ",t1.getRelationShowId());
				 int result=TestController.getHashSet(list, list1);
				 int i=list.size()>list1.size()?list.size():list1.size();
				 int a=(int) ((double)(list.size()+list1.size()-result)/i*100);
				 T1doRelation.updateRelation(SHOW_ID, t1.getRelationShowId(), a);				
			 });
			 */
			 Db.update("update t_1do_relation set SIMILARITY=0 where SHOW_ID=? or RELATION_SHOW_ID=?",SHOW_ID,SHOW_ID);
			 List<T1doRelation> list = T1doRelation.getList(SHOW_ID);
			 list.forEach(t1->{				 
				 T1doRelation.updateRelation(SHOW_ID, t1.getRelationShowId(), t1.getSIMILARITY());			
			 });
			 
			 List<T1doRelationFeedback> tk=T1doRelationFeedback.dao.find("select * from t_1do_relation_feedback where SHOW_ID=? and type=0",SHOW_ID);
			 Db.delete("delete from t_1do_relation_feedback where SHOW_ID=?",SHOW_ID);
			 DbUtil.insertRF(SHOW_ID);
			 tk.forEach(tt->{
				 Db.update("update t_1do_relation_feedback set type=0 where SHOW_ID=? and FBID=?",tt.getShowId(),tt.getFBID());
			 });
			 
			 List<T1doRelationRecord> tr=T1doRelationRecord.dao.find("select * from t_1do_relation_record where SHOW_ID=? and type=0",SHOW_ID);
			 Db.delete("delete from t_1do_relation_record where SHOW_ID=?",SHOW_ID);
			 DbUtil.insertlr1(SHOW_ID);
			 tr.forEach(tt->{
				 Db.update("update t_1do_relation_record set type=0 where SHOW_ID=? and RECORDID=?",tt.getShowId(),tt.getRECORDID());
			 });
		 }else{
			/*List<T1doLabel> list=T1doLabel.dao.find("select * from t_1do_label where SHOW_ID=?",SHOW_ID);
			List<T1doBase> base1=T1doBase.dao.find("select * from t_1do_base where SHOW_ID!=? ",SHOW_ID);
			base1.forEach(t1->{
				List<T1doLabel> list1=T1doLabel.dao.find("select * from t_1do_label where SHOW_ID=?",t1.getShowId());
				int result=TestController.getHashSet(list, list1);
				int i=list.size()>list1.size()?list.size():list1.size();
				int a=(int) ((double)(list.size()+list1.size()-result)/i*100);
				T1doRelation.saveRelation(SHOW_ID, t1.getShowId(), a,t1.getOStatus());
				T1doRelation.saveRelation(t1.getShowId(), SHOW_ID, a,3);
				
			});*/
			DbUtil.insertIdo(SHOW_ID);
			DbUtil.updateType(SHOW_ID);
			DbUtil.insertRF(SHOW_ID);
			DbUtil.insertlr1(SHOW_ID);
		 }
	}
}
