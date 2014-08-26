VoiTux
===================
**Bouh, !** 
Bienvenu, sur mon projet de voiture télécommander avec un Raspberry Pi.
Je réalise ce projet pour le fun et le divertissement.
Je vais mettre ici mes sources ainsi que mes notes personnel. C'est pourquoi je m'excuse d'avance pour les fautes d'orthographe et phrase incompréhensible. 


##Matériel 
La voiture est munie de 2 servomoteurs :
	 1.  Vitesse + marche avant et marche arrière
	 2. Direction 
Le Raspberry Pi est un modèle B rev2 avec une clef Wifi et la Pi Camera
PC avec linux (Ubuntu 14.04)

##Objectif
Commander une petite voiture avec un Raspberry Pi via un PC ou un téléphone Android 
Développer 3 programmes :
 1. **Raspberry Pi :** Commande les servomoteurs et transmet en streaming la vidéo de la caméra.
 2. **PC (Ubuntu) :** Envoie les commandes au Raspberry et récupère la vidéo de la caméra 
 3. **Android :** Une appli qui permet de visualiser la caméra et de commander la voiture


##1. Raspberry Pi

#### - Commande des servomoteurs avec : https://github.com/richardghirst/PiBits/tree/master/ServoBlaster
Servo :
 - 1 on P1-11          GPIO-17
 - 2 on P1-12          GPIO-18

Exemple de commande : `echo 1=150 > /dev/servoblaster`
`150` est un chiffre compris par défaut entre **50** et **250** qui correspond à **0%** et **100%** de la rotation du servomoteur. 

#### - Communication avec des socket
Le serveur est sur le raspberry et les données reçus son sous forme XML.
Par exemple : `<servo id='X'>0</servo><servo id='Y'>-10</servo>`
Ici **X** et **Y** sont les noms de mes servomoteurs. **0** et **-10** sont des exemples de *commande*.
Les *commandes* envoyées sont comprises entre `[-10,10]`

#### - Interaction entre les *commandes XML* et *servoblaster*
Entrée *XML avec les socket* : une valeur entre `[-10,10]` et X ou Y pour les noms des servos
Sortie *servomoteur*: Une valeur (Min,Max) entre `[50,250]` et 1 ou 2 pour les servos

Comme il faut que je puisse régler la course des servomoteurs définit les valeurs Max et Min. Afin d'adapter les entrées comprises entre `[-10,10]` et la sortie entre `[Min,Max]` j'utilise une équation au second degré.

#### - Camera en straming
Pour récupérer la vidéo j'utilise `raspivid`
Exemple de commande : `raspivid -t 0 -w 1280 -h 720 -fps 25 -b 2500000 -p 0,0,640,480 -o -` 

Autres valeurs :
	- SD Low: -w 480 -h 260 -fps 25 -b  800000
	- SD Medium: -w 640 -h 360 -fps 25 -b  1200000
	- SD High: -w 960 -h 540 -fps 25 -b  1800000
	- HD Ready: -w 1280 -h 720 -fps 25 -b  2500000
	- Full HD: -w 1920 -h 1080 -fps 25 -b  5000000

Ensuite le flux vidéo est transmis par GStreamer qui va s'occuper du streaming vers notre machine cible (celle ou l'on souhaite voir la vidéo).

Pour toute cette partie il y a des meilleures explications ici : 
Sources : [blog.nicolargo.com/2013/05/streaming-depuis-la-raspberry-camera.html](http://blog.nicolargo.com/2013/05/streaming-depuis-la-raspberry-camera.html)

Commande complète : `raspivid -t 0 -w 1280 -h 720 -fps 25 -b 2500000 -p 0,0,640,480 -o - | gst-launch -v fdsrc ! h264parse ! rtph264pay config-interval=1 pt=96 ! gdppay ! tcpserversink host=192.168.1.48 port=5000`
Host : `192.168.1.48` (exemple) c'est l’adresse ip du raspberry 
Port : `5000` (exemple)  port utilisé pour la communication. 
    

##2. PC (Ubuntu) :


##3. Android :


