package br.com.gustavo.catalog.repositories;

import br.com.gustavo.catalog.entities.Product;
import br.com.gustavo.catalog.tests.Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

@DataJpaTest
public class ProductRepositoryTests {

    @Autowired
    private ProductRepository productRepository;

    private long existingId;
    private long nonExistingId;
    private long countTotalProducts;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 25L;
        nonExistingId = 32L;
        countTotalProducts = 25L;
    }

    // retornar um Optional<Product> não vazio quando o id existir
    @Test
    public void findByIdShouldReturnOptionalWhenIdExists() {

        Optional<Product> result = productRepository.findById(existingId);

        // testa se o optional está presente (com o id que passamos)
        Assertions.assertTrue(result.isPresent());
    }

    // retornar um Optional<Product> vazio quando o id não existir
    @Test
    public void findByIdShouldReturnOptionalWhenIdNotExists() {

        Optional<Product> result = productRepository.findById(nonExistingId);

        // testa se o optional está vazio (com o id que passamos)
        Assertions.assertTrue(result.isEmpty());
    }

    // teste para ver se o save está persistindo os objetos e incrementando o id (criando produto e incrementando o id automaticamente)
    @Test
    public void saveShouldPersistWithAutoincrementWhenIdIsNull() {

        Product product = Factory.createProduct();
        product.setId(null);

        product = productRepository.save(product);

        Assertions.assertNotNull(product.getId());
        Assertions.assertEquals(countTotalProducts + 1, product.getId());
    }

    @Test
    public void deleteShouldDeleteObjectWhenIdExists() {

        // deleta o produto existente
        productRepository.deleteById(existingId);

        // procura o produto pelo id
        Optional<Product> result = productRepository.findById(existingId);

        // retorna falso pois deletamos o id
        Assertions.assertFalse(result.isPresent());
    }
}