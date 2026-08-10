package com.fiol.chatbot.client;

import com.fiol.chatbot.dto.Product;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryCatalogClientTest {

    private final InMemoryCatalogClient catalog = new InMemoryCatalogClient();

    @Test
    void givenCatalog_whenSearchingByExactCode_thenReturnsThatProductOnly() {
        List<Product> results = catalog.search("V002");

        assertThat(results).singleElement()
                .extracting(Product::name)
                .isEqualTo("Cuerdas de guitarra criolla");
    }

    @Test
    void givenCatalog_whenCodeCaseDiffers_thenReturnsSameResult() {
        assertThat(catalog.search("v002")).isEqualTo(catalog.search("V002"));
    }

    @Test
    void givenCatalog_whenSearchingByPartialName_thenReturnsAllMatchesSortedByName() {
        List<Product> results = catalog.search("cuerdas");

        // Sorted by name, so "Cuerdas de guitarra criolla" (V002) comes before
        // "Cuerdas de violin 4/4" (V001).
        assertThat(results)
                .extracting(Product::code)
                .containsExactly("V002", "V001");
    }

    @Test
    void givenCatalog_whenNameCaseDiffers_thenStillMatches() {
        assertThat(catalog.search("CUERDAS")).hasSize(2);
    }

    @Test
    void givenCatalog_whenSeveralProductsMatch_thenResultsAreSortedByName() {
        List<Product> results = catalog.search("violin");

        assertThat(results)
                .extracting(Product::name)
                .isSortedAccordingTo(String::compareTo);
    }

    @Test
    void givenCatalog_whenNothingMatches_thenReturnsEmptyList() {
        assertThat(catalog.search("saxofon")).isEmpty();
    }

    @Test
    void givenCatalog_whenQueryIsNullOrBlank_thenReturnsEmptyList() {
        assertThat(catalog.search(null)).isEmpty();
        assertThat(catalog.search("")).isEmpty();
        assertThat(catalog.search("   ")).isEmpty();
    }

    @Test
    void givenCatalog_whenQueryHasSurroundingSpaces_thenStillMatches() {
        assertThat(catalog.search("  V002  ")).hasSize(1);
    }
}
