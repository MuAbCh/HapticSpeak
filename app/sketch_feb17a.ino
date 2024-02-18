const int motorPin = 9; // Connect the vibration motor to digital pin 9

void setup() {
    pinMode(motorPin, OUTPUT);
    Serial.begin(9600); // Initialize serial communication for debugging
}

void loop() {
    // Read Morse code input from Serial Monitor
    String morseCode = "--- / -.-. .- -.-. .- -.. .-";
    //String morseCode = Serial.readStringUntil('\n');
    convertAndVibrate(morseCode);

    delay(5000);
}

void convertAndVibrate(String morseCode) {
    // Define Morse code mapping
    String morseCodeMap[] = {
            ".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..",
            ".---", "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.",
            "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..",
            "-----", ".----", "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----.",
            "/" // Use '/' as a word separator in Morse code
    };

    // Vibration patterns (example durations in milliseconds)
    int dotDuration = 1000;
    int dashDuration = 3000;
    int interSymbolPause = 250;
    int interLetterPause = 1000;

    for (int i = 0; i < morseCode.length(); i++) {
        char symbol = morseCode.charAt(i);

        if (symbol == '.') {
            vibrate(dotDuration);
        } else if (symbol == '-') {
            vibrate(dashDuration);
        } else if (symbol == ' ') {
            delay(interLetterPause);
        } else if (symbol == '/') {
            delay(interLetterPause);
        }

        delay(interSymbolPause);
    }
}

void vibrate(int duration) {
    digitalWrite(motorPin, HIGH);
    delay(duration);
    digitalWrite(motorPin, LOW);
}