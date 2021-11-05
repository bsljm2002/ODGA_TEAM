int X = A0;
int Y = A1;
int Z = A2;

#include <SoftwareSerial.h>

#define BT_RX 5
#define BT_TX 6
SoftwareSerial bluetooth(BT_RX, BT_TX);

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  bluetooth.begin(9600);
  while(!Serial){}
}

void loop() {
  // put your main code here, to run repeatedly:
  char buffer[1];
  int x = analogRead(X);
  int y = analogRead(Y);
  int z = analogRead(Z);
  Serial.print(x);
  Serial.print(",");
  Serial.print(y);
  Serial.print(",");
  Serial.println(z);
  
  
  
  
  if (Serial.available()>0){
    Serial.readBytesUntil('\n',buffer,1);
    bluetooth.write(buffer);
    bluetooth.write("\n");
   
   }
  if (bluetooth.available()>0){
    Serial.write(bluetooth.read());
  }

}
