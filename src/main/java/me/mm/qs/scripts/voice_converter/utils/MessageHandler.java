package me.mm.qs.scripts.voice_converter.utils;

import me.mm.qs.script.QScriptBase;
import me.mm.qs.script.types.MessageData;
import me.mm.qs.scripts.voice_converter.constants.MessageType;

import static me.mm.qs.script.Globals.*;

/**
 * Message handling utilities.
 * Methods in this class will be extracted to the module file.
 */
public class MessageHandler extends QScriptBase {

    /**
     * Parse time from message like "禁言@xxx 1天"
     */
    public int parseTimeBymessage(MessageData msg) {
        int timeStartIndex = msg.MessageContent.lastIndexOf(" ");
        String date = msg.MessageContent.substring(timeStartIndex + 1);
        date = date.trim();
        String t = "";
        if (date != null && !"".equals(date)) {
            for (int i = 0; i < date.length(); i++) {
                if (date.charAt(i) >= 48 && date.charAt(i) <= 57) {
                    t += date.charAt(i);
                }
            }
        }
        int time = Integer.parseInt(t);
        if (date.contains("天")) {
            return time * 60 * 60 * 24;
        } else if (date.contains("时") || date.contains("小时")) {
            return 60 * 60 * time;
        } else if (date.contains("分") || date.contains("分钟")) {
            return 60 * time;
        }
        return time;
    }

    /**
     * Check if message is a mute command
     */
    public boolean isMuteCommand(MessageData msg) {
        return msg.IsSend 
            && msg.MessageContent.matches("禁言 ?@[\\s\\S]+[0-9]+(天|分|时|小时|分钟|秒)") 
            && msg.mAtList.size() >= 1;
    }

    /**
     * Check if message is @me
     */
    public static boolean isAtMe(MessageData msg) {
        return msg.IsSend && msg.MessageContent.contains("@" + myUin);
    }

    /**
     * Test method - check if text contains keyword
     */
    public boolean containsKeyword(String text, String keyword) {
        return text != null && text.contains(keyword);
    }
    
    /**
     * 示例: 根据消息类型处理不同逻辑
     */
    public String getMessageTypeDesc(MessageData msg) {
        switch (msg.MessageType) {
            case MessageType.TEXT_OR_IMAGE:
                return "文字或图片消息";
            case MessageType.CARD:
                return "卡片消息";
            case MessageType.MIXED_MEDIA:
                return "图文消息";
            case MessageType.VOICE:
                return "语音消息";
            case MessageType.FILE:
                return "文件消息";
            case MessageType.REPLY:
                return "回复消息";
            default:
                return "未知类型";
        }
    }

}
