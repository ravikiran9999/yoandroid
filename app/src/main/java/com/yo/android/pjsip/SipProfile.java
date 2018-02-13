package com.yo.android.pjsip;

/**
 * Created by Ramesh on 13/8/16.
 */
public class SipProfile {
    private String username;
    private String password;
    private String domain;
    private boolean displayName;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isDisplayName() {
        return displayName;
    }

    public void setDisplayName(boolean displayName) {
        this.displayName = displayName;
    }


    private SipProfile(String username,
                       String password,
                       String domain) {
        this.username = username;
        this.password = password;
        this.domain = domain;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDomain() {
        return domain;
    }

    public static class Builder {

        private String username;

        private String password;

        private String domain;

        public Builder withUserName(String userName) {
            username = userName;
            return this;
        }

        public Builder withPassword(String passWord) {
            this.password = passWord;
            return this;
        }

        public Builder withServer(String domain) {
            this.domain = domain;
            return this;
        }

        public SipProfile build() {
            return new SipProfile(username, password, domain);
        }
    }
}
