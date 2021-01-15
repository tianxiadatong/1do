package com.luqi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseT1doBoardTaskReport<M extends BaseT1doBoardTaskReport<M>> extends Model<M> implements IBean {

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

	public M setSUMMARY(java.lang.String SUMMARY) {
		set("SUMMARY", SUMMARY);
		return (M)this;
	}
	
	public java.lang.String getSUMMARY() {
		return getStr("SUMMARY");
	}

	public M setNUMBER(java.lang.Integer NUMBER) {
		set("NUMBER", NUMBER);
		return (M)this;
	}
	
	public java.lang.Integer getNUMBER() {
		return getInt("NUMBER");
	}

	public M setProjectId(java.lang.Long projectId) {
		set("PROJECT_ID", projectId);
		return (M)this;
	}
	
	public java.lang.Long getProjectId() {
		return getLong("PROJECT_ID");
	}

}
