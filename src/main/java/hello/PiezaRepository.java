package hello;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PiezaRepository extends PagingAndSortingRepository<Pieza, Long> {

    List<Pieza> findByDescripcion(String descripcion);


}