package br.com.gustavo.catalog.resources;

import br.com.gustavo.catalog.dto.ProductDTO;
import br.com.gustavo.catalog.services.ProductService;
import br.com.gustavo.catalog.services.exceptions.DatabaseException;
import br.com.gustavo.catalog.services.exceptions.ResourceNotFoundException;
import br.com.gustavo.catalog.tests.Factory;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductResource.class)
public class ProductResourceTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Long existingId;
    private Long nonExistingId;
    private Long dependentId;
    private ProductDTO productDTO;
    private PageImpl<ProductDTO> page;

    @BeforeEach
    void setUp() throws Exception {

        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;

        productDTO = Factory.creatProductDTO();
        page = new PageImpl<>(List.of(productDTO));

        when(productService.findAllPaged(any())).thenReturn(page);

        // simulando o comportamento para buscar com id com id existente
        when(productService.findById(existingId)).thenReturn(productDTO);

        // simulando o comportamento para buscar por id com id não existente
        when(productService.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);

        // simulando o comportamento para atualizar com id existente
        when(productService.update(eq(existingId), any())).thenReturn(productDTO);

        // simulando o comportamento para atualizar com id não existente
        when(productService.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);

        // simulando os 3 possíveis cenários do service.delete
        doNothing().when(productService).delete(existingId);
        doThrow(ResourceNotFoundException.class).when(productService).delete(nonExistingId);
        doThrow(DatabaseException.class).when(productService).delete(dependentId);
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

    // findById deveria retornar uma exceção qnd passamos um id não existente
    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {

        ResultActions result =
                mockMvc.perform(get("/products/{id}", nonExistingId)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound());
    }

    // update deveria retornar um produtoDTO quando passamos um id existente
    @Test
    public void updateShouldReturnProductDTOWhenIdExists() throws Exception {

        // convertendo um objeto java (produtoDTO) em string (formato json)
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result =
                mockMvc.perform(put("/products/{id}", existingId)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.id").exists());
        result.andExpect(jsonPath("$.name").exists());
        result.andExpect(jsonPath("$.description").exists());
    }

    // update deveria retornar uma exceção quando passamos um id não existente
    @Test
    public void updateShouldReturnONotFoundWhenIdDoesNotExists() throws Exception {

        // convertendo um objeto java (produtoDTO) em string (formato json)
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result =
                mockMvc.perform(put("/products/{id}", nonExistingId)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound());
    }
}
