#sql("getByAddress")
select id,name,address,introduction
from system
where address=#para(address) and is_deleted=0
#end

#sql("getById")
select id,name,address,introduction
from system
where id=#para(id) and is_deleted=0
#end

#sql("getServer")
select id,name,address,introduction
from server
where address=#para(serverAddress)
#end

#sql("getAccount")
select id,name,account,password
from account
where id=#para(id)
#end

#sql("getConnect")
select * from system_server
where system_id=(select id from system where address=#para(systemAddress) and is_deleted=0)
and server_id=(select id from server where address=#para(serverAddress) and is_deleted=0)
and is_deleted=0
#end

#sql("connectSystemServer")
insert into system_server(system_id,server_id)
select system.id,server.id
from system,server
where system.address=#para(systemAddress) and server.address=#para(serverAddress)
and system.is_deleted=0 and server.is_deleted=0
#end

#sql("getSystemAccount")
select id ,name,account,password
from account
where object_id=#para(id) and object=1 and account=#para(account) and is_deleted=0
#end

#sql("listSystem")
select id,name,address,introduction
from system
where is_deleted=0
#end

#sql("showSystemServer")
select id,name as server_name ,address as server_address ,introduction as server_introduction
from server
where id in
    (select server_id from system_server,system
        where system.id=system_server.system_id and system.address=#para(address)
          and system.is_deleted=0 and system_server.is_deleted=0 )
#end

#sql("showSystemAccount")
select id ,name,account,password
from account
where object_id=#para(id) and object=1 and is_deleted=0
#end

#sql("deleteSystem")
update system
set is_deleted=1
where address=#para(address)
#end

#sql("selectConnect")
select system_server.id
from system_server,system
where system.address=#para(address) and system.id=system_server.system_id and system.is_deleted=1
#end
#sql("deleteConnect")
update system_server
set is_deleted=1
where id=#para(id)
#end

#sql("selectAccount")
select account.id
from account,system where system.address=#para(address)
and system.id=account.object_id and account.object=1 and system.is_deleted=1
#end
#sql("deleteAccount")
update account
set is_deleted=1
where id=#para(id)
#end

#sql("deleteSystemServer")
update system_server
set is_deleted=1
where system_id=#para(system_id) and server_id=#para(server_id)
#end

#sql("updateConnectServer")
update system_server
set server_id=#para(after_id)
where system_id=#para(system_id) and server_id=#para(before_id) and is_deleted=0
#end

#sql("updateConnectSystem")
update system_server
set system_id=#para(after_id)
where server_id=#para(server_id) and system_id=#para(before_id) and is_deleted=0
#end

#sql("updateAccount")
update account
set name=#para(name),account=#para(account),password=#para(password)
where id=#para(id) and is_deleted=0
#end