#sql("tree")
    select d.id,d.name,d.parent_id,d.order_number,d.is_used,d.remark,t.name as pName,d.is_parent
    from department d
    left join department t on d.parent_id = t.id
    where d.is_deleted = 0
    #if(isBlank(rq.name))
    and d.parent_id = #para(rq.parentId)
    #end
    #if(notBlank(rq.name))
    and position(#para(rq.name) in d.name) > 0
    #end
    order by d.order_number
#end

#sql("get")
    select id,level,is_parent,parent_id_list,is_used,parent_id
    from department
    where id = #para(id)
#end

#sql("listChildren")
select * from department where parent_id = #para(parentId)
#end

#sql("listAll")
    select d.id,d.id as value,d.name,d.name as label,d.parent_id,d.is_parent,t.name as pName
    from department d
    left join department t on d.parent_id = t.id
    where d.is_deleted = 0
    order by d.order_number
#end

#sql("getFullName")
    select group_concat(name separator ' / ') as name
     from department where (id in (select parent_id_list from department where id = #para(id)) or id = #para(id)) order by level
#end