package org.treasurehunt.common.constants;

import java.util.Set;

/**
 * Uploading-related constants.
 *
 * @author Rashed Al Maaitah
 * @version 1.0
 */
public class UploadingConstants {
    public static final String USER_UPLOAD_DIR = "user-photos/";
    public static final String HUNT_BG_UPLOAD_DIR = "hunt-bg-photos/";
    public static final String HUNT_MAP_UPLOAD_DIR = "hunt-map-photos/";
    public static final Set<String> ALLOWED_TYPES = Set.of("image/png", "image/jpeg");

}
