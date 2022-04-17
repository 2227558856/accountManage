package org.manage.system;

import cn.fabrice.common.pojo.BaseResult;
import cn.fabrice.common.pojo.DataResult;
import cn.fabrice.jfinal.service.BaseService;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.manage.common.module.Account;
import org.manage.common.module.Server;
import org.manage.common.module.System;
import org.manage.common.module.User;
import org.manage.server.ServerService;
import org.manage.system.constant.SystemConstants;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * 系统功能实现(处理参数)
 */
public class SystemService extends BaseService<System> {
    @Inject
    ServerService serverService;


    public SystemService() {
        super("system.", System.class, "system");
    }


    /***
     * 根据地址得到系统
     * @param address 系统地址
     * @return 系统
     */
    public System getSystem(String address){
        Kv cond=Kv.by("address",address);
        System s=get(cond,"getByAddress");
        return s;
    }

    /***
     * 根据id得到系统
     *
     * @param id 系统id
     * @return 系统
     */
    public System getSystemById(long id){
        Kv cond=Kv.by("id",id);
        System s=get(cond,"getById");
        return s;
    }

    /***
     * 根据id得到账号
     *
     * @param id 账号id
     * @return 账号Record
     */
    public Record getAccount(long id){
        Kv cond=Kv.by("id",id);
        SqlPara sqlPara=Db.getSqlPara("system.getAccount",cond);
        return Db.findFirst(sqlPara);
    }

    /**
     * 添加系统
     *
     * @param name 系统名称
     * @param address   系统访问地址
     * @param introduction  系统简介
     * @return 系统添加结果
     */
    public BaseResult addSystem(String name, String address, String introduction){
        Kv cond=Kv.by("address",address);
        System s=get(cond,"getByAddress");
        if(s!=null){
            return BaseResult.fail("系统已存在");
        }
        System system=new System();
        system.set("name",name);
        system.set("address",address);
        system.set("introduction",introduction);
        if(system.save()){
            return BaseResult.ok();
        }
        return BaseResult.fail("系统添加失败");
    }

    /**
     * 关联系统和服务器
     *
     * @param systemAddress 系统地址
     * @param serverAddress   服务器地址
     * @return 系统和服务器的关联结果
     */
    public BaseResult connectSystemServer(String systemAddress,String serverAddress){
        Kv cond=Kv.by("serverAddress",serverAddress);
        //首先查找服务器是否存在
        SqlPara sqlPara= Db.getSqlPara("system.getServer",cond);
        Record server= Db.findFirst(sqlPara);
        if(server==null){
            return BaseResult.fail("服务器不存在");
        }
        cond=Kv.by("systemAddress",systemAddress).set("serverAddress",serverAddress);
        //服务器存在,判断系统与服务器是否已经关联
        sqlPara= Db.getSqlPara("system.getConnect",cond);
        Record connect= Db.findFirst(sqlPara);
        if(connect!=null){
            return BaseResult.fail("系统与该服务器已经关联");
        }
        //将系统与服务器关联起来
        int r=update(cond,"connectSystemServer");
        if(r>0){
            return BaseResult.ok();
        }
        return BaseResult.fail("系统服务器关联失败");
    }

    /***
     * 添加系统管理账号
     *
     * @param address 系统地址
     * @param name  用户名(可以填姓名或者权限,例如小明,小李,系统管理员等)
     * @param account 该系统的管理账号
     * @param password 该系统的管理密码
     * @return 账号添加结果
     */
    public BaseResult addSystemAccount(String address,String name,String account,String password){
        Kv cond=Kv.by("address",address);
        System system=get(cond,"getByAddress");
        if(system==null){
            return BaseResult.fail("系统不存在");
        }
        //查找该系统中是否已存在该账号
        cond=Kv.by("account",account).set("id",system.getId());
        SqlPara sqlPara= Db.getSqlPara("system.getSystemAccount",cond);
        Record a= Db.findFirst(sqlPara);
        if(a!=null){
            //这个报错信息在最开始添加系统时并不会出现,但在后续给系统添加账号时有可能出现
            return BaseResult.fail("该账号已存在");
        }
        Account accountNew=new Account();
        accountNew.set("name",name);
        accountNew.set("account",account);
        accountNew.set("password",password);
        accountNew.set("object",1);
        accountNew.set("object_id",system.getId());
        if(accountNew.save()){
            return BaseResult.ok();
        }
        return BaseResult.fail("账号添加失败");
    }

