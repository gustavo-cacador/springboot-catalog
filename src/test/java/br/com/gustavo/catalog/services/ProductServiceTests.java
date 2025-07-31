package br.com.gustavo.catalog.services;

import br.com.gustavo.catalog.dto.ProductDTO;
import br.com.gustavo.catalog.entities.Category;
import br.com.gustavo.catalog.entities.Product;
import br.com.gustavo.catalog.repositories.CategoryRepository;
import br.com.gustavo.catalog.repositories.ProductRepository;
import br.com.gustavo.catalog.services.exceptions.DatabaseException;
import br.com.gustavo.catalog.services.exceptions.InvalidDataException;
import br.com.gustavo.catalog.services.exceptions.ResourceNotFoundException;
import br.com.gustavo.catalog.tests.Factory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private long existingId;
    private long nonExistingId;
    private long dependentId;
    private PageImpl<Product> page;
    private Product product;
    private Category category;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;
        product = Factory.createProduct();
        category = Factory.createCategory();
        productDTO = Factory.creatProductDTO();
        page = new PageImpl<>(List.of(product));

        // simulando comportamento para buscar produtos paginados
        Mockito.when(productRepository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);

        // simulando comportamento para salvar um produto
        Mockito.when(productRepository.save(ArgumentMatchers.any())).thenReturn(product);

        // simulando comportamento para buscar produto existente e inexistente por id
        Mockito.when(productRepository.findById(existingId)).thenReturn(Optional.of(product));
        Mockito.when(productRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        Mockito.when(productRepository.getReferenceById(existingId)).thenReturn(product);
        Mockito.when(productRepository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

        Mockito.when(categoryRepository.getReferenceById(existingId)).thenReturn(category);
        Mockito.when(categoryRepository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

        Mockito.doNothing().when(productRepository).deleteById(existingId);
        Mockito.doThrow(DataIntegrityViolationException.class).when(productRepository).deleteById(dependentId);

        // se existe id eu retorno true
        Mockito.when(productRepository.existsById(existingId)).thenReturn(true);
        // se n existe id eu retorno false
        Mockito.when(productRepository.existsById(nonExistingId)).thenReturn(false);

        Mockito.when(productRepository.existsById(dependentId)).thenReturn(true);
    }

    // update deveria lançar ResourceNotFoundException quando o id não existir
    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            productService.update(nonExistingId, productDTO);
        });
    }

    // update deveria retornar um ProductDTO quando o id existir
    @Test
    public void updateShoudlReturnProductDTOWhenIdExists() {

        ProductDTO result = productService.update(existingId, productDTO);

        Assertions.assertNotNull(result);
    }

    // findById deveria lançar ResourceNotFoundException quando o id não existir
    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            productService.findById(nonExistingId);
        });

        verify(productRepository).findById(nonExistingId);
    }

    // findById deveria retornar um ProductDTO quando o id existir
    @Test
    public void findByIdShoudlReturnProductDTOWhenIdExists() {

        ProductDTO result = productService.findById(existingId);

        Assertions.assertNotNull(result);
        verify(productRepository).findById(existingId);
    }

    @Test
    public void findAllPagedShouldReturnPage() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<ProductDTO> result = productService.findAllPaged(pageable);

        Assertions.assertNotNull(result);
        verify(productRepository).findAll(pageable);
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
        verify(productRepository, Mockito.times(1)).deleteById(existingId);
    }

    // teste para inserir quando a verificação dos dados forem válidos
    @Test
    public void insertShouldReturnProductDTOWhenValidData() {

        // usamos o mockito spy nesse caso pois nos precisamos mockar o metodo "validateData" para testar o nosso serviço
        ProductService serviceSpy = Mockito.spy(productService);
        Mockito.doNothing().when(serviceSpy).validateData(productDTO);

        ProductDTO result = serviceSpy.insert(productDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getName(), "Phone");
    }

    @Test
    public void insertShouldReturnInvalidDataExceptionWhenProductNameIsBlank() {

        productDTO.setName("");

        ProductService serviceSpy = Mockito.spy(productService);
        Mockito.doThrow(InvalidDataException.class).when(serviceSpy).validateData(productDTO);

        Assertions.assertThrows(InvalidDataException.class, () -> {
            ProductDTO result = serviceSpy.insert(productDTO);
        });
    }

    @Test
    public void insertShouldReturnInvalidDataExceptionWhenProductPriceIsNegativeOrZero() {

        productDTO.setPrice(-10.0);

        ProductService serviceSpy = Mockito.spy(productService);
        Mockito.doThrow(InvalidDataException.class).when(serviceSpy).validateData(productDTO);

        Assertions.assertThrows(InvalidDataException.class, () -> {
            ProductDTO result = serviceSpy.insert(productDTO);
        });
    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExistsAndValidData() {

        ProductService serviceSpy = Mockito.spy(productService);
        Mockito.doNothing().when(serviceSpy).validateData(productDTO);

        ProductDTO result = serviceSpy.update(existingId, productDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), existingId);
    }

    @Test
    public void updateShouldReturnInvalidDataExceptionWhenIdExistsAndProductNameIsBlank() {

        productDTO.setName("");

        ProductService serviceSpy = Mockito.spy(productService);
        Mockito.doThrow(InvalidDataException.class).when(serviceSpy).validateData(productDTO);

        Assertions.assertThrows(InvalidDataException.class, () -> {
            ProductDTO result = serviceSpy.update(existingId, productDTO);
        });
    }

    @Test
    public void updateShouldReturnInvalidDataExceptionWhenIdExistsAndProductPriceIsNegativeOrZero() {

        productDTO.setPrice(-12.0);

        ProductService serviceSpy = Mockito.spy(productService);
        Mockito.doThrow(InvalidDataException.class).when(serviceSpy).validateData(productDTO);

        Assertions.assertThrows(InvalidDataException.class, () -> {
            ProductDTO result = serviceSpy.update(existingId, productDTO);
        });
    }
}
