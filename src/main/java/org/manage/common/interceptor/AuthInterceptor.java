package org.manage.common.interceptor;

import org.manage.common.module.UserSession;
import org.manage.user.constant.UserConstants;
import org.manage.user.session.SessionService;
import cn.fabrice.common.constant.BaseConstants;
import cn.fabrice.common.pojo.BaseResult;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.ehcache.CacheKit;

/**
 * @author fye
 * @email fh941005@163.com
 * @date 2019-06-28 14:44
 * @description 认证拦截器
 */
public class AuthInterceptor implements Interceptor {
    @Override
    public void intercept(Invocation invocation) {
        Controller controller = invocation.getController();
        if (StrKit.notBlank(controller.getHeader(BaseConstants.TOKEN_NAME))) {
            String token = controller.getHeader(BaseConstants.TOKEN_NAME);
            SessionService sessionService = Aop.get(SessionService.class);
            //获取session实体
            UserSession userSession = CacheKit.get(BaseConstants.ACCOUNT_CACHE_NAME, token,
                    () -> sessionService.getByToken(token));
            if (userSession == null) {
                controller.renderJson(BaseResult.res(UserConstants.Result.ILLEGAL_TOKEN));
                return;
            }
            controller.setAttr(BaseConstants.ACCOUNT, userSession);
            controller.setAttr(BaseConstants.ACCOUNT_ID, userSession.getUserId().longValue());
            invocation.invoke();
            return;
        }
        controller.renderJson(BaseResult.res(UserConstants.Result.ILLEGAL_TOKEN));
    }
}
