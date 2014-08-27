Android
=========


L'application permet de communiquer avec des sockets afin de diriger les servos moteurs et de visualiser la camera en fond 




###1. Commande des servomoteurs 

Pour la commande des servopoteurs j'utilise un code trouver sur le net qui me permet de créé des Josticks. 
J'ai un peu adapter ce code à mon appli afin d'avoir 2 joystick ( un horizontale et un vertical ) pour la direction et la vitesse marche avant/arrière. 

J'envoie donc les commandes dans ce style : `<servo id='Y'>-10</servo>`




###2. Streaming de la camera avec GStreamer

Parti plus complexe vu que pour utiliser GSteamer avec android il y a peu d'explication et de tuto.
Le seul tuto que j'ai peut trouver et ici : http://docs.gstreamer.com/display/GstSDK/Android+tutorials
Qui ma permis de comprendre un peut GStreamer SDK. Mais ce tuto est réalisé avec une vertion de GStreamer datant de 2013 et qui ma poser des problems.

Pour avoir la derniére vertion : http://gstreamer.freedesktop.org/data/pkg/android/
Et pour avoir les tutoral avec la la vertion 1.0 : http://cgit.freedesktop.org/~slomo/gst-sdk-tutorials/tree/gst-sdk/tutorials

Note : Je n'ai pas  réussi à définir la variable `GSTREAMER_ROOT_ANDROID` avec eclipse donc je l'ai defini en dur dans le jini/Android.mk 

Pour l'appli VoiTux je me suis principalement inspiré du tutorial 3 : 
http://docs.gstreamer.com/display/GstSDK/Android+tutorial+3%3A+Video
