package com.luqi.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.PropKit;
import com.luqi.common.model.T1doFeedback;
import com.luqi.util.HttpClientUtil;
import com.luqi.util.HttpUtil;
import com.luqi.util.UrlUtil;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName RecordService
 * @Description
 * @auther Sherry
 * @date 2019/9/26 4:06 PM
 */
public class RecordService {

    //新日志系统转日志
    private static String transferLogUrl="/join/convertDiary";

    /**
     * 转日志
     * @param feedback 反馈
     * @return
     */
    public static boolean transferLog(T1doFeedback feedback) throws Exception {
        //获取转日志参数
        JSONObject logArgs = getLogArgs(feedback);
        //新日志系统转日志
        String result = HttpUtil.doPost(PropKit.get("logUrl")+transferLogUrl,logArgs);
        if (null != result && JSON.parseObject(result).getInteger("code") == 2000) {
            feedback.setIsCreateReport(true);
            feedback.update();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取转日志参数
     * @param t1doFeedback
     * @return
     */
    private static JSONObject getLogArgs(T1doFeedback t1doFeedback){
        JSONObject logArgs = new JSONObject();
        logArgs.put("userName",t1doFeedback.get("userName"));
        logArgs.put("fromName",t1doFeedback.get("userName"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        logArgs.put("date", sdf.format(t1doFeedback.getFbTime()));
        JSONArray data = new JSONArray();
        //备注
        String mark = "用户" + t1doFeedback.getOUserName() + "于" + t1doFeedback.getFbTime() + "在1do(" + t1doFeedback.getStr("title") + ")里反馈:" + t1doFeedback.getFBCONTENT();
        //若为附件，type=3
        if (t1doFeedback.getFbType() == 3) {
            String attach = t1doFeedback.getAttrPath();
            String fileName = attach.substring(attach.lastIndexOf("/") + 1);
            fileName = fileName.split("\\.")[0];
            String fileType = attach.substring(attach.lastIndexOf(".") + 1);
            data.add(getLogData("file", t1doFeedback.getFBCONTENT(), fileName, fileType, attach, t1doFeedback.getFBCONTENT(), mark));
        } else {
            data.add(getLogData("text", t1doFeedback.getFBCONTENT(), "", "", "", t1doFeedback.getFBCONTENT(), mark));
        }
        logArgs.put("data",data);
        logArgs.put("sourceApp","1do");
        logArgs.put("transferType","log");
        return logArgs;
    }

    /**
     * 获得日志参数中data参数
     * @param msgType 消息类型file/text
     * @param Name 名称
     * @param fileName 文件名
     * @param fileType 文件类型
     * @param url 地址
     * @param Content 内容
     * @param mark 备注
     * @return
     */
    private static JSONObject getLogData(String msgType, String Name, String fileName, String fileType, String url, String Content, String mark) {
        JSONObject data = new JSONObject();
        data.put("msgType", msgType);
        data.put("Name", Name);
        data.put("fileName", fileName);
        data.put("fileType",fileType);
        data.put("url",url);
        data.put("Content",Content);
        data.put("mark",mark);
        return data;
    }
}
