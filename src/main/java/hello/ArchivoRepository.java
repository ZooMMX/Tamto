package hello;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 30/11/14
 * Time: 20:40
 */
public interface ArchivoRepository extends CrudRepository<Archivo, Long> {
    @Query("SELECT a.id FROM Archivo a")
    List<Long> findAllIds();
}
