package org.manage.common.module;

import org.manage.common.module.base.BaseUserSession;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class UserSession extends BaseUserSession<UserSession> {
	public static final UserSession dao = new UserSession().dao();

	/**
	 * session是否过期
	 * <p>
	 * 当当前时间戳大于过期时间戳，则说明session过期
	 *
	 * @return 过期-true/未过期-false
	 */
	public boolean isExpired() {
		return getExpiresTime() != 0 && getExpiresTime() < java.lang.System.currentTimeMillis();
	}
}

