package com.luqi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseT1doCallback<M extends BaseT1doCallback<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Long id) {
		set("id", id);
		return (M)this;
	}
	
	public java.lang.Long getId() {
		return getLong("id");
	}

	public M setFormalUrl(java.lang.String formalUrl) {
		set("formal_url", formalUrl);
		return (M)this;
	}
	
	public java.lang.String getFormalUrl() {
		return getStr("formal_url");
	}

	public M setTestUrl(java.lang.String testUrl) {
		set("test_url", testUrl);
		return (M)this;
	}
	
	public java.lang.String getTestUrl() {
		return getStr("test_url");
	}

	public M setType(java.lang.Integer type) {
		set("type", type);
		return (M)this;
	}
	
	public java.lang.Integer getType() {
		return getInt("type");
	}

	public M setSource(java.lang.Integer source) {
		set("source", source);
		return (M)this;
	}
	
	public java.lang.Integer getSource() {
		return getInt("source");
	}

	public M setGmtCreate(java.util.Date gmtCreate) {
		set("gmt_create", gmtCreate);
		return (M)this;
	}
	
	public java.util.Date getGmtCreate() {
		return get("gmt_create");
	}

	public M setGmtModified(java.util.Date gmtModified) {
		set("gmt_modified", gmtModified);
		return (M)this;
	}
	
	public java.util.Date getGmtModified() {
		return get("gmt_modified");
	}

	public M setIsDeleted(java.lang.Boolean isDeleted) {
		set("is_deleted", isDeleted);
		return (M)this;
	}
	
	public java.lang.Boolean getIsDeleted() {
		return get("is_deleted");
	}

}
