package com.haitaos.finallbbs.controller;


import com.alibaba.fastjson.JSONObject;
import com.haitaos.finallbbs.cache.IpLimitCache;
import com.haitaos.finallbbs.dto.ValidateDTO;
import com.haitaos.finallbbs.provider.VaptchaProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ValidateController {

    @Autowired
    private IpLimitCache ipLimitCache;

    @ResponseBody//@ResponseBody返回json格式的数据
    @RequestMapping(value = "/validate", method = RequestMethod.POST)
    public Object post(@RequestParam(name = "token", required = false) String token,
                       @RequestParam(name = "scene", required = false) int scene,
                       @RequestParam(name = "ip", required = false) String ip) {

        System.out.println("token:"+token+"scene:"+scene+"ip:"+ip);
        if(ipLimitCache.putInterval(ip, token)==0) {
            ipLimitCache.addIpScores(ip,10);
            ValidateDTO validateDTO = new ValidateDTO();
            validateDTO.setMsg("验证频繁，需至少间隔30S");
            validateDTO.setSocre(100);
            validateDTO.setSuccess(0);
            return validateDTO;
        }
        String json = VaptchaProvider.getValidateResult(token,scene,ip);

        JSONObject obj = JSONObject.parseObject(json);
        Integer success = obj.getInteger("success");
        Integer score = obj.getInteger("score");
        String msg = obj.getString("msg");
        ValidateDTO validateDTO = new ValidateDTO();
        validateDTO.setMsg(msg);
        validateDTO.setSocre(score);
        validateDTO.setSuccess(success);

        return validateDTO;
    }

    @Deprecated
    @ResponseBody//@ResponseBody返回json格式的数据
    @RequestMapping(value = "/getIp", method = RequestMethod.GET)
    public String getIp() {

        String returnCitySN = VaptchaProvider.getIp();
        String json = returnCitySN.split("=")[1].split(";")[0];
        JSONObject obj = JSONObject.parseObject(json);
        String cip = obj.getString("cip");
        String cid = obj.getString("cid");
        String cname = obj.getString("cname");
        return cip;
    }

}
