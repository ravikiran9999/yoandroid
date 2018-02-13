package com.yo.android.vox;

/**
 * Created by Ramesh on 2/7/16.
 */
public class GetBalance extends UserDetails {

    public GetBalance(String user, String secret, String section, String action) {
        super(user, secret, section, action);
        setData(new GetBalanceData());
    }

    public GetBalance addPhoneNumber(String mobile) {
        GetBalanceData data = new GetBalanceData();
        data.setSUBSCRIBERID(mobile);
        setData(data);
        return this;
    }

    public class GetBalanceData extends AbstractData {
        private String SUBSCRIBERID;

        public GetBalanceData() {
        }

        public void setSUBSCRIBERID(String SUBSCRIBERID) {
            this.SUBSCRIBERID = SUBSCRIBERID;
        }

    }

}
