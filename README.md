# ENSI-SmartPark
Le système de parking intelligent en temps réel à déployer sur le campus de La Manouba se compose de trois éléments:


1- Partie mobile : Les utilisateurs utilise l'application mobile pour avoir toutes les informations relatives aux places vacantes du parking.

2- Partie Arduino: chaque place de parking représente une noeuds périphériques :
  Ils sont composés par une Carte Arduino, Module Xbee, Capteurs Ultrason, Shield Xbee et une source d’alimentation.
(a) Carte Arduino Uno : Pour assurer le fonctionnement du programme stocké.
(b) Shield Xbee + Module Xbee : Pour assurer la communication entre les noeud périphériques et le noeud central.
(c) Capteur Ultrason : Nous renseigne sur la présence ou l’absence d’une voiture. Il renvoi 1 binaire si la place est occupé

3- Un script Python est lancé en permanance sur un serveur qui reçoit les informations de la noeud centrale et les envoie vers le serveur central.
