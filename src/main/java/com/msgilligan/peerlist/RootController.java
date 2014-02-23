package com.msgilligan.peerlist;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * User: sean
 * Date: 2/23/14
 * Time: 12:11 AM
 */
@Controller
public class RootController {

    @RequestMapping("/")
    public String index() {
        return "index.html";
    }
}
