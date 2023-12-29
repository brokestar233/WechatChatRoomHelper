package com.zdy.project.wechat_chatroom_helper.wechat.chatroomView

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

/**
 * Created by Mr.Zdy on 2017/8/27.
 */

class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var avatar: ImageView = itemView.findViewById(ChatRoomViewFactory.id_avatar)
    var nickname: TextView = itemView.findViewById(ChatRoomViewFactory.id_nickname)
    var time: TextView = itemView.findViewById(ChatRoomViewFactory.id_time)
    var msgState: ImageView = itemView.findViewById(ChatRoomViewFactory.id_msg_state)
    var content: TextView = itemView.findViewById(ChatRoomViewFactory.id_content)
    var unreadMark: View = itemView.findViewById(ChatRoomViewFactory.id_unread_mark)
    var unreadCount: TextView = itemView.findViewById(ChatRoomViewFactory.id_unread_count)
    var divider: View = itemView.findViewById(ChatRoomViewFactory.id_divider)
    var mute: View = itemView.findViewById(ChatRoomViewFactory.id_mute)

}
