package br.com.gustavo.catalog.services;

import br.com.gustavo.catalog.dto.ProductDTO;
import br.com.gustavo.catalog.repositories.ProductRepository;
import br.com.gustavo.catalog.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest

// nenhum teste vai influenciar nos outros testes, exemplo: teste delete n vai influenciar no teste findAllPaged, logo, o seed do banco de dados vai "resetar"
// quando influenciava, o teste findAllPaged retornava 24 produtos, mas agora com a annotation Transactional, vai retornar o padrão (25 produtos)
@Transactional
public class ProductServiceIntegracaoTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private Long existingId;
    private Long nonExistingId;
    private Long countTotalProducts;

    @BeforeEach
    void setUp() throws Exception {

        existingId = 1L;
        nonExistingId = 26L;
        countTotalProducts = 25L;
    }

    // teste para retornar produtos paginados (pagina 0, com tamanho de 10 (10 produtos por pagina))
    @Test
    public void findAllPagedShouldReturnPageWhenPage0Size10() {

        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<ProductDTO> result = productService.findAllPaged(pageRequest);

        // resultado eh vazio? não, ou seja, assertFalse correto
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(0, result.getNumber());
        Assertions.assertEquals(10, result.getSize());
        Assertions.assertEquals(countTotalProducts, result.getTotalElements());
    }

    // teste para verificar se retorna uma busca paginada inválida (50 paginas com 10 produtos cada pagina)
    @Test
    public void findAllPagedShouldReturnEmptyPageWhenPageDoesNotExist() {

        PageRequest pageRequest = PageRequest.of(50, 10);

        Page<ProductDTO> result = productService.findAllPaged(pageRequest);

        // resultado eh vazio? sim, pois eh impossivel ter 50 paginas com 10 produtos (temos 25 produtos só), logo, assertTrue correto
        Assertions.assertTrue(result.isEmpty());
    }

    // teste para verificar se está buscando produtos paginados pelo nome (ordem alfabetica)
    @Test
    public void findAllPagedShouldReturnSortedPageWhenSortByName() {

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name"));

        Page<ProductDTO> result = productService.findAllPaged(pageRequest);

        // ele n eh vazio, pois conseguimos sim ter 10 produtos em uma página só, logo, assertFalse que o resultado retorna vazio
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals("Macbook Pro", result.getContent().get(0).getName());
        Assertions.assertEquals("PC Gamer", result.getContent().get(1).getName());
        Assertions.assertEquals("PC Gamer Alfa", result.getContent().get(2).getName());
    }

    // teste para verificar se está realmente deletando produto com id existente, buscando os produtos do nosso seed do banco (import.sql)
    @Test
    public void deleteShouldDeleteResourceWhenIdExists() {

        productService.delete(existingId);

        Assertions.assertEquals(countTotalProducts - 1, productRepository.count());
    }

    // teste para retornar ResourceNotFoundException qnd id do produto n existir
    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            productService.delete(nonExistingId);
        });
    }
}
