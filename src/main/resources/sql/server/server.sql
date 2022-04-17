#sql("getByAddress")
select id,name,address,introduction
from server
where address=#para(address) and is_deleted=0
#end

#sql("getById")
select id,name,address,introduction
from server
where id=#para(id) and is_deleted=0
#end

#sql("getSystem")
select name,address,introduction
from system
where address=#para(systemAddress) and is_deleted=0
#end

#sql("getServerAccount")
select id ,name,account,password
from account
where object_id=#para(id) and object=2 and account=#para(account) and is_deleted=0
#end

#sql("listServer")
select id,name,address,introduction
from server
where is_deleted=0
#end

#sql("showServerSystem")
select id,name as system_name ,address as system_address ,introduction as system_introduction
from system
where id in
      (select system_id from system_server,server
       where server.id=system_server.server_id and server.address=#para(address)
         and server.is_deleted=0 and system_server.is_deleted=0 )
#end

#sql("showServerAccount")
select id,account,password
from account
where object_id=#para(id) and object=2 and is_deleted=0
#end

#sql("deleteServer")
update server
set is_deleted=1
where address=#para(address)
#end

#sql("selectConnect")
select system_server.id
from system_server,server where server.address=#para(address)
and server.id=system_server.server_id and server.is_deleted=1
#end
#sql("deleteConnect")
update system_server
set is_deleted=1
where id=#para(id)
#end

#sql("selectAccount")
select account.id
from account,server where server.address=#para(address)
and server.id=account.object_id and account.object=2 and server.is_deleted=1
#end
#sql("deleteAccount")
update account
set is_deleted=1
where id=#para(id)
#end