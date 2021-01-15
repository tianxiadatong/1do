package com.luqi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseApproval<M extends BaseApproval<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Long id) {
		set("id", id);
		return (M)this;
	}
	
	public java.lang.Long getId() {
		return getLong("id");
	}

	public M setSource(java.lang.Integer source) {
		set("source", source);
		return (M)this;
	}
	
	public java.lang.Integer getSource() {
		return getInt("source");
	}

	public M setType(java.lang.Integer type) {
		set("type", type);
		return (M)this;
	}
	
	public java.lang.Integer getType() {
		return getInt("type");
	}

	public M setName(java.lang.String name) {
		set("name", name);
		return (M)this;
	}
	
	public java.lang.String getName() {
		return getStr("name");
	}

	public M setGmtCreate(java.lang.Long gmtCreate) {
		set("gmtCreate", gmtCreate);
		return (M)this;
	}
	
	public java.lang.Long getGmtCreate() {
		return getLong("gmtCreate");
	}

	public M setGmtModified(java.lang.Long gmtModified) {
		set("gmtModified", gmtModified);
		return (M)this;
	}
	
	public java.lang.Long getGmtModified() {
		return getLong("gmtModified");
	}

	public M setIsDeleted(java.lang.Boolean isDeleted) {
		set("isDeleted", isDeleted);
		return (M)this;
	}
	
	public java.lang.Boolean getIsDeleted() {
		return get("isDeleted");
	}

}
