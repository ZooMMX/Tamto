package hello.productos;

import hello.Pieza;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by octavioruiz on 23/02/16.
 */
public interface ProductRepository extends PagingAndSortingRepository<Product, Long> {
    public Product findByCode(String code);
}
