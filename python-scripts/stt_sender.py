import speech_recognition as sr
import requests

JAVA_URL = "";

acquired_text = []

def listen():
    recognizer = sr.Recognizer()

    with sr.Microphone() as source:
        print("\nSetting up speech recognition...")
        recognizer.adjust_for_ambient_noise(source, duration=1)
        print("\nReady to detect Speech!")

        while True:
            try:
                audio_data = recognizer.listen(source, timeout=None, phrase_time_limit=10)
                text = recognizer.recognize_google(audio_data)
                print(f"User said: {text}")
                acquired_text.append(text)
            except sr.UnknownValueError:
                # Google STT couldn't understand the audio
                print("... (Could not understand audio) ...")
            except sr.RequestError as e:
                # Could not request results from Google STT
                print(f"Google STT Service error: {e}")
            except KeyboardInterrupt:
                print("\nStopping speech detection.")
                break



def send(transcript):
    print("Sending transcript to java...")
    try:
        payload = {"transcript": transcript}
        response = requests.post(JAVA_URL, json=payload)

        if response.status_code == 200:
            print("Successfully sent to Java!")
        else:
            print(f"Java server responded with error: {response.status_code}")
    except requests.exceptions.ConnectionError:
        print("Failed to connect to Java server. Is it running?")

if __name__ == "__main__":
    listen()