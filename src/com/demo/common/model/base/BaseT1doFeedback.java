package com.demo.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseT1doFeedback<M extends BaseT1doFeedback<M>> extends Model<M> implements IBean {

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

	public M setFbTime(java.util.Date fbTime) {
		set("FB_TIME", fbTime);
		return (M)this;
	}
	
	public java.util.Date getFbTime() {
		return get("FB_TIME");
	}

	public M setTimeStamp(java.lang.Long timeStamp) {
		set("TIME_STAMP", timeStamp);
		return (M)this;
	}
	
	public java.lang.Long getTimeStamp() {
		return getLong("TIME_STAMP");
	}

	public M setFBCONTENT(java.lang.String FBCONTENT) {
		set("FBCONTENT", FBCONTENT);
		return (M)this;
	}
	
	public java.lang.String getFBCONTENT() {
		return getStr("FBCONTENT");
	}

	public M setFbType(java.lang.Integer fbType) {
		set("FB_TYPE", fbType);
		return (M)this;
	}
	
	public java.lang.Integer getFbType() {
		return getInt("FB_TYPE");
	}

	public M setATTRID(java.lang.String ATTRID) {
		set("ATTRID", ATTRID);
		return (M)this;
	}
	
	public java.lang.String getATTRID() {
		return getStr("ATTRID");
	}

	public M setFbUser(java.lang.String fbUser) {
		set("FB_USER", fbUser);
		return (M)this;
	}
	
	public java.lang.String getFbUser() {
		return getStr("FB_USER");
	}

	public M setFbUserName(java.lang.String fbUserName) {
		set("FB_USER_NAME", fbUserName);
		return (M)this;
	}
	
	public java.lang.String getFbUserName() {
		return getStr("FB_USER_NAME");
	}

	public M setUSERID(java.lang.String USERID) {
		set("USERID", USERID);
		return (M)this;
	}
	
	public java.lang.String getUSERID() {
		return getStr("USERID");
	}

	public M setAttrName(java.lang.String attrName) {
		set("ATTR_NAME", attrName);
		return (M)this;
	}
	
	public java.lang.String getAttrName() {
		return getStr("ATTR_NAME");
	}

	public M setAttrPath(java.lang.String attrPath) {
		set("ATTR_PATH", attrPath);
		return (M)this;
	}
	
	public java.lang.String getAttrPath() {
		return getStr("ATTR_PATH");
	}

	public M setStar(java.lang.Integer star) {
		set("star", star);
		return (M)this;
	}
	
	public java.lang.Integer getStar() {
		return getInt("star");
	}

	public M setIsoverdue(java.lang.Integer isoverdue) {
		set("isoverdue", isoverdue);
		return (M)this;
	}
	
	public java.lang.Integer getIsoverdue() {
		return getInt("isoverdue");
	}

	public M setModifyTime(java.util.Date modifyTime) {
		set("modifyTime", modifyTime);
		return (M)this;
	}
	
	public java.util.Date getModifyTime() {
		return get("modifyTime");
	}

	public M setAT(java.lang.String AT) {
		set("AT", AT);
		return (M)this;
	}
	
	public java.lang.String getAT() {
		return getStr("AT");
	}

	public M setShortMessage(java.lang.String shortMessage) {
		set("shortMessage", shortMessage);
		return (M)this;
	}
	
	public java.lang.String getShortMessage() {
		return getStr("shortMessage");
	}

}
