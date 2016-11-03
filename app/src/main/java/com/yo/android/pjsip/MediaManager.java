package com.yo.android.pjsip;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
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

    public String getRingtone() {
        return Settings.System.DEFAULT_RINGTONE_URI.toString();
    }

    public void playRingtone() {
        ringtone = RingtoneManager.getRingtone(context, Uri.parse(getRingtone()));
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        ringtone.play();


    }

    public void stopRingTone() {
        if (ringtone != null) {
            ringtone.stop();
            ringtone = null;
        }
    }

    public void setVibrate() {
        if (audioManager != null) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            /*Vibrator vibrator =(Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(5000);*/
        }
    }

    public boolean isSilentMode() {
        return audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ? true : false;
    }

    public boolean isVibrationMode() {
        return audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE ? true : false;
    }

    public void setAudioMode(int mode) {
        audioManager.setMode(mode);
    }

}
