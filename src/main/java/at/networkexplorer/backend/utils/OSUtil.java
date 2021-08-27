package at.networkexplorer.backend.utils;

public class OSUtil {

    public enum OS {
        WINDOWS, LINUX, MAC, SOLARIS
    }// Operating systems.

    private static OS os = null;

    /**
     * Get the OperatingSystem of the host computer
     * @return {@link at.networkexplorer.backend.utils.OSUtil.OS}
     */
    public static OS getOS() {
        if (os == null) {
            String operSys = System.getProperty("os.name").toLowerCase();
            if (operSys.contains("win")) {
                os = OS.WINDOWS;
            } else if (operSys.contains("nix") || operSys.contains("nux")
                    || operSys.contains("aix")) {
                os = OS.LINUX;
            } else if (operSys.contains("mac")) {
                os = OS.MAC;
            } else if (operSys.contains("sunos")) {
                os = OS.SOLARIS;
            }
        }
        return os;
    }

}
