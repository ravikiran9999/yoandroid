package com.yo.android.pjsip;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;

/**
 * Created by Ramesh on 14/8/16.
 */
public class MediaManager {

    private final AudioManager audioManager;
    private Context context;
    private Ringtone ringtone;

    public MediaManager(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.context = context;
    }

    public void setSpeakerOn(boolean isSpeakerOn) {
        audioManager.setSpeakerphoneOn(isSpeakerOn);
    }

    public void setMicrophoneMuteOn(boolean isMuteOn) {
        audioManager.setMicrophoneMute(isMuteOn);
    }

    public boolean isMicrophoneMute() {
        return audioManager.isMicrophoneMute();
    }

    public boolean isSpeakerOn() {
        return audioManager.isSpeakerphoneOn();
    }

    private String getRingtone() {
        return Settings.System.DEFAULT_RINGTONE_URI.toString();
    }

    public void playRingtone() {
        if (ringtone == null) {
            ringtone = RingtoneManager.getRingtone(context, Uri.parse(getRingtone()));
            audioManager.setMode(AudioManager.MODE_RINGTONE);
            if (!ringtone.isPlaying()) {
                ringtone.play();
            }
        }
    }

    public void stopRingTone() {
        if (ringtone != null) {
            ringtone.stop();
            ringtone = null;
        }
    }


}
