package org.manage.user;

import org.manage.common.interceptor.AuthInterceptor;
import org.manage.common.module.User;
import org.manage.common.module.UserSession;
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


/***
 * @author Administrator
 * 用户功能体现(接参传参)
 */
@Path("/user")
@ValidateParam
public class UserController extends Controller {
    @Inject
    UserService userService;
    @Inject
    SessionService sessionService;


    /**
     * 初始页面添加管理员
     *
     * @param account 管理员账号
     * @param password 管理员密码
     */
    @Clear({AuthInterceptor.class})
    @Param(name = "account", required = true)
    @Param(name = "password", required = true)
    public void addManage(String account,String password) {
        BaseResult result = userService.addUser("管理员",account, password,1);
        renderJson(result);
    }

    /**
     * 用户登陆
     *注解Clear({AuthInterceptor.class})用于屏蔽全局拦截器,即使用登录功能时不需要检测access_token
     * (其他的功能基本都要检测,即登录才能调用这些功能)
     *
     * @param keepLogin 是否保持登陆：true/false
     * @param account   登陆账号
     * @param password  登陆密码
     */
    @Clear({AuthInterceptor.class})
    @Param(name = "keepLogin", rule = ValidateRuleConstants.Key.BOOLEAN)
    @Param(name = "account", required = true)
    @Param(name = "password", required = true)
    public void login(boolean keepLogin, String account, String password) {
        BaseResult result = userService.login(keepLogin, account, password);
        renderJson(result);
    }

    /**
     * 退出登录
     */
    public void logout() {
        UserSession session = getAttr(BaseConstants.ACCOUNT);
        String token = session.getSessionId();
        if (sessionService.deleteByToken(token)) {
            //同时删除缓存token
            CacheKit.remove(BaseConstants.ACCOUNT_CACHE_NAME, token);
            renderJson(BaseResult.ok());
            return;
        }
        renderJson(BaseResult.fail());
    }

    /**
     * 获取用户信息
     *
     * @param id 用户ID
     */
    @Param(name = "id", required = true, rule = ValidateRuleConstants.Key.ID)
    public void get(long id) {
        User user = userService.get(id);
        renderJson(DataResult.data(user));
    }

    /**
     * 管理员添加用户
     *
     * @param name 用户名
     * @param account 用户账号
     * @param password 初始密码
     */
    @Param(name = "name", required = true)
    @Param(name = "account", required = true)
    @Param(name = "password", required = true)
    public void addUser(String name,String account,String password) {
        BaseResult result = userService.addUser(name,account, password,2);
        renderJson(result);
    }

    /**
     * 修改密码
     * 是用户在管理员创建账号+初始密码后自行修改密码,不提供忘记密码功能
     *
     * @param password 初始密码
     */
    @Param(name = "password", required = true)
    public void updatePassword(String password) {
        //从token中获取user信息(为什么getAttr这样写就能获取到呢?)
        long id=getAttr(BaseConstants.ACCOUNT_ID);
        BaseResult result = userService.updatePassword(password,id);
        renderJson(result);
    }

    /**
     * 删除用户
     *
     * @param account 删除账号
     */
    @Param(name = "account", required = true)
    public void deleteUser(String account) {
        renderJson(userService.deleteUser(account));
    }

    /**
     * 显示所有用户(管理员管理用户)
     *
     */
    public void listUser() {
        renderJson(DataResult.data(userService.listUser()));
    }
}
