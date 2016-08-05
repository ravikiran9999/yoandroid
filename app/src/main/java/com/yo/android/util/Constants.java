package com.yo.android.util;

public class Constants {

    // Chat
    public static final String MESSAGE = "message";
    public static final String MESSAGE_ID = "message_id";
    public static final String YOUR_PHONE_NUMBER = "yourPhoneNumber";
    public static final String OPPONENT_PHONE_NUMBER = "opponentPhoneNumber";
    public static final String OPPONENT_ID = "opponentId";
    public static final String CHAT_ROOM_ID = "chatRoomId";
    public static final String CHAT_FORWARD = "forward";

    public static final String LOGED_IN_USER_ID = "logedInUserId";
    public static final String TYPE = "type";
    public static final String TEXT = "text";
    public static final String IMAGE = "image";
    public static final String SELECTED_TEXT = "selected_text";
    public static final String GROUP_NAME = "groupName";
    public static final String SELECTED_CONTACTS = "selected_contacts";
    public static final String CONTACT = "contact";

    public static final String CHAT_FRAG = "ChatFrag";
    public static final String CONT_FRAG = "ContFrag";
    public static final String Yo_CONT_FRAG = "YoContFrag";
    public static final String YO_NOTIFICATION = "notification";

    public static final String SENT = "SENT";
    public static final String SEEN = "SEEN";
    public static final String RECEIVED = "RECEIVED";

    //Preference
    public static final String USER_ID = "user_id";
    public static final String USER_AVATAR = "user_avatar";
    public static final String USER_NAME = "first_name";
    public static final String USER_STATUS = "user_status";
    public static final String PHONE_NUMBER = "phone";
    public static final String CURRENT_BALANCE = "current_balance";
    public static final String CURRENCY_SYMBOL = "currency_symbol";
    public static final String SUBSCRIBER_ID = "subscriber_id";
    public static final String DIALER_FILTER = "dialer_filter";
    public static final String FCM_REFRESH_TOKEN = "fcm_refresh_token";

    public static final String COUNTRY_NAME = "country_name";
    public static final String COUNTRY_CALL_RATE = "country_call_rate";
    public static final String COUNTRY_CALL_PULSE = "country_call_pulse";
    public static final String COUNTRY_CODE_PREFIX = "country_code_prefix";
    public static final String COUNTRY_CODE_FROM_SIM = "country_code_from_sim";

    // Firebase tables
    public static final String APP_USERS = "AppUsers";
    public static final String ROOMS = "Rooms";
    public static final String ROOM = "Room";
    public static final String CHATS = "chats";
    public static final String ROOM_ID = "RoomID";
    public static final String FIREBASE_TOKEN = "firebase_token";

    public static final String MAGAZINE_ADD_ARTICLE_ID = "magazine_add_article_id";
    public static final int ADD_ARTICLES_TO_MAGAZINE = 201;
    public static final String LOGED_IN = "loged_in";
    public static final String LOGED_IN_AND_VERIFIED = "loged_in_not_verified";
    public static final int ADD_STORY_ACTION = 101;
    public static final int GO_TO_SETTINGS = 103;
    public static final String ENABLE_PROFILE_SCREEN = "enable_profile_screen";
    public static final String ENABLE_FOLLOW_TOPICS_SCREEN = "enable_follow_more_topics_screen";
    public static final String OPPONENT_CONTACT_IMAGE = "contact_image";
    public static final String IS_OPPONENT_YO_USER = "is_yo_user";
    public static final String NOTIFICATION_ALERTS = "notification_alerts";
    public static final String SYNCE_CONTACTS = "sync_contacts";
    public static final String FROM_CHAT_ROOMS = "from_chat_activity";
    public static int SUCCESS_CODE = 200;

    public static final int ADD_IMAGE_CAPTURE = 1;
    public static final int ADD_SELECT_PICTURE = 2;


    public static String DELETE_MAGAZINE_ACTION = "com.edit.magazine.DELETE";
    public static String EDIT_MAGAZINE_ACTION = "com.edit.magazine.EDIT";
    public static String SESSION_EXPIRE = "SESSION_EXPIRE";
    public static String CHAT_ROOM_REFRESH = "CHAT_ROOM_REFRESH";
    public static String UPDATE_NOTIFICATIONS = "UPDATE_NOTIFICATIONS";
}
