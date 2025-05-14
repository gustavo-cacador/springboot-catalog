package br.com.gustavo.catalog.repositories;

import br.com.gustavo.catalog.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
