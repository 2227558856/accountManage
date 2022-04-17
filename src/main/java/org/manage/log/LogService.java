package org.manage.log;

import cn.fabrice.common.pojo.BaseResult;
import cn.fabrice.common.pojo.DataResult;
import cn.fabrice.jfinal.service.BaseService;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import org.manage.common.module.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * 日志
 */
public class LogService extends BaseService<Log> {
    public LogService(){
        super("log.",Log.class,"log");
    }

    /***
     * 显示所有的日志信息
     * @return 返回处理后的可视化日志信息
     */
    public DataResult listLog(){
        List<Log> logList=list("listLog");
        List<Map<String,Object>> resultList=new ArrayList<Map<String,Object>>();
        //对每条日志进行处理,得到可视化日志信息
        for(int i=0;i<logList.size();i++){
            Map<String,Object> result=new HashMap<>();
            Log log= logList.get(i);
            //1.将操作由枚举转化成可视的字符串
            if(log.getOperate()==1){
                result.put("operate","增加");
            }
            else if(log.getOperate()==2){
                result.put("operate","删除");
            }
            else if(log.getOperate()==3){
                result.put("operate","修改");
            }
            //2.将操作对象由id转换成名称
            String name=getObjectName(log.getObject(),log.getObjectId().longValue());
            //从getObjectName方法得到的content中减去四个得到name
            name=name.substring(0,name.length()-4);
            result.put("object",name);
            //3.content直接传递即可
            result.put("content",log.getContent());
            //4.user_id转换为用户名在listLog语句中已经实现直接获取即可
            result.put("user_name",log.getStr("name"));
            //5.将result存入list中
            resultList.add(result);
        }
        return DataResult.data(resultList);
    }

    /***
     * 生成日志信息
     * @param operate 操作  ---1-增加;2-删除;3-修改
     * @param object 操作对象  ---1-系统;2-服务器
     * @param object_id 操作对象的id,用于找到其名称
     * @param con   ---粗糙的内容,后续要加工,将它们拼凑成content
     * @param operate_object 表示具体的操作对象,例如是关联or账号or地址等等
     * @param user_id 进行此次操作的用户id,用于找到其名字
     * @return 返回添加日志的结果,成功or失败
     */
    public BaseResult addLog(int operate,int object,long object_id,String con,String operate_object,long user_id){
        Log log=new Log();
        //首先将不需要处理的信息存入log中
        log.set("operate",operate);
        log.set("object",object);
        log.set("object_id",object_id);
        log.set("user_id",user_id);
        //再根据信息拼凑content,使用户观感更佳
        String content="";
        //1.新增
        if(operate==1){
            content+="对"+getObjectName(object,object_id)+operate_object+"进行了新增操作,新增内容为:"+con;
        }
        //2.删除
        if(operate==2){
            content=content+"对"+getObjectName(object,object_id)+operate_object+"进行了删除操作,删除内容为:"+con;
        }
        if(operate==3){
            content=content+"对"+getObjectName(object,object_id)+operate_object+"进行了更新操作,更新内容为:"+con;
        }
        log.set("content",content);
        System.out.println(content);
        System.out.println(log);
        if(log.save()){
            return BaseResult.ok();
        }
        return BaseResult.fail("日志添加失败");
    }

    /***
     * 为addLog中查找名称提供便利
     * @param object 对象
     * @param object_id 对象id
     * @return 对象名称
     */
    public String getObjectName(int object,long object_id){
        String content="";
        if(object==1){
            Kv cond = Kv.by("id",object_id);
            SqlPara sqlPara= Db.getSqlPara("log.getSystemById",cond);
            Record system=Db.findFirst(sqlPara);
            String name=system.getStr("name");
            content+=name+"系统中的";
        }
        else if(object==2){
            Kv cond = Kv.by("id",object_id);
            SqlPara sqlPara= Db.getSqlPara("log.getServerById",cond);
            Record server=Db.findFirst(sqlPara);
            String name=server.getStr("name");
            content+=name+"服务器的";
        }
        return content;
    }
}
