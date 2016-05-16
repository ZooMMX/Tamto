package hello;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 30/11/14
 * Time: 20:40
 */
public interface ArchivoRepository extends PagingAndSortingRepository<Archivo, Long> {
    @Query("SELECT a.id FROM Archivo a")
    List<Long> findAllIds();

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update Archivo archivo set archivo.categoria =:newCategory where archivo.id =:archivoId")
    void updateCategory(@Param("archivoId") Long archivoId, @Param("newCategory") Archivo.CategoriaArchivo newCategory);
}
