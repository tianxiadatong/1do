package com.luqi.common.model;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.luqi.common.model.base.BaseT1doType;
import com.luqi.util.IDUtil;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class T1doType extends BaseT1doType<T1doType> {
	public static final T1doType dao = new T1doType().dao();
	
	/*
	 2018年6月21日 方升群  保存T1doType
	*/
	public static boolean save(String meg) {
		return new T1doType().setOTypeId(1).setOTypeName(meg).setOParentId(0).save();
	}
	/**   
	　* 描述：   设置来源
	　* 创建人：coco   
	　* 创建时间：2019年9月29日 下午4:26:22         
	*/
	public static void setSOURCE(List<Record> t3) {
		//String[] sources= {"全部","1do","主动办","三实数据平台","其他","领导批示","城市大脑","综合信息系统","1call办","综合指挥平台"};
        //  String[]  type1= {"城管执法","民政","环卫","住建","人社"};	
		//	List<Record> brr=Db.find("select * from t_1do_type ");
			List<Record> brr=Db.find("select t.*,a.num from t_1do_type t left join(select O_PARENT_ID,count(*) num from t_1do_type GROUP BY O_PARENT_ID)a on t.O_PARENT_ID=a.O_PARENT_ID");

			for (Record record : t3) {
				for(Record br:brr) {
					if(br.getInt("num")>1) {
						if(record.getInt("SOURCE")==br.getInt("O_PARENT_ID")&&br.getLong("O_TYPE_ID")==Long.valueOf(record.getStr("O_TYPE_ID"))) {
							 record.set("EVENT_TYPE", br.getStr("OTHER_NAME"));
							 record.set("SYSTEM", br.getStr("O_SOURE_NAME"));
							 break;
						 }
					}else {
						if(record.getInt("SOURCE")==br.getInt("O_PARENT_ID")) {
							 record.set("EVENT_TYPE", br.getStr("OTHER_NAME"));
							 record.set("SYSTEM", br.getStr("O_SOURE_NAME"));
							 break;
						}
					}
					
				}
			/*
			 * if(record.getInt("SOURCE")==8)
			 * record.set("O_TYPE_ID",type1[Integer.valueOf(record.getStr("O_TYPE_ID"))-1]);
			 * else record.set("O_TYPE_ID","1do事件");
			 * 
			 * record.set("SOURCE", sources[(record.getInt("SOURCE"))]);
			 */
			}
	}
	/**   
	　* 描述：   看板来源数据
	　* 创建人：coco   
	　* 创建时间：2019年9月29日 下午5:19:54         
	*/
	public static List<Record> getSourceData() {
		List<Record> arr=Db.find("select O_PARENT_ID `key`, O_SOURE_NAME systemName from t_1do_type GROUP BY `key`");
		List<Record> brr=Db.find("select O_PARENT_ID,O_TYPE_ID `key`, O_TYPE_NAME typeName from t_1do_type ");
		
		for (Record record : arr) {
			List<Record> crr=new ArrayList<Record>();
			for (Record record1 : brr) {
			   if(record.getInt("key")==record1.getInt("O_PARENT_ID")) {
				   crr.add(record1);
			   }
		  }
			record.set("eventType", crr);
		}
		/**
		String[] sources= {"全部","1do","主动办","三实数据平台","其他","领导批示","城市大脑","综合信息系统","1call办"};
		JSONArray arr=new JSONArray();
		for (int i = 0; i < sources.length; i++) {
			if(i==4) {
				continue;
			}
			JSONObject json=new JSONObject();
			json.put("key", i);
			json.put("systemName", sources[i]);
		    JSONArray brr=new JSONArray();
			JSONObject json1=new JSONObject();
			json1.put("key",0);
			json1.put("typeName","全部");
			brr.add(json1);
			if(i==8) {
				String[]  type= {"城管执法","民政","环卫","住建","人社"};
		        for (int j = 0; j < type.length; j++) {
		        	JSONObject json2=new JSONObject();
					json2.put("key",j+1);
					json2.put("typeName",type[j]);
					brr.add(json2);
				}
				
			}
			json.put("eventType", brr);
			arr.add(json);
		
		}
			*/
		return arr;
	}
}
