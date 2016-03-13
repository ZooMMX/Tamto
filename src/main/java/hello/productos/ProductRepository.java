package hello.productos;

import hello.Pieza;
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

}