    /***
     * 一个完整的添加系统流程
     * @param list 前端json字符串
     * @return 返回系统添加结果
     */
    public BaseResult addSystem(String list){
        final String[] detailResult = new String[1];
        JSONObject jsonObject=JSONObject.fromObject(list);
        String systemName=jsonObject.getString("system_name");
        String systemAddress=jsonObject.getString("system_address");
        String systemIntroduction=jsonObject.getString("system_introduction");
        boolean r = Db.tx(() -> {
            //1.添加系统
            BaseResult result=addSystem(systemName,systemAddress,systemIntroduction);
            if(result.isFail()){
                //即此时系统已存在,直接返回错误信息,回滚
                detailResult[0] ="系统已存在";
                return false;
            }
            //2.添加服务器部署
            String s=jsonObject.getString("server_address_array");
            JSONArray serverAddressArray=JSONArray.fromObject(s);
            JSONObject object;
            for(int i=0;i<serverAddressArray.size();i++){
                object=serverAddressArray.getJSONObject(i);
                String serverAddress=object.getString("server_address");
                //将服务器和系统关联起来
                result=connectSystemServer(systemAddress,serverAddress);
                if(result.isFail()){
                    //即此时系统和服务器关联失败,直接返回错误信息,回滚
                    detailResult[0] ="系统与服务器关联失败";
                    return false;
                }
            }
            //3.添加系统账号
            s=jsonObject.getString("system_account_array");
            JSONArray systemAccountArray=JSONArray.fromObject(s);
            for(int i=0;i<systemAccountArray.size();i++){
                object=systemAccountArray.getJSONObject(i);
                String systemAccountName=object.getString("system_account_name");
                String systemAccount=object.getString("system_account");
                String systemPassword=object.getString("system_password");
                //将系统账号添加到account表
                result=addSystemAccount(systemAddress,systemAccountName,systemAccount,systemPassword);
                if(result.isFail()){
                    //即此时系统账号添加失败,直接返回错误信息,回滚
                    detailResult[0] ="系统管理账号添加失败";
                    return false;
                }
            }
            //一切顺利,添加成功
            return true;
        });
        if(r){
           return BaseResult.ok();
        }
        return BaseResult.fail(detailResult[0]);
    }


    /***
     * 显示所有系统
     */
    public List<System> listSystem() {
        return list("listSystem");
    }

    /***
     * 显示系统详情
     *
     * @param address 系统地址
     */
    public BaseResult showSystem(String address){
        Map<String,Object> result=new HashMap<>();
        //存入系统基本信息
        Kv cond=Kv.by("address",address);
        System system=get(cond,"getByAddress");
        result.put("system_id",system.getId());
        result.put("system_name",system.getName());
        result.put("system_address",address);
        result.put("system_introduction",system.getIntroduction());
        //存入与系统关联的服务器信息
        SqlPara sqlPara=Db.getSqlPara("system.showSystemServer",cond);
        List<Record> server=Db.find(sqlPara);
        result.put("server",server);
        //存入系统的管理账号
        cond=Kv.by("id",system.getId());
        sqlPara=Db.getSqlPara("system.showSystemAccount",cond);
        List<Record> account=Db.find(sqlPara);
        result.put("account",account);
        return DataResult.data(result);
    }

    /***
     * 删除系统
     *
     * @param address 系统地址
     */
    public BaseResult deleteSystem(String address){
        final String[] result = new String[1];
        boolean r = Db.tx(() -> {
            //1.删除系统主体
            Kv cond = Kv.by("address", address);
            if (update(cond, "deleteSystem") == 0) {
                result[0] = "系统主体删除失败";
                return false;
            }
            //2.删除系统与服务器联系
            SqlPara sqlPara = Db.getSqlPara("system.selectConnect", cond);
            List<Record> connectId = Db.find(sqlPara);
            for (int i = 0; i < connectId.size(); i++) {
                Kv c=Kv.by("id",connectId.get(i).getStr("id"));
                if (update(c, "deleteConnect") == 0) {
                    result[0] = "系统与服务器关联删除失败";
                    return false;
                }
            }
            //3.删除系统管理账号
            sqlPara = Db.getSqlPara("system.selectAccount", cond);
            List<Record> accountId = Db.find(sqlPara);
            for (int i = 0; i < accountId.size(); i++) {
                Kv c=Kv.by("id",accountId.get(i).getStr("id"));
                if (update(c, "deleteAccount") == 0) {
                    result[0] = "系统管理账号删除失败";
                    return false;
                }
            }
            return true;
        });
        if(r){
            return BaseResult.ok();
        }
        return BaseResult.fail(result[0]);
    }

