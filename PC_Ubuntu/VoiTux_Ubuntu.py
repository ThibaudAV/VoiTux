# -*- coding: utf-8 -*-
#!/usr/bin/env python

import sys, os, socket
import gi
gi.require_version('Gst', '1.0')
from gi.repository import Gst, GObject, Gtk, Gdk

# Needed for window.get_xid(), xvimagesink.set_window_handle(), respectively:
from gi.repository import GdkX11, GstVideo

# Camera :
HOST_camera = "192.168.0.19"
PORT_camera = 5000

# Direction, Commande :
HOST_socket = "192.168.0.19"
PORT_socket = 8881


class GTK_Main(object):

    def __init__(self):

        self.left_right = ""
        self.up_down = ""

        try:
            self.clientsocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.clientsocket.connect((HOST_socket, PORT_socket))
        except Exception, e:
            print e
        

        self.interface = Gtk.Builder()
        self.interface.add_from_file('VoiTux_Ubuntu.glade')
        self.window = self.interface.get_object('window1') # Window Main
        self.window.show()

        self.windowParametre = self.interface.get_object('window2') # Window Paramètre

        # event clavier
        self.window.connect('key-press-event', self.on_key_press_event)
        self.window.connect('key-release-event', self.on_key_release_event)

        # event button
        buttonGauche = self.interface.get_object('gauche')
        buttonGauche.connect("toggled", self.on_button_toogled,"X")
        buttonDroite = self.interface.get_object('droite')
        buttonDroite.connect("toggled", self.on_button_toogled,"X")
        buttonAvant = self.interface.get_object('avant')
        buttonAvant.connect("toggled", self.on_button_toogled,"Y")
        buttonArriere = self.interface.get_object('arriere')
        buttonArriere.connect("toggled", self.on_button_toogled,"Y")


        ### Paramètre
        
        ## Variable par defaut des paramétres
        self.maxValueDirection = 200
        self.minValueDirection = 100
        self.maxValueMAA = 50
        self.minValueMAA = 20
        # Init Paramètre : On ecrase les variables par defaut des paramétre si elle sont enregistées
        try:
            pass
        
            fo = open("VoiTux.conf.txt", "rw+")
            # fo.seek(0,0)

            for line in fo:
                param , value = line.split('=',1)
                print "Load Paramètre : %s = %s"%(param,value)
                self.setParametreValue(self,param,value) 
            fo.close()
        except Exception, e:
            pass


        # On envoie les paramétres par defaut par socket au Pi
        self.commandeServoSocket("MaxX",self.maxValueDirection)
        self.commandeServoSocket("MinX",self.minValueDirection)
        self.commandeServoSocket("MaxY",self.maxValueMAA)
        self.commandeServoSocket("MinY",self.minValueMAA)



        # Interface Paramètre
        self.scaleMinDirection = self.interface.get_object("minDirection")
        self.scaleMinDirection.set_range(50, 250)
        self.scaleMinDirection.set_increments(0, 10)
        self.scaleMinDirection.set_digits(0)
        self.scaleMinDirection.set_value(self.minValueDirection)
        self.scaleMinDirection.connect("value-changed", self.setScaleMinDirection)

        self.scaleMaxDirection = self.interface.get_object("maxDirection")
        self.scaleMaxDirection.set_range(50, 250)
        self.scaleMaxDirection.set_increments(1, 10)
        self.scaleMaxDirection.set_digits(0)
        self.scaleMaxDirection.set_value(self.maxValueDirection)
        self.scaleMaxDirection.connect("value-changed", self.setScaleMaxDirection)

        self.scaleMaxMAA = self.interface.get_object("maxMAA")
        self.scaleMaxMAA.set_range(-250, 250)
        self.scaleMaxMAA.set_increments(1, 10)
        self.scaleMaxMAA.set_digits(0)
        self.scaleMaxMAA.set_value(self.maxValueMAA)
        self.scaleMaxMAA.connect("value-changed", self.setScaleMaxMAA)
        
        self.scaleMinMAA = self.interface.get_object("minMAA")
        self.scaleMinMAA.set_range(-250, 250)
        self.scaleMinMAA.set_increments(1, 10)
        self.scaleMinMAA.set_digits(0)
        self.scaleMinMAA.set_value(self.minValueMAA)
        self.scaleMinMAA.connect("value-changed", self.setScaleMinMAA)

        # Bouton sauvegarder Paramètre
        self.saveParametreButton = self.interface.get_object("saveParametre")
        self.saveParametreButton.connect("clicked", self.saveParametre)




        # On init la Camera
        self.startVideo = self.interface.get_object("startVideo")
        self.startVideo.connect("clicked", self.start_stop)
        self.stopVideo = self.interface.get_object("quit")
        self.stopVideo.connect("clicked", self.exit)

        self.startVideo.initialeLabel = self.startVideo.get_label()

        self.interface.connect_signals(self)





        # Set up the gstreamer pipeline

        # Solution 1
        # 
        # Sur le Raspberry : raspivid -t 0 -w 1280 -h 720 -fps 25 -b 2500000 -p 0,0,640,480 -o - | nc.traditional -l -p  5000
        
        # self.player = Gst.parse_launch ("fdsrc name=source ! h264parse ! avdec_h264 ! videoconvert ! autovideosink sync=false")

        # self.conn_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        # self.conn_sock.connect((HOST_camera, PORT_camera))

        # self.source = self.player.get_by_name("source")
        # self.source.set_property('fd', self.conn_sock.fileno())


        # Solution 2
        # 
        # Sur le raspberry idem que la solution 1
        # 
        # self.conn_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        # self.conn_sock.connect((HOST_camera, PORT_camera))

        # self.player = Gst.parse_launch ("fdsrc fd=%i ! h264parse ! avdec_h264 ! videoconvert ! autovideosink sync=false"%self.conn_sock.fileno())



        # Solution 3 
        # 
        # Sur le Raspberry : raspivid -t 0 -w 1280 -h 720 -fps 25 -b 2500000 -p 0,0,640,480 -o - | gst-launch -v fdsrc ! h264parse ! rtph264pay config-interval=1 pt=96 ! gdppay ! tcpserversink host=192.168.1.48 port=5000
        # 
        # self.player = Gst.parse_launch ("tcpclientsrc host=%s port=%i ! gdpdepay ! rtph264depay ! avdec_h264 ! videoconvert ! autovideosink sync=false"%(HOST_camera,PORT_camera))
        
        # Solution 4 En UDP 
        # 
        # Sur le Raspberry : raspivid -t 0 -w 1280 -h 720 -fps 25 -b 2500000 -p 0,0,640,480 -o - | gst-launch -v fdsrc ! h264parse ! rtph264pay config-interval=1 pt=96 ! gdppay ! tcpserversink host=192.168.1.48 port=5000
        # 
        self.player = Gst.parse_launch ("udpsrc port=%i caps="application/x-rtp, media=(string)video, clock-rate=(int)90000, encoding-name=(string)H264, payload=(int)96" ! queue ! rtph264depay ! avdec_h264 ! videoconvert ! autovideosink sync=false"%(PORT_camera))
        

        bus = self.player.get_bus()
        bus.add_signal_watch()
        bus.enable_sync_message_emission()
        bus.connect("message", self.on_message)
        bus.connect("sync-message::element", self.on_sync_message)

    def setParametreValue(self, widget, param, value):
        value = int(value)
        if param == "maxValueDirection":
            self.maxValueDirection = value
        elif param == "minValueDirection":
            self.minValueDirection = value
        elif param == "maxValueMAA":
            self.maxValueMAA = value
        elif param == "minValueMAA":
            self.minValueMAA = value
    def saveParametre(self,widget):
        print "Save Paramètre"
        fo = open("VoiTux.conf.txt", "rw+")
        fo.write("maxValueDirection="+str(self.maxValueDirection)+"\n")
        fo.write("minValueDirection="+str(self.minValueDirection)+"\n")
        fo.write("maxValueMAA="+str(self.maxValueMAA)+"\n")
        fo.write("minValueMAA="+str(self.minValueMAA)+"\n")
        fo.close()

    def start_stop(self, w):
        if self.startVideo.get_label() == self.startVideo.initialeLabel:
            self.startVideo.set_label("Stop")
            self.player.set_state(Gst.State.PLAYING)
        else:
            self.player.set_state(Gst.State.NULL)
            self.startVideo.set_label(self.startVideo.initialeLabel)

    def exit(self, widget, data=None):
        Gtk.main_quit()

    def on_message(self, bus, message):
        t = message.type
        if t == Gst.MessageType.EOS:
            self.player.set_state(Gst.State.NULL)
            self.startVideo.set_label(self.startVideo.initialeLabel)
        elif t == Gst.MessageType.ERROR:
            err, debug = message.parse_error()
            print "Error: %s" % err, debug
            self.player.set_state(Gst.State.NULL)
            self.startVideo.set_label(self.startVideo.initialeLabel)

    def on_sync_message(self, bus, message):
        if message.get_structure().get_name() == 'prepare-window-handle':
            imagesink = message.src
            imagesink.set_property("force-aspect-ratio", True)
            Gdk.threads_enter()
            # imagesink.set_window_handle(self.movie_window.get_property('window').get_xid())
            imagesink.set_window_handle(self.interface.get_object('zoneVideo').get_property('window').get_xid())

            Gdk.threads_leave()

    def commandeServoSocket(self, id,value):
        try:
             self.clientsocket.send("<servo id='"+id+"'>"+str(int(value))+"</servo>")
        except Exception, e:
            print e
        # self.clientsocket.send("<servo id='"+id+"'>"+str(int(value))+"</servo>")
        print "<servo id='"+id+"'>"+str(int(value))+"</servo>"

    def setScaleMinDirection(self,widget):
        self.minValueDirection = int(widget.get_value())
        self.scaleMaxDirection.set_range(widget.get_value(), 250) # contrainte pour le max 
        self.commandeServoSocket("MinX",widget.get_value())
    def setScaleMaxDirection(self,widget):
        self.maxValueDirection = int(widget.get_value())
        self.scaleMinDirection.set_range(50, widget.get_value())
        self.commandeServoSocket("MaxX",widget.get_value())

    def setScaleMinMAA(self,widget):
        self.minValueMAA = int(widget.get_value())
        self.scaleMaxMAA.set_range(widget.get_value(), 250)
        self.commandeServoSocket("MinY",widget.get_value())
    def setScaleMaxMAA(self,widget):
        self.maxValueMAA = int(widget.get_value())
        self.scaleMinMAA.set_range(50, widget.get_value())
        self.commandeServoSocket("MaxY",widget.get_value())
        
    # si on utilise les bouttons 
    def on_button_toogled(self,widget,name):
        if widget.get_active():
            print name+"10"
        else:
            print name+"0"


    def on_key_press_event(self,widget, event):
        # global left_right, up_down
        keyname = Gdk.keyval_name(event.keyval)

        if self.left_right != keyname and self.up_down != keyname:
            if keyname == "Left": #If Escape pressed, reset text
                # print "Key %s (%d) was pressed" % (keyname, event.keyval)
                self.commandeServoSocket("X",10)
                print "Left"
                self.left_right = keyname
            if keyname == "Right": #If Escape pressed, reset text
                # print "Key %s (%d) was pressed" % (keyname, event.keyval)
                print "Right"
                self.left_right = keyname
                self.commandeServoSocket("X",-10)
            if keyname == "Up": #If Escape pressed, reset text
                # print "Key %s (%d) was pressed" % (keyname, event.keyval)
                print "Up"
                self.commandeServoSocket("Y",10)
                self.up_down = keyname
            if keyname == "Down": #If Escape pressed, reset text
                # print "Key %s (%d) was pressed" % (keyname, event.keyval)
                print "Down"
                self.commandeServoSocket("Y",-10)
                self.up_down = keyname
    def on_key_release_event(self,widget, event):
        # global self.left_right, up_down
        keyname = Gdk.keyval_name(event.keyval)
        if keyname == "Left": #If Escape pressed, reset text
            # print "Key %s (%d) was re" % (keyname, event.keyval)
            self.commandeServoSocket("X",0)
            print "release Left"
            self.left_right = ""
        if keyname == "Right": #If Escape pressed, reset text
            # print "Key %s (%d) was pressed" % (keyname, event.keyval)
            self.commandeServoSocket("X",0)
            print "release Right"
            self.left_right = ""
        if keyname == "Up": #If Escape pressed, reset text
            # print "Key %s (%d) was pressed" % (keyname, event.keyval)
            self.commandeServoSocket("Y",0)
            print "release Up"
            self.up_down = ""
        if keyname == "Down": #If Escape pressed, reset text
            # print "Key %s (%d) was pressed" % (keyname, event.keyval)
            self.commandeServoSocket("Y",0)
            print "release Down"
            self.up_down = ""


    def on_parametre_clicked(self, widget=None, data=None):
        self.windowParametre.show()
    def on_closeParametre_clicked(self, widget):
        self.windowParametre.hide()



GObject.threads_init()
Gst.init(None)        
GTK_Main()
Gtk.main()
