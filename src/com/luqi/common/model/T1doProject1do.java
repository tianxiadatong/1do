package com.luqi.common.model;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.luqi.common.model.base.BaseT1doProject1do;

import java.util.List;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class T1doProject1do extends BaseT1doProject1do<T1doProject1do> {
	public static final T1doProject1do dao = new T1doProject1do().dao();
	//查询1do所在的项目节点
	public static T1doProject1do getT1doProject1do(String showId) {
		return dao.findFirst("select * from t_1do_project_1do where SHOW_ID=?",showId);
	}

	/**
	 * 根据1doshowId查询1do上级节点
	 * @param showId
	 * @return
	 */
	public static List<T1doProject1do> getItemsByShowId(String showId) {
		String sql = "SELECT * FROM t_1do_project_1do WHERE SHOW_ID IN (SELECT SHOW_ID FROM t_1do_base WHERE O_STATUS <> 6 AND O_CUSTOMER LIKE concat('%',?,'%') OR O_EXECUTOR LIKE concat('%',?,'%'))";
		return dao.find(sql, showId, showId);
	}

	/**
	 * 获取该用户，项目的1do
	 * @param ids 项目及其子集id
	 * @param showId 人员showId
	 * @return
	 */
	public static List<Record> getProject1do(String ids,String showId) {
		String sql = "SELECT\n" +
				"\tp.ITEM_ID parentid, \n" +
				"\tb.O_FINISH_TIME finishTime,\n" +
				"\tb.O_TITLE topic,\n" +
				"\tb.O_STATUS status,b.SHOW_ID\n" +
				"FROM\n" +
				"\tt_1do_base b,\n" +
				"\tt_1do_project_1do p\n" +
				"WHERE\n" +
				"\tb.SHOW_ID = p.SHOW_ID \n" +
				"\tAND\n" +
				"\tb.O_STATUS <> 6 \n" +
				"\tAND (b.O_CUSTOMER LIKE concat( '%', ?, '%' ) \n" +
				"\tOR b.O_EXECUTOR LIKE concat( '%', ?, '%' ) )\n" +
				"\tAND p.ITEM_ID IN ("+ids+")";
		return Db.find(sql,showId,showId);
	}
	/**
	 * 获取该用户，项目的1do
	 * @param ids 项目及其子集id
	 * @param hide 
	 * @return
	 */
	public static List<Record> getProject1do2(String ids, Integer hide) {
		String sql = "SELECT\n" +
				"\tp.ITEM_ID parentid, \n" +
				"\tp.SHOW_ID id, \n" +
				"\tb.O_FINISH_TIME finishTime,\n" +
				"\tb.O_TITLE topic,\n" +
				"\tb.O_STATUS status,b.SHOW_ID\n" +
				"FROM\n" +
				"\tt_1do_base b,\n" +
				"\tt_1do_project_1do p\n" +
				"WHERE\n" +
				"\tb.SHOW_ID = p.SHOW_ID \n" +
				"\tAND\n" +
				"\tb.O_STATUS < ? \n" +
				"\tAND p.ITEM_ID IN ("+ids+")";
		return Db.find(sql,6-hide);
	}

	/**
	 * 更新依赖节点
	 * @param itemId
	 * @param showId
	 */
	public static void updateItemId(long itemId, String showId) {
		String sql = "UPDATE `t_1do_project_1do` SET `ITEM_ID` = ? WHERE `SHOW_ID` = ?";
		Db.update(sql, itemId, showId);
	}
}
