package com.luqi.common.model;

import java.util.List;

import com.luqi.common.model.base.BaseT1doCallbackField;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class T1doCallbackField extends BaseT1doCallbackField<T1doCallbackField> {
	public static final T1doCallbackField dao = new T1doCallbackField().dao();
	//获得回调接口字段
	public static List<T1doCallbackField> getT1doCallbackField(Long id) {
		
		return dao.find("select * from t_1do_callback_field where (callback_id=? or type=1) and is_deleted=0",id);
	}
}
