package com.finance.controller;

import com.finance.service.CurrencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping("/rates")
    public ResponseEntity<List<Map<String, Object>>> getCurrencyRates() {
        return ResponseEntity.ok(currencyService.getCurrencyData());
    }

    @GetMapping("/all-rates")
    public ResponseEntity<Map<String, BigDecimal>> getAllRates() {
        return ResponseEntity.ok(currencyService.getAllRates());
    }
}
