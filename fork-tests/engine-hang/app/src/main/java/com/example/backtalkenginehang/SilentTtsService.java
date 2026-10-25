package com.example.backtalkenginehang;

import android.speech.tts.SynthesisRequest;
import android.speech.tts.SynthesisCallback;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.util.Log;

public final class SilentTtsService extends TextToSpeechService {
  private static final String TAG = "BackTalkHangTest";
  private boolean hangInjected;

  @Override
  public int onStartCommand(android.content.Intent intent, int flags, int startId) {
    Log.i(TAG, "TTS_ENGINE_SELECTED");
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  protected String[] onGetLanguage() {
    return new String[] {"eng", "USA", ""};
  }

  @Override
  protected int onLoadLanguage(String language, String country, String variant) {
    return TextToSpeech.LANG_AVAILABLE;
  }

  @Override
  protected int onIsLanguageAvailable(String language, String country, String variant) {
    return TextToSpeech.LANG_AVAILABLE;
  }

  @Override
  protected void onSynthesizeText(SynthesisRequest request, SynthesisCallback callback) {
    String text = request.getCharSequenceText() == null ? "" : request.getCharSequenceText().toString();
    if (!hangInjected && text.length() > 100) {
      hangInjected = true;
      Log.i(TAG, "HANG_INJECTED");
      try {
        Thread.sleep(12000L);
      } catch (InterruptedException ignored) {
        Thread.currentThread().interrupt();
      }
      callback.error();
      return;
    }

    Log.i(TAG, "RECOVERY_REQUEST");
    callback.start(22050, android.media.AudioFormat.ENCODING_PCM_16BIT, 1);
    callback.audioAvailable(new byte[2205], 0, 2205);
    callback.done();
  }

  @Override
  protected void onStop() {}
}
