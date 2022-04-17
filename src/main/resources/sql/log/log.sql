#sql("listLog")
select operate,object,object_id,content,user.name
from log,user
where user.id=log.user_id
#end

#sql("getSystemById")
select name
from system
where id=#para(id)
#end

#sql("getServerById")
select name
from server
where id=#para(id)
#end

#sql("getUserById")
select name
from user
where id=#para(id)
#end