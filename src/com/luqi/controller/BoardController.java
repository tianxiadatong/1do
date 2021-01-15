package com.luqi.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.luqi.common.model.T1doBoard;
import com.luqi.common.model.T1doBoardRemark;
import com.luqi.common.model.T1doBoardTask;
import com.luqi.common.model.T1doBoardTask1do;
import com.luqi.common.model.T1doBoardTaskRemarks;
import com.luqi.common.model.T1doBoardTaskReport;
import com.luqi.common.model.T1doLog;
import com.luqi.common.model.T1doProject;
import com.luqi.common.model.T1doProject1do;
import com.luqi.common.model.T1doProjectStakeholder;
import com.luqi.interceptor.LoginInterceptor;
import com.luqi.model.BoardTemp;
import com.luqi.model.ProjectModel;
import com.luqi.service.BoardService;
import com.luqi.service.BoardTaskService;
import com.luqi.timer.BoardTaskTrendTask;
import com.luqi.util.CallUtil;
import com.luqi.util.HttpUtil;
import com.luqi.util.JsonUtil;
import com.luqi.util.MsgUtil;
import com.luqi.util.StrUtil;
import com.luqi.util.UrlUtil;
import com.luqi.util.node.InfiniteLevelTreeUtil;
import com.luqi.util.node.Node;

import org.apache.poi.ss.formula.functions.T;

/**
 * @ClassName BoardController
 * @Description 项目看板Controller
 * @auther Sherry
 * @date 2019/7/24 10:38 AM
 */
@Before(LoginInterceptor.class)
public class BoardController extends Controller {
    /**
     * @Author Sherry
     * @Description 新增Item节点
     * @Date 11:43 AM 2019/7/24
     */
    public void addItem() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        T1doBoard item = JSON.parseObject(json.toJSONString(), T1doBoard.class);
        //校验参数
        if (item.getTYPE() == null) {
            renderJson(MsgUtil.errorMsg("类型不能为空"));
            return;
        }
        //若项目子节点，需传projectId
        Long projectId = json.getLong("projectId");

        JSONObject user = getSessionAttr("user");
        //权限控制
        //若是新增项目分类（type=1），则只有POWER=1整理层和POWER=2领导可以操作
        if (item.getTYPE() == 1 && (user.getInteger("POWER") == 1 || user.getInteger("POWER") == 2)) {
            BoardService.addItem(item, user, projectId, getSessionAttr("temp"));
            //若是新增项目子节点（type=2）
        } else if (item.getTYPE() == 2) {
            if (projectId == null) {
                renderJson(MsgUtil.errorMsg("项目id不能为空"));
                return;
            }
            //领导和整理层可以操作
            if (user.getInteger("POWER") == 1 || user.getInteger("POWER") == 2) {
                BoardService.addItem(item, user, projectId, getSessionAttr("temp"));
                //普通用户为项目干系人或创建人则可以
            } else {
                T1doProject project = T1doProject.getT1doProjectById(projectId);
                if (project.getCrateUser().equals(user.getString("loginName"))) {
                    BoardService.addItem(item, user, projectId, getSessionAttr("temp"));
                } else {
                    T1doProjectStakeholder stakeholder = T1doProjectStakeholder.isExist(json.getLong("projectId"), user.getString("loginName"));
                    if (stakeholder == null) {
                        renderJson(MsgUtil.errorMsg("权限不足"));
                        return;
                    } else {
                        BoardService.addItem(item, user, projectId, getSessionAttr("temp"));
                    }
                }
            }
        } else {
            renderJson(MsgUtil.errorMsg("权限不足"));
            return;
        }

