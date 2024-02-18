void setup() {
    Serial.begin(9600);
}

void loop() {
    if (Serial.available() > 0) {
        char incomingChar = Serial.read();
        // Process the received Morse Code character
    }
}