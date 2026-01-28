package com.sky.controller.admin;


import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.result.Result;
import com.sky.websocket.WebSocketServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/order")
@Slf4j
@Api("自测订单消息推送")
public class OrderReceivedNotificationController {

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private ObjectMapper objectMapper;

    @ApiOperation("自定义测试新订单消息")
    @PostMapping("/newOrder")
    public Result processOrderReceivedNotification() {

        Map map = new HashMap();
        map.put("type", 1);
        map.put("orderId", "Order 123456");
        map.put("content", "You have received a new order, id: Order 123456");

        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);

        return Result.success();
    }


}
