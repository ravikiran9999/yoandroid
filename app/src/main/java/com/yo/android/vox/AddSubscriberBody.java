package com.yo.android.vox;

public class AddSubscriberBody extends UserDetails {
    public AddSubscriberBodyData DATA;

    public AddSubscriberBody(String user, String secret, String section, String action) {
        super(user, secret, section, action);
    }

    public AddSubscriberBody addSubscriberBody(String username, String mobile) {
        DATA = new AddSubscriberBodyData(username, mobile);
        setData(DATA);
        return this;
    }


}
