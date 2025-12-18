package me.mm.qs.script;

import android.content.Context;
import me.mm.qs.script.types.*;
import static me.mm.qs.script.Globals.*;

import java.util.ArrayList;
import java.util.Map;

/**
 * QStory Script Base Class for IDE hint support.
 * This class defines all methods available in the QStory Beanshell environment.
 * Global variables are now in Globals.java.
 * Actual script logic should be written in classes extending this base class.
 */
public abstract class QScriptBase {

    // Global variables are now in Globals.java - imported via static import
    // Access them directly: myUin, context, appPath, loader, pluginID

    // --- Callback Methods (to be overridden by scripts) ---

    /**
     * Called when a message is received.
     * @param msg Message data object
     */
    public void onMsg(MessageData msg) {}

    /**
     * Called when a member is muted.
     * @param GroupUin Group number
     * @param UserUin  Muted user's QQ number
     * @param OPUin    Administrator who performed the mute
     * @param time     Mute duration in seconds
     */
    public void onForbiddenEvent(String GroupUin, String UserUin, String OPUin, long time) {}

    /**
     * Called when a member joins or leaves a group.
     * @param GroupUin Group number
     * @param UserUin  User's QQ number
     * @param type     2 for join, 1 for leave
     */
    public void onTroopEvent(String GroupUin, String UserUin, int type) {}

    /**
     * Called when the script floating window is opened.
     * @param type Chat type (1: private chat, 2: group chat)
     * @param uin  Friend's QQ number or Group number
     */
    public void onClickFloatingWindow(int type, String uin) {}

    /**
     * Called when a message is about to be sent.
     * @param msg           Message content to be sent
     * @param GroupUinOrFriendUin Group number or friend's QQ number
     * @param type          Chat type (2: group, 1 or 100: private)
     * @return Modified message content
     */
    public String getMsg(String msg, String GroupUinOrFriendUin, int type) { return msg; }

    /**
     * Called when creating a long-press menu for a message.
     * @param msg Message data object
     */
    public void onCreateMenu(MessageData msg) {}

    /**
     * Called when an unparsed raw message is received.
     * @param msg Raw message object
     */
    public void callbackOnRawMsg(Object msg) {}

    // --- API Methods ---

    // Sending Messages
    /**
     * Send text, image, or图文 message.
     * For图文, use [PicUrl=图片地址]. For @, use [AtQQ=QQ号].
     * @param GroupUin Group number (empty for private chat)
     * @param UserUin  User's QQ number (empty for group chat)
     * @param msg      Message content
     */
    public native void sendMsg(String GroupUin, String UserUin, String msg);

    /**
     * Send a single picture.
     * @param GroupUin Group number (empty for private chat)
     * @param UserUin  User's QQ number (empty for group chat)
     * @param Path     Image local or network path
     */
    public native void sendPic(String GroupUin, String UserUin, String Path);

    /**
     * Send a JSON card message.
     * @param GroupUin Group number (empty for private chat)
     * @param UserUin  User's QQ number (empty for group chat)
     * @param card     Card content
     */
    public native void sendCard(String GroupUin, String UserUin, String card);

    /**
     * Send a reply message (group chat only).
     * @param GroupUin Group number
     * @param msg      Message object to reply to
     * @param replyMsg Reply text
     */
    public native void sendReply(String GroupUin, Object msg, String replyMsg);

    /**
     * Send a file.
     * @param GroupUin Group number (empty for private chat)
     * @param UserUin  User's QQ number (empty for group chat)
     * @param Path     File path
     */
    public native void sendFile(String GroupUin, String UserUin, String Path);

    /**
     * Send a voice message.
     * @param GroupUin Group number (empty for private chat)
     * @param UserUin  User's QQ number (empty for group chat)
     * @param Path     Voice file path
     */
    public native void sendVoice(String GroupUin, String UserUin, String Path);

    /**
     * Send a video.
     * @param group   Group number
     * @param userUin User's QQ number
     * @param path    Video file path
     */
    public native void sendVideo(String group, String userUin, String path);

    /**
     * Like a user.
     * @param UserUin User's QQ number
     * @param count   Number of likes
     */
    public native void sendLike(String UserUin, int count);

    /**
     * Poke a user.
     * @param group Group number (empty for private poke)
     * @param uin   User's QQ number
     */
    public native void sendPai(String group, String uin);

    /**
     * Send an emoji reaction to a message.
     * @param msg     Message object
     * @param emojiId Emoji ID
     */
    public native void replyEmoji(Object msg, String emojiId);

    /**
     * Send a ProtoBuf message (experimental).
     * @param cmd      Command
     * @param jsonBody JSON body
     */
    public native void sendProto(String cmd, String jsonBody);

    // Group Operations
    /**
     * Set a group member's card name (admin only).
     * @param GroupUin Group number
     * @param UserUin  Member's QQ number
     * @param Name     New card name
     */
    public native void setCard(String GroupUin, String UserUin, String Name);

