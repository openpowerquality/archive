#!/usr/bin/env python

from ConfigParser import SafeConfigParser
import datetime
import os
import os.path
from subprocess import call, Popen, PIPE
import sys

date = str(datetime.datetime.now())
print "===== config.py " + date + " ====="
print

# ---------- Files ----------
# USB Files
config_ini = "config.ini"
custom_wpa_supplicant = "wpa_supplicant.conf"

# Files in rpi config dir
interfaces_template = "interfaces.template"
wpa_template = "wpa_supplicant.wpa.conf.template"
wep_template = "wpa_supplicant.wep.conf.template"
pid_file = "config.pid"

# System files
settings_file = "settings.set"

# ---------- Paths ----------
# USB Paths
usb_path = "/media/usb"
config_ini_path = os.path.join(usb_path, config_ini)
custom_wpa_supplicant_path = os.path.join(usb_path, custom_wpa_supplicant)

# Paths in rpi config dir
config_path = "/home/pi/Gen1/Software/rpi-firmware/rpid/config"
templates_path = os.path.join(config_path, "templates")
wpa_template_path = os.path.join(templates_path, wpa_template)
wep_template_path = os.path.join(templates_path, wep_template)
pid_path = os.path.join(config_path, pid_file)

# System Paths
settings_path = os.path.join("/usr/local/opqd/", settings_file)
wpa_supplicant_path = "/etc/wpa_supplicant/wpa_supplicant.conf"

def path_info(name, path):
    return name + " = " + path + " -- (exists? " + str(os.path.exists(path)) + ")"

def display_state():
    print "---------- Files ----------"
    print " USB files"
    print "config_ini = " + config_ini
    print "custom_wpa_supplicant = " + custom_wpa_supplicant
    print
    print "Files in rpi config dir"
    print "interfaces_template = " + interfaces_template
    print "wpa_template = " + wpa_template
    print "wep_template = " + wep_template
    print "pid_file = " + pid_file
    print
    print "System files"
    print "settings_file = " + settings_file
    print
    print "---------- Paths ----------"
    print "USB paths"
    print path_info("usb_path", usb_path)
    print path_info("config_ini_path", config_ini_path)
    print path_info("custom_wpa_supplicant_path", custom_wpa_supplicant_path)
    print
    print "Paths in rpi config dir"
    print path_info("config_path", config_path)
    print path_info("templates_path", templates_path)
    print path_info("wpa_template_path", wpa_template_path)
    print path_info("wep_template_path", wep_template_path)
    print path_info("pid_path", pid_path)
    print
    print "System paths"
    print path_info("settings_path", settings_path)
    print path_info("wpa_supplicant_path", wpa_supplicant_path)
    print

def write_pid():
    f = open(pid_path, "w")
    f.write(str(os.getpid()))
    f.close() 

def destroy_pid():
    if os.path.exists(pid_path):
        os.remove(pid_path) 

def parse_config(path):
    parser = SafeConfigParser()
    parser.read(path)
    
    config_fields = {"wifi_config": ["ssid", "key", "security"],
              "device_config": ["access_key", "opqhub_addr", "event_throttle",
                                "expected_voltage", "voltage_tolerance",
                                "expected_frequency", "frequency_tolerance",
                                "heartbeat_interval"]}
              
    wifi_results = {}
    device_results = {}
              
    for section in config_fields:
        for field in config_fields[section]:
            if not parser.has_option(section, field):
                print "ERROR: config field " + field + " missing!"
                exit()
            if section == "wifi_config":
                wifi_results[field] = parser.get(section, field)
            elif section == "device_config":
                device_results[field] = parser.get(section, field)
                
    return wifi_results, device_results

