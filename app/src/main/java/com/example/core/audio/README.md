# RAMISA on-device acoustic classification

`YamnetModel` runs the actual YAMNet neural model on CPU, without an API key,
network request, or generated replacement results. `YamnetMicrophoneMonitor`
classifies complete, non-overlapping 15,600-sample windows of 16 kHz mono PCM.
RMS dBFS and the waveform are derived from those same samples. They do not
measure calibrated environmental sound pressure.

The model predicts acoustic categories; it cannot verify a dangerous situation,
recognise Bangla/English hotwords, or determine that surroundings are safe.
The default 0.65 Screaming score gate and two successive-window requirement
are prototype heuristics, not clinically or operationally validated thresholds.
The UI labels scores accordingly. Prompts are opt-in and require explicit user
confirmation before opening the existing SOS flow. A 15-second timeout dismisses
the prompt; it never sends an alert. Cancellation applies a prompt cooldown.
Manual previews and prerecorded replay cannot enter the live alert path.

Monitoring is deliberately scoped to the visible screen. The microphone is
stopped/released on Stop, lifecycle ON_STOP, navigation/disposal, permission or
capture failure, and ViewModel cleanup. Audio is held in memory only. There is
no hidden/background microphone service in this implementation.

## Pinned assets and provenance

- Runtime dependency: `com.google.mediapipe:tasks-audio:1.2.3`.
- Model URL: https://storage.googleapis.com/mediapipe-models/audio_classifier/yamnet/float32/1/yamnet.tflite
- Model SHA-256: `4d8b4a53282dc83ef04e3e7dbc4fbc98082e34e44ed798e16c3a0cdd4c584faf`.
- Model bytes: 4,126,810. Packaged as `src/main/assets/yamnet.tflite`.
- Debug replay URL: https://storage.googleapis.com/mediapipe-assets/speech_16000_hz_mono.wav
- Replay SHA-256: `71caf50b8757d6ab9cad5eae4d36669d3c20c225a51660afd7fe0dc44cdb74f6`.
- Replay bytes: 136,798. Packaged only in debug assets as
  `yamnet_speech_16000.wav`. Contains mono PCM16, 16 kHz, 68,360 samples.
- Official asset/hash provenance:
  https://github.com/google-ai-edge/mediapipe-samples/blob/main/examples/audio_classifier/android/app/download_model.gradle.kts
- Official model source and license (Apache 2.0):
  https://github.com/tensorflow/models/tree/master/research/audioset/yamnet
- Official API:
  https://developers.google.com/edge/mediapipe/solutions/audio/audio_classifier

`YamnetAudioSmokeTest` verifies the model checksum and executes real inference
on the bundled speech fixture and digital silence without a microphone or
network. The debug UI's "Replay speech sample — no alerts" button provides the
same basic demonstration on an emulator. A physical-device microphone test is
still required before relying on capture behavior.

Actual 1.2.3 audio/core AARs declare minSdk 24 and contain x86_64 plus ARM ABIs.
Current Google setup documentation advertises Android 28+, so API 24–27 devices
still need separate runtime verification. Initialization failures are surfaced
instead of silently switching to simulated readings.
