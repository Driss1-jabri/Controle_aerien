package com.example.controle_aerien.services;

import com.example.controle_aerien.Djikstra.services.DjikstraImpl;
import com.example.controle_aerien.dao.AeroportRepository;
import com.example.controle_aerien.dao.VolRepository;
import com.example.controle_aerien.entities.Aeroport;
import com.example.controle_aerien.entities.Avion;
import com.example.controle_aerien.entities.Vol;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
@AllArgsConstructor

public class VolService {
    @Autowired
    private VolRepository volRepository;
    @Autowired
    private AeroportService aeroportService;
    @Autowired
    DjikstraImpl djik ;
    @Autowired
    private AvionService avionService;

    public void saveVol(Vol vol)
    {
        volRepository.save(vol);
    }
    public Vol getVolById(Long id)
    {
        return volRepository.findById(id).get();
    }
    public List<Vol> getAllVol()
    {
        return volRepository.findAll();
    }
    public void deleteVolById(Long Id)
    {
        volRepository.deleteById(Id);
    }

    public Vol AddAvionToVol(Vol vol)
    {
        Avion avion = vol.getAeroportDepart().getAvionsSol().get(0);
        vol.setAvion(avion);
        avion.setDisponibilite(false);
        return (vol);
    }

    public Vol AddAeroportToVol(Long idAeroDepart ,Long idAeroArrive,Vol vol)
    {
        //calculate temps d'arrivée(update later)

        Aeroport aeroportdepart = aeroportService.getAeroportById(idAeroDepart);
        Aeroport aeroportarrive = aeroportService.getAeroportById(idAeroArrive);

        if(aeroportdepart.getAvionsSol().size()!=0 && aeroportarrive.getNbPlaceSol() > aeroportarrive.getAvionsSol().size()  && aeroportdepart.isDisponibilite() && aeroportarrive.isDisponibilite() )
        {
            vol.setAeroportDepart(aeroportdepart);
            vol.setAeroportArrivee(aeroportarrive);
            return vol;
        }
        return null;
    }

