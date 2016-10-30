Raspberry Pi
=========

Cette partie concerne uniquement le serveur sur le Raspberry. 

Le Raspberry est installé avec la distribution Rasbian (Jessie lite). 
Les différentes versions de "VoiTux_vX.py" sont différentes tentatives avec des méthodes et librairies différentes. Elles ne sont pas optimales, je les garde au cas ou ..
Exemple de librairie testé pour la commande du servomoteur : *ServoBlaster*, *RPIO*, *pigpio*

La bonne version et la "VoiTux_v6.py" qui utilise
-  `pigpio` pour la commande du servomoteur
- `GStreamer` en UDP pour la vidéo

### Installation de pigpio

Toutes les explications de son fonctionnement sont ici : [pigpio library](http://abyz.co.uk/rpi/pigpio/)

Installation rapide : 
> sudo apt-get install pigpio python-pigpio python3-pigpio


### Installation de GStreamer 

L'installation de GStreamer 1.0 est pas toujours évident 

Installation rapide : 
> sudo apt-get install python-gst-1.0 gstreamer1.0-plugins-good gstreamer1.0-plugins-ugly gstreamer1.0-plugins-bad gstreamer1.0-tools

Si cela ne marche pas voici un autre exemple d'installation : [Raspberry_Pi_Camera](http://wiki.oz9aec.net/index.php/Raspberry_Pi_Camera)

## VoiTux_v6.py


### Run
Pour lancer le serveur : (simple efficace)
> sudo python VoiTux_v6.py  


### Fonctionnement 
Je vais tenter de résumer le fonctionnement. Pour avoir les détailles, je vous laisse lire le code :) Ce n'est pas très long. 


Une fois le serveur lancé il initialise 
- un serveur socket
- le servomoteur pour la direction (`GPIO 4`) 
- le moteur pour la vitesse (marche avant `GPIO 27` et marche arrière `GPIO 22`)

Lors de la connexion d'un client un Client Socket est créée et la vidéo GStreamer en UDP est initialisé avec celui-ci. 


#### - Communication avec des socket
Les données attendus sont au format XML.  

Par exemple : `<servo id='X'>0</servo>`  
Par exemple : `<servo id='Y'>-10</servo>`  
Ici **X** signifie que la commande est pour la direction et **Y** pour la vitesse.  
Les valeurs **0** et **-10** sont des exemples de *commande*.  
Les *commandes* envoyées sont comprises entre `[-10,10]`  


#### - Conversion des *commandes* en PWM

Entrée : valeur entre `[-10,10]`  
Sortie : valeur entre un `[min,max]` différent. Il est donc nécessaire de faire une conversion linéaire. 

##### Pour la direction 

Le servomoteur est à calibrer car on n'utilise pas toutes son amplitude. 

Exemple 
> self.directionMinGauche  = 1300  # Valeur min (buté gauche)  (max max = 700)  
self.directionMaxDroite  = 1770  # Valeur max (buté droite)  (max max = 2500)


##### Pour la vitesse :

Il y a 2 pwm une pour la marche avant et une pour la marche arrière. La vitesse est commandée entre 0 et 100.  
Entre -10 et 0 c'est la marche arrière et entre 0 et 10 c'est la marche avant.




#### - Vidéo en Streaming

Pour récupérer la vidéo j'utilise `raspivid`  
Exemple de commande : `raspivid -t 0 -w 1280 -h 720 -fps 25 -b 2500000 -p 0,0,640,480 -o -` 

Autres valeurs :
- SD Low: -w 480 -h 260 -fps 25 -b  800000
- SD Medium: -w 640 -h 360 -fps 25 -b  1200000
- SD High: -w 960 -h 540 -fps 25 -b  1800000
- HD Ready: -w 1280 -h 720 -fps 25 -b  2500000
- Full HD: -w 1920 -h 1080 -fps 25 -b  5000000

Ensuite le flux vidéo est transmis par GStreamer qui va s'occuper du streaming vers notre machine cible (Android par exemple :) ).

Pour toute cette partie il y a des meilleures explications ici : 
- [blog.nicolargo.com/2013/05/streaming-depuis-la-raspberry-camera.html](http://blog.nicolargo.com/2013/05/streaming-depuis-la-raspberry-camera.html)
- [How to stream video and audio from a Raspberry Pi with no latency](http://blog.tkjelectronics.dk/2013/06/how-to-stream-video-and-audio-from-a-raspberry-pi-with-no-latency/)


##### Le meilleur compromis trouvé est en UDP :  
Commande complète pour l'émission :  
`raspivid -t 999999 -w 640 -h 360 -fps 25 -b 2000000 -p 0,0,640,480 -o - | gst-launch-1.0 -v fdsrc ! 'video/x-h264,width=640,height=480'" ! h264parse" ! queue" ! rtph264pay config-interval=1 pt=96" ! udpsink host=192.168.0.11 port=5000 &`  

- Host : `192.168.0.11` (exemple) C'est l'adresse du destinataire donc le client socket (Pour nous c'est Android)  
- Port : `5000` (exemple)  port utilisé pour la communication. 

Pour tester la réception il est possible d'utiliser cette commande :  
`gst-launch-1.0 -v udpsrc port=5000 caps="application/x-rtp, media=(string)video, clock-rate=(int)90000, encoding-name=(string)H264, payload=(int)96" ! queue ! rtph264depay ! avdec_h264 ! videoconvert ! autovideosink sync=false`




##### Autres solution en TCP :   
Commande complète pour l'émission :  
`raspivid -t 0 -w 1280 -h 720 -fps 25 -b 2500000 -p 0,0,640,480 -o - | gst-launch -v fdsrc ! h264parse ! rtph264pay config-interval=1 pt=96 ! gdppay ! tcpserversink host=192.168.1.48 port=5000`  
- Host : `192.168.1.48` (exemple) c'est l’adresse ip du Raspberry  
- Port : `5000` (exemple)  port utilisé pour la communication. 
    

Pour tester la réception il est possible d'utiliser cette commande :  
`gst-launch-1.0 -v tcpclientsrc host=192.168.1.48 port=5000 ! gdpdepay ! rtph264depay ! avdec_h264 ! videoconvert ! autovideosink sync=false`  
- Host : `192.168.1.48` (exemple) c'est l’adresse ip du raspberry  
- Port : `5000` (exemple)  port utilisé pour la communication.