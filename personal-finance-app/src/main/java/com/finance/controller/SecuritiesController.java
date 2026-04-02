package com.finance.controller;

import com.finance.service.SecuritiesService;
import com.finance.service.SecuritiesService.SecurityItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/securities")
public class SecuritiesController {

    private final SecuritiesService securitiesService;

    public SecuritiesController(SecuritiesService securitiesService) {
        this.securitiesService = securitiesService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<SecurityItem>> search(
            @RequestParam("q") String q,
            @RequestParam(value = "min", defaultValue = "2") int minChars) {
        List<SecurityItem> list = securitiesService.searchWithMinChars(q, minChars);
        return ResponseEntity.ok(list);
    }
}
