package com.finance.controller;

import com.finance.dto.MarketQuote;
import com.finance.service.MarketDataStreamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/market")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MarketDataStreamController {

    private final MarketDataStreamService streamService;

    public MarketDataStreamController(MarketDataStreamService streamService) {
        this.streamService = streamService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamQuotes(@RequestParam String symbols,
                                   @RequestParam(defaultValue = "NSE") String exchange,
                                   @RequestParam(defaultValue = "5") long intervalSeconds) {

        List<String> symbolList = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .limit(20) // prevent abuse
                .collect(Collectors.toList());

        if (symbolList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No symbols provided");
        }

        Duration interval = Duration.ofSeconds(intervalSeconds);
        return streamService.subscribe(symbolList, exchange, interval);
    }

    @GetMapping("/quote")
    public ResponseEntity<?> getQuote(@RequestParam String symbol,
                                      @RequestParam(defaultValue = "NSE") String exchange) {

        Optional<MarketQuote> quote = streamService.getQuote(symbol, exchange);
        return quote.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
