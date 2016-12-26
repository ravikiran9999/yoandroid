package com.yo.android.chat.firebase;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.yo.android.util.Constants;

import de.greenrobot.event.EventBus;

/**
 * Created by rdoddapaneni on 12/20/2016.
 */

public class ListenerJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters job) {
        EventBus.getDefault().register(this);
        EventBus.getDefault().post(Constants.CHAT_MESSAGE_NOTIFICATION);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

}
