package org.manage.user;

import cn.fabrice.jfinal.service.BaseService;
import org.manage.common.module.User;
import org.manage.common.module.UserSession;
import org.manage.user.constant.UserConstants;
import org.manage.user.session.SessionService;
import cn.fabrice.common.constant.BaseConstants;
import cn.fabrice.common.constant.BaseEncryptedKey;
import cn.fabrice.common.pojo.BaseResult;
import cn.fabrice.common.pojo.DataResult;

import com.jfinal.aop.Inject;
import com.jfinal.kit.HashKit;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.ehcache.CacheKit;

import java.util.List;
import java.util.Objects;

/**
 * @author Administrator
 * 用户功能实现(处理参数)
 */
public class UserService extends BaseService<User> {
    /**
     * 用户session业务处理类
     */
    @Inject
    SessionService sessionService;


    public UserService() {
        super("user.", User.class, "user");
        this.setToBeEncryptedInfo("photo_file_id," + BaseEncryptedKey.FILE_ID);
    }

    /**
     * 登录
     *
     * @param keepLogin 是否保持登录状态
     * @param account   登录账号
     * @param password  登录密码
     * @return 登录结果集
     */
    public BaseResult login(boolean keepLogin, String account, String password) {
        Kv cond = Kv.by("account", account);
        User user = get(cond, "getByAccount");
        if (user == null) {
            return BaseResult.res(UserConstants.Result.ACCOUNT_NOT_EXIST);
        }
        String salt = user.getSalt();
        String hashedPass = HashKit.sha256(salt + password);
        if (!user.getPassword().equals(hashedPass)) {
            return BaseResult.res(UserConstants.Result.ERROR_PASSWORD);
        }
        //只允许单个地方登录，此时判断该用户是否存在session信息
        UserSession userSession = sessionService.getByAccount(user.getId().longValue());
        if (userSession != null) {
            //判断信息是否过期
            if (userSession.isExpired()) {
                //信息过期，直接删除
                if (!sessionService.deleteByInnerSql(userSession.getId().longValue())) {
                    return BaseResult.res(UserConstants.Result.ACCOUNT_IS_LOGON);
                }
            } else {
                String token = userSession.getSessionId();
                //信息未过期，则处理
                if (sessionService.deleteByInnerSql(userSession.getId().longValue())) {
                    CacheKit.remove(BaseConstants.ACCOUNT_CACHE_NAME, token);
                } else {
                    return BaseResult.res(UserConstants.Result.ACCOUNT_IS_LOGON);
                }
            }
        }
        // 如果用户勾选保持登录，暂定过期时间为 3 年，否则为 120 分钟，单位为秒
        long liveSeconds = keepLogin ? 94608000 : 7200;
        userSession = sessionService.add(user.getId().longValue(), liveSeconds);
        if (userSession == null) {
            return BaseResult.fail(UserConstants.Message.ACCOUNT_SESSION_SAVED_FAIL);
        }
        //登录成功，放入缓存
        CacheKit.put(BaseConstants.ACCOUNT_CACHE_NAME, userSession.getSessionId(), userSession);
        //将sessionId作为accessToken
        user.put(BaseConstants.TOKEN_NAME, userSession.getSessionId());
        user.put(BaseConstants.SESSION_ID, userSession.getId());
        return DataResult.data(user);
    }

    /***
     * 管理员添加普通用户
     *
     * @param name 添加用户名
     * @param account 添加账号
     * @param password 添加密码
     * @param role 添加身份   --1表示管理员;--2表示普通用户
     * @return 添加结果--成功or失败
     */
    public BaseResult addUser(String name,String account,String password,int role){
        User user=new User();
        if(Objects.equals(name, "管理员")){
            Kv cond = Kv.by("name", name);
            User result = get(cond, "getByName");
            if (result != null) {
                return BaseResult.fail("管理员账号已存在,无法创建,请登录管理员账号以创建普通用户");
            }
        }
        Kv cond = Kv.by("account", account);
        User result = get(cond, "getByAccount");
        if (result != null) {
            return BaseResult.res(UserConstants.Result.ACCOUNT_IS_EXIST);
        }
        user.setName(name);
        user.setAccount(account);
        user.setPassword(password);
        String salt = HashKit.generateSaltForSha256();
        user.setSalt(salt);
        user.setPassword(HashKit.sha256(salt+password));
        user.setRole(role);
        if(user.save()){
            return BaseResult.ok();
        }
        else{
            return BaseResult.res(UserConstants.Result.USER_ADD_FAIL);
        }
    }

    /***
     * 修改密码
     *
     * @param password 添加密码
     * @return 添加结果--成功or失败
     */
    public BaseResult updatePassword(String password,long id){
        Kv cond1 = Kv.by("id",id);
        User user=get(cond1,"getById");
        password=HashKit.sha256(user.getSalt()+password);
        Kv cond2 = Kv.by("password", password).set("id",id);
        int result = update(cond2, "updatePassword");
        if (result == 0) {
            return BaseResult.res(UserConstants.Result.UPDATE_ERROR);
        }
        else{
            return BaseResult.ok();
        }
    }

    /***
     * 管理员删除用户
     *
     * @param account 删除账号
     * @return 删除结果
     */
    public BaseResult deleteUser(String account){
        Kv cond2 = Kv.by("account", account);
        int result = update(cond2, "deleteUser");
        if (result == 0) {
            return BaseResult.fail("用户删除失败");
        }
        return BaseResult.ok();
    }

    /**
     * 获取所有用户列表
     *
     * @return 返回所有用户列表数据
     */
    public List<User> listUser() {
        return list("listUser");
    }
}