    /**
     * Set a group member's special title (owner only).
     * @param GroupUin Group number
     * @param UserUin  Member's QQ number
     * @param title    Title
     */
    public native void setTitle(String GroupUin, String UserUin, String title);

    /**
     * Revoke a message.
     * @param msg Message object
     */
    public native void revokeMsg(Object msg);

    /**
     * Delete a message.
     * @param msg Message object
     */
    public native void deleteMsg(Object msg);

    /**
     * Mute a member (admin only).
     * @param GroupUin Group number
     * @param UserUin  Member's QQ number (empty for全体禁言)
     * @param time     Duration in seconds
     */
    public native void forbidden(String GroupUin, String UserUin, int time);

    /**
     * Kick a member (admin only).
     * @param GroupUin Group number
     * @param UserUin  Member's QQ number
     * @param isBlack  Whether to block re-application
     */
    public native void kick(String GroupUin, String UserUin, boolean isBlack);

    // Information Retrieval
    /**
     * Get a member's name in a group.
     * @param group Group number
     * @param uin   Member's QQ number
     * @return Member's name
     */
    public native String getMemberName(String group, String uin);

    /**
     * Get the list of groups.
     * @return ArrayList of GroupInfo objects
     */
    public native ArrayList<GroupInfo> getGroupList();

    /**
     * Get information for a specific group.
     * @param GroupUin Group number
     * @return GroupInfo object
     */
    public native GroupInfo getGroupInfo(String GroupUin);

    /**
     * Get the list of members in a group.
     * @param GroupUin Group number
     * @return ArrayList of GroupMemberInfo objects
     */
    public native ArrayList<GroupMemberInfo> getGroupMemberList(String GroupUin);

    /**
     * Get information for a specific member in a group.
     * @param group Group number
     * @param uin   Member's QQ number
     * @return GroupMemberInfo object
     */
    public native GroupMemberInfo getMemberInfo(String group, String uin);

    /**
     * Get the list of muted members in a group.
     * @param GroupUin Group number
     * @return ArrayList of ForbiddenInfo objects
     */
    public native ArrayList<ForbiddenInfo> getForbiddenList(String GroupUin);

    /**
     * Get the friend list.
     * @return ArrayList of FriendInfo objects
     */
    public native ArrayList<FriendInfo> getFriendList();

    /**
     * Check if a user is a friend.
     * @param uin User's QQ number
     * @return True if is friend
     */
    public native boolean isFriend(String uin);

    // Data Storage
    /**
     * Store a string value.
     * @param ConfigName Configuration name
     * @param key        Key
     * @param value      Value
     */
    public native void putString(String ConfigName, String key, String value);

    /**
     * Retrieve a string value.
     * @param ConfigName Configuration name
     * @param key        Key
     * @return Value
     */
    public native String getString(String ConfigName, String key);

    /**
     * Retrieve a string value with default.
     * @param ConfigName Configuration name
     * @param key        Key
     * @param def        Default value
     * @return Value or default
     */
    public native String getString(String ConfigName, String key, String def);

    /**
     * Store an integer value.
     * @param ConfigName Configuration name
     * @param key        Key
     * @param value      Value
     */
    public native void putInt(String ConfigName, String key, int value);

    /**
     * Retrieve an integer value with default.
     * @param ConfigName Configuration name
     * @param key        Key
     * @param def        Default value
     * @return Value or default
     */
    public native int getInt(String ConfigName, String key, int def);

    /**
     * Store a long value.
     * @param ConfigName Configuration name
     * @param key        Key
     * @param value      Value
     */
    public native void putLong(String ConfigName, String key, long value);

    /**
     * Retrieve a long value with default.
     * @param ConfigName Configuration name
     * @param key        Key
     * @param def        Default value
     * @return Value or default
     */
    public native long getLong(String ConfigName, String key, long def);

    /**
     * Store a boolean value.
     * @param ConfigName Configuration name
     * @param key        Key
     * @param value      Value
     */
    public native void putBoolean(String ConfigName, String key, boolean value);

    /**
     * Retrieve a boolean value with default.
     * @param ConfigName Configuration name
     * @param key        Key
     * @param def        Default value
     * @return Value or default
     */
    public native boolean getBoolean(String ConfigName, String key, boolean def);

    /**
     * Store a float value.
     * @param ConfigName Configuration name
     * @param key        Key
     * @param value      Value
     */
    public native void putFloat(String ConfigName, String key, float value);

    /**
     * Retrieve a float value with default.
     * @param ConfigName Configuration name
     * @param key        Key
     * @param def        Default value
     * @return Value or default
     */
    public native float getFloat(String ConfigName, String key, float def);

    /**
     * Store a double value.
     * @param ConfigName Configuration name
     * @param key        Key
     * @param value      Value
     */
    public native void putDouble(String ConfigName, String key, double value);

    /**
     * Retrieve a double value with default.
     * @param ConfigName Configuration name
     * @param key        Key
     * @param def        Default value
     * @return Value or default
     */
    public native double getDouble(String ConfigName, String key, double def);

    // Skey Methods
    /**
     * Get group RKey.
     * @return RKey
     */
    public native String getGroupRKey();

