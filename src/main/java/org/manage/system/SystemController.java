package org.manage.system;

import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;
import com.mysql.cj.protocol.x.XServerSession;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.manage.common.interceptor.AuthInterceptor;
import org.manage.common.module.Account;
import org.manage.common.module.Server;
import org.manage.common.module.System;
import org.manage.common.module.UserSession;
import org.manage.log.LogService;
import org.manage.server.ServerService;
import org.manage.user.session.SessionService;
import cn.fabrice.common.constant.BaseConstants;
import cn.fabrice.common.pojo.BaseResult;
import cn.fabrice.common.pojo.DataResult;
import cn.fabrice.jfinal.annotation.Param;
import cn.fabrice.jfinal.annotation.ValidateParam;
import cn.fabrice.jfinal.constant.ValidateRuleConstants;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.Path;
import com.jfinal.plugin.ehcache.CacheKit;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * 系统功能体现(接参传参)
 */
@Path("/system")
@ValidateParam
public class SystemController extends Controller {
    @Inject
    SystemService systemService;
    @Inject
    LogService logService;
    @Inject
    ServerService serverService;
    @Inject
    SessionService sessionService;


    /**
     * 获取前端的header请求
     *
     * @return 返回包含header的Map
     */
    public Map<String,String> getHeader(){
        HttpServletRequest request=getRequest();
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, String> headerMap = new HashMap<>(8);
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headerMap.put(name, request.getHeader(name));
        }
        return headerMap;
    }

    /**
     * 从header中获取token,根据token获得登录者id
     *(虽然在AuthInterceptor中有提供方法,可以直接通过getAttr获取,但是这个方法毕竟是自己写了的,
     * 就在SystemController中用一用吧,在ServerController中使用的是getAttr)
     *
     * @return 登录者id
     */
    public long getLoginId(){
        Map<String,String> headerMap=getHeader();
        java.lang.System.out.println(headerMap);
        String token=headerMap.get("access_token");
        UserSession session=sessionService.getByToken(token);
        java.lang.System.out.println(session);
        return session.getUserId().longValue();
    }

    /***
     * 添加系统
     */
    public void addSystem(){
        //以字符串形式接收前端传来的json字符串
        String list=getRawData();
        renderJson(systemService.addSystem(list));
    }

    /***
     * 显示所有系统
     */
    public void listSystem(){
        renderJson(DataResult.data(systemService.listSystem()));
    }

    /***
     * 添加系统管理账号
     */
    @Param(name = "address", required = true)
    @Param(name = "name", required = true)
    @Param(name = "account", required = true)
    @Param(name = "password", required = true)
    public void addSystemAccount(String address,String name,String account,String password){
        long loginId=getLoginId();
        BaseResult result=systemService.addSystemAccount(address,name,account,password);
        if(result.isOk()){
            System system=systemService.getSystem(address);
            String con="用户:"+name+",账号:"+account+",密码:"+password;
            //其中user_id是存储在token信息中的
            logService.addLog(1,1,system.getId().longValue(),con,"管理账号", loginId);
        }
        renderJson(result);
    }

    /***
     * 新添加系统所部署的服务器
     */
    @Param(name="system_address",required = true)
    @Param(name="server_address",required = true)
    public  void connectSystemServer(String system_address,String server_address){
        long loginId=getLoginId();
        BaseResult result=systemService.connectSystemServer(system_address,server_address);
        if(result.isOk()){
            System system=systemService.getSystem(system_address);
            Server server=serverService.getServer(server_address);
            String con="将系统:"+system.getName()+"部署到服务器:"+server.getName();
            //其中user_id是存储在token信息中的
            logService.addLog(1,1,system.getId().longValue(),con,"部署关系", loginId);
        }
        renderJson(result);
    }

    /***
     * 显示系统详细信息
     */
    @Param(name = "address",required = true)
    public void showSystem(String address){
        renderJson(systemService.showSystem(address));
    }

    /***
     * 删除系统
     */
    @Param(name="address",required = true)
    public  void deleteSystem(String address){
        long loginId=getLoginId();
        System system=systemService.getSystem(address);
        java.lang.System.out.println(system);
        BaseResult result=systemService.deleteSystem(address);
        if(result.isOk()){
            String con="被删除的系统名称:"+system.getName()+",被删除的系统地址:"+system.getAddress();
            logService.addLog(2,1,system.getId().longValue(),con,"整个系统及其联系",loginId);
        }
        renderJson(result);
    }

    /***
     * 删除系统与服务器之间的关联
     * @param systemAddress 系统地址(在点击进入系统详情时传入)
     * @param serverAddress 服务器地址(在选择删除服务器时传入)
     */
    @Param(name="systemAddress",required = true)
    @Param(name="serverAddress",required = true)
    public void deleteSystemServer(String systemAddress,String serverAddress){
        long loginId=getLoginId();
        BaseResult result=systemService.deleteSystemServer(systemAddress,serverAddress);
        if(result.isOk()){
            System system=systemService.getSystem(systemAddress);
            Server server=serverService.getServer(serverAddress);
            String con="解除系统:"+system.getName()+"和服务器:"+server.getName()+"之间的关联";
            logService.addLog(2,1,system.getId().longValue(),con,"关联关系", loginId);
        }
        renderJson(result);
    }

    /***
     * 删除系统管理账号
     * @param system_id 系统id
     * @param account_id 账号id
     */
    @Param(name="system_id",required = true)
    @Param(name="account_id",required = true)
    public void deleteSystemAccount(long system_id,long account_id){
        long loginId=getLoginId();
        Record account=systemService.getAccount(account_id);
        BaseResult result=systemService.deleteSystemAccount(account_id);
        if(result.isOk()){
            String con="被删除的管理账号:"+account.getStr("account");
            logService.addLog(2,1,system_id,con,"管理账号", loginId);
        }
        renderJson(result);
    }

    /***
     * 修改系统自身信息(名称,地址,简介等),如果只修改了其中一部分,那就把原来的信息传过来
     *
     * @param id 系统id
     * @param name 修改后的系统名称
     * @param address 修改后的系统访问地址
     * @param introduction 修改后的系统简介
     */
    @Param(name = "id", required = true)
    @Param(name = "name", required = true)
    @Param(name = "address", required = true)
    @Param(name = "introduction", required = true)
    public void updateSystem(long id,String name,String address,String introduction){
        long loginId=getLoginId();
        //存储修改前的信息
        System system=systemService.getSystemById(id);
        String oldName=system.getName();
        String oldAddress=system.getAddress();
        String oldIntroduction= system.getIntroduction();
        //修改系统信息
        BaseResult result=systemService.updateObject(1,id,name,address,introduction);
        if(result.isOk()){
            String con="修改前系统信息:系统名称:"+oldName+",系统地址:"+oldAddress+",系统简介:"+oldIntroduction+
                    ";修改后系统信息:系统名称:"+name+",系统地址:"+address+",系统简介:"+introduction;
            logService.addLog(3,1,system.getId().longValue(),con,"系统信息", loginId);
        }
        renderJson(result);
    }

    /***
     * 修改系统与服务器的联系
     *
     * @param id 系统id(由系统详情页面传递)
     * @param beforeAddress 修改前的服务器地址
     * @param afterAddress 修改后的服务器地址
     */
    @Param(name = "id", required = true)
    @Param(name = "beforeAddress", required = true)
    @Param(name = "afterAddress", required = true)
    public void updateSystemServer(long id,String beforeAddress,String afterAddress){
        long loginId=getLoginId();
        //修改系统与服务器的关联
        BaseResult result=systemService.updateConnect(1,id,beforeAddress,afterAddress);
        if(result.isOk()){
            String con="修改前系统部署服务器:"+beforeAddress+",修改后系统部署服务器"+afterAddress;
            logService.addLog(3,1,id,con,"系统与服务器关联",loginId);
        }
        renderJson(result);
    }

    /***
     * 修改系统管理账号
     *
     * @param id 系统id(便于记录log)
     * @param account_id 此账号的id(唯一确定一条账号)
     * @param name 修改后的用户名
     * @param account 修改后的账号
     * @param password 修改后的密码
     */
    @Param(name = "id", required = true)
    @Param(name = "account_id", required = true)
    @Param(name = "name", required = true)
    @Param(name = "account", required = true)
    @Param(name = "password", required = true)
    public void updateSystemAccount(long id,long account_id,String name,String account,String password){
        long loginId=getLoginId();
        Record old=systemService.getAccount(account_id);
        String oldName=old.getStr("name");
        String oldAccount=old.getStr("account");
        String oldPassword=old.getStr("password");
        BaseResult result=systemService.updateAccount(account_id,name,account,password);
        if(result.isOk()){
            String con="修改前账号信息:用户名:"+oldName+",账号:"+oldAccount+",密码:"+oldPassword+
                    ";修改后账号信息:用户名:"+name+",账号:"+account+",密码:"+password;
            logService.addLog(3,1,id,con,"系统管理账号", loginId);
        }
        renderJson(result);
    }

}
