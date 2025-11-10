package iwaproject.transaction.repository;

import iwaproject.transaction.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {}