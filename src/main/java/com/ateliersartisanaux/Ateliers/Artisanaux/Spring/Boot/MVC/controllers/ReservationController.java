package com.ateliersartisanaux.Ateliers.Artisanaux.Spring.Boot.MVC.controllers;


import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ateliersartisanaux.Ateliers.Artisanaux.Spring.Boot.MVC.models.Atelier;
import com.ateliersartisanaux.Ateliers.Artisanaux.Spring.Boot.MVC.models.CreneauHoraire;
import com.ateliersartisanaux.Ateliers.Artisanaux.Spring.Boot.MVC.models.Participant;
import com.ateliersartisanaux.Ateliers.Artisanaux.Spring.Boot.MVC.models.Reservation;
import com.ateliersartisanaux.Ateliers.Artisanaux.Spring.Boot.MVC.services.AtelierService;
import com.ateliersartisanaux.Ateliers.Artisanaux.Spring.Boot.MVC.services.CreneauHoraireService;
import com.ateliersartisanaux.Ateliers.Artisanaux.Spring.Boot.MVC.services.ParticipantService;
import com.ateliersartisanaux.Ateliers.Artisanaux.Spring.Boot.MVC.services.ReservationService;

import java.util.List;
import java.util.NoSuchElementException;

@Controller
public class ReservationController {

    private final ReservationService reservationService;
    private final ParticipantService participantService;
    private final AtelierService atelierService;
    private final CreneauHoraireService creneauHoraireService;

    public ReservationController(ReservationService reservationService, ParticipantService participantService, AtelierService atelierService, CreneauHoraireService creneauHoraireService) {
        this.reservationService = reservationService;
		this.participantService = participantService;
		this.atelierService = atelierService;
		this.creneauHoraireService = creneauHoraireService;
    }
    
    // le nom des endpoints est sensible à la casse
    // Obtenir la vue principale, mainPage.html
    @GetMapping("/")
    public String showMainPage() {
        return "mainPage";
    }
    // Obtenir la vue login, login.html
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
    // Obtenir la vue conditions.html
    @GetMapping("/conditions")
    public String showConditionsPage() {
        return "conditions";
    }
    @GetMapping("/cancel")
    public String showCancelReservationPage() {
        return "cancelReservation";
    }
    
    // Méthode de préparation du formulaire pour ajouter une réservation
    @GetMapping("/showReservationForm")
    // requestParam prend en paramètre un name qui doit être identique dans le formulaire, check reservationsByParticipant
    public String showMakeReservationPage(@RequestParam(name = "idParticipant", required = true) Long idParticipant, Model model) {
        // Utilisez l'idParticipant comme nécessaire
    	
    	// récupère la liste de tous les ateliers pour le formulaire (option/select)
    	List<Atelier> ateliers = atelierService.getAllAtelier();
    	// récupère les créneaux
    	List<CreneauHoraire> creneauxHoraire = creneauHoraireService.getAllCreneauHoraire();
    	
    	 // Créez un objet participant avec l'idParticipant
        Participant participant = new Participant();
        participant.setIdParticipant(idParticipant);
    	
        // Stocker les informations du participant, des ateliers, des créneaux
        model.addAttribute("idParticipant", idParticipant);
        model.addAttribute("participant", participant); // Use "participant" instead of "idParticipant"
        model.addAttribute("ateliers", ateliers);
        model.addAttribute("creneauxHoraire", creneauxHoraire);
        // Ajoutez d'autres logiques nécessaires ici

        return "showReservationForm";
    }

    @PostMapping("/makeReservation")
    public String makeReservation(@ModelAttribute("reservation") @DateTimeFormat(pattern = "MM/dd/yyyy") Reservation reservation, Model model) {
    	
    	  if (reservation.getParticipant() == null) {
    	        reservation.setParticipant(new Participant());
    	    }

        // Méthodes custom définies dans reservationService
        if (reservationService.isReservationUnique(reservation)) {
            reservationService.saveReservation(reservation);
            model.addAttribute("confirmationMessage", "Réservation réussie !");
        } else {
            model.addAttribute("error", "La réservation existe déjà pour cet atelier, ce créneau horaire et cette date.");
        }

        // Rechargez les données nécessaires pour le formulaire (si nécessaire)
        model.addAttribute("ateliers", atelierService.getAllAtelier());
        model.addAttribute("creneauxHoraire", creneauHoraireService.getAllCreneauHoraire());

        return "showReservationForm";
    }




    @GetMapping("/reservations")
    public String showReservations(Model model) {
        List<Reservation> reservations = reservationService.getAllReservations();
        model.addAttribute("reservations", reservations);
        return "reservations";
    }

    @GetMapping("/reservations/participant")
    public String getReservationsByParticipant(@RequestParam("email") String email, Model model) {
        // Vérifier si l'email est fourni
        if (email == null || email.isEmpty()) {
            // Gérer le cas où l'email n'est pas fourni
            return "redirect:/reservations"; // redirect
        }

    	// récupère la liste de tous les ateliers pour le formulaire (option/select)
    	List<Atelier> ateliers = atelierService.getAllAtelier();
    	// récupère les créneaux
    	List<CreneauHoraire> creneauxHoraire = creneauHoraireService.getAllCreneauHoraire();
        // Appeler le service pour obtenir les réservations par participant
        List<Reservation> reservations = reservationService.getReservationsByParticipantEmail(email);
        
        // Appeler le service pour obtenir les informations du participant
        Participant participant = participantService.getParticipantByEmail(email);
        


        // Ajouter les réservations au modèle pour les afficher dans la vue
        model.addAttribute("reservations", reservations);
        model.addAttribute("participant", participant);
        model.addAttribute("ateliers", ateliers);
        model.addAttribute("creneauxHoraire", creneauxHoraire);

        return "reservationsByParticipant"; // le nom de la vue Thymeleaf
    }
    
    // delete accessible depuis la liste des résa par participant
    @PostMapping("/reservations/delete")
    public String deleteReservation(@RequestParam("reservationId") Long reservationId) {
        // Vérifier si l'ID de réservation est fourni
        if (reservationId == null) {
            // Gérer le cas où l'ID n'est pas fourni
            // Rediriger ou afficher un message d'erreur, selon votre choix
            return "redirect:/reservations"; // par exemple
        }
        String email = "";
        // Assurez-vous d'ajuster la manière dont vous obtenez la réservation
        Reservation reservation = reservationService.getReservationById(reservationId);
            email = reservation.getParticipant().getEmailParticipant();
            
        // Supprimer la réservation de la base de données
        reservationService.deleteReservationById(reservationId);
     
        

        // Rediriger vers la page de réservations du participant après la suppression
        return "redirect:/reservations/participant?email=" + email;

    }
    // delete avec un id depuis la page cancel
    @PostMapping("/reservations/deleteById") 
    // RequestParam doit matcher le nom de l'attribut NAME dans le template Thymeleaf
    public String deleteReservationById(@RequestParam("ID") Long reservationId, Model model) {
        try {
            // Essayer de supprimer la réservation de la base de données
            reservationService.deleteReservationById(reservationId);

            // Set confirmation message
            model.addAttribute("confirmationMessage", "La réservation " + reservationId + " a été annulée avec succès.");
        } catch (NoSuchElementException e) {
            // Gérer le cas où la réservation avec l'ID spécifié n'existe pas
            model.addAttribute("confirmationMessage", "La réservation avec l'ID " + reservationId + " n'existe pas.");
        }

        // Rediriger vers la page des annulations
        return "cancelReservation";
    }
}

