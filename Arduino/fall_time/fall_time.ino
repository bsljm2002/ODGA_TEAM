#include <SD.h>

#include <SoftwareSerial.h>

#define BT_RX 5
#define BT_TX 6
SoftwareSerial bluetooth(BT_RX, BT_TX);

int X = A0;
int Y = A1;
int Z = A2;
int startState = 0;
int endState = 0;
int i=0;
int buzzer = 9;
int bleState = 0;

File myFile;

void setup() {
  Serial.begin(9600);
  bluetooth.begin(9600);
  pinMode(10, OUTPUT);
  pinMode(2, INPUT_PULLUP);
  pinMode(8, INPUT_PULLUP);
  
  // SD카드를 사용하기 위해서는 꼭 10번 핀을 출력모드로 설정해야 한다.
  // 아두이노 mega를 쓰시는분은 53번핀으로 바꿔준다.
  if (!SD.begin(4)) {
    Serial.println("initialization failed!");
    return;
  }
  Serial.println("initialization done.");

  // test.txt파일을 읽기모드로 연다.(한번에 한파일만 열수 있다.)
    myFile = SD.open("odga.txt", FILE_WRITE);
    myFile.print("x");
    myFile.print(",");
    myFile.print("y");
    myFile.print(",");
    myFile.println("z");
}

void loop() {
 
  int startBtnValue = digitalRead(2);
  int endBtnValue = digitalRead(8);
  unsigned long times = millis();
  i++;
    
  //delay(500);
  if(bluetooth.available()){
    char start_bt = bluetooth.read();
      switch(start_bt){
        case '1':
          myFile = SD.open("odga.txt", FILE_WRITE);
          //makeSound();         
          if (myFile) {
            while(1){
             
              int x = analogRead(X);
              int y = analogRead(Y);
              int z = analogRead(Z);
              
          //    Serial.print(i);
              Serial.print(x);
              Serial.print(y);
              Serial.println(z);
              
              //myFile.print(i);
              
              myFile.print(x);
              myFile.print(",");
              myFile.print(y);
              myFile.print(",");
              myFile.println(z);
              
              start_bt = bluetooth.read();
              if(start_bt=='0')
                break; 
                
            }
          }
        case '0':
          //makeSound();
          closeFile();
      }
  }
    
}
