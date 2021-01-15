package com.luqi.common.model;

import java.util.List;

import com.luqi.common.model.base.BaseT1doAttr;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class T1doAttr extends BaseT1doAttr<T1doAttr> {
	public static final T1doAttr dao = new T1doAttr().dao();
	/*
	 2018年10月31日 coco 注解：获得附件集合
	*/
	public static List<T1doAttr> getAttr(String SHOW_ID) {
		return T1doAttr.dao.find("select ID,SHOW_ID,unix_timestamp(UPLOAD_TIME)UPLOAD_TIME,ATTR_NAME,ATTR_PATH,UPLOAD_USER,UPLOAD_USER_NAME from t_1do_attr where SHOW_ID=?",SHOW_ID);
	}
}
