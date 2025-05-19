package br.com.gustavo.catalog.services;

import br.com.gustavo.catalog.entities.Product;
import br.com.gustavo.catalog.repositories.ProductRepository;
import br.com.gustavo.catalog.services.exceptions.DatabaseException;
import br.com.gustavo.catalog.services.exceptions.ResourceNotFoundException;
import br.com.gustavo.catalog.tests.Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    private long existingId;
    private long nonExistingId;
    private long dependentId;
    private PageImpl<Product> page;
    private Product product;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;
        product = Factory.createProduct();
        page = new PageImpl<>(List.of(product));

        // simulando comportamento para buscar produtos paginados
        Mockito.when(productRepository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);

        // simulando comportamento para salvar um produto
        Mockito.when(productRepository.save(ArgumentMatchers.any())).thenReturn(product);

        // simulando comportamento para buscar produto existente e inexistente por id
        Mockito.when(productRepository.findById(existingId)).thenReturn(Optional.of(product));
        Mockito.when(productRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        Mockito.doNothing().when(productRepository).deleteById(existingId);
        Mockito.doThrow(DataIntegrityViolationException.class).when(productRepository).deleteById(dependentId);

        // se existe id eu retorno true
        Mockito.when(productRepository.existsById(existingId)).thenReturn(true);
        // se n existe id eu retorno false
        Mockito.when(productRepository.existsById(nonExistingId)).thenReturn(false);

        Mockito.when(productRepository.existsById(dependentId)).thenReturn(true);
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenDependentId() {

        Assertions.assertThrows(DatabaseException.class, () -> {
            productService.delete(dependentId);
        });
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            productService.delete(nonExistingId);
        });
    }

    // o delete n faz nada quando o id existe
    @Test
    public void deleteShouldDoNothingWhenIdExists() {

        // n vai ter exceção nenhuma se eu chamar um delete com um id que existe
        Assertions.assertDoesNotThrow(() -> {
            productService.delete(existingId);
        });

        // verifica se o metodo deleteById foi chamado nessa ação do teste acima
        Mockito.verify(productRepository, Mockito.times(1)).deleteById(existingId);
    }
}
