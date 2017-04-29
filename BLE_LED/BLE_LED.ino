/*
 * Copyright (c) 2016 Intel Corporation.  All rights reserved.
 * See the bottom of this file for the license terms.
 */

#include <CurieBLE.h>

BLEPeripheral blePeripheral;  // BLE Peripheral Device (the board you're programming)
BLEService ledService("19B10000-E8F2-537E-4F6C-D104768A1214"); // BLE LED Service
boolean isLedOn = false;

// BLE LED Switch Characteristic - custom 128-bit UUID, read and writable by central
BLECharCharacteristic  switchCharacteristic("19B10001-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);
const int ledPin = 5; // pin to use for the LED



void setup() {
  Serial.begin(9600);

  // set LED pin to output mode
  pinMode(ledPin, (int)switchCharacteristic.value());

  // set advertised local name and service UUID:
  blePeripheral.setLocalName("LED");
  blePeripheral.setAdvertisedServiceUuid(ledService.uuid());

  // add service and characteristic:
  blePeripheral.addAttribute(ledService);
  blePeripheral.addAttribute(switchCharacteristic);

  // set the initial value for the characeristic:
  switchCharacteristic.setValue(0);

  // begin advertising BLE service:
  blePeripheral.begin();

  Serial.println("BLE LED Peripheral");
}

void loop() {
  // listen for BLE peripherals to connect:
  BLECentral central = blePeripheral.central();

  // set the brightness of pin 5:
  
  // change the brightness for next time through the loop:
  //brightness = brightness + fadeAmount;


  if (central) {
    Serial.print("Connected to central: ");
    // print the central's MAC address:
    Serial.println(central.address());

    // while the central is still connected to peripheral:
    while (central.connected()) {
      // if the remote device wrote to the characteristic,
      // use the value to control the LED:
      
      if (switchCharacteristic.written()) {
        if (switchCharacteristic.value() == '1') {   // any value other than 0
          Serial.println(switchCharacteristic.value());
          Serial.println("LED on");
          isLedOn = true;
          digitalWrite(ledPin, HIGH);         // will turn the LED on
          //analogWrite(ledPin, (int)switchCharacteristic.value()); 
          
          
//          Serial.println(switchCharacteristic.value());
//          int light = map((int)switchCharacteristic.value(),10,49,0,255);
//          analogWrite(ledPin, (int)switchCharacteristic.value()); 
//          Serial.println(F("Brightness has changed."));
           
          
        } else if(switchCharacteristic.value() == '0') {                              // a 0 value
          Serial.println(F("LED off"));
          isLedOn = false;
          digitalWrite(ledPin, LOW);          // will turn the LED off
        }  
        
        
      }

    }   

    // when the central disconnects, print it out:
    Serial.print(F("Disconnected from central: "));
    Serial.println(central.address());
  }
}

/*
   Copyright (c) 2016 Intel Corporation.  All rights reserved.

   This library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with this library; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
   */
