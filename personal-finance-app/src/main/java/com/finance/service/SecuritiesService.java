package com.finance.service;

import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Fetches list of securities (stocks, ETFs, mutual funds) from NSE/BSE via Groww's instrument CSV.
 * Used for investment name autocomplete on the Add Investment page.
 */
@Service
public class SecuritiesService {

    private static final String INSTRUMENTS_CSV_URL = "https://growwapi-assets.groww.in/instruments/instrument.csv";

    private final RestTemplate restTemplate;
    private List<SecurityItem> allSecurities = Collections.emptyList();
    private long lastFetchTime = 0;
    private static final long CACHE_VALID_MS = 24 * 60 * 60 * 1000; // 24 hours

    public SecuritiesService() {
        this.restTemplate = new RestTemplate();
    }

    @PostConstruct
    public void loadSecurities() {
        fetchAndParseCsv();
    }

    @Scheduled(cron = "0 0 6 * * *") // Refresh daily at 6 AM
    public void refreshSecurities() {
        fetchAndParseCsv();
    }

    private void fetchAndParseCsv() {
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.setAccept(Collections.singletonList(org.springframework.http.MediaType.parseMediaType("text/csv")));
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                    INSTRUMENTS_CSV_URL,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                allSecurities = parseCsv(response.getBody());
                lastFetchTime = System.currentTimeMillis();
                System.out.println("Securities list updated: " + allSecurities.size() + " instruments");
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch securities CSV: " + e.getMessage());
            if (allSecurities.isEmpty()) {
                allSecurities = getFallbackSecurities();
                System.out.println("Using fallback securities list: " + allSecurities.size() + " items");
            }
        }
    }

    private List<SecurityItem> parseCsv(String csv) {
        List<SecurityItem> result = new ArrayList<>();
        String[] lines = csv.split("\n");
        if (lines.length < 2) return result;

        String headerLine = lines[0];
        Map<String, Integer> columnIndex = new HashMap<>();
        String[] headers = parseCsvLine(headerLine);
        for (int i = 0; i < headers.length; i++) {
            columnIndex.put(headers[i].trim().toLowerCase(), i);
        }

        int symbolIdx = columnIndex.getOrDefault("trading_symbol", columnIndex.getOrDefault("groww_symbol", 2));
        int nameIdx = columnIndex.getOrDefault("name", 4);
        int segmentIdx = columnIndex.getOrDefault("segment", -1);
        int exchangeIdx = columnIndex.getOrDefault("exchange", 0);
        int typeIdx = columnIndex.getOrDefault("instrument_type", -1);

        Set<String> seen = new HashSet<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) continue;
            String[] cols = parseCsvLine(line);
            if (cols.length <= Math.max(symbolIdx, nameIdx)) continue;

            String symbol = getColumn(cols, symbolIdx);
            String name = getColumn(cols, nameIdx);
            String segment = segmentIdx >= 0 ? getColumn(cols, segmentIdx) : "";
            String exchange = exchangeIdx >= 0 ? getColumn(cols, exchangeIdx) : "";
            String type = typeIdx >= 0 ? getColumn(cols, typeIdx) : "";

            if (symbol == null || symbol.isBlank() || name == null || name.isBlank()) continue;

            String key = (exchange + "|" + symbol).toLowerCase();
            if (seen.contains(key)) continue;
            seen.add(key);

            String segLower = segment.toLowerCase();
            String typeLower = type.toLowerCase();
            boolean isEtf = segLower.contains("etf") || typeLower.contains("etf");
            boolean isMf = segLower.contains("mf") || typeLower.contains("mutual") || name.toLowerCase().contains("mutual fund");
            String displayType = isEtf ? "ETF" : (isMf ? "MF" : "EQ");
            result.add(new SecurityItem(symbol.trim(), name.trim(), displayType, exchange.trim()));
        }
        return result;
    }

    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString().trim());
        return values.toArray(new String[0]);
    }

    private String getColumn(String[] cols, int idx) {
        if (idx < 0 || idx >= cols.length) return "";
        String s = cols[idx];
        return s == null ? "" : s.replace("\"", "").trim();
    }

    /**
     * Search securities by symbol or name (case-insensitive, contains).
     * Returns at most 50 matches.
     */
    public List<SecurityItem> search(String query) {
        if (query == null || query.trim().length() < 2) {
            return Collections.emptyList();
        }
        String q = query.trim().toLowerCase();
        return allSecurities.stream()
                .filter(s -> s.symbol().toLowerCase().contains(q) || s.name().toLowerCase().contains(q))
                .limit(50)
                .collect(Collectors.toList());
    }

    public List<SecurityItem> searchWithMinChars(String query, int minChars) {
        if (query == null || query.trim().length() < minChars) return Collections.emptyList();
        return search(query);
    }

    private List<SecurityItem> getFallbackSecurities() {
        // Minimal fallback if CSV fetch fails (e.g. network or URL change)
        return List.of(
                new SecurityItem("RELIANCE", "Reliance Industries Ltd", "EQ", "NSE"),
                new SecurityItem("TCS", "Tata Consultancy Services Ltd", "EQ", "NSE"),
                new SecurityItem("HDFCBANK", "HDFC Bank Ltd", "EQ", "NSE"),
                new SecurityItem("INFY", "Infosys Ltd", "EQ", "NSE"),
                new SecurityItem("NIFTY 50", "Nifty 50 ETF", "ETF", "NSE")
        );
    }

    public record SecurityItem(String symbol, String name, String type, String exchange) {}
}
