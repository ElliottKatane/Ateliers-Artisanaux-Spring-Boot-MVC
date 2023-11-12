package com.ateliersartisanaux.Ateliers.Artisanaux.Spring.Boot.MVC.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ateliersartisanaux.Ateliers.Artisanaux.Spring.Boot.MVC.models.Atelier;

@Repository
public interface AtelierRepository extends JpaRepository<Atelier, Long> {

    // Exemple de méthode pour rechercher tous les ateliers par un certain critère (remplacez le nom de la méthode et le type de critère)
    List<Atelier> findByIdAtelier(String idAtelier);

    // Exemple de méthode pour rechercher un atelier par son nom (remplacez le nom de la méthode et le type de critère)
    Atelier findByNomAtelier(String nomAtelier);

}
