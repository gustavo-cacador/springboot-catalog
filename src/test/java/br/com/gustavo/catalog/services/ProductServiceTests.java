package br.com.gustavo.catalog.services;

import br.com.gustavo.catalog.repositories.ProductRepository;
import br.com.gustavo.catalog.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    private long existingId;
    private long nonExistingId;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 34L;

        Mockito.doNothing().when(productRepository).deleteById(existingId);

        Mockito.doThrow(EmptyResultDataAccessException.class).when(productRepository).deleteById(nonExistingId);

        // se existe id eu retorno true
        Mockito.when(productRepository.existsById(existingId)).thenReturn(true);
        // se n existe id eu retorno false
        Mockito.when(productRepository.existsById(nonExistingId)).thenReturn(false);
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