        renderJson(MsgUtil.successMsg(item));
    }

    /**
     * @Author Sherry
     * @Description 列表新增项目看板
     * @Date 2:11 PM 2019/8/20
     */
    public void addProjectBoard() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        //节点
        T1doBoard item = JSON.parseObject(json.toJSONString(), T1doBoard.class);
        if (item.getItemName() == null) {
            renderJson(MsgUtil.errorMsg("项目名称不能为空"));
            return;
        }
        //类型
        Integer type = json.getInteger("type");
        if (type == null) {
            type = 0;
        }
        //用户
        JSONObject user = getSessionAttr("user");
        JSONObject res = BoardService.addProjectBoard(item, user, type);
        renderJson(MsgUtil.successMsg(res));
    }

    /**
     * @Author Sherry
     * @Description 修改节点
     * @Date 9:13 AM 2019/7/25
     */
    public void updateItem() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        T1doBoard item = JSON.parseObject(json.toJSONString(), T1doBoard.class);
        //校验参数
        if (StrKit.isBlank(item.getItemName())) {
            renderJson(MsgUtil.errorMsg("节点名称不能为空"));
            return;
        }
        if (item.getID() == null) {
            renderJson(MsgUtil.errorMsg("节点id不能为空"));
            return;
        }
        //项目id
        Long projectId = json.getLong("projectId");

        JSONObject user = getSessionAttr("user");

        //权限控制
        //若是新增项目分类（type=1），则只有POWER=1整理层和POWER=2领导可以操作
        if (item.getTYPE() == 1 && (user.getInteger("POWER") == 1 || user.getInteger("POWER") == 2)) {
            BoardService.updateItem(item, user, projectId, getSessionAttr("temp"));
            //若是新增项目子节点（type=2）
        } else if (item.getTYPE() == 2) {
            if (projectId == null) {
                renderJson(MsgUtil.errorMsg("项目id不能为空"));
                return;
            }
            //领导和整理层可以操作
            if (user.getInteger("POWER") == 1 || user.getInteger("POWER") == 2) {
                BoardService.updateItem(item, user, projectId, getSessionAttr("temp"));
                //普通用户为项目干系人或创建人则可以
            } else {
                T1doProject project = T1doProject.getT1doProjectById(projectId);
                if (project.getCrateUser().equals(user.getString("loginName"))) {
                    BoardService.updateItem(item, user, projectId, getSessionAttr("temp"));
                } else {
                    T1doProjectStakeholder stakeholder = T1doProjectStakeholder.isExist(json.getLong("projectId"), user.getString("loginName"));
                    if (stakeholder == null) {
                        renderJson(MsgUtil.errorMsg("权限不足"));
                        return;
                    } else {
                        BoardService.updateItem(item, user, projectId, getSessionAttr("temp"));
                    }
                }
            }
        } else {
            renderJson(MsgUtil.errorMsg("权限不足"));
            return;
        }

        renderJson(MsgUtil.successMsg(item));
    }

    /**
     * @Author Sherry
     * @Description 删除节点
     * @Date 1:44 PM 2019/7/25
     */
    public void deleteItem() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        Long id = json.getLong("id");
        //校验参数
        if (id == null) {
            renderJson(MsgUtil.errorMsg("节点id不能为空"));
            return;
        }
        T1doBoard item = T1doBoard.getItemById(id);
        if (item == null) {
            renderJson(MsgUtil.errorMsg("该节点不存在"));
            return;
        }
        //获取子节点
        List<T1doBoard> children = T1doBoard.getHasChildren(id);
        if (children != null && children.size() > 0) {
            renderJson(MsgUtil.errorMsg("含有子分类，不能删除"));
            return;
        }
        Long projectId = json.getLong("projectId");
        //用户
        JSONObject user = getSessionAttr("user");
        //权限控制
        //若是新增项目分类（type=1），则只有POWER=1整理层和POWER=2领导可以操作
        if (item.getTYPE() == 1 && (user.getInteger("POWER") == 1 || user.getInteger("POWER") == 2)) {
            BoardService.deleteItem(item, user, projectId, getSessionAttr("temp"));
            //若是新增项目子节点（type=2）
        } else if (item.getTYPE() == 2) {
            if (projectId == null) {
                renderJson(MsgUtil.errorMsg("项目id不能为空"));
                return;
            }
            //领导和整理层可以操作
            if (user.getInteger("POWER") == 1 || user.getInteger("POWER") == 2) {
                BoardService.deleteItem(item, user, projectId, getSessionAttr("temp"));
                //普通用户为项目干系人或创建人则可以
            } else {
                T1doProject project = T1doProject.getT1doProjectById(projectId);
                if (project.getCrateUser().equals(user.getString("loginName"))) {
                    BoardService.deleteItem(item, user, projectId, getSessionAttr("temp"));
                } else {
                    T1doProjectStakeholder stakeholder = T1doProjectStakeholder.isExist(json.getLong("projectId"), user.getString("loginName"));
                    if (stakeholder == null) {
                        renderJson(MsgUtil.errorMsg("权限不足"));
                        return;
                    } else {
                        BoardService.deleteItem(item, user, projectId, getSessionAttr("temp"));
                    }
                }
            }
        } else {
            renderJson(MsgUtil.errorMsg("权限不足"));
            return;
        }
        renderJson(MsgUtil.successMsg(item));
    }

    /**
     * @Author Sherry
     * @Description 获取项目分类
     * @Date 1:49 PM 2019/7/24
     */
    @Clear(LoginInterceptor.class)
    public void getClassification() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        /*Integer hide = json.getInteger("hide");
        if (hide == null) {
            hide = 0;
        }*/
        //用户
        JSONObject user = getSessionAttr("user");
        //权限POWER=1整理层，POWER=2领导，POWER=3普通用户
        int power = user.getInteger("POWER");
        if ((power == 1 || power == 2)) {
            JSONObject res = BoardService.getClassification(0, user);
            //是否是新的
            Integer isNew = json.getInteger("isNew");
            if (isNew != null && isNew == 1) {
                setSessionAttr("temp", new ArrayList<BoardTemp>());
            }
            List<BoardTemp> boardTemps = getSessionAttr("temp");
            if (boardTemps.size() == 0) {
                res.put("isLastOne", true);
            } else {
                res.put("isLastOne", false);
            }
            renderJson(MsgUtil.successMsg(res));
        } else {
            renderJson(MsgUtil.errorMsg("没有权限"));
        }
        //插入session
        //insertSession((List<Record>) res.get("items"),"projectItems");

    }

    /**
     * @Author Sherry
     * @Description 新增项目
     * @Date 4:55 PM 2019/7/24
     */
    public void addProject() {
        ProjectModel projectModel = JSON.parseObject(HttpKit.readData(getRequest()), ProjectModel.class);
        T1doBoard t = T1doBoard.getItemById(projectModel.getParentId());

        //校验参数
        if (StrKit.isBlank(projectModel.getName())) {
            renderJson(MsgUtil.errorMsg("项目名称不能为空"));
            return;
        }
        if (projectModel.getParentId() == null) {
            renderJson(MsgUtil.errorMsg("父节点不能为空"));
            return;
        }
        if (projectModel.getStakeHolder() == null || projectModel.getStakeHolder().size() == 0) {
            renderJson(MsgUtil.errorMsg("干系人不能为空"));
            return;
        }
        if (t != null && t.getTYPE() == 3) {
            renderJson(MsgUtil.errorMsg("项目下不能建项目"));
            return;
        }
        JSONObject user = getSessionAttr("user");
        //权限控制
        //只有POWER=1整理层和POWER=2领导可以操作
        if (user.getInteger("POWER") == 1 || user.getInteger("POWER") == 2) {
            BoardService.addProject(projectModel, user);
            renderJson(MsgUtil.successMsg("新增成功"));
        } else {
            renderJson(MsgUtil.errorMsg("权限不足"));
        }

    }

    /**
     * @Author Sherry
     * @Description 修改项目
     * @Date 9:52 AM 2019/7/25
     */
    public void updateProject() {
        ProjectModel projectModel = JSON.parseObject(HttpKit.readData(getRequest()), ProjectModel.class);
        //校验参数
        if (projectModel.getId() == null) {
            renderJson(MsgUtil.errorMsg("项目id不能为空"));
            return;
        }
        if (StrKit.isBlank(projectModel.getName())) {
            renderJson(MsgUtil.errorMsg("项目名称不能为空"));
            return;
        }
        if (projectModel.getParentId() == null) {
            renderJson(MsgUtil.errorMsg("父节点不能为空"));
            return;
        }
        if (projectModel.getStakeHolder() == null || projectModel.getStakeHolder().size() == 0) {
            renderJson(MsgUtil.errorMsg("干系人不能为空"));
            return;
        }
        JSONObject user = getSessionAttr("user");
        //权限控制
        //POWER=1整理层和POWER=2领导可以操作
        if (user.getInteger("POWER") == 1 || user.getInteger("POWER") == 2) {
            BoardService.updateProject(projectModel, user);
            renderJson(MsgUtil.successMsg("修改成功"));
        } else {
            //干系人也可以操作
            T1doProjectStakeholder stakeholder = T1doProjectStakeholder.isExist(projectModel.getId(), user.getString("loginName"));
            if (stakeholder == null) {
                renderJson(MsgUtil.errorMsg("权限不足"));
            } else {
                BoardService.updateProject(projectModel, user);
                renderJson(MsgUtil.successMsg("修改成功"));
            }
        }

    }

    /**
     * @Author Sherry
     * @Description 删除项目
     * @Date 1:55 PM 2019/7/25
     */
    public void deleteProject() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        Long id = json.getLong("id");
        //校验参数
        if (id == null) {
            renderJson(MsgUtil.errorMsg("项目id不能为空"));
            return;
        }
        T1doBoard item = T1doBoard.getItemById(id);
        if (item == null) {
            renderJson(MsgUtil.errorMsg("该项目不存在"));
            return;
        }
        //获取子节点
        List<T1doBoard> children = T1doBoard.getHasChildren(id);
        if (children != null && children.size() > 0) {
            renderJson(MsgUtil.errorMsg("含有子分类，不能删除"));
            return;
        }
        //用户
        JSONObject user = getSessionAttr("user");
        //权限控制
        //POWER=1整理层和POWER=2领导可以操作
        if (user.getInteger("POWER") == 1 || user.getInteger("POWER") == 2) {
            BoardService.deleteProject(item, user);
            renderJson(MsgUtil.successMsg("删除成功"));
        } else {
            //干系人也可以操作
            T1doProjectStakeholder stakeholder = T1doProjectStakeholder.isExist(id, user.getString("loginName"));
            if (stakeholder == null) {
                renderJson(MsgUtil.errorMsg("权限不足"));
            } else {
                BoardService.deleteProject(item, user);
                renderJson(MsgUtil.successMsg("删除成功"));
            }
        }
    }

    /**
     * @Author Sherry
     * @Description 获取项目子节点
     * @Date 3:51 PM 2019/7/25
     */
    @Clear(LoginInterceptor.class)
    public void getProjectChildNode() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        Long id = json.getLong("id");
        //校验参数
        if (id == null) {
            renderJson(MsgUtil.errorMsg("项目id不能为空"));
            return;
        }
        //项目
        Record project = T1doBoard.getProjectById(id);
        if (project == null) {
            renderJson(MsgUtil.errorMsg("项目不存在"));
            return;
        }
        Integer hide = json.getInteger("hide");
        if (hide == null) {
            hide = 0;
        }
        //用户
        JSONObject user = getSessionAttr("user");
        JSONObject res = BoardService.getProjectChildNode(hide, user, id);
        //是否是新的
        Integer isNew = json.getInteger("isNew");
        if (isNew != null && isNew == 1) {
            setSessionAttr("temp", new ArrayList<BoardTemp>());
        }
        List<BoardTemp> boardTemps = getSessionAttr("temp");
        if (boardTemps.size() == 0) {
            res.put("isLastOne", true);
        } else {
            res.put("isLastOne", false);
        }
        //将结果插入session
        //insertSession((List<Record>) res.get("items"),"project"+id);
        renderJson(MsgUtil.successMsg(res));

    }

    /**
     * @Author Sherry
     * @Description 获取项目子节点（日志系统调用）
     * @Date 3:51 PM 2019/7/25
     */
    @Clear(LoginInterceptor.class)
    public void getProjectChildNodes() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        Long id = json.getLong("id");
        //校验参数
        if (id == null) {
            renderJson(MsgUtil.errorMsg("项目id不能为空"));
            return;
        }
        //项目
        Record project = T1doBoard.getProjectById(id);
        if (project == null) {
            renderJson(MsgUtil.errorMsg("项目不存在"));
            return;
        }
        List<Record> items = T1doBoard.getProjectChildren(id, 0);
        renderJson(MsgUtil.successMsg(items));
    }

    /**
     * @Author Sherry
     * @Description 设为重点项目
     * @Date 4:38 PM 2019/7/25
     */
    public void setKeyProject() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        Long id = json.getLong("id");
        //校验参数
        if (id == null) {
            renderJson(MsgUtil.errorMsg("项目id不能为空"));
            return;
        }

        T1doBoard item = T1doBoard.getItemById(id);
        if (item == null || item.getTYPE() != 3) {
            renderJson(MsgUtil.errorMsg("该项目不存在"));
            return;
        }
        /*//用户
        JSONObject user = getSessionAttr("user");
       //权限控制
        //POWER=1整理层和POWER=2领导可以操作
        if (user.getInteger("POWER") == 1 || user.getInteger("POWER") == 2) {

            renderJson(MsgUtil.successMsg(BoardService.setKeyProject(id)));
        } else {
            //干系人也可以操作
            T1doProjectStakeholder stakeholder = T1doProjectStakeholder.isExist(id, user.getString("loginName"));
            if (stakeholder == null) {
                renderJson(MsgUtil.errorMsg("权限不足"));
            } else {
                renderJson(MsgUtil.successMsg(BoardService.setKeyProject(id)));
            }
        }*/
        renderJson(MsgUtil.successMsg(BoardService.setKeyProject(id)));

    }

    /**
     * @Author Sherry
     * @Description 设置为办结
     * @Date 4:51 PM 2019/7/25
     */
    public void setFinish() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        Long id = json.getLong("id");
        //校验参数
        if (id == null) {
            renderJson(MsgUtil.errorMsg("项目id不能为空"));
            return;
        }
        if (id > 10000000) {
            id = Long.valueOf(String.valueOf(id).substring(1));
        }
        T1doBoard item = T1doBoard.getItemById(id);
        if (item == null) {
            renderJson(MsgUtil.errorMsg("该项目不存在"));
            return;
        }

        //用户
        JSONObject user = getSessionAttr("user");
        //权限控制
        //POWER=1整理层和POWER=2领导可以操作
        if (user.getInteger("POWER") == 1 || user.getInteger("POWER") == 2) {
            BoardService.setFinish(id);
        } else {
            //干系人也可以操作
            T1doProjectStakeholder stakeholder = T1doProjectStakeholder.isExist(id, user.getString("loginName"));
            if (stakeholder == null) {
                renderJson(MsgUtil.errorMsg("权限不足"));
                return;
            } else {
                BoardService.setFinish(id);
            }
        }
        renderJson(MsgUtil.successMsg("设置成功"));
    }

    /**
     * @Author coco
     * @Description 调用所有项目及节点（修改1do详情所属项目时）
     * @Date 9:51 AM 2019/7/26
     */
    public void getAllItem() {
        List<T1doBoard> t1doBoards = T1doBoard.getAll();
        List<Node> list = InfiniteLevelTreeUtil.getInfiniteLevelTree(t1doBoards);
        renderJson(MsgUtil.successMsg(list));

    }

    /**
     * @Author coco
     * @Description 修改1do所属项目
     * @Date 9:51 AM 2019/7/26
     */
    public void update1doItem() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        JSONObject user = getSessionAttr("user");
        int power = T1doProjectStakeholder.getFlag(user.getString("loginName"), json.getString("SHOW_ID"), user.getIntValue("POWER"));//1整理层2领导3项目干系人4普通用户
        T1doProject1do t1 = T1doProject1do.getT1doProject1do(json.getString("SHOW_ID"));
        if (power != 3) {
            if (json.getLong("ID") == 0) {
                T1doLog.saveLog(user, t1.getItemId(), json.getString("SHOW_ID"));
                t1.delete();
                renderJson(MsgUtil.successMsg("修改成功"));
                return;
            } else {
                if (power == 4) {
                    T1doBoard t = T1doBoard.getMaxItemById(json.getLong("ID"));//修改后的节点所属项目
                    if (t1 != null) {
                        T1doBoard t2 = T1doBoard.getMaxItemById(t1.getItemId());//修前后的节点所属项目
                        System.out.println(t.getID() + "少时诵诗书所" + t2.getID());
                        if (t.getID().longValue() == t2.getID().longValue()) {
                            t1.setItemId(json.getLong("ID")).update();
                            T1doLog.saveLog(user, t1.getItemId(), json.getString("SHOW_ID"));
                            renderJson(MsgUtil.successMsg("修改成功"));
                            return;
                        }
                    }
                } else {
                    if (t1 != null) {
                        t1.setItemId(json.getLong("ID")).update();
                        T1doLog.saveLog(user, t1.getItemId(), json.getString("SHOW_ID"));

                    } else {
                        new T1doProject1do().setItemId(json.getLong("ID")).setShowId(json.getString("SHOW_ID")).save();

                    }
                    renderJson(MsgUtil.successMsg("修改成功"));
                    return;

                }
            }

        }
        renderJson(MsgUtil.errorMsg("权限不足"));

    }

    /**
     * @Author Sherry
     * @Description 获取项目分类（修改项目时调用）
     * @Date 1:56 PM 2019/7/26
     */
    public void getProjectItems() {
        //用户
        JSONObject user = getSessionAttr("user");
        //权限控制(权限升级了这里没法体现，去掉权限控制)
        //POWER=1整理层和POWER=2领导可以操作
        // if (user.getInteger("POWER") == 1 || user.getInteger("POWER") == 2) {
        List<Node> nodes = BoardService.getProjectItems1();
        renderJson(MsgUtil.successMsg(nodes));
        /*
         * }else { renderJson(MsgUtil.errorMsg("权限不足")); }
         */
    }

    /**
     * @Author Sherry
     * @Description 获取项目（编辑项目）
     * @Date 2:58 PM 2019/7/28
     */
    public void getProject() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        Long id = json.getLong("id");
        //校验参数
        if (id == null) {
            renderJson(MsgUtil.errorMsg("项目id不能为空"));
            return;
        }
        if (id > 10000000) {
            id = Long.valueOf(String.valueOf(id).substring(1));
        }
        T1doBoard item = T1doBoard.getItemById(id);
        if (item == null) {
            renderJson(MsgUtil.errorMsg("该项目不存在"));
            return;
        }
        Record res;
        //用户
        JSONObject user = getSessionAttr("user");
        //权限控制
        //POWER=1整理层和POWER=2领导可以操作
        if (user.getInteger("POWER") == 1 || user.getInteger("POWER") == 2) {
            res = BoardService.getProject(id);
        } else {
            //干系人也可以操作
            T1doProjectStakeholder stakeholder = T1doProjectStakeholder.isExist(id, user.getString("loginName"));
            if (stakeholder == null) {
                renderJson(MsgUtil.errorMsg("权限不足"));
                return;
            } else {
                res = BoardService.getProject(id);
            }
        }
        renderJson(MsgUtil.successMsg(res));
    }

    /**
     * @Author Sherry
     * @Description 更新项目分类（思维导图）
     * @Date 2:16 PM 2019/7/29
     */
