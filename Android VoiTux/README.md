Android
=========

L'application Android **VoiTux** permet de se connecter au serveur lancé sur la voiture. Une fois connectée il est possible de contrôler la voiture grâce à 2 Joysticks et de visualiser la vidéo en Streaming. 

Installer l'apk : [VoiTux-2.0-release.apk](VoiTux/VoiTux-2.0-release.apk)

> L'apk est généré pour les téléphones ARM. Comme j'utilise un galaxy s5. 

## Utilisations 

J'ai créé l'application VoiTux de façon à qu'elle puisse servir pour d'autres projets. Il y a donc plusieurs configurations possibles, notamment pour GStreamer. 
Pour `VoiTux_v6.py`  Voici la configuration par défaut:
- Créer une nouvelle connexion (Bouton **+**)
- Ajouter un nom. *ex: `Local`*
- Ajouter l'adresse ip de la voiture. *ex: `192.168.0.18`*
- Activer le la vidéo 
- Mode simple
- Vidéo Port : `5000`
- Sélectionner la pipeline `udpsrc ..`
- Activer la commande Joystick
- Port commande : `8881`
- Valider

Lancer le serveur sur la voiture. `sudo python VoiTux_v6.py`

> Voir la page consacré au serveur sur le Raspberry [VoiTux Raspberry](../Serveur VoiTux)  

Exécuter la connexion avec le bouton `Play`  
Si tout se passe bien vous devriez avoir le contrôle de la voiture :)

## Fonctionnement  

- Target SDK version : API 23: Android 6.0 (Marshmallow)
- Mini SDK version : API 15: Android 4.0.3 (IceCreamSandwich)

###1. Commande des servomoteurs 

Pour la commande de direction et vitesse j'utilise un code trouvé sur le net qui me permet de créer des joysticks tactiles.  Un horizontal pour la direction et un vertical pour la vitesse (marche avant/arrière).

Lors de l'ouverture d'une *connexion* le client Socket initialise une connexion au serveur python de la voiture. La position des joysticks est donc envoyée et contrôle la voiture. 
Les commandes envoyées par socket dans ce style : `<servo id='Y'>-10</servo>`  


###2. Streaming de la camera avec GStreamer

Partie plus complexe vue que pour utiliser GSteamer avec Android, il y a peu d'explication et presque pas de tuto. (et encore moins à jour)  

Si vous installez les sources penser à bien changer la valeur de `gstAndroidRoot` dans le `gradle.properties`. Et à télécharger le pkg pour android : [gstreamer.freedesktop.org/data/pkg/android](https://gstreamer.freedesktop.org/data/pkg/android)  
> J'ai utilisé la la version *gstreamer-1.0-android-universal-1.9.90*


Il est aussi nécessaire d’installer le NDK pour Android pour compiler GStreamer.

Mes sources :  
- (https://gstreamer.freedesktop.org/planet/)  Voir le poste de Septembre 06, 2016
- (https://arunraghavan.net/2016/09/gstreamer-on-android-and-universal-builds/)
- (https://cgit.freedesktop.org/~slomo/gst-sdk-tutorials/tree/gst-sdk/tutorials) pas très a jours met peut être utile

