Android
=========


L'application permet de communiquer avec des sockets afin de diriger les servos moteurs et de visualiser la camera en fond 




###1. Commande des servomoteurs 

Pour la commande des servomoteurs j'utilise un code trouvé sur le net qui me permet de créer des joysticks. 
J'ai un peu adapteé ce code à mon appli afin d'avoir 2 joystick ( un horizontal et un vertical ) pour la direction et la vitesse marche avant/arrière. 

J'envoie donc les commandes dans ce style : `<servo id='Y'>-10</servo>`




###2. Streaming de la camera avec GStreamer

Partie plus complexe vu que pour utiliser GSteamer avec android il y a peu d'explication et de tuto.
Le seul tuto que j'ai pu trouver est ici : http://docs.gstreamer.com/display/GstSDK/Android+tutorials
Ce qui m'a permis de comprendre un peu GStreamer SDK. Mais ce tuto est réalisé avec une version de GStreamer datant de 2013 et qui m'a posé des problemes.

Pour avoir la dernière version : http://gstreamer.freedesktop.org/data/pkg/android/
Et pour avoir les tutoriels avec la version 1.0 : http://cgit.freedesktop.org/~slomo/gst-sdk-tutorials/tree/gst-sdk/tutorials

Note : Je n'ai pas  réussi à définir la variable `GSTREAMER_ROOT_ANDROID` avec eclipse donc je l'ai defini en dur dans le jini/Android.mk 

Pour l'appli VoiTux je me suis principalement inspiré du tutorial 3 : 
http://docs.gstreamer.com/display/GstSDK/Android+tutorial+3%3A+Video
