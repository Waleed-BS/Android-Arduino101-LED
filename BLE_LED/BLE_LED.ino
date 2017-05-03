#include <CurieBLE.h>

// BLE Peripheral Device (the board you're programming)
BLEPeripheral blePeripheral;

// BLE LED Service
BLEService ledService("19B10000-E8F2-537E-4F6C-D104768A1214");

// BLE LED Switch Characteristic - custom 128-bit UUID, read and writable by central
BLEIntCharacteristic  switchCharacteristic("19B10001-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);

// pin to use for the LED
const int ledPin = 5;
int brightness = 0;
int fadeamount = 5;


// SETUP
//---------------------------------------------------------------------------------------------

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

// MAIN LOOP
//---------------------------------------------------------------------------------------------

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

        int out = (int) switchCharacteristic.value();
        
        start:
        if (out >= 0 && out <= 257) {
          analogWrite(ledPin, out);
          Serial.println(out);
          //Fade       
          while (out == 256) {  
            analogWrite(ledPin, brightness);
            Serial.println(brightness);
            brightness = brightness + fadeamount;
            if (brightness <= 0 || brightness >= 255) 
              fadeamount = -fadeamount;
            delay(30);
            out = switchCharacteristic.value();
            if(out != 256)
              goto start; 
          }
          //Blink
          while (out == 257) {  
            analogWrite(ledPin, 255);
            delay(50);
            analogWrite(ledPin, 0);
            delay(300);
            out = switchCharacteristic.value();
            if(out != 257)
              goto start;  
          }
        }
        else {
          Serial.print(out);
          Serial.println(" Value no good");
        }
      }
    }

    // when the central disconnects, print it out:
    Serial.print(F("Disconnected from central: "));
    Serial.println(central.address());
  }
}
