package com.luqi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseT1doBoardTask1do<M extends BaseT1doBoardTask1do<M>> extends Model<M> implements IBean {

	public M setID(java.lang.Long ID) {
		set("ID", ID);
		return (M)this;
	}
	
	public java.lang.Long getID() {
		return getLong("ID");
	}

	public M setDATE(java.util.Date DATE) {
		set("DATE", DATE);
		return (M)this;
	}
	
	public java.util.Date getDATE() {
		return get("DATE");
	}

	public M setDATA(java.lang.String DATA) {
		set("DATA", DATA);
		return (M)this;
	}
	
	public java.lang.String getDATA() {
		return getStr("DATA");
	}

	public M setItemId(java.lang.Long itemId) {
		set("ITEM_ID", itemId);
		return (M)this;
	}
	
	public java.lang.Long getItemId() {
		return getLong("ITEM_ID");
	}

	public M setATEMP(java.lang.String ATEMP) {
		set("ATEMP", ATEMP);
		return (M)this;
	}
	
	public java.lang.String getATEMP() {
		return getStr("ATEMP");
	}

	public M setBTEMP(java.lang.String BTEMP) {
		set("BTEMP", BTEMP);
		return (M)this;
	}
	
	public java.lang.String getBTEMP() {
		return getStr("BTEMP");
	}

	public M setShowId(java.lang.String showId) {
		set("SHOW_ID", showId);
		return (M)this;
	}
	
	public java.lang.String getShowId() {
		return getStr("SHOW_ID");
	}

	public M setPlannedTime(java.lang.String plannedTime) {
		set("PLANNED_TIME", plannedTime);
		return (M)this;
	}
	
	public java.lang.String getPlannedTime() {
		return getStr("PLANNED_TIME");
	}

}
