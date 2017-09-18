package com.yo.dialer.model;

import com.yo.android.pjsip.SipProfile;
import com.yo.dialer.DialerConfig;

/**
 * Created by Rajesh Babu on 17/7/17.
 */

public class SipProperties {

    private String sipServer = DialerConfig.NEXGE_SERVER_IP + ":" + DialerConfig.NEXGE_SERVER_TCP_PORT + DialerConfig.TCP;
    private String turnServer = "turn.pjsip.org:33478";
    private String turnUserName = "abzlute01";
    private String turnUserPwd = "abzlute01";
    private boolean isTurnEnable = false;
    private boolean isStunEnable = true;
    private boolean isVad = false;
    private boolean isICE = true;
    private String displayName = null;
    private String stunServer = "stun.pjsip.org";//"34.230.108.83:3479";//
    private boolean isDisplayName = false;
    private String proxyServer = DialerConfig.NEXGE_SERVER_IP + ":" + DialerConfig.NEXGE_SERVER_TCP_PORT + DialerConfig.TCP;

    private String sipUsername;

    public String getSipUsername() {
        return sipUsername;
    }

    public void setSipUsername(String sipUsername) {
        this.sipUsername = sipUsername;
    }

    private String sipPassword;
    //private String register = "173.82.147.172:6000";
    private String register = "185.106.240.205:6000";


    private SipProperties() {
    }


    public String getSipServer() {
        return sipServer;
    }

    public void setSipServer(String sipServer) {
        this.sipServer = sipServer;
    }

    public String getTurnServer() {
        return turnServer;
    }

    public void setTurnServer(String turnServer) {
        this.turnServer = turnServer;
    }

    public String getTurnUserName() {
        return turnUserName;
    }

    public void setTurnUserName(String turnUserName) {
        this.turnUserName = turnUserName;
    }

    public String getTurnUserPwd() {
        return turnUserPwd;
    }

    public void setTurnUserPwd(String turnUserPwd) {
        this.turnUserPwd = turnUserPwd;
    }

    public boolean isTurnEnable() {
        return isTurnEnable;
    }

    public void setTurnEnable(boolean turnEnable) {
        isTurnEnable = turnEnable;
    }

    public boolean isStunEnable() {
        return isStunEnable;
    }

    public void setStunEnable(boolean stunEnable) {
        isStunEnable = stunEnable;
    }

    public String getSipPassword() {
        return sipPassword;
    }

    public void setSipPassword(String sipPassword) {
        this.sipPassword = sipPassword;
    }

    public boolean isVad() {
        return isVad;
    }

    public void setVad(boolean vad) {
        isVad = vad;
    }

    public boolean isICE() {
        return isICE;
    }

    public void setICE(boolean ICE) {
        isICE = ICE;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getStunServer() {
        return stunServer;
    }

    public void setStunServer(String stunServer) {
        this.stunServer = stunServer;
    }

    public boolean isDisplayName() {
        return isDisplayName;
    }

    public void showDisplayName(boolean displayName) {
        isDisplayName = displayName;
    }


    public String getProxyServer() {
        return proxyServer;
    }

    public void setProxyServer(String proxyServer) {
        this.proxyServer = proxyServer;
    }

    public String getRegister() {
        return register;
    }

    public void setRegister(String register) {
        this.register = register;
    }

    public static class Builder {
        private String sipServer = DialerConfig.NEXGE_SERVER_IP + ":" + DialerConfig.NEXGE_SERVER_TCP_PORT + DialerConfig.TCP;
        private String turnServer = "turn.pjsip.org:33478";
        private String turnUserName = "abzlute01";
        private String turnUserPwd = "abzlute01";
        private boolean isTurnEnable = false;
        private boolean isVad = false;
        private boolean isICE = true;
        private String displayName = "YO!";
        private String stunServer = "stun.pjsip.org";//"34.230.108.83:3479";//
        private boolean isDisplayName = false;
        private boolean isAddProxy = false;
        private String proxyServer = DialerConfig.NEXGE_SERVER_IP + ":" + DialerConfig.NEXGE_SERVER_TCP_PORT + DialerConfig.TCP;

        private String sipUsername;
        private String sipPassword;
        private String register = DialerConfig.NEXGE_SERVER_IP + ":" + DialerConfig.NEXGE_SERVER_TCP_PORT + DialerConfig.TCP;

        public Builder withSipServer(String sipServer) {
            this.sipServer = sipServer;
            return this;
        }

        public Builder withTurnEnable(boolean enableTurn) {
            this.isTurnEnable = enableTurn;
            return this;
        }

        public Builder withVadEnable(boolean enableVad) {
            this.isVad = enableVad;
            return this;
        }

        public Builder withICEEnable(boolean enableIce) {
            this.isICE = enableIce;
            return this;
        }

        public Builder showDisplayName(boolean show) {
            this.isDisplayName = show;
            return this;
        }

        public Builder withDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder withProxyServer(String proxyServer) {
            this.proxyServer = proxyServer;
            return this;
        }

        public Builder withRegister(String register) {
            this.register = register;
            return this;
        }

        public Builder withUserName(String username) {
            this.sipUsername = username;
            return this;
        }

        public Builder withPassword(String password) {
            this.sipPassword = password;
            return this;
        }
        public Builder withStunServer(String stunServer) {
            this.stunServer = stunServer;
            return this;
        }
        public Builder withTurnServer(String turnServer) {
            this.turnServer = turnServer;
            return this;
        }
        public Builder withTurnServerUsername(String username) {
            this.turnUserName = username;
            return this;
        }
        public Builder withTurnServerPassword(String password) {
            this.turnUserPwd = password;
            return this;
        }
        public SipProperties build() {
            SipProperties properties = new SipProperties();
            properties.setDisplayName(displayName);
            properties.showDisplayName(isDisplayName);
            properties.setICE(isICE);
            properties.setVad(isVad);
            properties.setProxyServer(proxyServer);
            properties.setRegister(register);
            properties.setSipServer(sipServer);
            properties.setSipUsername(sipUsername);
            properties.setSipPassword(sipPassword);
            properties.setRegister(register);
            properties.setStunServer(stunServer);
            return properties;
        }



    }


}
