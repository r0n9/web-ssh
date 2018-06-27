package vip.r0n9.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import vip.r0n9.JsonUtil;

@Controller
public class WebSshController {
    @GetMapping("/")
    public String showIndex(Model model) {
        return "index";
    }


    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public ObjectNode connect() {
        ObjectNode node = JsonUtil.createObjectNode();
        node.put("status", 0);
        node.put("id", "1");
        node.put("encoding", "utf-8");
        return node;
    }
}
