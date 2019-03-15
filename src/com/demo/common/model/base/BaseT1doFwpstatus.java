package com.demo.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseT1doFwpstatus<M extends BaseT1doFwpstatus<M>> extends Model<M> implements IBean {

	public M setID(java.lang.Integer ID) {
		set("ID", ID);
		return (M)this;
	}
	
	public java.lang.Integer getID() {
		return getInt("ID");
	}

	public M setShowId(java.lang.String showId) {
		set("SHOW_ID", showId);
		return (M)this;
	}
	
	public java.lang.String getShowId() {
		return getStr("SHOW_ID");
	}

	public M setOUser(java.lang.String oUser) {
		set("O_USER", oUser);
		return (M)this;
	}
	
	public java.lang.String getOUser() {
		return getStr("O_USER");
	}

	public M setIsSend(java.lang.Integer isSend) {
		set("isSend", isSend);
		return (M)this;
	}
	
	public java.lang.Integer getIsSend() {
		return getInt("isSend");
	}

	public M setOnline(java.lang.Integer online) {
		set("online", online);
		return (M)this;
	}
	
	public java.lang.Integer getOnline() {
		return getInt("online");
	}

	public M setModifyTime(java.util.Date modifyTime) {
		set("modifyTime", modifyTime);
		return (M)this;
	}
	
	public java.util.Date getModifyTime() {
		return get("modifyTime");
	}

	public M setGmtModified(java.util.Date gmtModified) {
		set("gmt_modified", gmtModified);
		return (M)this;
	}
	
	public java.util.Date getGmtModified() {
		return get("gmt_modified");
	}

}
