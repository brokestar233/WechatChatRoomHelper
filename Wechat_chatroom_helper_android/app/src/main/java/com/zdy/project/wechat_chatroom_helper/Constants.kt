package com.zdy.project.wechat_chatroom_helper

import android.graphics.Color

/**
 * Created by zhudo on 2017/7/2.
 */

object Constants {

    val WECHAT_PACKAGE_NAME = "com.tencent.mm"


    var defaultValue: DefaultValue = DefaultValue(true)

    class DefaultValue(val isWechatUpdate7: Boolean) {


        val CONVERSATION_ITEM_HEIGHT = if (!isWechatUpdate7) 64f else 72f
        val DEFAULT_TOOLBAR_TINT_COLOR = Color.parseColor("#" + if (!isWechatUpdate7) "FFFFFF" else "181818")

        val CONVERSATION_ITEM_NICKNAME_PADDING_TOP = if (!isWechatUpdate7) 10f else 14f
        val CONVERSATION_ITEM_CONTENT_PADDING_BOTTOM = if (!isWechatUpdate7) 12f else 16f

        val DEFAULT_DARK_TOOLBAR_COLOR = "191919"
        val DEFAULT_DARK_HELPER_COLOR = "232323"
        val DEFAULT_DARK_NICKNAME_COLOR = "D5D5D5"
        val DEFAULT_DARK_CONTENT_COLOR = "696969"
        val DEFAULT_DARK_TIME_COLOR = "6E6E6E"
        val DEFAULT_DARK_DIVIDER_COLOR = "383838"
        val DEFAULT_DARK_HIGHLIGHT_COLOR = "2F2F2F"


        val DEFAULT_LIGHT_TOOLBAR_COLOR = if (!isWechatUpdate7) "303030" else "EEEEEE"
        val DEFAULT_LIGHT_HELPER_COLOR = "FFFFFF"
        val DEFAULT_LIGHT_NICKNAME_COLOR = "353535"
        val DEFAULT_LIGHT_CONTENT_COLOR = "999999"
        val DEFAULT_LIGHT_TIME_COLOR = "BBBBBB"
        val DEFAULT_LIGHT_DIVIDER_COLOR = "DADADA"
        val DEFAULT_LIGHT_HIGHLIGHT_COLOR = "F0F0F0"

    }
}
