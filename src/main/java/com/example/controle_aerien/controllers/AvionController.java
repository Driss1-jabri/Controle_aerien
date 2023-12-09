package com.example.controle_aerien.controllers;

import com.example.controle_aerien.entities.Aeroport;
import com.example.controle_aerien.entities.Avion;
import com.example.controle_aerien.services.AvionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class AvionController {
    @Autowired
    AvionService avionservice;

    @GetMapping("/avions")
    public List<Avion> getAllAvions(){
        return avionservice.getALLAvions();
    }
    @GetMapping("/avion/{id}")
    public ResponseEntity<Avion> getAviontById(@PathVariable Long id)
    {
        Avion avion = avionservice.getAvionById(id);
        if(avion!=null)
            return ResponseEntity.ok(avion);//200 OK
        else
            return ResponseEntity.notFound().build();//404 NOT FOUND
    }

    @PostMapping("/create_avion")
    public void saveAvion(@RequestBody Avion avion)
    {
        avionservice.saveAvion(avion);
    }
    @DeleteMapping ("/delete_avion/{id}")
    public void deleteAvionById(@PathVariable Long id)
    {
        avionservice.deleteAvionByID(id);
    }
}





