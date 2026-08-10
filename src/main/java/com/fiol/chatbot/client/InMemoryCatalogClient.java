package com.fiol.chatbot.client;

import com.fiol.chatbot.dto.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Component
public class InMemoryCatalogClient implements CatalogClient {

    private static final List<Product> CATALOG = List.of(
            new Product("V001", "Cuerdas de violin 4/4 (juego completo)", new BigDecimal("18500.00")),
            new Product("V002", "Cuerdas de guitarra criolla", new BigDecimal("8200.00")),
            new Product("A010", "Arco para violin 4/4", new BigDecimal("45000.00")),
            new Product("R005", "Resina para arco (colofonia)", new BigDecimal("3500.00")),
            new Product("F020", "Funda rigida para violin", new BigDecimal("32000.00")));

    @Override
    public List<Product> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String trimmed = query.trim();

        return CATALOG.stream()
                .filter(p -> p.code().equalsIgnoreCase(trimmed))
                .findFirst()
                .map(List::of)
                .orElseGet(() -> CATALOG.stream()
                        .filter(p -> p.name().toLowerCase().contains(trimmed.toLowerCase()))
                        .sorted(Comparator.comparing(Product::name))
                        .toList());
    }
}
