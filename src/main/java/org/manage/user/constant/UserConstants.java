package org.manage.user.constant;

import cn.fabrice.common.constant.BaseResultConstants;

import java.util.Collections;
import java.util.List;

/**
 * @author fye
 * @description 用户相关常量类
 */
public class UserConstants {

    /**
     * 登录的用户信息
     */
    public static final String LOGIN_USER = "LoginUser";

    public static final String USER_CACHE_NAME = "loginUser";

    public static final String PERMISSIONS_NAME_KEY = "permissions";
    public static final String PERMISSIONS_ALL_VALUE = "*:*:*";
    public static final List<String> PERMISSIONS_ADMIN_VALUE = Collections.singletonList(PERMISSIONS_ALL_VALUE);

    /**
     * 结果常量类
     */
    public static final class Result {
        /**
         * 账号不存在
         */
        public static final int ACCOUNT_NOT_EXIST = 50001;
        /**
         * 密码错误
         */
        public static final int ERROR_PASSWORD = 50002;
        /**
         * 账号被禁用
         */
        public static final int ACCOUNT_IS_FORBIDDEN = 50003;
        /**
         * 违法的token信息
         */
        public static final int ILLEGAL_TOKEN = 50004;
        /**
         * 过期的token信息
         */
        public static final int EXPIRED_TOKEN = 50005;
        /**
         * 账号已被登录，清空已登录账号失败
         */
        public static final int ACCOUNT_IS_LOGON = 50006;
        /**
         * 用户被删除
         */
        public static final int USER_IS_DELETED = 50007;
        /**
         * 用户添加失败
         */
        public static final int USER_ADD_FAIL = 50008;
        /**
         * 账号已存在
         */
        public static final int ACCOUNT_IS_EXIST = 50009;
        /**
         * 账号已存在
         */
        public static final int UPDATE_ERROR = 50010;

        /**
         * 赋值父类，填充返回消息值
         */
        public static void init() {
            BaseResultConstants.addResultInfo(ILLEGAL_TOKEN, Message.ILLEGAL_TOKEN);
            BaseResultConstants.addResultInfo(EXPIRED_TOKEN, Message.EXPIRED_TOKEN);
            BaseResultConstants.addResultInfo(ACCOUNT_NOT_EXIST, Message.ACCOUNT_NOT_EXIST);
            BaseResultConstants.addResultInfo(ERROR_PASSWORD, Message.ERROR_PASSWORD);
            BaseResultConstants.addResultInfo(ACCOUNT_IS_FORBIDDEN, Message.ACCOUNT_IS_FORBIDDEN);
            BaseResultConstants.addResultInfo(ACCOUNT_IS_LOGON, Message.ACCOUNT_IS_LOGON);
            BaseResultConstants.addResultInfo(USER_IS_DELETED, Message.USER_IS_DELETED);
            BaseResultConstants.addResultInfo(USER_ADD_FAIL, Message.USER_ADD_FAIL);
            BaseResultConstants.addResultInfo(ACCOUNT_IS_EXIST, Message.ACCOUNT_IS_EXIST);
            BaseResultConstants.addResultInfo(UPDATE_ERROR, Message.UPDATE_ERROR);
        }
    }

    /**
     * 消息常量类
     */
    public static final class Message {
        public static final String ILLEGAL_TOKEN = "违法的TOKEN信息";
        public static final String EXPIRED_TOKEN = "TOKEN信息已过期";
        public static final String ACCOUNT_NOT_EXIST = "账号不存在";
        public static final String ERROR_PASSWORD = "密码错误";
        public static final String ACCOUNT_IS_FORBIDDEN = "账号被禁用";
        public static final String ACCOUNT_SESSION_SAVED_FAIL = "账号session保存失败，请联系系统管理员";
        public static final String ACCOUNT_IS_LOGON = "账号已被登录，清空已登录账号失败";
        public static final String USER_IS_DELETED = "用户已被删除";
        public static final String USER_ADD_FAIL = "用户添加失败";
        public static final String ACCOUNT_IS_EXIST = "用户已存在";
        public static final String UPDATE_ERROR = "修改失败";
    }
}