    public Vol addVol(Long idAeroDepart ,Long idAeroArrive)
    {
        Vol vol = new Vol();
        vol = AddAeroportToVol(idAeroDepart,idAeroArrive,vol);
        //temps depart affectation

        if(vol != null) {
            vol = AddAvionToVol(vol);
            return volRepository.save(vol);
        }
        return null;
    }
    public void StartVol(Long id)
    {
        Vol vol = volRepository.findById(id).get();


        HashMap<String ,Integer> dji = djik.djisktraalgo(vol.getAeroportDepart().getId(),vol.getAeroportArrivee().getId());

        if (vol != null) {
            Long aeroportDepartId = vol.getAeroportDepart().getId();
            Long aeroportArriveeId = vol.getAeroportArrivee().getId();

            // Assuming avion has x and y coordinates
            double currentX = vol.getAvion().getPosition().getX();
            double currentY = vol.getAvion().getPosition().getY();
            vol.getAvion().setSpeed(400);
            Integer speed = vol.getAvion().getSpeed();
            while (!avionReachedDestination(currentX, currentY, aeroportArriveeId)) {
                // Update avion position and display progress
                speed = updateAvionPosition(vol,aeroportArriveeId,speed);
                displayProgress(vol);
                currentX=vol.getAvion().getPosition().getX();
                currentY=vol.getAvion().getPosition().getY();


                // Sleep for 1 second (1000 milliseconds)
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            while(avionReachedDestination(vol.getAvion().getPosition().getX(), vol.getAvion().getPosition().getY(), aeroportArriveeId)) {

                if(vol.getAeroportArrivee().getAvionsSol().size() < vol.getAeroportArrivee().getNbPlaceSol())
                {
                    vol.getAeroportArrivee().getAvionsVol().remove(vol.getAvion());
                    vol.getAeroportArrivee().getAvionsSol().add(vol.getAvion());
                    System.out.println("DESTIONATION ARRIVED !!!!! ");
                }
            }

        }
    }

    private void displayProgress(Vol vol) {
        // Display the avion's current position
        System.out.println("Avion Position: (" + vol.getAvion().getPosition().getX() + ", " + vol.getAvion().getPosition().getY() + ")");
    }

    private int updateAvionPosition(Vol vol, Long aeroportArriveeId, int speed) {
        Aeroport aeroportArrivee = aeroportService.getAeroportById(aeroportArriveeId);
        if (aeroportArrivee != null) {
            // Assuming a simple linear movement for demonstration purposes
            double deltaXA = aeroportArrivee.getPosition().getX() - vol.getAvion().getPosition().getX();
            double deltaXD = vol.getAeroportDepart().getPosition().getX() - vol.getAvion().getPosition().getX();
            double deltaYA = aeroportArrivee.getPosition().getY() - vol.getAvion().getPosition().getY();
            double deltaYD = vol.getAeroportDepart().getPosition().getY() - vol.getAvion().getPosition().getY();
            double newX;
            double newY;

            // Calculate distanceAvionArriv and direction
            double distanceAvionArriv = Math.sqrt(deltaXA * deltaXA + deltaYA * deltaYA);
            double distanceAvionDepart = Math.sqrt(deltaXD * deltaXD + deltaYD * deltaYD);

            /*if(distanceAvionArriv < 10)
            {
                 newX = vol.getAeroportArrivee().getPosition().getX();
                 newY = vol.getAeroportArrivee().getPosition().getY();
            }*/

                if (distanceAvionArriv < 50) {
                    // ATTERISSAGE
                    if (vol.getAeroportDepart().getAvionsVol().contains(vol.getAvion()))
                    {
                        System.out.println("ATTERISSAGE--------------------------");
                        // Your existing code...

                        // Remove avion from the current aeroport
                        vol.getAeroportDepart().getAvionsVol().remove(vol.getAvion());
                        aeroportService.saveAeroport(vol.getAeroportDepart());

                        // Set avion's new aeroport
                        vol.getAvion().setAeroport(vol.getAeroportArrivee());
                        avionService.saveAvion(vol.getAvion());

                        // Add avion to the new aeroport
                        vol.getAeroportArrivee().getAvionsVol().add(vol.getAvion());
                        aeroportService.saveAeroport(vol.getAeroportArrivee());
                    }
                    // Update speed
                    speed = speed - 20;
                    System.out.println(speed);
                }
            if (distanceAvionDepart < 50) {// DECOLAGE
                if (vol.getAeroportDepart().getAvionsSol().contains(vol.getAvion())) {
                    System.out.println("DECOLAGE--------------------------");

                    // Remove avion from the departure aeroport's ground
                    vol.getAeroportDepart().getAvionsSol().remove(vol.getAvion());

                    // Save changes to the departure aeroport
                    aeroportService.saveAeroport(vol.getAeroportDepart());

                    // Add avion to the departure aeroport's airborne list
                    vol.getAeroportDepart().getAvionsVol().add(vol.getAvion());

                    // Set the new aeroport for the avion
                    vol.getAvion().setAeroport(vol.getAeroportArrivee());

                    // Save changes to the avion
                    avionService.saveAvion(vol.getAvion());

                    // Save changes to the departure aeroport (again, in case the cascade type is not set properly)
                    aeroportService.saveAeroport(vol.getAeroportDepart());

                    System.out.println("SIZE avionsVol: " + vol.getAeroportDepart().getAvionsVol().size());
                    System.out.println("SIZE avionsSol: " + vol.getAeroportDepart().getAvionsSol().size());
                }

                speed = speed + 20;
                System.out.println(speed);
            }

                double directionX = deltaXA / distanceAvionArriv;
                double directionY = deltaYA / distanceAvionArriv;



                // Calculate the distanceAvionArriv to move based on speed (e.g., 100 km/h)
                double distanceAvionArrivToMove = speed / 60.0; // Convert speed to distanceAvionArriv per second


                // Calculate the new position
                newX = vol.getAvion().getPosition().getX() + (directionX * distanceAvionArrivToMove);
                newY = vol.getAvion().getPosition().getY() + (directionY * distanceAvionArrivToMove);



            // Update avion's position
            vol.getAvion().getPosition().setX((int)newX);
            vol.getAvion().getPosition().setY((int)newY);

            vol.getAvion().setSpeed(speed);
            avionService.saveAvion(vol.getAvion());
            System.out.println("distanceAvionArriv :" + distanceAvionArriv);
            System.out.println("NEW X : " + newX);
            System.out.println("NEW Y : " +newY);
            System.out.println(" Distance par second : " + distanceAvionArrivToMove + "KM/S");
            System.out.println("direc x : " + directionX);
            System.out.println("direc y : " + directionY);

        }
        return speed;
    }


    private boolean avionReachedDestination(double currentX, double currentY, Long aeroportArriveeId) {
        Aeroport aeroportArrivee = aeroportService.getAeroportById(aeroportArriveeId);
        return aeroportArrivee != null && (int) currentX == (int) aeroportArrivee.getPosition().getX() && (int) currentY == (int) aeroportArrivee.getPosition().getY();
    }


}
