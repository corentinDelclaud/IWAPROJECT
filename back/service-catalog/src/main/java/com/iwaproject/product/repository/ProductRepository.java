package com.iwaproject.product.repository;

import com.iwaproject.product.model.Game;
import com.iwaproject.product.model.Product;
import com.iwaproject.product.model.ServiceType;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends CrudRepository<Product, Integer> {

    // Trouver tous les services disponibles
    @Query("SELECT * FROM SERVICE")
    List<Product> findAll();

    // Trouver tous les services disponibles
    @Query("SELECT * FROM SERVICE WHERE IS_AVAILABLE = true")
    List<Product> findByIsAvailableTrue();

    // Trouver par jeu
    @Query("SELECT * FROM SERVICE WHERE GAME = :game")
    List<Product> findByGame(@Param("game") String game);

    // Trouver par type de service
    @Query("SELECT * FROM SERVICE WHERE SERVICE_TYPE = :serviceType")
    List<Product> findByServiceType(@Param("serviceType") String serviceType);

    // Trouver par jeu et type
    @Query("SELECT * FROM SERVICE WHERE GAME = :game AND SERVICE_TYPE = :serviceType")
    List<Product> findByGameAndServiceType(@Param("game") String game, @Param("serviceType") String serviceType);

    // Trouver par provider
    @Query("SELECT * FROM SERVICE WHERE ID_PROVIDER = :idProvider")
    List<Product> findByIdProvider(@Param("idProvider") String idProvider);

    // Trouver les services disponibles par jeu
    @Query("SELECT * FROM SERVICE WHERE GAME = :game AND IS_AVAILABLE = true")
    List<Product> findByGameAndIsAvailableTrue(@Param("game") String game);

    // Trouver les services disponibles par type
    @Query("SELECT * FROM SERVICE WHERE SERVICE_TYPE = :serviceType AND IS_AVAILABLE = true")
    List<Product> findByServiceTypeAndIsAvailableTrue(@Param("serviceType") String serviceType);

    //Trouver les services disponibles par jeu et/ou type et/ou fourchette de prix et/ou provider
    // exemple : /products/filter?game=GAME_NAME&serviceType=TYPE_NAME&minPrice=10.00&maxPrice=50.00&idProvider="1"
    @Query("SELECT * FROM SERVICE WHERE " +
            "(:game IS NULL OR GAME = :game) AND " +
            "(:type IS NULL OR SERVICE_TYPE = :type) AND " +
            "(:minPrice IS NULL OR PRICE >= :minPrice) AND " +
            "(:maxPrice IS NULL OR PRICE <= :maxPrice) AND " +
            "(:idProvider IS NULL OR ID_PROVIDER = :idProvider)")
    List<Product> findByFilters(
            @Param("game") String game,
            @Param("type") String type,
            @Param("minPrice") Float minPrice,
            @Param("maxPrice") Float maxPrice,
            @Param("idProvider") String idProvider
    );
}
