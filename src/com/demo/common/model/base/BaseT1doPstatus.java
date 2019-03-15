package com.demo.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseT1doPstatus<M extends BaseT1doPstatus<M>> extends Model<M> implements IBean {

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

	public M setOUserName(java.lang.String oUserName) {
		set("O_USER_NAME", oUserName);
		return (M)this;
	}
	
	public java.lang.String getOUserName() {
		return getStr("O_USER_NAME");
	}

	public M setOStatus(java.lang.Integer oStatus) {
		set("O_STATUS", oStatus);
		return (M)this;
	}
	
	public java.lang.Integer getOStatus() {
		return getInt("O_STATUS");
	}

	public M setSTATUS(java.lang.String STATUS) {
		set("STATUS", STATUS);
		return (M)this;
	}
	
	public java.lang.String getSTATUS() {
		return getStr("STATUS");
	}

	public M setUserType(java.lang.Integer userType) {
		set("USER_TYPE", userType);
		return (M)this;
	}
	
	public java.lang.Integer getUserType() {
		return getInt("USER_TYPE");
	}

	public M setIsDelete(java.lang.Integer isDelete) {
		set("isDelete", isDelete);
		return (M)this;
	}
	
	public java.lang.Integer getIsDelete() {
		return getInt("isDelete");
	}

	public M setIsSend(java.lang.Integer isSend) {
		set("isSend", isSend);
		return (M)this;
	}
	
	public java.lang.Integer getIsSend() {
		return getInt("isSend");
	}

	public M setResult(java.lang.String result) {
		set("result", result);
		return (M)this;
	}
	
	public java.lang.String getResult() {
		return getStr("result");
	}

	public M setOtherid(java.lang.Integer otherid) {
		set("otherid", otherid);
		return (M)this;
	}
	
	public java.lang.Integer getOtherid() {
		return getInt("otherid");
	}

	public M setOnline(java.lang.Integer online) {
		set("online", online);
		return (M)this;
	}
	
	public java.lang.Integer getOnline() {
		return getInt("online");
	}

	public M setGmtModified(java.util.Date gmtModified) {
		set("gmt_modified", gmtModified);
		return (M)this;
	}
	
	public java.util.Date getGmtModified() {
		return get("gmt_modified");
	}

	public M setUrgeIslook(java.lang.Boolean urgeIslook) {
		set("urge_isLook", urgeIslook);
		return (M)this;
	}
	
	public java.lang.Boolean getUrgeIslook() {
		return get("urge_isLook");
	}

	public M setSort(java.lang.Integer sort) {
		set("sort", sort);
		return (M)this;
	}
	
	public java.lang.Integer getSort() {
		return getInt("sort");
	}

	public M setISLOOK(java.lang.Integer ISLOOK) {
		set("ISLOOK", ISLOOK);
		return (M)this;
	}
	
	public java.lang.Integer getISLOOK() {
		return getInt("ISLOOK");
	}

}
