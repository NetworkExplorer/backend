package at.networkexplorer.backend.messages;

public class Messages {

    // Errors
    public static final String FOLDER_NOT_FOUND = "Path '%s' does not exist.";
    public static final String FILES_NOT_EXIST = "One or more files/folders does not exist!";
    public static final String COULD_NOT_CREATE = "Could not create folder: %s";

    // Successfull results
    public static final String DELETE_SUCCESS = "Deleted successfully";
    public static final String CREATE_SUCCESS = "Created %s successfully";
    public static final String MOVED_SUCCESS = "Moved from /%s to /%s";
    public static final String UPLOAD_SUCCESS = "Uploaded %s to /%s";
    public static final String DISCOVER = "Discover";

    public static final String CREATED_ACCOUNT = "User created successfully";
    public static final String USER_CREATE_ERROR = "Cannot create user '%s'";
    public static final String USERNAME_NOT_FOUND = "User not found with username %s";

}
