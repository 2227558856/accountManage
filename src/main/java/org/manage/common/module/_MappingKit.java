package org.manage.common.module;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

/**
 * Generated by JFinal, do not modify this file.
 * <pre>
 * Example:
 * public void configPlugin(Plugins me) {
 *     ActiveRecordPlugin arp = new ActiveRecordPlugin(...);
 *     _MappingKit.mapping(arp);
 *     me.add(arp);
 * }
 * </pre>
 */
public class _MappingKit {
	
	public static void mapping(ActiveRecordPlugin arp) {
		arp.addMapping("account", "id", Account.class);
		arp.addMapping("log", "id", Log.class);
		arp.addMapping("server", "id", Server.class);
		arp.addMapping("system", "id", System.class);
		arp.addMapping("system_server", "id", SystemServer.class);
		arp.addMapping("user", "id", User.class);
		arp.addMapping("user_session", "id", UserSession.class);
	}
}


