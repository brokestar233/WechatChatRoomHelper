package com.zdy.project.wechat_chatroom_helper.wechat.chatroomView

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.io.model.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.wechat.manager.DrawableMaker
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.ConversationReflectFunction
import java.util.*


@Suppress("DEPRECATION")
/**
 * Created by Mr.Zdy on 2017/8/27.
 */

class ChatRoomRecyclerViewAdapter constructor(private val mContext: Context) : RecyclerView.Adapter<ChatRoomViewHolder>() {


    private lateinit var onItemActionListener: OnItemActionListener

    var data = ArrayList<ChatInfoModel>()

    fun setOnItemActionListener(onDialogItemActionListener: OnItemActionListener) {
        this.onItemActionListener = onDialogItemActionListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        return ChatRoomViewHolder(ChatRoomViewFactory.getItemView(mContext))
    }

    private fun getObject(position: Int): ChatInfoModel {
        return data[position]
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {

        val item = getObject(position)

        LogUtils.log("onBindViewHolder, position = $position, " + item.toString())

//        val spannableStringBuilder = SpannableStringBuilder()
//        spannableStringBuilder.append("[${item.field_unReadCount}条]")
//        val firstLength = spannableStringBuilder.length
//        spannableStringBuilder.setSpan(ForegroundColorSpan(0xFFF44336.toInt()), 0, firstLength, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
//        spannableStringBuilder.append("[${item.field_unReadMuteCount}条]")
//        spannableStringBuilder.setSpan(ForegroundColorSpan(0xFFFFA726.toInt()), firstLength, spannableStringBuilder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
//        spannableStringBuilder.append(item.content)


        holder.nickname.text = item.nickname
        holder.content.text = item.content
        holder.time.text = item.conversationTime


        if (item.field_username.isNotEmpty()) {
            ConversationReflectFunction.getConversationAvatar(item.field_username.toString(), holder.avatar)
            holder.itemView.setOnClickListener {
                onItemActionListener.onItemClick(holder.itemView, position, item)
            }
            holder.itemView.setOnLongClickListener {
                onItemActionListener.onItemLongClick(holder.itemView, position, item)
            }
        }

        holder.nickname.setTextColor(Color.parseColor("#" + AppSaveInfo.nicknameColorInfo(mContext)))
        holder.content.setTextColor(Color.parseColor("#" + AppSaveInfo.contentColorInfo(mContext)))
        holder.time.setTextColor(Color.parseColor("#" + AppSaveInfo.timeColorInfo(mContext)))
        holder.divider.setBackgroundColor(Color.parseColor("#" + AppSaveInfo.dividerColorInfo(mContext)))

        /**
         * 是否为置顶
         */
        if (item.stickyFlag != 0L) {
            holder.itemView.background = ChatRoomViewFactory.getItemViewBackgroundSticky(mContext)
        } else {
            holder.itemView.background = ChatRoomViewFactory.getItemViewBackground(mContext)
        }

        //不是免打扰的群（正常群或者服务号）
        if (item.chatRoomMuteFlag || !item.field_username.contains("@chatroom")) {

            holder.mute.visibility = View.GONE
            holder.unreadMark.background = BitmapDrawable(mContext.resources)

            if (item.unReadCount > 0) {
                holder.unreadCount.background = DrawableMaker.getRedCircleDrawable()
                if (item.unReadCount > 99) holder.unreadCount.text = "…"
                else holder.unreadCount.text = item.unReadCount.toString()
            } else {
                holder.unreadCount.background = BitmapDrawable(mContext.resources)
                holder.unreadCount.text = ""
            }


        } else {
            holder.mute.visibility = View.VISIBLE
            holder.unreadCount.background = BitmapDrawable(mContext.resources)
            holder.unreadCount.text = ""

            if (item.unReadCount > 0) holder.unreadMark.background = DrawableMaker.getRedCircleDrawable()
            else holder.unreadMark.background = BitmapDrawable(mContext.resources)

        }


    }


//    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int, payloads: MutableList<Any>) {
//        if (payloads.isEmpty()) {
//            onBindViewHolder(holder, position)
//        } else {
//            val bundle = payloads[0] as Bundle
//            val content = bundle.getCharSequence("content")
//            val conversationTime = bundle.getCharSequence("conversationTime")
//            val unReadMuteCount = bundle.getInt("unReadMuteCount")
//            val unReadCount = bundle.getInt("unReadCount")
//
//            if (content != null) holder.content.text = content
//            if (conversationTime != null) holder.time.text = conversationTime
//            if (unReadCount > 0)
//                holder.unread.background = ShapeDrawable(object : Shape() {
//                    override fun draw(canvas: Canvas, paint: Paint) {
//                        val size = (canvas.width / 2).toFloat()
//
//                        paint.isAntiAlias = true
//                        paint.color = -0x10000
//                        paint.style = Paint.Style.FILL_AND_STROKE
//                        canvas.drawCircle(size, size, size, paint)
//                    }
//                })
//            else holder.unread.background = BitmapDrawable(mContext.resources)
//        }
//    }

    override fun getItemCount(): Int {
        return data.size
    }

    interface OnItemActionListener {
        fun onItemClick(view: View, relativePosition: Int, chatInfoModel: ChatInfoModel)

        fun onItemLongClick(view: View, relativePosition: Int, chatInfoModel: ChatInfoModel): Boolean
    }


}
