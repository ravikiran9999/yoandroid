package com.yo.android.vox;

/**
 * Created by Ramesh on 2/7/16.
 */
public class GetSubscriberId extends UserDetails {

    public GetSubscriberId(String user, String secret, String section, String action) {
        super(user, secret, section, action);
        setData(new GetSubscriberIdData());
    }

    public GetSubscriberId addPhoneNumber(String mobile) {
        GetSubscriberIdData data = new GetSubscriberIdData();
        data.setSubscriberId(mobile);
        setData(data);
        return this;
    }

    public class GetSubscriberIdData extends AbstractData {
        private String USERNAME;

        public void setSubscriberId(String subscriberId) {
            this.USERNAME = subscriberId;
        }

    }

}
