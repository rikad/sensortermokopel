
void setup() {
  Serial.begin(9600);
}

void loop() {
  
  //read
  //convert analog to milivolt
  float v1 = analogRead(A0) * (5.0 / 1023.0) * 1000;
  float v2 = analogRead(A1) * (5.0 / 1023.0) * 1000;  
  
  //convert milivolt to celcius
  v1 = v1 * 0.3989 + 28.726;
  v2 = v1 * 0.3989 + 28.726;
  
  //combine data and send to serial
  String h1 = ConvertToString(v1);
  String h2 = ConvertToString(v2);

  Serial.println(h1+"/"+h2);
  delay(2000);
}

String ConvertToString(float data) {
  char buf[7];
  String hasil = "";

  dtostrf(data, 6, 2, buf);

  //convert chararray to string
  for(int i=0;i<sizeof(buf);i++)
  {
    hasil += buf[i];
  }

  return hasil;
}
