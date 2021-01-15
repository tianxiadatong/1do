package com.luqi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseT1doStatus<M extends BaseT1doStatus<M>> extends Model<M> implements IBean {

	public M setShowId(java.lang.String showId) {
		set("SHOW_ID", showId);
		return (M)this;
	}
	
	public java.lang.String getShowId() {
		return getStr("SHOW_ID");
	}

	public M setID(java.lang.Integer ID) {
		set("ID", ID);
		return (M)this;
	}
	
	public java.lang.Integer getID() {
		return getInt("ID");
	}

	public M setOStatus(java.lang.Integer oStatus) {
		set("O_STATUS", oStatus);
		return (M)this;
	}
	
	public java.lang.Integer getOStatus() {
		return getInt("O_STATUS");
	}

}
