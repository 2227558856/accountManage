package org.manage.log;

import cn.fabrice.common.pojo.BaseResult;
import cn.fabrice.jfinal.annotation.ValidateParam;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.Path;


/**
 * @author Administrator
 * 日志
 */
@Path("/log")
@ValidateParam
public class LogController extends Controller {
    /***
     *好奇怪,这里必须加Inject才能调用到logService,但是不加也不会报错,只有在要用到logService的时候才会报错
     */
    @Inject
    LogService logService;


    public void listLog(){
        renderJson(logService.listLog());
    }
}
