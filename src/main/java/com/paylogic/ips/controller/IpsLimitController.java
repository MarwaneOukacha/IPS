package com.paylogic.ips.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.paylogic.ips.services.IpsLimitService;

@RestController
@RequestMapping("/limit")
public class IpsLimitController {

    @Autowired
    private IpsLimitService service;

    // CAMT.009
    @PostMapping("/get/{accountId}")
    public void getLimit(@PathVariable String accountId)
                            {

        service.sendCamt009(accountId);
    }

    // CAMT.011
    @PostMapping("/update")
    public void updateLimit(@RequestParam String accountId,
                              @RequestParam String amount) {

        service.sendCamt011(accountId, amount);
    }

    // OUTPUT
    @GetMapping("/output")
    public String getOutput() {
        return service.fetchIpsOutput();
    }
}