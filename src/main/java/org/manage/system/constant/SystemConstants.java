package org.manage.system.constant;

import cn.fabrice.common.constant.BaseResultConstants;

/**
 * @author Administrator
 * 系统相关常量类
 */
public class SystemConstants {
    /**
     * 结果常量类
     */
    public static final class Result {
        /**
         * 系统已存在
         */
        public static final int SYSTEM_IS_EXIST = 60001;
        /**
         * 系统添加失败
         */
        public static final int SYSTEM_ADD_FAIL = 60002;
        /**
         * 服务器不存在
         */
        public static final int SERVER_NOT_EXIST = 60003;
        /**
         * 系统服务器关联失败
         */
        public static final int CONNECT_FAIL = 60004;
        /**
         * 账号已存在
         */
        public static final int ACCOUNT_IS_EXIST = 60005;
        /**
         * 账号添加失败
         */
        public static final int ACCOUNT_ADD_FAIL = 60006;
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
        public static final int UPDATE_ERROR = 50010;

        /**
         * 赋值父类，填充返回消息值
         */
        public static void init() {
            BaseResultConstants.addResultInfo(SYSTEM_IS_EXIST, Message.SYSTEM_IS_EXIST);
            BaseResultConstants.addResultInfo(SYSTEM_ADD_FAIL, Message.SYSTEM_ADD_FAIL);
            BaseResultConstants.addResultInfo(SERVER_NOT_EXIST, Message.SERVER_NOT_EXIST);
            BaseResultConstants.addResultInfo(CONNECT_FAIL, Message.CONNECT_FAIL);
            BaseResultConstants.addResultInfo(ACCOUNT_IS_EXIST, Message.ACCOUNT_IS_EXIST);
            BaseResultConstants.addResultInfo(ACCOUNT_ADD_FAIL, Message.ACCOUNT_ADD_FAIL);
        }
    }

    /**
     * 消息常量类
     */
    public static final class Message {
        public static final String SYSTEM_IS_EXIST = "系统已存在";
        public static final String SYSTEM_ADD_FAIL = "系统添加失败";
        public static final String SERVER_NOT_EXIST = "服务器不存在";
        public static final String CONNECT_FAIL = "系统服务器关联失败";
        public static final String ACCOUNT_IS_EXIST = "账号已存在";
        public static final String ACCOUNT_ADD_FAIL = "账号添加失败";
        public static final String USER_ADD_FAIL = "用户添加失败";
        public static final String UPDATE_ERROR = "修改失败";
    }
}