    /**
     * 删除系统和服务器的关联
     *
     * @param systemAddress 系统地址
     * @param serverAddress   服务器地址
     * @return 删除结果
     */
    public BaseResult deleteSystemServer(String systemAddress,String serverAddress){
        System system=getSystem(systemAddress);
        Server server=serverService.getServer(serverAddress);
        Kv cond=Kv.by("system_id",system.getId()).set("server_id",server.getId());
        int result=update(cond,"deleteSystemServer");
        if(result==0){
            return BaseResult.fail("系统服务器关联删除失败");
        }
        return BaseResult.ok();
    }

    /**
     * 删除系统管理账号
     *
     * @param account_id 管理账号
     * @return 删除结果
     */
    public BaseResult deleteSystemAccount(long account_id){
        Kv cond=Kv.by("id",account_id);
        int result=update(cond,"deleteAccount");
        if(result==0){
            return BaseResult.fail("系统管理账号删除失败");
        }
        return BaseResult.ok();
    }

    /***
     *修改对象(系统or服务器)信息
     *
     * @param object 修改对象,如果是系统就传为1,服务器就传为2
     * @param object_id 修改对象的id,用来确定对象
     * @param name 修改对象的修改后or原来的名称
     * @param address 修改对象的修改后or原来的地址
     * @param introduction 修改对象的修改后or原来的简介
     * @return
     */
    public BaseResult updateObject(int object,long object_id,String name,String address,String introduction){
        boolean result=false;
        if(object==1){
            System system=getSystemById(object_id);
            system.setName(name);
            system.setAddress(address);
            system.setIntroduction(introduction);
            result=system.update();
        }
        else if(object==2){
            Server server=serverService.getServerById(object_id);
            server.setName(name);
            server.setAddress(address);
            server.setIntroduction(introduction);
            result=server.update();
        }
        if(result){
            return BaseResult.ok();
        }
        return BaseResult.fail("修改失败");
    }

    /***
     *修改对象(系统or服务器)信息
     *
     * @param object 修改对象,如果是系统就传为1,服务器就传为2
     * @param object_id 修改对象的id,用来确定对象
     * @param beforeAddress 修改前的关联地址
     * @param afterAddress 修改后的关联地址
     * @return
     */
    public BaseResult updateConnect(int object,long object_id,String beforeAddress,String afterAddress){
        int result=0;
        if(object==1){
            //根据系统address和之前的服务器address找到唯一的一条system_server记录
            Server beforeServer=serverService.getServer(beforeAddress);
            long beforeId=beforeServer.getId().longValue();
            //根据新填入的服务器地址找到新的服务器,将记录中的before_id替换为该服务器id
            Server afterServer=serverService.getServer(afterAddress);
            long afterId=afterServer.getId().longValue();
            Kv cond=Kv.by("system_id",object_id).set("before_id",beforeId).set("after_id",afterId);
            SqlPara sqlPara=Db.getSqlPara("system.updateConnectServer",cond);
            result=Db.update(sqlPara);
        }
        else if(object==2){
            System beforeSystem=getSystem(beforeAddress);
            long beforeId=beforeSystem.getId().longValue();
            System afterSystem=getSystem(afterAddress);
            long afterId=afterSystem.getId().longValue();
            Kv cond=Kv.by("server_id",object_id).set("before_id",beforeId).set("after_id",afterId);
            SqlPara sqlPara=Db.getSqlPara("system.updateConnectSystem",cond);
            result=Db.update(sqlPara);
        }
        if(result==1){
            return BaseResult.ok();
        }
        return BaseResult.fail("修改失败");
    }

    /***
     *修改对象(系统or服务器)账号
     *
     * @param account_id 此账号的id,由对象详情处传入
     * @param name 修改后的用户名
     * @param account 修改后的账号
     * @param password 修改后的密码
     * @return
     */
    public BaseResult updateAccount(long account_id,String name,String account,String password){
        int result=0;
        Kv cond=Kv.by("id",account_id).set("name",name).set("account",account).set("password",password);
        SqlPara sqlPara=Db.getSqlPara("system.updateAccount",cond);
        result=Db.update(sqlPara);
        if(result==1){
            return BaseResult.ok();
        }
        return BaseResult.fail("修改失败");
    }
}
