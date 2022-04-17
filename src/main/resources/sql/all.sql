#--------------------用户模块相关SQL操作--------------------#
#session相关的sql操作
#namespace("session")
#include("user/session.sql")
#end

#用户相关的sql操作
#namespace("user")
#include("user/user.sql")
#end

#namespace("department")
#include("user/department.sql")
#end
#--------------------用户模块相关SQL操作--------------------#

#--------------------日志相关SQL操作--------------------#
#日志相关的sql操作
#namespace("log")
#include("log/log.sql")
#end
#--------------------日志相关SQL操作--------------------#

#--------------------服务器相关SQL操作--------------------#
#服务器相关的sql操作
#namespace("server")
#include("server/server.sql")
#end
#--------------------服务器相关SQL操作--------------------#

#--------------------系统模块相关SQL操作--------------------#
#系统相关的sql操作
#namespace("system")
#include("system/system.sql")
#end
#--------------------系统模块相关SQL操作--------------------#