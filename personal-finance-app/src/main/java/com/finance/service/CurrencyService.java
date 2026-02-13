package com.finance.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/INR";

    // Store rates: Key = Currency Code (e.g., "USD"), Value = Rate in INR
    private Map<String, BigDecimal> currentRates = new HashMap<>();
    private Map<String, BigDecimal> yesterdayRates = new HashMap<>();

    // Currencies to show in the dashboard cards (top 4)
    private static final List<String> CARD_CURRENCIES = List.of("USD", "EUR", "GBP", "CNY");

    // Fetch rates hourly - store ALL currencies from API for the converter
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void fetchRates() {
        try {
            Map<String, Object> response = restTemplate.getForObject(API_URL, Map.class);
            if (response != null && response.containsKey("rates")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> rates = (Map<String, Object>) response.get("rates");
                Map<String, BigDecimal> newRates = new HashMap<>();
                // INR = 1 (base)
                newRates.put("INR", BigDecimal.ONE);
                for (Map.Entry<String, Object> e : rates.entrySet()) {
                    String currency = e.getKey();
                    Object rateValue = e.getValue();
                    if (rateValue == null) continue;
                    
                    // Handle both Integer and Double (and other Number types)
                    double rateInForeign;
                    if (rateValue instanceof Number) {
                        rateInForeign = ((Number) rateValue).doubleValue();
                    } else if (rateValue instanceof String) {
                        try {
                            rateInForeign = Double.parseDouble((String) rateValue);
                        } catch (NumberFormatException ex) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    
                    if (rateInForeign == 0) continue;
                    // API gives 1 INR = X foreign → invert to 1 foreign = 1/X INR
                    BigDecimal inverseRate = BigDecimal.ONE.divide(
                            BigDecimal.valueOf(rateInForeign), 6, RoundingMode.HALF_UP);
                    newRates.put(currency, inverseRate);
                }
                if (currentRates.isEmpty()) {
                    yesterdayRates.putAll(newRates);
                }
                currentRates = newRates;
                System.out.println("Updated currency rates: " + currentRates.size() + " currencies");
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch currency rates: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Capture daily snapshot at midnight (for "yesterday's" comparison)
    @Scheduled(cron = "0 0 0 * * *")
    public void snapshotDailyRates() {
        if (!currentRates.isEmpty()) {
            yesterdayRates = new HashMap<>(currentRates);
        }
    }

    /** Returns data for dashboard cards (USD, EUR, GBP, CNY only). */
    public List<Map<String, Object>> getCurrencyData() {
        if (currentRates.isEmpty()) {
            fetchRates();
        }
        List<Map<String, Object>> data = new ArrayList<>();
        for (String code : CARD_CURRENCIES) {
            if (currentRates.containsKey(code)) {
                data.add(buildCurrencyItem(code, currentRates.get(code), yesterdayRates.getOrDefault(code, currentRates.get(code))));
            }
        }
        return data;
    }

    /** Returns all rates (for converter). Key = currency code, value = rate (1 unit = X INR). */
    public Map<String, BigDecimal> getAllRates() {
        if (currentRates.isEmpty()) {
            fetchRates();
        }
        return new HashMap<>(currentRates);
    }

    private Map<String, Object> buildCurrencyItem(String code, BigDecimal current, BigDecimal yesterday) {
        Map<String, Object> item = new HashMap<>();
        item.put("code", code);
        item.put("rate", current);
        BigDecimal change = BigDecimal.ZERO;
        if (yesterday != null && yesterday.compareTo(BigDecimal.ZERO) > 0) {
            change = current.subtract(yesterday).divide(yesterday, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        item.put("change", change);
        return item;
    }
}
