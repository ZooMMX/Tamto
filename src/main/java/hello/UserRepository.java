package hello;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 21/11/14
 * Time: 13:35
 */
public interface UserRepository extends CrudRepository<User, String> {
    User findByUsername(String username);
    List<User> findByOrderByUsernameAsc();

    @Query("SELECT u FROM User u WHERE u.enabled = 1 ORDER BY u.fullname ASC")
    public List<User> findActiveUsers();

    @Query("SELECT u FROM User u WHERE u.enabled = 0 ORDER BY u.fullname ASC")
    public List<User> findInactiveUsers();
}
