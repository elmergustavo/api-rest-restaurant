package url.api.restaurantapi.models.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import url.api.restaurantapi.models.dao.IClienteDao;
import url.api.restaurantapi.models.entity.Cliente;

import java.util.List;

@Service
public class ClienteServiceImpl implements  IClienteService{

    @Autowired
    private IClienteDao clienteDao;

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> findAll() {
        return (List<Cliente>) clienteDao.findAll();
    }
}
