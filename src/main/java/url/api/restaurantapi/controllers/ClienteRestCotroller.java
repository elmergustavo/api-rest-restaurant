package url.api.restaurantapi.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import url.api.restaurantapi.models.entity.Cliente;
import url.api.restaurantapi.models.services.IClienteService;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:4200"})
@RestController
@RequestMapping("/api")
public class ClienteRestCotroller {

    @Autowired
    private IClienteService clienteService;
    private final Logger log = LoggerFactory.getLogger(ClienteRestCotroller.class);

    @GetMapping("/clientes")
    public List<Cliente> index(){
        return clienteService.findAll();
    }

    @GetMapping("/clientes/{id}")
    //@ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> show(@PathVariable Long id) {
        Cliente cliente = null;
        Map<String, Object> response = new HashMap<>();
        try {
            cliente = clienteService.findById(id);
        } catch (DataAccessException e) {
            response.put("mensaje", "Error al realizar la consulta en la base de datos");
            response.put("error", e.getMessage().concat(": ".concat(e.getMostSpecificCause().getMessage())));
            return new ResponseEntity<Map<String, Object>>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }


        if (cliente == null) {
            response.put("mensaje", "El cliente ID: ".concat(id.toString().
                    concat(" no existe en la base de datos!")));
            return new ResponseEntity<Map<String, Object>>(response,HttpStatus.NOT_FOUND);

        }
        return new ResponseEntity<Cliente>(cliente,HttpStatus.OK);
    }

    @PostMapping("/clientes")
    public ResponseEntity<?> create(@Validated @RequestBody Cliente cliente, BindingResult result) {

        Cliente clienteNew = null;
        Map<String, Object> response = new HashMap<>();

        if(result.hasErrors()) {

            List<String> errors = result.getFieldErrors()
                    .stream()
                    .map(err -> "El campo '" + err.getField() +"' "+ err.getDefaultMessage())
                    .collect(Collectors.toList());

            response.put("errors", errors);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            clienteNew = clienteService.save(cliente);
        } catch(DataAccessException e) {
            response.put("mensaje", "El Correo electronico ya existe en la base de datos");
            response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("mensaje", "El cliente ha sido creado con éxito!");
        response.put("cliente", clienteNew);
        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
    }


    @PutMapping("clientes/{id}")
    //@ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> update(@RequestBody Cliente cliente, @PathVariable Long id){
        Cliente clienteActual = clienteService.findById(id);
        Cliente clienteUpdated = null;
        Map<String, Object> response = new HashMap<>();

        if (clienteActual == null) {
            response.put("mensaje", "Error: no se puede editar, el cliente ID: ".concat(id.toString().
                    concat(" no existe en la base de datos!")));
            return new ResponseEntity<Map<String, Object>>(response,HttpStatus.NOT_FOUND);

        }

        try {
            clienteActual.setApellido(cliente.getApellido());
            clienteActual.setNombre(cliente.getNombre());
            clienteActual.setEmail(cliente.getEmail());
            clienteActual.setCreateAt(cliente.getCreateAt());

             clienteUpdated = clienteService.save(clienteActual);
        } catch ( DataAccessException e ) {
            response.put("mensaje", "El Correo electronico ya existe en la base de datos");
            response.put("error", e.getMessage().concat(": ".concat(e.getMostSpecificCause().getMessage())));
            return new ResponseEntity<Map<String, Object>>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }


        response.put("mensaje", "El cliente ha sido actualizado con exito!");
        response.put("cliente", clienteUpdated);
        return new ResponseEntity<Map<String, Object>>(response,HttpStatus.CREATED);
    }

    @DeleteMapping("/clientes/{id}")
    //@ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> delete(@PathVariable Long id){
        Map<String, Object> response = new HashMap<>();


        try {
            Cliente cliente = clienteService.findById(id);
            String nombreFotoAnterior = cliente.getFoto();
            if (nombreFotoAnterior !=null && nombreFotoAnterior.length() > 0){
                Path rutaFotoAnterior = Paths.get("uploads").resolve(nombreFotoAnterior).toAbsolutePath();
                File archivoFotoAnterior = rutaFotoAnterior.toFile();

                if (archivoFotoAnterior.exists() && archivoFotoAnterior.canRead()) {
                    archivoFotoAnterior.delete();
                }
            }
            clienteService.delete(id);
        } catch ( DataAccessException e ) {
            response.put("mensaje", "Error al eliminar el cliente en la base de datos");
            response.put("error", e.getMessage().concat(": ".concat(e.getMostSpecificCause().getMessage())));
            return new ResponseEntity<Map<String, Object>>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        response.put("mensaje", "El cliente eliminado con exito");
        return new ResponseEntity<Map<String, Object>>(response,HttpStatus.OK);
    }

    @PostMapping("/clientes/upload")
    public ResponseEntity<?> upload(@RequestParam ("archivo")MultipartFile archivo, @RequestParam("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        Cliente cliente = clienteService.findById(id);
        if (!archivo.isEmpty()){
            String nombreArchivo = UUID.randomUUID().toString() + "_" +archivo.getOriginalFilename().replace(" ","");
            Path rutaArchivo = Paths.get("uploads").resolve(nombreArchivo).toAbsolutePath();
            log.info(rutaArchivo.toString());
            try {
                Files.copy(archivo.getInputStream(), rutaArchivo);
            }  catch (IOException e){
                response.put("mensaje", "Error al subir la imagen del cliente" + nombreArchivo);
                response.put("error", e.getMessage().concat(": ".concat(e.getCause().getMessage())));
                return new ResponseEntity<Map<String, Object>>(response,HttpStatus.INTERNAL_SERVER_ERROR);

            }
            String nombreFotoAnterior = cliente.getFoto();
            if (nombreFotoAnterior !=null && nombreFotoAnterior.length() > 0){
                Path rutaFotoAnterior = Paths.get("uploads").resolve(nombreFotoAnterior).toAbsolutePath();
                File archivoFotoAnterior = rutaFotoAnterior.toFile();

                if (archivoFotoAnterior.exists() && archivoFotoAnterior.canRead()) {
                    archivoFotoAnterior.delete();
                }
            }

            cliente.setFoto(nombreArchivo);
            clienteService.save(cliente);
            response.put("cliente", cliente);
            response.put("Mensaje", "Has subido correctamente la imagen" + nombreArchivo);
        }
        return new ResponseEntity<Map<String, Object>>(response,HttpStatus.CREATED);
    }


    @GetMapping("/uploads/img/{nombreFoto:.+}")
    public ResponseEntity<org.springframework.core.io.Resource> verFoto(@PathVariable String nombreFoto){

        Path rutaArchivo = Paths.get("uploads").resolve(nombreFoto).toAbsolutePath();
       log.info(rutaArchivo.toString());

        org.springframework.core.io.Resource recurso = null;

        try {
            recurso = new UrlResource(rutaArchivo.toUri());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if(!recurso.exists() && !recurso.isReadable()) {
            throw new RuntimeException("Error no se pudo cargar la imagen: " + nombreFoto);
        }
        HttpHeaders cabecera = new HttpHeaders();
        cabecera.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"");

        return new ResponseEntity<Resource>(recurso, cabecera, HttpStatus.OK);
    }
}
