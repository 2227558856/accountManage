package org.manage.server.constant;

import cn.fabrice.common.constant.BaseResultConstants;

/**
 * @author Administrator
 */
public class ServerConstant {
    /**
     * 结果常量类
     */
    public static final class Result {
        /**
         * 服务器已存在
         */
        public static final int SERVER_IS_EXIST = 70001;
        /**
         * 服务器添加失败
         */
        public static final int SERVER_ADD_FAIL = 60002;
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
            BaseResultConstants.addResultInfo(SERVER_IS_EXIST, Message.SERVER_IS_EXIST);
            BaseResultConstants.addResultInfo(SERVER_ADD_FAIL, Message.SERVER_ADD_FAIL);
        }
    }

    /**
     * 消息常量类
     */
    public static final class Message {
        public static final String SERVER_IS_EXIST = "服务器已存在";
        public static final String SERVER_ADD_FAIL = "服务器添加失败";
        public static final String ACCOUNT_IS_FORBIDDEN = "账号被禁用";
        public static final String ACCOUNT_SESSION_SAVED_FAIL = "账号session保存失败，请联系系统管理员";
        public static final String ACCOUNT_IS_LOGON = "账号已被登录，清空已登录账号失败";
        public static final String USER_IS_DELETED = "用户已被删除";
        public static final String USER_ADD_FAIL = "用户添加失败";
        public static final String ACCOUNT_IS_EXIST = "用户已存在";
        public static final String UPDATE_ERROR = "修改失败";
    }
}
