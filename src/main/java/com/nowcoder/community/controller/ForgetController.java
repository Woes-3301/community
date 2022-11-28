package com.nowcoder.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ForgetController {


    @RequestMapping(path = "/forget",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/forget";
    }


}
