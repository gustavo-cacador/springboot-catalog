package br.com.gustavo.catalog.repositories;

import br.com.gustavo.catalog.entities.Product;
import br.com.gustavo.catalog.projections.ProductProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // vamos buscar os produtos paginados por id, na ordem alfabetica pelo name
    // qnd estamos passando uma busca paginada, temos que obrigatoriamente passar um "countQuery"
    @Query(nativeQuery = true, value = """
            SELECT DISTINCT tb_product.id, tb_product.name
            FROM tb_product
            INNER JOIN tb_product_category ON tb_product_category.product_id = tb_product.id
            WHERE (:categoryIds IS NULL OR tb_product_category.category_id IN (:categoryIds))
            AND (LOWER(tb_product.name) LIKE LOWER(CONCAT('%',:name,'%')))
            ORDER BY tb_product.name
            """,
                    countQuery = """
            SELECT COUNT(*) FROM (
            SELECT DISTINCT tb_product.id, tb_product.name
            FROM tb_product
            INNER JOIN tb_product_category ON tb_product_category.product_id = tb_product.id
            WHERE (:categoryIds IS NULL OR tb_product_category.category_id IN (:categoryIds))
            AND (LOWER(tb_product.name) LIKE LOWER(CONCAT('%',:name,'%')))
            ) AS tb_result
            """)
    Page<ProductProjection> searchProducts(List<Long> categoryIds, String name, Pageable pageable);
}
