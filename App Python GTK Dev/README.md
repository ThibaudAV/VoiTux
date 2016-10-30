PC (Ubuntu)
=========



Partie client qui permet de contrôler la voiture à partir d'un ordinateur.
J'ai développé et testé ce script sous Ubuntu (Linux). Le scripte est en Python et à une interface graphique GTK (`VoiTux_Ubuntu Glade`).

Le contrôle se fait via les flèches du clavier ou les boutons de l'interface. Il est aussi possible d'avoir la vidéo de la caméra grasse à GStreamer.

L'interface ne permet pas ( pour le moment ) de modifier l’adresse Ip du Raspberry Pi ainsi que les ports (caméra direction)
Il faut donc modifier le fichier VoiTux_Ubuntu.py 

Une fenêtre de paramètre permet de modifier les "butées" (valeurs Max et Min) des servomoteurs les paramètres sont ensuite enregistrés dans un fichier `VoiTux.conf.txt` et re-initialiser au lancement du script.
