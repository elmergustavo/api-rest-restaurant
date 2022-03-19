package url.api.restaurantapi.models.services;

import url.api.restaurantapi.models.entity.Cliente;

import java.util.List;

public interface IClienteService {
    public List<Cliente> findAll();
}
