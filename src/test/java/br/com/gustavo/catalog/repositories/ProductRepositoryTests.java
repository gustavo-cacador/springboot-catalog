package br.com.gustavo.catalog.repositories;

import br.com.gustavo.catalog.entities.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

@DataJpaTest
public class ProductRepositoryTests {

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void deleteShouldDeleteObjectWhenIdExists() {

        productRepository.deleteById(1L);

        Optional<Product> result = productRepository.findById(1L);

        Assertions.assertFalse(result.isPresent());
    }
}
