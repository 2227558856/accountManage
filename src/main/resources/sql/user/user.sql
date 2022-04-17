#sql("getByAccount")
select id,account,salt,password,role
from user
where account = #para(account) and is_deleted = 0
#end

#sql("getByName")
select id,account,salt,password,role
from user
where name = #para(name) and is_deleted = 0
#end

#sql("getById")
select id,account,salt,password,role
from user
where id = #para(id) and is_deleted = 0
#end

#sql("updatePassword")
update user
set password=#para(password)
where id=#para(id)
#end

#sql("deleteUser")
update user
set is_deleted=1
where account=#para(account)
#end

#sql("listUser")
select id,name,account
from user
where role = 2 and is_deleted=0
#end



#sql("list")
    select u.id,u.account,u.name,u.photo_file_id,u.photo_file_name,u.email,
           u.gender,u.status,u.id_card,u.mobile,u.department_id,u.type,
           d.name as deptName
    from user u
        join department d on u.department_id = d.id
        #if(rq.departmentId != 0)
            and (d.id = #para(rq.departmentId) or find_in_set(#para(rq.departmentId),d.parent_id_list) > 0)
        #end
    where u.is_deleted = 0
    #if(notBlank(rq.name))
        and position(#para(rq.name) in u.name) > 0
    #end
    #if(rq.status != 0)
        and u.status = #para(rq.status)
    #end
    #if(notBlank(rq.account))
     and position(#para(rq.account) in u.account) > 0
    #end
    #if(notBlank(rq.mobile))
     and position(#para(rq.mobile) in u.mobile) > 0
    #end
    #if(notBlank(rq.startTime))
     and date_format(u.created_time,'%Y-%m-%d') >= #para(rq.startTime)
    #end
    #if(notBlank(rq.endTime))
     and date_format(u.created_time,'%Y-%m-%d') <= #para(rq.endTime)
    #end
#end

#sql("get")
    select id,name,photo_file_id,photo_file_name,email,account
    from user
    where id = #para(id)
#end