def update_settings(device_settings, settings_file):
    # Read in the current settings file
    f = open(settings_file, "r")
    lines = f.readlines()
    f.close()
    
    # Resulting lines will be stored here
    results = []
    
    # Replace fields given in config
    for line in lines:
        if line.startswith("device.key"):
            results.append("device.key              :S  :" + device_settings["access_key"] + "\n")
        elif line.startswith("ws.url"):
            results.append("ws.url                  :S  :" + device_settings["opqhub_addr"] + "\n")
        elif line.startswith("filter.thresh.f"):
            results.append("filter.thresh.f         :F  :" + device_settings["frequency_tolerance"] + "\n")
        elif line.startswith("filter.thresh.vrms"):
            results.append("filter.thresh.vrms      :F  :" + device_settings["voltage_tolerance"] + "\n")
        elif line.startswith("filter.expected.f"):
            results.append("filter.expected.f       :F  :" + device_settings["expected_frequency"] + "\n")
        elif line.startswith("filter.expected.vrms"):
            results.append("filter.expected.vrms    :F  :" + device_settings["expected_voltage"] + "\n")
        elif line.startswith("filter.expected.vrms"):
            results.append("device.throttle         :I  :" + device_settings["event_throttle"] + "\n")  
        elif line.startswith("device.pinginterval"):
            results.append("device.pinginterval     :I  :" + device_settings["heartbeat_interval"] + "\n") 
        else:
            results.append(line)
            
    # Write out the result
    f = open(settings_file, "w")
    for line in results:
        f.write(line)
    f.close()

def get_wpa_template(configuration):
    security = configuration["security"]
    if (not security == "wep") and (not security == "wpa"):
        print "ERROR: Unknown security type " + security
        print "Should be one of either wep or wpa."
        exit()

    if security == "wep":
        print "Using wep template..."
        f = open(wep_template_path, "r")
        t = f.read()
        f.close()
        return t
    else:
        print "Using wpa template..."
        f = open(wpa_template_path, "r")
        t = f.read()
        f.close()
        return t

def make_wpa_supplicant(configuration, template):
    t = template.replace("_SSID_", configuration["ssid"])
    t = t.replace("_KEY_", configuration["key"])
    return t

def write_supplicant(supplicant, path):
    f = open(path, "w")
    f.write(supplicant)
    f.close()

def restart_networking():
    call(["sudo", "ifdown", "wlan0"])
    call(["sudo", "ifup", "wlan0"])
    call(["sudo", "dhclient", "wlan0"])

def configure():
    # Read in configuration
    print "Configuration file " + config_ini_path + " found!"
    wifi_config, device_config = parse_config(config_ini_path)
    print
    
    # Print out the configurations for sanity
    print "Parsed wifi_configuration = " + str(wifi_config)
    print "Parsed device_configuration = " + str(device_config)
    print    

    # Update settings.set
    print "Updating /usr/local/opqd/settings.set..."
    update_settings(device_config, settings_path)
    print    

    # Update wifi-configuration
    print "Generating wpa_supplicant..."
    template = get_wpa_template(wifi_config)
    wpa_supplicant = make_wpa_supplicant(wifi_config, template)    

    print "Generated wpa_supplicant.conf..."
    print
    print wpa_supplicant
    print

    print "Copying wpa_supplicant.conf to device..."
    write_supplicant(wpa_supplicant, "/etc/wpa_supplicant/wpa_supplicant.conf")
    
    print "Attempting to restart networking..."
    restart_networking()
    print

    print "Results of ifconfig"
    ifconfig = Popen("ifconfig", stdout=PIPE)
    output = ifconfig.communicate()[0]
    print output

def copy_custom_wpa_supplicant():
    print "Custom configuration " + custom_wpa_supplicant_path + " found!"
    print "Copying " + custom_wpa_supplicant_path + " to device."
    print
    f = open(custom_wpa_supplicant_path, "r")
    supplicant = f.read()
    f.close()

    write_supplicant(supplicant, wpa_supplicant_path)  

if __name__ == "__main__":
    # Write PID file
    write_pid()    
 
    # Display state of system
    display_state()

    # Check for configuration file and configure
    if os.path.exists(config_ini_path):
        configure()
    else:
        print "ERROR: " + config_path + " not found!"
        print

    # Check for provided wpa_supplicant.conf
    # Note: If wpa_supplicant.conf is found on usb, it will always be used
    # for configuring wireless, even if config.ini is specified.
    if os.path.exists(custom_wpa_supplicant_path):
        copy_custom_wpa_supplicant()

    # Destroy PID file
    destroy_pid()