    /**
     * Get friend RKey.
     * @return RKey
     */
    public native String getFriendRKey();

    /**
     * Get standard skey.
     * @return Skey
     */
    public native String getSkey();

    /**
     * Get real skey.
     * @return Skey
     */
    public native String getRealSkey();

    /**
     * Get pskey.
     * @param url URL
     * @return Pskey
     */
    public native String getPskey(String url);

    /**
     * Get PT4Token.
     * @param str Input string
     * @return PT4Token
     */
    public native String getPT4Token(String str);

    /**
     * Get GTK.
     * @param str Input string
     * @return GTK
     */
    public native String getGTK(String str);

    /**
     * Get BKN.
     * @param pskey Pskey
     * @return BKN
     */
    public native long getBKN(String pskey);

    // Other Methods
    /**
     * Get current top activity.
     * @return Activity object or null
     */
    public native android.app.Activity getActivity();

    /**
     * Show a toast message.
     * @param content Content to show
     */
    public native void toast(Object content);

    /**
     * Load another Java script file.
     * @param Path Script file path
     */
    public native void load(String Path);

    /**
     * Load a JAR file.
     * @param JarPath JAR file path
     */
    public native void loadJar(String JarPath);

    /**
     * Evaluate and execute Java code dynamically.
     * @param code Java code string
     */
    public native void eval(String code);

    /**
     * Log an exception to the script directory.
     * @param throwable Throwable object
     */
    public native void error(Throwable throwable);

    /**
     * Output log to the script directory.
     * @param content Log content
     */
    public native void log(Object content);

    /**
     * Perform an HTTP GET request.
     * @param url URL
     * @return Response content
     */
    public native String httpGet(String url);

    /**
     * Perform an HTTP GET request with headers.
     * @param url     URL
     * @param headers Request headers
     * @return Response content
     */
    public native String httpGet(String url, Map<String, String> headers);

    /**
     * Perform an HTTP POST request with form data.
     * @param url  URL
     * @param data Form data
     * @return Response content
     */
    public native String httpPost(String url, Map<String, String> data);

    /**
     * Perform an HTTP POST request with form data and headers.
     * @param url     URL
     * @param headers Request headers
     * @param data    Form data
     * @return Response content
     */
    public native String httpPost(String url, Map<String, String> headers, Map<String, String> data);

    /**
     * Perform an HTTP POST request with JSON data.
     * @param url  URL
     * @param data JSON data
     * @return Response content
     */
    public native String httpPostJson(String url, String data);

    /**
     * Perform an HTTP POST request with JSON data and headers.
     * @param url     URL
     * @param headers Request headers
     * @param data    JSON data
     * @return Response content
     */
    public native String httpPostJson(String url, Map<String, String> headers, String data);

    /**
     * Download a file via HTTP.
     * @param url  File URL
     * @param path Save path (relative to script directory)
     */
    public native void httpDownload(String url, String path);

    /**
     * Download a file via HTTP with headers.
     * @param url     File URL
     * @param path    Save path (relative to script directory)
     * @param headers Request headers
     */
    public native void httpDownload(String url, String path, Map<String, String> headers);

    // File Operations
    /**
     * Read text from a file.
     * @param path File path
     * @return File content
     */
    public native String readFileText(String path);

    /**
     * Write text to a file (overwrite mode).
     * @param path Text file path
     * @param text Text content
     */
    public native void writeTextToFile(String path, String text);

    /**
     * Append text to a file.
     * @param path Text file path
     * @param text Text content
     */
    public native void writeTextAppendToFile(String path, String text);

    /**
     * Read bytes from a file.
     * @param path File path
     * @return Byte array
     */
    public native byte[] readFileBytes(String path);

    /**
     * Write bytes to a file.
     * @param path  File path
     * @param bytes Byte array
     */
    public native void writeBytesToFile(String path, byte[] bytes);

    // Floating Window Menu Methods
    /**
     * Add a permanent menu item.
     * @param Name          Display name
     * @param CallbackName  Callback method name
     * @return Item ID
     */
    public native String addItem(String Name, String CallbackName);

    /**
     * Add a temporary menu item.
     * @param ItemName      Display name
     * @param CallbackName  Callback method name
     */
    public native void addTemporaryItem(String ItemName, String CallbackName);

    /**
     * Remove a menu item.
     * @param ItemID Item ID
     */
    public native void removeItem(String ItemID);

    // Long Press Message Menu Methods
    /**
     * Add a menu item for long-pressing a message.
     * @param Name          Display name
     * @param CallbackName  Callback method name
     * @return Item ID
     */
    public native String addMenuItem(String Name, String CallbackName);

    // Current Window Methods
    /**
     * Get current chat type.
     * @return 1 for private chat, 2 for group chat
     */
    public native int getChatType();

    /**
     * Get current group number.
     * @return Group number or empty string
     */
    public native String getCurrentGroupUin();

    /**
     * Get current friend's QQ number.
     * @return Friend's QQ number or empty string
     */
    public native String getCurrentFriendUin();
}