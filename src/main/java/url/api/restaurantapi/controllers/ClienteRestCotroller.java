package url.api.restaurantapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import url.api.restaurantapi.models.entity.Cliente;
import url.api.restaurantapi.models.services.IClienteService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ClienteRestCotroller {

    @Autowired
    private IClienteService clienteService;

    @GetMapping("/clientes")
    public List<Cliente> index(){
        return clienteService.findAll();
    }
}
