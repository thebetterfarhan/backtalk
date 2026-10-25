package com.example.backtalkenginehang;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public final class MainActivity extends Activity {
  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setFocusable(true);

    Button longAnnouncement = new Button(this);
    longAnnouncement.setText("Trigger split speech");
    longAnnouncement.setContentDescription(
        "This is a deliberately long accessibility announcement used to create multiple speech "
            + "chunks for the BackTalk engine hang recovery test. It contains enough sentences "
            + "to cross the chunking limit and leave a later control available.");
    layout.addView(longAnnouncement);

    Button recoveryAnnouncement = new Button(this);
    recoveryAnnouncement.setText("Trigger recovery speech");
    recoveryAnnouncement.setContentDescription("Recovery speech request");
    layout.addView(recoveryAnnouncement);

    setContentView(layout);
    longAnnouncement.requestFocus();
  }
}
