package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.ProduitGratuitException;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


@Api(description = "API pour es opérations CRUD sur les produits.")

@RestController
public class ProductController {

    @Autowired
    private ProductDao productDao;


    //Récupérer la liste des produits

    @RequestMapping(value = "/Produits", method = RequestMethod.GET)

    public MappingJacksonValue listeProduits() {

        Iterable<Product> produits = productDao.findAll();

        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");

        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);

        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);

        produitsFiltres.setFilters(listDeNosFiltres);

        return produitsFiltres;
    }

    //Calcule la différence  entre le prix d'achat et le prix de vente
    @RequestMapping(value = "/AdminProduits", method = RequestMethod.GET)
    public List<String> calculerMargeProduit() {

       List<Product> listeDeProduits = productDao.findAll();

       List<String> listeMarge = new ArrayList<>();

        for (Product produit : listeDeProduits) {
            int marge = produit.getPrix() - produit.getPrixAchat();
            listeMarge.add(produit.toString() + ": " + marge);
        }
        return listeMarge;
    }

    @GetMapping(value = "/ProduitsOrdre")
    public List<Product> trierProduitsParOrdreAlphabetique () {

        List<Product> listeDeProduitsEnOrdre = productDao.findByOrderByNom();

        return listeDeProduitsEnOrdre;
    }


    //Récupérer un produit par son Id
    @ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
    @GetMapping(value = "/Produits/{id}")
    public Product afficherUnProduit(@PathVariable int id) {

        Product produit = productDao.findById(id);

        if (produit == null)
            throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est INTROUVABLE. Écran Bleu si je pouvais.");

        return produit;
    }


    //ajouter un produit
    @PostMapping(value = "/Produits")
    public ResponseEntity<Void> ajouterProduit(@Valid @RequestBody Product product) {

        Product productAdded = productDao.save(product);

        if (productAdded == null)
            return ResponseEntity.noContent().build();

        if (productAdded.getPrix() == 0)
            throw new ProduitGratuitException("C'est pas gratuit mon coco! Faut veiller à pas trop me prendre pour un faisan!");

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping(value = "/Produits/{id}")
    public void supprimerProduit(@PathVariable int id) {

        productDao.delete(id);
    }

    @PutMapping(value = "/Produits")
    public void updateProduit(@RequestBody Product product) {

        productDao.save(product);
    }

    //Pour les tests
    @GetMapping(value = "test/produits/{prix}")
    public List<Product> testeDeRequetes(@PathVariable int prix) {

        return productDao.chercherUnProduitCher(400);
    }

}
