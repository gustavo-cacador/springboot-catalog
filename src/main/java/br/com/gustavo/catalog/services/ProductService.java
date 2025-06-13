package br.com.gustavo.catalog.services;

import br.com.gustavo.catalog.dto.CategoryDTO;
import br.com.gustavo.catalog.dto.ProductDTO;
import br.com.gustavo.catalog.entities.Category;
import br.com.gustavo.catalog.entities.Product;
import br.com.gustavo.catalog.projections.ProductProjection;
import br.com.gustavo.catalog.repositories.CategoryRepository;
import br.com.gustavo.catalog.repositories.ProductRepository;
import br.com.gustavo.catalog.services.exceptions.DatabaseException;
import br.com.gustavo.catalog.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(Pageable pageable) {
        Page<Product> list = productRepository.findAll(pageable);
        return list.map(ProductDTO::new);
    }

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Optional<Product> obj = productRepository.findById(id);
        Product product = obj.orElseThrow(() -> new ResourceNotFoundException("Categoria com id: " + id + ", não encontrada."));
        return new ProductDTO(product, product.getCategories());
    }

    @Transactional(readOnly = true)
    public Page<ProductProjection> testQuery(Pageable pageable) {
        return productRepository.searchProducts(Arrays.asList(1L, 3L), "", pageable);
    }

    @Transactional
    public ProductDTO insert(ProductDTO dto) {
        Product product = new Product();
        copyDtoToEntity(dto, product);
        product = productRepository.save(product);
        return new ProductDTO(product);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        try {
            var product = productRepository.getReferenceById(id);
            copyDtoToEntity(dto, product);
            product = productRepository.save(product);
            return new ProductDTO(product);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Categoria com id: " + id + ", não encontrado.");
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recurso não encontrado");
        }
        try {
            productRepository.deleteById(id);
        }
        catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Falha de integridade referencial");
        }
    }

    private void copyDtoToEntity(ProductDTO dto, Product entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setDate(dto.getDate());
        entity.setPrice(dto.getPrice());
        entity.setImgUrl(dto.getImgUrl());

        entity.getCategories().clear();
        for (CategoryDTO categoryDTO : dto.getCategories()) {
            var category = categoryRepository.getReferenceById(categoryDTO.getId());
            entity.getCategories().add(category);
        }
    }
}
