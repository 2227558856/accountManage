package org.manage.server;

import cn.fabrice.common.pojo.BaseResult;
import cn.fabrice.common.pojo.DataResult;
import cn.fabrice.jfinal.service.BaseService;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.manage.common.module.Account;
import org.manage.common.module.Server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
public class ServerService extends BaseService<Server> {
    public ServerService() {
        super("server.", Server.class, "server");
    }


    /***
     * 根据地址得到服务器
     *
     * @param address 服务器地址
     * @return 服务器
     */
    public Server getServer(String address){
        Kv cond=Kv.by("address",address);
        Server s=get(cond,"getByAddress");
        return s;
    }

    /***
     * 根据id得到服务器
     *
     * @param id 服务器id
     * @return 服务器
     */
    public Server getServerById(long id){
        Kv cond=Kv.by("id",id);
        Server s=get(cond,"getById");
        return s;
    }

    /**
     * 添加服务器
     *
     * @param name 服务器名称
     * @param address   服务器访问地址
     * @param introduction  服务器简介
     * @return 服务器添加结果
     */
    public BaseResult addServer(String name, String address, String introduction){
        Kv cond=Kv.by("address",address);
        Server s=get(cond,"getByAddress");
        if(s!=null){
            return BaseResult.fail("服务器已存在");
        }
        Server server=new Server();
        server.set("name",name);
        server.set("address",address);
        server.set("introduction",introduction);
        if(server.save()){
            return BaseResult.ok();
        }
        return BaseResult.fail("服务器添加失败");
    }

    /**
     * 关联系统和服务器
     *
     * @param systemAddress 系统地址
     * @param serverAddress   服务器地址
     * @return 系统和服务器的关联结果
     */
    public BaseResult connectSystemServer(String systemAddress,String serverAddress){
        Kv cond=Kv.by("systemAddress",systemAddress);
        //首先查找系统是否存在
        SqlPara sqlPara= Db.getSqlPara("server.getSystem",cond);
        Record system= Db.findFirst(sqlPara);
        if(system==null){
            return BaseResult.fail("系统不存在");
        }
        cond=Kv.by("systemAddress",systemAddress).set("serverAddress",serverAddress);
        //系统存在,判断系统与服务器是否已经关联
        sqlPara= Db.getSqlPara("system.getConnect",cond);
        Record connect= Db.findFirst(sqlPara);
        if(connect!=null){
            return BaseResult.fail("该系统已部署到服务器");
        }
        //将系统与服务器关联起来
        sqlPara= Db.getSqlPara("system.connectSystemServer",cond);
        int r=Db.update(sqlPara);
        if(r>0){
            return BaseResult.ok();
        }
        return BaseResult.fail("系统部署到服务器失败");
    }

    /***
     * 添加服务器登录账号
     *
     * @param address 服务器地址
     * @param account 服务器登录账号
     * @param password 服务器登录密码
     * @return 账号添加结果
     */
    public BaseResult addServerAccount(String address,String account,String password){
        Kv cond=Kv.by("address",address);
        Server server=get(cond,"getByAddress");
        //查找该服务器中是否已存在该账号
        cond=Kv.by("account",account).set("id",server.getId());
        SqlPara sqlPara= Db.getSqlPara("server.getServerAccount",cond);
        Record a= Db.findFirst(sqlPara);
        if(a!=null){
            //这个报错信息在最开始添加服务器时并不会出现,但在后续给系统添加账号时有可能出现
            return BaseResult.fail("该账号已存在");
        }
        Account accountNew=new Account();
        accountNew.set("account",account);
        accountNew.set("password",password);
        accountNew.set("object",2);
        accountNew.set("object_id",server.getId());
        if(accountNew.save()){
            return BaseResult.ok();
        }
        return BaseResult.fail("账号添加失败");
    }

