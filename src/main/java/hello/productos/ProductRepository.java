package hello.productos;

import hello.Pieza;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by octavioruiz on 23/02/16.
 */
public interface ProductRepository extends PagingAndSortingRepository<Product, Long> {
    public Product findByCode(String code);

    @Query("SELECT p FROM Product p WHERE p.code LIKE %:searchTerm% OR p.name LIKE %:searchTerm%")
    public Page<Product> search(@Param("searchTerm") String searchTerm, Pageable pageable);
}
