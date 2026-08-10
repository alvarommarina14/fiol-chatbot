package com.fiol.chatbot.client;

import com.fiol.chatbot.dto.Product;

import java.util.List;

public interface CatalogClient {

    List<Product> search(String query);
}
