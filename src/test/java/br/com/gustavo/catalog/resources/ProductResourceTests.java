package br.com.gustavo.catalog.resources;

import br.com.gustavo.catalog.dto.ProductDTO;
import br.com.gustavo.catalog.services.ProductService;
import br.com.gustavo.catalog.services.exceptions.ResourceNotFoundException;
import br.com.gustavo.catalog.tests.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductResource.class)
public class ProductResourceTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private Long existingId;
    private Long nonExistingId;
    private ProductDTO productDTO;
    private PageImpl<ProductDTO> page;

    @BeforeEach
    void setUp() throws Exception {

        existingId = 1L;
        nonExistingId = 2L;

        productDTO = Factory.creatProductDTO();
        page = new PageImpl<>(List.of(productDTO));

        when(productService.findAllPaged(any())).thenReturn(page);

        // simulando o comportamento do id existente
        when(productService.findById(existingId)).thenReturn(productDTO);

        // simulando o comportamento do id não existente
        when(productService.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
    }

    // findAll do meu controller deveria retornar uma pagina de Produtos
    @Test
    public void findAllShouldReturnPage()  throws Exception{

        mockMvc.perform(get("/products")).andExpect(status().isOk());
    }

    // outra forma de fazer (legibilidade)
    @Test
    public void findAllShouldReturnPage2()  throws Exception{

        ResultActions result =
                mockMvc.perform(get("/products")
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk());
    }

    // findById deveria retornar um produto pelo id qnd id for existente
    @Test
    public void findByIdShouldReturnProductWhenIdExists() throws Exception{

        ResultActions result =
                mockMvc.perform(get("/products/{id}", existingId)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.id").exists());
        result.andExpect(jsonPath("$.name").exists());
        result.andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {

        ResultActions result =
                mockMvc.perform(get("/products/{id}", nonExistingId)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound());
    }
}
