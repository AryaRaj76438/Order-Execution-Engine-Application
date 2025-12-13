package com.dex.orderengine.controller;

import com.dex.orderengine.service.OrderExecutionService;
import com.dex.orderengine.service.OrderQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final OrderExecutionService orderExecutionService;
    private final OrderQueueService queueService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("recentOrders", orderExecutionService.getRecentOrders());
        model.addAttribute("queueStats", queueService.getQueueStats());
        return "index";
    }
}
