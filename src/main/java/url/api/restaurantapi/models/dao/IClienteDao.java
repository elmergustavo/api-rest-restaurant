package url.api.restaurantapi.models.dao;

import org.springframework.data.repository.CrudRepository;
import url.api.restaurantapi.models.entity.Cliente;

public interface IClienteDao extends CrudRepository<Cliente, Long> {


}