    /***
     * 一个完整的添加服务器流程
     * @param list 前端json字符串
     * @return 返回服务器添加结果
     */
    public BaseResult addServer(String list){
        final String[] detailResult = new String[1];
        JSONObject jsonObject=JSONObject.fromObject(list);
        String serverName=jsonObject.getString("server_name");
        String serverAddress=jsonObject.getString("server_address");
        String serverIntroduction=jsonObject.getString("server_introduction");
        boolean r = Db.tx(() -> {
            //1.添加服务器
            BaseResult result=addServer(serverName,serverAddress,serverIntroduction);
            if(result.isFail()){
                //即此时服务器已存在,直接返回错误信息,回滚
                detailResult[0] ="服务器已存在";
                return false;
            }
            //2.添加服务器部署
            String s=jsonObject.getString("system_address_array");
            JSONArray systemAddressArray=JSONArray.fromObject(s);
            JSONObject object;
            for(int i=0;i<systemAddressArray.size();i++){
                object=systemAddressArray.getJSONObject(i);
                String systemAddress=object.getString("system_address");
                //将服务器和系统关联起来
                result=connectSystemServer(systemAddress,serverAddress);
                if(result.isFail()){
                    //即此时系统和服务器关联失败,直接返回错误信息,回滚
                    detailResult[0] ="系统与服务器关联失败";
                    return false;
                }
            }
            //3.添加服务器账号
            s=jsonObject.getString("server_account_array");
            JSONArray serverAccountArray=JSONArray.fromObject(s);
            for(int i=0;i<serverAccountArray.size();i++){
                object=serverAccountArray.getJSONObject(i);
                String serverAccount=object.getString("server_account");
                String serverPassword=object.getString("server_password");
                //将服务器账号添加到account表
                result=addServerAccount(serverAddress,serverAccount,serverPassword);
                if(result.isFail()){
                    //即此时服务器账号添加失败,直接返回错误信息,回滚
                    detailResult[0] ="服务器登录账号添加失败";
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
     * 显示所有服务器
     */
    public List<Server> listServer() {
        return list("listServer");
    }

    /***
     * 显示服务器详情
     *
     * @param address 服务器地址
     */
    public BaseResult showServer(String address){
        Map<String,Object> result=new HashMap<>();
        //存入服务器基本信息
        Kv cond=Kv.by("address",address);
        Server server=get(cond,"getByAddress");
        result.put("server_id",server.getId());
        result.put("server_name",server.getName());
        result.put("server_address",address);
        result.put("server_introduction",server.getIntroduction());
        //存入服务器上部署的系统信息
        SqlPara sqlPara=Db.getSqlPara("server.showServerSystem",cond);
        List<Record> system=Db.find(sqlPara);
        result.put("system",system);
        //存入服务器的登录账号
        cond=Kv.by("id",server.getId());
        sqlPara=Db.getSqlPara("server.showServerAccount",cond);
        List<Record> account=Db.find(sqlPara);
        result.put("account",account);
        return DataResult.data(result);
    }

    /***
     * 删除服务器
     *
     * @param address 服务器地址
     */
    public BaseResult deleteServer(String address){
        final String[] result = new String[1];
        boolean r = Db.tx(() -> {
            //1.删除服务器主体
            Kv cond = Kv.by("address", address);
            if (update(cond, "deleteServer") == 0) {
                result[0] = "服务器主体删除失败";
                return false;
            }
            //2.删除服务器与系统联系
            SqlPara sqlPara = Db.getSqlPara("server.selectConnect", cond);
            List<Record> connectId = Db.find(sqlPara);
            for (int i = 0; i < connectId.size(); i++) {
                Kv c=Kv.by("id",connectId.get(i).getStr("id"));
                if (update(c, "deleteConnect") == 0) {
                    result[0] = "服务器与系统关联删除失败";
                    return false;
                }
            }
            //3.删除服务器登录账号
            sqlPara = Db.getSqlPara("server.selectAccount", cond);
            List<Record> accountId = Db.find(sqlPara);
            for (int i = 0; i < accountId.size(); i++) {
                Kv c=Kv.by("id",accountId.get(i).getStr("id"));
                if (update(c, "deleteAccount") == 0) {
                    result[0] = "服务器登录账号删除失败";
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
     * 删除服务器登录账号
     *
     * @param account_id 账号id
     * @return 删除结果
     */
    public BaseResult deleteServerAccount(long account_id){
        Kv cond=Kv.by("id",account_id);
        int result=update(cond,"deleteAccount");
        if(result==0){
            return BaseResult.fail("服务器登录账号删除失败");
        }
        return BaseResult.ok();
    }
}
