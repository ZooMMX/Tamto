package hello;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PiezaRepository extends PagingAndSortingRepository<Pieza, Long> {

    List<Pieza> findByDescripcion(String descripcion);

    /**
     * Devuelve clave, valor. Cuenta la cantidad de piezas de cada tipo en la BD.
     * @return
     */
    @Query("SELECT p.tipoPieza, count(p.tipoPieza) FROM Pieza p GROUP BY p.tipoPieza")
    List<Object[]> countByType();

    @Query("SELECT count(p) FROM Pieza p WHERE p.enabled = 1")
    public Long countEnabled();
}