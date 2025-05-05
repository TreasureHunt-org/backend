package org.treasurehunt.common.constants;

/**
 * Path-related constants.
 *
 * @author Rashed Al Maaitah
 * @version 1.0
 */
public final class PathConstants {

    private PathConstants(){}

    public static final String AUTH_BASE = "auth";
    public static final String AUTH_SIGNIN = "signin";
    public static final String AUTH_SIGNUP = "signup";
    public static final String AUTH_SIGNOUT = "signout";
    public static final String AUTH_REFRESH_TOKEN = "refresh-token";
    public static final String AUTH_CHANGE_PASSWORD = "change-password";
    public static final String AUTH_ME = "me";


    public static final String USER_BASE = "users";
    public static final String USER_IMAGE = "{id}/image";


    public static final String HUNT_BASE = "hunts";
    public static final String HUNT_ID = "{id}";
    public static final String HUNT_ID_CHALLENGE = "{id}/challenges";
    public static final String HUNT_ME = "me";

    public static final String CHALLENGE_BASE = "challenges";
    public static final String CHALLENGE_SUBMIT = "challenges/submit";

}