/*    public void updateProjectItems() {
        JSONArray newData = JSON.parseArray(HttpKit.readData(getRequest()));
        //用户
        JSONObject user = getSessionAttr("user");
        //权限控制
        //POWER=1整理层和POWER=2领导可以操作
        if (user.getInteger("POWER") == 1 || user.getInteger("POWER") == 2) {
            JSONArray oldData = getSessionAttr("projectItems");
            BoardService.updateProjectItems(user, oldData, newData, 1);
            //session中存上新的数据
            setSessionAttr("projectItems", newData);
            renderJson(MsgUtil.successMsg("修改成功"));
        } else {
            renderJson(MsgUtil.errorMsg("权限不足"));
        }
    }*/

    /**
     * @Author Sherry
     * @Description 更新项目子节点（思维导图）
     * @Date 2:16 PM 2019/7/29
     */
/*    public void updateProjectChildNodes() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        Long id = json.getLong("id");
        //校验参数
        if (id == null) {
            renderJson(MsgUtil.errorMsg("项目id不能为空"));
            return;
        }
        //新数据
        JSONArray newData = json.getJSONArray("items");
        //旧数据
        JSONArray oldData = getSessionAttr("project" + id);
        //用户
        JSONObject user = getSessionAttr("user");
        //权限控制
        //POWER=1整理层和POWER=2领导可以操作
        if (user.getInteger("POWER") == 1 || user.getInteger("POWER") == 2) {
            BoardService.updateProjectItems(user, oldData, newData, 2);
            //session中存上新的数据
            setSessionAttr("project" + id, newData);
        } else {
            //干系人也可以操作
            T1doProjectStakeholder stakeholder = T1doProjectStakeholder.isExist(id, user.getString("loginName"));
            if (stakeholder == null) {
                renderJson(MsgUtil.errorMsg("权限不足"));
                return;
            } else {
                BoardService.updateProjectItems(user, oldData, newData, 2);
            }
        }
        renderJson(MsgUtil.successMsg("修改成功"));
    }*/

    /**
     * 将结果插入session
     */
    private void insertSession(List<Record> list, String name) {
        //将结果插入session
        JSONArray array = new JSONArray();
        array.addAll(list);
        setSessionAttr(name, array);
    }

    /**
     * @Author coco
     * @Description 节点添加干系人
     * @Date 10:09 AM 2019/8/20
     */
    public void insertStakeholder() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        T1doProjectStakeholder.insertStakeholder(json);
        renderJson(MsgUtil.successMsg("添加成功"));
    }

    /**
     * @Author coco
     * @Description 项目列表查询
     * @Date 10:09 AM 2019/8/20
     */
    public void getProjectList() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        JSONObject user = getSessionAttr("user");
        Page<Record> data = T1doProject.getProjectList(json, user.getString("loginName"), user.getInteger("POWER"));
        renderJson(MsgUtil.successMsg(data));

    }

    /**
     * @Author coco
     * @Description 项目列表删除或恢复
     * @Date 10:09 AM 2019/11/18
     */
    public void deleteOrReplyToProject() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        renderJson(MsgUtil.successMsg(T1doProject.deleteOrReplyToProject(json.getLongValue("ITEM_ID"))));

    }

    /**
     * @Author coco
     * @Description 修改项目列表中的列表名称
     * @Date 10:09 AM 2019/8/20
     */
    public void updateProjectListName() {

        JSONObject json = JsonUtil.getJSONObject(getRequest());
        T1doProject t = json.toJavaObject(T1doProject.class);
        t.update();
        renderJson(MsgUtil.successMsg("修改成功"));


    }

    /**
     * @Author coco
     * @Description 删除项目列表数据
     * @Date 10:09 AM 2019/8/20
     */
    public void deleteProjectList() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        T1doProject t = json.toJavaObject(T1doProject.class);
        if (t.getItemId() == null) {
            renderJson(MsgUtil.errorMsg("项目id不能为空"));
        }
        List<T1doBoard> tb = T1doBoard.getChildren(t.getItemId());
        //删除项目表
        t.delete();
        //删除项目节点
        T1doBoard project = new T1doBoard();
        project.setID(t.getItemId());
        project.delete();
        //删除子节点
        for (T1doBoard t1doBoard : tb) {
            t1doBoard.delete();
        }
        renderJson(MsgUtil.successMsg("删除成功"));
    }


    /**
     * @Author Sherry
     * @Description 获取每日项目公司日报报送时间
     * @Date 2:20 PM 2019/8/22
     */
    @Clear(LoginInterceptor.class)
    public void getCompanyLogTimes() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        Long id = json.getLong("projectId");
        String date = json.getString("date");
        //校验参数
        if (id == null) {
            renderJson(MsgUtil.errorMsg("项目id不能为空"));
            return;
        }
        T1doBoard item = T1doBoard.getItemById(id);
        if (item == null) {
            renderJson(MsgUtil.errorMsg("该项目不存在"));
            return;
        }
        //时间
        if (StrKit.isBlank(date)) {
            renderJson(MsgUtil.errorMsg("日期不能为空"));
            return;
        }
        List<Record> res = BoardService.getCompanyLogTimes(id, date);
        renderJson(MsgUtil.successMsg(res));
    }

    /**
     * @Author Sherry
     * @Description 获取报告标题，及所有时间
     * @Date 3:40 PM 2019/8/22
     */
    @Clear(LoginInterceptor.class)
    public void getTitle() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        Long id = json.getLong("projectId");
        //校验参数
        if (id == null) {
            renderJson(MsgUtil.errorMsg("项目id不能为空"));
            return;
        }
        T1doBoard project = T1doBoard.getItemById(id);
        if (project == null) {
            renderJson(MsgUtil.errorMsg("该项目不存在"));
            return;
        }
        Record res = BoardService.getTitle(project);
        renderJson(MsgUtil.successMsg(res));
    }

    /**
     * @Author Sherry
     * @Description 获取日报字数
     * @Date 4:23 PM 2019/8/22
     */
    @Clear(LoginInterceptor.class)
    public void getLogNumber() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        Long id = json.getLong("projectId");
        String date = json.getString("date");
        //校验参数
        if (id == null) {
            renderJson(MsgUtil.errorMsg("项目id不能为空"));
            return;
        }
        T1doBoard item = T1doBoard.getItemById(id);
        if (item == null) {
            renderJson(MsgUtil.errorMsg("该项目不存在"));
            return;
        }
        //时间
        if (StrKit.isBlank(date)) {
            renderJson(MsgUtil.errorMsg("日期不能为空"));
            return;
        }
        List<Record> res = BoardService.getLogNumber(id, date);
        renderJson(MsgUtil.successMsg(res));
    }

    /**
     * @Author Sherry
     * @Description 修改/新增报告备注
     * @Date 4:45 PM 2019/8/22
     */
    public void updateRemarks() {
        T1doBoardRemark t1doBoardRemark = JSON.parseObject(HttpKit.readData(getRequest()), T1doBoardRemark.class);
        //校验参数
        if (t1doBoardRemark.getProjectId() == null) {
            renderJson(MsgUtil.errorMsg("项目id不能为空"));
            return;
        }
        T1doBoard item = T1doBoard.getItemById(t1doBoardRemark.getProjectId());
        if (item == null) {
            renderJson(MsgUtil.errorMsg("该项目不存在"));
            return;
        }
        //时间
        if (t1doBoardRemark.getDATE() == null) {
            renderJson(MsgUtil.errorMsg("日期不能为空"));
            return;
        }
        //类型
        if (t1doBoardRemark.getTYPE() == null) {
            renderJson(MsgUtil.errorMsg("类型不能为空"));
            return;
        }
        BoardService.updateRemarks(t1doBoardRemark);
        renderJson(MsgUtil.successMsg("成功"));
    }

    /**
     * @Author Sherry
     * @Description 获取日报备注接口
     * @Date 9:14 AM 2019/8/23
     */
    @Clear(LoginInterceptor.class)
    public void getReportRemarks() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        Long id = json.getLong("projectId");
        String date = json.getString("date");
        //校验参数
        if (id == null) {
            renderJson(MsgUtil.errorMsg("项目id不能为空"));
            return;
        }
        T1doBoard item = T1doBoard.getItemById(id);
        if (item == null) {
            renderJson(MsgUtil.errorMsg("该项目不存在"));
            return;
        }
        //时间
        if (StrKit.isBlank(date)) {
            renderJson(MsgUtil.errorMsg("日期不能为空"));
            return;
        }
        List<T1doBoardRemark> res = BoardService.getReportRemarks(id, date);
        renderJson(MsgUtil.successMsg(res));
    }

    /**
     * 　* 描述：   获取项目完整进展图
     * 　* 创建人：coco
     * 　* 创建时间：2019年8月23日 下午4:39:10
     */
    @Clear(LoginInterceptor.class)
    public void getBoardTask() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        List<T1doBoardTask> list = T1doBoardTask.getBoardTask(json);
        /*
         * List<T1doBoardTask> all=new ArrayList<T1doBoardTask>(); for (T1doBoardTask
         * t1doBoardTask : list) { all.add(t1doBoardTask);
         * if(t1doBoardTask.getItemId()==null) continue; List<T1doBoardTask> list1
         * =T1doBoardTask.dao.
         * find("select a.*,b.O_DESCRIBE TASK,(case   when b.O_STATUS=3  then '待接单' when b.O_STATUS=4  then"
         * +
         * " '已接单' else '已完成' end )COMPLETION,b.O_EXECUTOR PRINCIPLE_SHOW_ID,b.O_EXECUTOR_NAME PRINCIPLE "
         * +
         * " from t_1do_board_task_1do a,t_1do_base b where a.ITEM_ID=? and  a.DATE=? and a.SHOW_ID=b.SHOW_ID "
         * ,t1doBoardTask.getItemId(),json.getString("date")); all.addAll(list1); }
         */
        renderJson(MsgUtil.successMsg(list));
    }

    /**
     * 　* 描述：   修改完整进展图中的完成情况
     * 　* 创建人：coco
     * 　* 创建时间：2019年8月23日 下午4:39:10
     */
    public void updateBoardTaskCompletion() {

        JSONObject json = JsonUtil.getJSONObject(getRequest());
        /*
         * //日期是今天或者昨天
         * if(json.getString("DATE").equals(TimeUtil.getDateTime())||json.getString(
         * "DATE").equals(TimeUtil.getDate(TimeUtil.getNextDay(new Date(),-1)))){
         * }
         */
        Date date;
        if (json.getDate("DATE") == null) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            date = c.getTime();
        } else {
            date = json.getDate("DATE");
        }
        T1doBoard t = new T1doBoard();
        t.setID(json.getLong("ITEM_ID")).setCOMPLETION(json.getString("COMPLETION")).setFinishDate(date).update();

        T1doBoardTask tt = T1doBoardTask.getTask(date, json.getLong("ITEM_ID"));
        if (tt != null) {
            if (tt.getCOMPLETION().equals(json.getString("COMPLETION"))) {
                renderJson(MsgUtil.errorMsg("完成情况相同未修改"));
                return;
            }
            if (json.getString("COMPLETION").equals("已完成")) {
                //其他状态变成已完成状态，data取a字段，b字段变成data
                tt.setBTEMP(tt.getDATA()).setDATA(tt.getATEMP());
            } else if (tt.getCOMPLETION().equals("已完成")) {
                //已完成变成其他状态，data取b字段，a字段变成data
                tt.setATEMP(tt.getDATA()).setDATA(tt.getBTEMP());
            }
            tt.setCOMPLETION(json.getString("COMPLETION")).update();
        }

        renderJson(MsgUtil.successMsg("修改成功"));
    }

    /**
     * @Author Sherry
     * @Description 修改/新增报告任务评估
     * @Date 10:38 AM 2019/8/23
     */
    public void updateEvaluate() {
        T1doBoardTaskRemarks t1doBoardTaskRemarks = JSON.parseObject(HttpKit.readData(getRequest()), T1doBoardTaskRemarks.class);
        if (t1doBoardTaskRemarks.getCompanyId() == null) {
            renderJson(MsgUtil.errorMsg("公司id不能为空"));
            return;
        }
        if (t1doBoardTaskRemarks.getTaskId() == null) {
            renderJson(MsgUtil.errorMsg("任务id不能为空"));
            return;
        }
        BoardService.updateEvaluate(t1doBoardTaskRemarks);
        renderJson(MsgUtil.successMsg("成功"));
    }

    /**
     * @Author Sherry
     * @Description 根据任务获取项目公司日志接口
     * @Date 9:34 AM 2019/8/26
     */
    public void getLogs() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        Long id = json.getLong("taskId");
        //校验参数
        if (id == null) {
            renderJson(MsgUtil.errorMsg("任务id不能为空"));
            return;
        }
        T1doBoardTask t1doBoardTask = T1doBoardTask.dao.findById(id);
        if (t1doBoardTask == null) {
            renderJson(MsgUtil.errorMsg("该任务不存在"));
            return;
        }
        List<Record> res = BoardService.getLogs(t1doBoardTask);
        renderJson(MsgUtil.successMsg(res));
    }

    /**
     * @Author Sherry
     * @Description 任务关联日志接口
     * @Date 2:15 PM 2019/8/26
     */
    public void taskLinkedLogs() throws Exception {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        Long taskId = json.getLong("taskId");
        Long logId = json.getLong("logId");
        Long itemId = json.getLong("itemId");
        //校验参数
        if (taskId == null) {
            renderJson(MsgUtil.errorMsg("任务id不能为空"));
            return;
        }
        if (logId == null) {
            renderJson(MsgUtil.errorMsg("日报id不能为空"));
            return;
        }
        if (itemId == null) {
            renderJson(MsgUtil.errorMsg("项目项id不能为空"));
            return;
        }
        BoardService.taskLinkedLogs(taskId, itemId, logId);
        renderJson(MsgUtil.successMsg("关联成功"));
    }


    /**
     * @Author Sherry
     * @Description 获取项目进展列表
     * @Date 9:16 AM 2019/8/27
     */
    @Clear(LoginInterceptor.class)
    public void getProjectProgress() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        Long id = json.getLong("projectId");
        String date = json.getString("date");
        //校验参数
        if (id == null) {
            renderJson(MsgUtil.errorMsg("项目id不能为空"));
            return;
        }
        T1doBoard item = T1doBoard.getItemById(id);
        if (item == null) {
            renderJson(MsgUtil.errorMsg("该项目不存在"));
            return;
        }
        //时间
        if (StrKit.isBlank(date)) {
            renderJson(MsgUtil.errorMsg("日期不能为空"));
            return;
        }
        List<JSONObject> res = BoardService.getProjectProgress(id, date);
        renderJson(MsgUtil.successMsg(res));
    }
    /**
     　* 描述：   1do项目看板分享
     　* 创建人：coco
     　* 创建时间：2019年8月28日 下午2:11:08
     */
    /*
     * public void share() { JSONObject json = JsonUtil.getJSONObject(getRequest());
     * JSONObject user = getSessionAttr("user"); String[] str1=new String[5];
     * if(!user.containsKey("LAST_UPDATE_TIME")||TimeUtil.getFlag(new Date(),
     * user.getDate("LAST_UPDATE_TIME"),1)) { str1
     * =StrUtil.getToken(HttpUtil.doPost4(StrUtil.getData(user.getString(
     * "useraccount")), UrlUtil.loginURL + "/Base-Module/CompanyUserLogin/Login"));
     * DbUtil.updateUser(str1); user.put("LoginToken", str1[0]);
     * user.put("LAST_UPDATE_TIME",new Date()); setSessionAttr("user", user); } else
     * { str1=StrUtil.assignment(user); } long timestamp = new Date().getTime();
     * JSONArray arr=json.getJSONArray("to"); List<String> list=(List)arr;
     * if(!json.containsKey("type")||json.getString("type").equals("Message")) {
     * String str2 = StrUtil.SendData(user,list,timestamp,json.getString("url"));
     * String result = HttpUtil.doPost2(str2, UrlUtil.loginURL +
     * "/Chat-Module/chat/sendmsg",str1[0],str1[1]); if (result == null)
     * renderJson(MsgUtil.errorMsg("分享失败")); else
     * renderJson(MsgUtil.successMsg("分享成功")); }else {
     * CallUtil.share(json.getString("url").replace(UrlUtil.attrUrl, ""),
     * UrlUtil.loginURL, str1, json.getString("type"), list);
     * renderJson(MsgUtil.successMsg("分享成功")); }
     *
     * }
     */

    /**
     * 　* 描述：   1do项目看板分享
     * 　* 创建人：coco
     * 　* 创建时间：2019年8月28日 下午2:11:08
     */
    public void share() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        JSONObject user = getSessionAttr("user");
        boolean flag = true;
        while (flag) {
            String[] str1 = StrUtil.getToken(HttpUtil.doPost4(StrUtil.getData(user.getString("useraccount")), UrlUtil.loginURL + "/Base-Module/CompanyUserLogin/Login"));
            long timestamp = System.currentTimeMillis();
            JSONArray arr = json.getJSONArray("to");
            List<String> list = (List) arr;
            if (!json.containsKey("type") || json.getString("type").equals("Message")) {
                String str2 = StrUtil.SendData(user, list, timestamp, json.getString("url"));
                String result = HttpUtil.doPost2(str2, UrlUtil.loginURL + "/Chat-Module/chat/sendmsg", str1[0], str1[1]);
                if (result != null){
                    flag = false;
                }
            } else {
                flag = CallUtil.share(json.getString("url").replace(UrlUtil.attrUrl, ""), UrlUtil.loginURL, str1, json.getString("type"), list);

            }
        }
        renderJson(MsgUtil.successMsg("分享成功"));

    }

    /**
     * 　* 描述：
     * 　* 创建人：coco
     * 　* 创建时间：2019年8月28日 下午4:07:19
     */
    @Clear
    public void action() {
        renderJson(MsgUtil.successMsg(null));
    }

    /**
     * @Author Sherry
     * @Description 日志系统挂接项目任务
     * @Date 2019-09-10 15:30
     */
    @Clear
    public void updateLog() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        BoardService.updateLog(json);
        renderJson(MsgUtil.successMsg("成功"));
    }

    @Clear
    public void getLogNew() {
        BoardTaskService.getLogNew();
        renderJson(MsgUtil.successMsg("成功"));
    }

    /**
     * @Author Sherry
     * @Description 根据群名获取项目及任务（日志系统调用）
     * @Date 10:32 AM 2019/9/25
     */
    @Clear
    public void getProjectsByGroup() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        String groupId = json.getString("groupId");
        if (StrKit.isBlank(groupId)) {
            renderJson(MsgUtil.errorMsg("群id不能为空"));
        }
        Record res = BoardService.getProjectsByGroup(groupId);
        renderJson(MsgUtil.successMsg(res));
    }

    /**
     * 　* 描述： 获得所有项目
     * 　* 创建人：coco
     * 　* 创建时间：2019年10月9日 下午4:49:34
     */
    @Clear(LoginInterceptor.class)
    public void getAllProjects() {
        renderJson(MsgUtil.successMsg(T1doProject.getAllProjects()));
    }

    /**
     * @Author Sherry
     * @Description 获取所有未关联的项目
     * @Date 5:31 PM 2020/1/8
    */
    public void getAllUnrelatedProjects() {
        renderJson(MsgUtil.successMsg(T1doProject.getAllUnrelatedProjects()));
    }

    /**
     * 　* 描述：  获得1do进展图
     * 　* 创建人：coco
     * 　* 创建时间：2019年10月12日 下午4:12:14
     */
    public void get1doByItemID() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        renderJson(MsgUtil.successMsg(T1doBoardTask1do.getTask(json.getLong("ITEM_ID"), json.getString("DATE"))));
    }

    /**
     * 　* 描述：  对整份报告写总结
     * 　* 创建人：coco
     * 　* 创建时间：2019年10月12日 下午1:46:13
     */
    public void summary() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        T1doBoardTaskReport r = json.toJavaObject(T1doBoardTaskReport.class);
        T1doBoardTaskReport r1 = T1doBoardTaskReport.getT1doBoardTaskReport(r);
        if (r1 != null) {
            r.setID(r1.getID()).update();
        } else {
            r.save();
        }
        renderJson(MsgUtil.successMsg(r));
    }

    /**
     * 描述：  删除总结
     * 创建人：coco
     * 创建时间：2019年10月12日 下午1:46:13
     */
    public void deleteSummary() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        T1doBoardTaskReport r = json.toJavaObject(T1doBoardTaskReport.class);
        T1doBoardTaskReport r1 = T1doBoardTaskReport.getT1doBoardTaskReport(r);
        r1.delete();
        renderJson(MsgUtil.successMsg("删除成功"));
    }

    /**
     * 描述：  获得整份报告的总结
     * 创建人：coco
     * 创建时间：2019年10月12日 下午1:46:13
     */
    public void getSummary() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        List<T1doBoardTaskReport> list = T1doBoardTaskReport.getSummary(json);

        renderJson(MsgUtil.successMsg(list));
    }

    /**
     * 节点撤销
     */
    public void undoNode() {
        Object object = getSessionAttr("temp");
        List<BoardTemp> boardTemps;
        if (object != null) {
            boardTemps = (List<BoardTemp>) object;
            if (boardTemps.size() > 0) {
                BoardService.undoNode(boardTemps);
                JSONObject res = new JSONObject();
                if (boardTemps.size() == 0) {
                    res.put("isLastOne", true);
                } else {
                    res.put("isLastOne", false);
                }
                renderJson(MsgUtil.successMsg(res));
                return;
            }
        }
        renderJson(MsgUtil.errorMsg("撤销失败"));

    }


    /**
     * @Author Sherry
     * @Description 项目总览节点关联项目
     * @Date 11:35 AM 2020/1/6
     */
    public void relatedProject() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        //节点id
        Long id = json.getLong("id");
        if (id == null) {
            renderJson(MsgUtil.errorMsg("节点id不能为空"));
            return;
        }
        //项目id
        String projectIdStr = json.getString("projectIds");
        if (StrKit.isBlank(projectIdStr)) {
            renderJson(MsgUtil.errorMsg("项目id不能为空"));
            return;
        }
        List<Long> projectIds = JSONArray.parseArray(projectIdStr, Long.class);
        if (projectIds == null || projectIds.size() == 0) {
            renderJson(MsgUtil.errorMsg("项目id不能为空"));
            return;
        }
        T1doBoard item = T1doBoard.getItemById(id);
        if (item == null) {
            renderJson(MsgUtil.errorMsg("该节点不存在"));
            return;
        }
        JSONObject user = getSessionAttr("user");
        //权限控制
        //只有POWER=1整理层和POWER=2领导可以操作
        if (user.getInteger("POWER") == 1 || user.getInteger("POWER") == 2) {
            BoardService.relatedProject(id, projectIds);
            renderJson(MsgUtil.successMsg("修改成功"));
        } else {
            renderJson(MsgUtil.errorMsg("没有权限"));
        }

    }

    /**
     * @Author Sherry
     * @Description 项目总览节点取消关联项目
     * @Date 4:00 PM 2020/1/8
    */
    public void unRelatedProject() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        //节点id
        Long id = json.getLong("id");
        if (id == null) {
            renderJson(MsgUtil.errorMsg("节点id不能为空"));
            return;
        }
        T1doBoard item = T1doBoard.getItemById(id);
        if (item == null) {
            renderJson(MsgUtil.errorMsg("该项目不存在"));
            return;
        }
        JSONObject user = getSessionAttr("user");
        //权限控制
        //只有POWER=1整理层和POWER=2领导可以操作
        if (user.getInteger("POWER") == 1 || user.getInteger("POWER") == 2) {
            BoardService.unRelatedProject(item);
            renderJson(MsgUtil.successMsg("修改成功"));
        } else {
            renderJson(MsgUtil.errorMsg("没有权限"));
        }

    }

    /**
     * @Author Sherry
     * @Description 获取项目进展图
     * @Date 5:26 PM 2020/1/6
    */
    public void getProjectTrend() {
        JSONObject json = JsonUtil.getJSONObject(getRequest());
        Long projectId = json.getLong("projectId");
        if (projectId == null) {
            renderJson(MsgUtil.errorMsg("项目id不能为空"));
            return;
        }
        renderJson(MsgUtil.successMsg(BoardService.getProjectTrend(projectId)));
    }
}
