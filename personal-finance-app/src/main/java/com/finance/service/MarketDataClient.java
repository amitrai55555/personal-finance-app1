package com.finance.service;

import com.finance.dto.MarketQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MarketDataClient {

    private static final Logger log = LoggerFactory.getLogger(MarketDataClient.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Map<String, SymbolMapping> FREE_TIER_SYMBOLS = Map.of(
            "RELIANCE", new SymbolMapping("AAPL", "NASDAQ", true),
            "TCS", new SymbolMapping("MSFT", "NASDAQ", true),
            "SENSEX", new SymbolMapping("SPY", "NYSE", true),
            "NIFTY", new SymbolMapping("QQQ", "NASDAQ", true)
    );

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;
    private final String defaultExchange;
    private final Duration cacheTtl;
    private final Map<String, CachedQuote> cache = new ConcurrentHashMap<>();

    public MarketDataClient(@Value("${twelvedata.api.key:}") String apiKey,
                            @Value("${twelvedata.base-url:https://api.twelvedata.com}") String baseUrl,
                            @Value("${twelvedata.exchange:NSE}") String defaultExchange,
                            @Value("${twelvedata.cache-ttl-seconds:30}") long cacheTtlSeconds) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.defaultExchange = defaultExchange;
        this.cacheTtl = Duration.ofSeconds(cacheTtlSeconds);

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    public Optional<MarketQuote> getQuote(String symbol) {
        return getQuote(symbol, defaultExchange);
    }

    public Optional<MarketQuote> getQuote(String symbol, String exchange) {
        if (symbol == null || symbol.isBlank()) {
            return Optional.empty();
        }
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("Twelve Data API key is not configured; skipping market data fetch.");
            return Optional.empty();
        }

        SymbolMapping mapping = resolveSymbol(symbol, exchange);
        String apiSymbol = mapping.apiSymbol();
        String apiExchange = mapping.apiExchange() != null ? mapping.apiExchange() : exchange;

        if (mapping.replaced()) {
            log.info("Swapping {} on {} to free-tier symbol {} on {}", symbol, exchange, apiSymbol, apiExchange);
        }

        String cacheKey = apiExchange + ":" + apiSymbol;
        CachedQuote cached = cache.get(cacheKey);
        Instant now = Instant.now();
        if (isFresh(cached, now)) {
            return Optional.of(cached.quote());
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/quote")
                .queryParam("symbol", apiSymbol)
                .queryParam("exchange", apiExchange)
                .queryParam("apikey", apiKey);

        try {
            ResponseEntity<QuoteResponse> response = restTemplate.getForEntity(uriBuilder.toUriString(), QuoteResponse.class);
            QuoteResponse body = response.getBody();
            if (body == null) {
                log.warn("Twelve Data returned an empty response body for {} on {}", symbol, exchange);
                return useCachedQuote(cached, cacheKey, "empty response body");
            }

            if (body.isError()) {
                QuoteError error = body.getError();
                String code = error != null ? error.getCode() : body.getCode();
                String message = error != null ? error.getMessage() : body.getMessage();
                log.warn("Twelve Data error for {} on {} (status={}, code={}): {}",
                        symbol,
                        exchange,
                        body.getStatus(),
                        code,
                        message != null ? message : "unknown error");
                return useCachedQuote(cached, cacheKey, "provider error response");
            }

            MarketQuote quote = body.toMarketQuote(apiSymbol, apiExchange);
            cache.put(cacheKey, new CachedQuote(quote, now));
            return Optional.ofNullable(quote);
        } catch (RestClientException ex) {
            log.warn("Failed to fetch Twelve Data quote for {} on {}: {}", apiSymbol, apiExchange, ex.getMessage());
            return useCachedQuote(cached, cacheKey, "client exception");
        }
    }

    private SymbolMapping resolveSymbol(String symbol, String exchange) {
        SymbolMapping mapped = FREE_TIER_SYMBOLS.get(symbol.toUpperCase());
        if (mapped != null) {
            return mapped;
        }
        return new SymbolMapping(symbol, exchange, false);
    }

    private static BigDecimal toBigDecimal(String value) {
        try {
            return value == null || value.isBlank() ? null : new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDateTime toDateTime(String value) {
        try {
            return value == null || value.isBlank() ? null : LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isFresh(CachedQuote cached, Instant now) {
        return cached != null && cached.fetchedAt().isAfter(now.minus(cacheTtl));
    }

    private Optional<MarketQuote> useCachedQuote(CachedQuote cached, String cacheKey, String reason) {
        if (cached != null) {
            log.debug("Using cached quote for {} due to {}", cacheKey, reason);
            return Optional.of(cached.quote());
        }
        return Optional.empty();
    }

    private record CachedQuote(MarketQuote quote, Instant fetchedAt) {
    }

    private record SymbolMapping(String apiSymbol, String apiExchange, boolean replaced) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuoteResponse {
        private String symbol;
        private String exchange;
        private String currency;
        private String datetime;
        private String previous_close;
        private String open;
        private String high;
        private String low;
        private String close;
        private String change;
        private String percent_change;
        private String volume;
        private Boolean is_market_open;
        private String code;
        private String message;
        private String status;
        private QuoteError error;

        boolean isError() {
            // Twelve Data sometimes returns top-level code/message without the nested error object
            return (status != null && "error".equalsIgnoreCase(status)) || error != null || code != null;
        }

        MarketQuote toMarketQuote(String requestedSymbol, String requestedExchange) {
            MarketQuote quote = new MarketQuote();
            quote.setSymbol(symbol != null ? symbol : requestedSymbol);
            quote.setExchange(exchange != null ? exchange : requestedExchange);
            quote.setCurrency(currency);
            quote.setLastPrice(toBigDecimal(close));
            quote.setChange(toBigDecimal(change));
            quote.setPercentChange(toBigDecimal(percent_change));
            quote.setOpen(toBigDecimal(open));
            quote.setHigh(toBigDecimal(high));
            quote.setLow(toBigDecimal(low));
            quote.setPreviousClose(toBigDecimal(previous_close));
            quote.setVolume(toBigDecimal(volume));
            quote.setAsOf(toDateTime(datetime));
            quote.setMarketOpen(Boolean.TRUE.equals(is_market_open));
            return quote;
        }
        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getDatetime() {
            return datetime;
        }

        public void setDatetime(String datetime) {
            this.datetime = datetime;
        }

        public String getPrevious_close() {
            return previous_close;
        }

        public void setPrevious_close(String previous_close) {
            this.previous_close = previous_close;
        }

        public String getOpen() {
            return open;
        }

        public void setOpen(String open) {
            this.open = open;
        }

        public String getHigh() {
            return high;
        }

        public void setHigh(String high) {
            this.high = high;
        }

        public String getLow() {
            return low;
        }

        public void setLow(String low) {
            this.low = low;
        }

        public String getClose() {
            return close;
        }

        public void setClose(String close) {
            this.close = close;
        }

        public String getChange() {
            return change;
        }

        public void setChange(String change) {
            this.change = change;
        }

        public String getPercent_change() {
            return percent_change;
        }

        public void setPercent_change(String percent_change) {
            this.percent_change = percent_change;
        }

        public String getVolume() {
            return volume;
        }

        public void setVolume(String volume) {
            this.volume = volume;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Boolean getIs_market_open() {
            return is_market_open;
        }

        public void setIs_market_open(Boolean is_market_open) {
            this.is_market_open = is_market_open;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public QuoteError getError() {
            return error;
        }

        public void setError(QuoteError error) {
            this.error = error;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuoteError {
        private String code;
        private String message;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
