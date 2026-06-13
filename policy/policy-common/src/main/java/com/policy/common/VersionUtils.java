package com.policy.common;

public class VersionUtils {
    public static final String INIT_VERSION = "1.0";

    public static String getNextVersion(String currentVersion, boolean minorVersion) {
        String[] versionSplit = currentVersion.split("\\.");
        if (minorVersion) {
            return versionSplit[0] + "." + (Integer.parseInt(versionSplit[1]) + 1);
        }
        return (Integer.parseInt(versionSplit[0]) + 1) + "." + versionSplit[1];
    }


    public static String getNextVersion(String currentVersion) {
        return getNextVersion(currentVersion, false);
    }
}
