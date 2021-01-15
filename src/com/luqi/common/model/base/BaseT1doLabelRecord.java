package com.luqi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseT1doLabelRecord<M extends BaseT1doLabelRecord<M>> extends Model<M> implements IBean {

	public M setRECORDID(java.lang.Long RECORDID) {
		set("RECORDID", RECORDID);
		return (M)this;
	}
	
	public java.lang.Long getRECORDID() {
		return getLong("RECORDID");
	}

	public M setID(java.lang.Long ID) {
		set("ID", ID);
		return (M)this;
	}
	
	public java.lang.Long getID() {
		return getLong("ID");
	}

	public M setLABEL(java.lang.String LABEL) {
		set("LABEL", LABEL);
		return (M)this;
	}
	
	public java.lang.String getLABEL() {
		return getStr("LABEL");
	}

	public M setTYPE(java.lang.Integer TYPE) {
		set("TYPE", TYPE);
		return (M)this;
	}
	
	public java.lang.Integer getTYPE() {
		return getInt("TYPE");
	}

	public M setWeight(java.lang.Integer weight) {
		set("weight", weight);
		return (M)this;
	}
	
	public java.lang.Integer getWeight() {
		return getInt("weight");
	}

}
