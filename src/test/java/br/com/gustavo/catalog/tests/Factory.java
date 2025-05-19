package br.com.gustavo.catalog.tests;

import br.com.gustavo.catalog.dto.ProductDTO;
import br.com.gustavo.catalog.entities.Category;
import br.com.gustavo.catalog.entities.Product;

import java.time.Instant;

public class Factory {

    public static Product createProduct() {
        Product product = new Product(1L, "Phone", "Good Phone", 800.0, "https://img.com/img.png", Instant.parse("2020-10-20T03:00:00Z"));
        product.getCategories().add(new Category(2L, "Electronics"));
        return product;
    }

    public static ProductDTO creatProductDTO() {
        Product product = createProduct();
        return new ProductDTO(product, product.getCategories());
    }
}
