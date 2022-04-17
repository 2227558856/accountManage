package org.manage.server;

import cn.fabrice.common.constant.BaseConstants;
import cn.fabrice.common.pojo.DataResult;
import cn.fabrice.jfinal.annotation.Param;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import cn.fabrice.common.pojo.BaseResult;
import cn.fabrice.jfinal.annotation.ValidateParam;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.Path;
import org.manage.common.module.Server;
import org.manage.common.module.System;
import org.manage.log.LogService;
import org.manage.system.SystemService;

/**
 * @author Administrator
 */
@Path("/server")
@ValidateParam
public class ServerController extends Controller {
    @Inject
    ServerService serverService;
    @Inject
    LogService logService;
    @Inject
    SystemService systemService;

    /***
     * 添加服务器
     */
    public void addServer(){
        String list=getRawData();
        renderJson(serverService.addServer(list));
    }

    /***
     * 显示所有服务器
     */
    public void listServer(){
        renderJson(DataResult.data(serverService.listServer()));
    }

    /***
     * 添加服务器登录账号
     */
    @Param(name = "address", required = true)
    @Param(name = "account", required = true)
    @Param(name = "password", required = true)
    public void addServerAccount(String address,String account,String password){
        BaseResult result=serverService.addServerAccount(address,account,password);
        if(result.isOk()){
            /*
            * loginId根据token获取
            * 1.token在全局登录拦截器中的intercept方法中进行查询得到session(token=session_id)
            * 2.将session中的信息通过controller.setAttr(BaseConstants.ACCOUNT_ID, userSession.getUserId().longValue());放到controller中
            * 3.直接用getAttr(BaseConstants.ACCOUNT_ID);就可以得到其中的user_id
            */
            long loginId = getAttr(BaseConstants.ACCOUNT_ID);
            Server server=serverService.getServer(address);
            String con="账号:"+account+",密码:"+password;
            logService.addLog(1,2,server.getId().longValue(),con,"登录账号", loginId);
        }
        renderJson(result);
    }

    /***
     * 新添加服务器上所部署的系统
     */
    @Param(name="system_address",required = true)
    @Param(name="server_address",required = true)
    public  void connectSystemServer(String system_address,String server_address){
        BaseResult result=serverService.connectSystemServer(system_address,server_address);
        if(result.isOk()){
            long loginId = getAttr(BaseConstants.ACCOUNT_ID);
            System system=systemService.getSystem(system_address);
            Server server=serverService.getServer(server_address);
            String con="在服务器:"+server.getName()+"上新部署了系统:"+system.getName();
            logService.addLog(1,2,server.getId().longValue(),con,"部署关系",loginId);
        }
        renderJson(result);
    }

    /***
     * 显示服务器详细信息
     */
    @Param(name = "address",required = true)
    public void showServer(String address){
        renderJson(serverService.showServer(address));
    }

    /***
     * 删除服务器
     */
    @Param(name="address",required = true)
    public  void deleteServer(String address){
        Server server=serverService.getServer(address);
        BaseResult result=serverService.deleteServer(address);
        if(result.isOk()){
            long loginId = getAttr(BaseConstants.ACCOUNT_ID);
            String con="被删除的服务器名称:"+server.getName()+",被删除的服务器地址:"+server.getAddress();
            logService.addLog(2,2,server.getId().longValue(),con,"整个服务器及其联系",loginId);
        }
        renderJson(result);
    }

    /***
     * 删除服务器登录账号
     * @param server_id 服务器地址
     * @param account_id 账号
     */
    @Param(name="server_id",required = true)
    @Param(name="account_id",required = true)
    public void deleteServerAccount(long server_id,long account_id){
        Record account=systemService.getAccount(account_id);
        BaseResult result=serverService.deleteServerAccount(account_id);
        if(result.isOk()){
            long loginId = getAttr(BaseConstants.ACCOUNT_ID);
            String con="被删除的登录账号:"+account.getStr("account");
            logService.addLog(2,2,server_id,con,"登录账号",loginId);
        }
        renderJson(result);
    }

    /***
     * 修改服务器自身信息(名称,地址,简介等),如果只修改了其中一部分,那就把原来的信息传过来
     *
     * @param id 服务器id
     * @param name 修改后的服务器名称
     * @param address 修改后的服务器访问地址
     * @param introduction 修改后的服务器简介
     */
    @Param(name = "id", required = true)
    @Param(name = "name", required = true)
    @Param(name = "address", required = true)
    @Param(name = "introduction", required = true)
    public void updateServer(long id,String name,String address,String introduction){
        //存储修改前的信息
        Server server=serverService.getServerById(id);
        String oldName=server.getName();
        String oldAddress=server.getAddress();
        String oldIntroduction= server.getIntroduction();
        //修改系统信息
        BaseResult result=systemService.updateObject(2,id,name,address,introduction);
        if(result.isOk()){
            long loginId = getAttr(BaseConstants.ACCOUNT_ID);
            String con="修改前服务器信息:服务器名称:"+oldName+",服务器地址:"+oldAddress+",服务器简介:"+oldIntroduction+
                    ";修改后服务器信息:服务器名称:"+name+",服务器地址:"+address+",服务器简介:"+introduction;
            logService.addLog(3,2,server.getId().longValue(),con,"服务器信息",loginId);
        }
        renderJson(result);
    }

    /***
     * 修改系统与服务器的联系
     *
     * @param id 服务器id(由服务器详情页面传递)
     * @param beforeAddress 修改前的服务器地址
     * @param afterAddress 修改后的服务器地址
     */
    @Param(name = "id", required = true)
    @Param(name = "beforeAddress", required = true)
    @Param(name = "afterAddress", required = true)
    public void updateSystemServer(long id,String beforeAddress,String afterAddress){
        //修改系统与服务器的关联
        BaseResult result=systemService.updateConnect(2,id,beforeAddress,afterAddress);
        if(result.isOk()){
            long loginId = getAttr(BaseConstants.ACCOUNT_ID);
            String con="修改前服务器所部属的系统:"+beforeAddress+",修改后服务器所部署的系统:"+afterAddress;
            logService.addLog(3,2,id,con,"系统与服务器关联",loginId);
        }
        renderJson(result);
    }

    /***
     * 修改服务器登录账号
     *
     * @param id 服务器id(便于记录log)
     * @param account_id 此账号的id(唯一确定一条账号)
     * @param account 修改后的账号
     * @param password 修改后的密码
     */
    @Param(name = "id", required = true)
    @Param(name = "account_id", required = true)
    @Param(name = "account", required = true)
    @Param(name = "password", required = true)
    public void updateServerAccount(long id,long account_id,String account,String password){
        Record old=systemService.getAccount(account_id);
        String oldAccount=old.getStr("account");
        String oldPassword=old.getStr("password");
        BaseResult result=systemService.updateAccount(account_id,null,account,password);
        if(result.isOk()){
            long loginId = getAttr(BaseConstants.ACCOUNT_ID);
            String con="修改前账号信息:账号:"+oldAccount+",密码:"+oldPassword+
                    ";修改后账号信息:账号:"+account+",密码:"+password;
            logService.addLog(3,2,id,con,"服务器登录账号",loginId);
        }
        renderJson(result);
    }
}
