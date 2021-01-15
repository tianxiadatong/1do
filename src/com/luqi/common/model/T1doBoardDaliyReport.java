package com.luqi.common.model;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.template.expr.ast.Id;
import com.luqi.common.model.base.BaseT1doBoardDaliyReport;

import java.util.Date;
import java.util.List;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class T1doBoardDaliyReport extends BaseT1doBoardDaliyReport<T1doBoardDaliyReport> {
	public static final T1doBoardDaliyReport dao = new T1doBoardDaliyReport().dao();

	/**
	 * 获取项目各公司日报最早报送时间
	 * @param projectId
	 * @param date
	 * @return
	 */
	public static List<Record> getCompanyLogTimeList(long projectId, String date) {
		String sql = "SELECT MIN(`TIME`) time,r.`COMPANY` companyShowId,d.`D_PATH_NAME` companyName FROM `t_1do_board_daliy_report` r,`t_reg_company_dept` d WHERE r.`COMPANY`=d.`SHOW_ID` and `DATE`=? and `PROJECT_ID`=? GROUP BY company ORDER BY time";
		return Db.find(sql, date, projectId);
	}

	/**
	 * 获取该项目所有报告的日期，以日报日期为准
	 * @return
	 */
	public static List<Record> getProjectReportDate(long projectId) {
		String sql = "select `DATE` date from `t_1do_board_daliy_report` WHERE `PROJECT_ID` = ? and date<left(now(),10) GROUP BY `DATE` ORDER BY `DATE` DESC";
		return Db.find(sql, projectId);
	}

	/**
	 * 获取日报字数
	 * @param projectId
	 * @param date
	 * @return
	 */
	public static List<Record> getLogNumber(long projectId, String date) {
		String sql = "SELECT r.`COMPANY` companyShowId,d.`D_PATH_NAME` companyName,sum(r.`NUMBER`) number FROM `t_1do_board_daliy_report` r,`t_reg_company_dept` d WHERE r.`COMPANY`=d.`SHOW_ID` and `DATE`=? and `PROJECT_ID`=? GROUP BY company ORDER BY number desc";
		return Db.find(sql, date, projectId);
	}

    /**
     * 获取项目当日某公司日志列表
     * @param projectId
     * @param date
     * @param taskId
     * @param companyShowId
     * @return
     */
	public static List<Record> getLogByCompany(long projectId, Date date, long taskId, String companyShowId) {
		String sql = "SELECT r.`ID` id,r.`CONTENT` content,c.`D_PATH_NAME` companyName,r.`TASK_ID` taskId FROM `t_1do_board_daliy_report` r,`t_reg_company_dept` c WHERE `DATE` = ? and `PROJECT_ID` = ? and (`TASK_ID` = ? or `TASK_ID` IS NULL or `TASK_ID` = 0) and r.`COMPANY` = c.`SHOW_ID`";
		if (StrKit.notBlank(companyShowId)) {
			sql = sql + " and r.`COMPANY` in ("+companyShowId+")";
		}
		return Db.find(sql, date, projectId, taskId);
	}

	/**
	 * 关联任务
	 * @param taskId
	 * @param logId
	 */
	public static void linkedTask(Long taskId, long logId) {
		String sql = "UPDATE `t_1do_board_daliy_report` SET `TASK_ID` = ? WHERE `ID` = ?";
		Db.update(sql,taskId,logId);
	}

	/**
	 * 根据日志id删除
	 * @param reportId
	 */
	public static void deleteByReportId(Long reportId) {
		String sql = "DELETE FROM t_1do_board_daliy_report WHERE REPORT_ID = ?";
		Db.update(sql, reportId);
	}

	/**
	 * 根据id查询
	 * @param id
	 * @return
	 */
	public static T1doBoardDaliyReport getInstanceById(long id) {
		String sql = "SELECT d.*,b.`ITEM_NAME` projectName,t.`TASK` taskName FROM `t_1do_board_daliy_report` d, `t_1do_board` b,`t_1do_board_task` t " +
				"WHERE d.`ID` = ? and  d.`PROJECT_ID` = b.`ID` and d.`TASK_ID` = t.`ITEM_ID` ";
		return dao.findFirst(sql, id);
	}
}
