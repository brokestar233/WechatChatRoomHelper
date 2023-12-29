package com.zdy.project.wechat_chatroom_helper.wechat.chatroomView

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.bingoogolapple.swipebacklayout.BGASwipeBackLayout2
import cn.bingoogolapple.swipebacklayout.MySwipeBackLayout
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.PageType
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.io.model.ChatInfoModel
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils
import com.zdy.project.wechat_chatroom_helper.wechat.dialog.WhiteListDialogBuilder
import com.zdy.project.wechat_chatroom_helper.wechat.manager.DrawableMaker
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.RuntimeInfo
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser.WXObject
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.adapter.MainAdapter
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.adapter.MainAdapterLongClick
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.main.MainLauncherUI
import com.zdy.project.wechat_chatroom_helper.wechat.plugins.hook.message.MessageFactory
import de.robv.android.xposed.XposedHelpers
import network.ApiManager


/**
 * Created by Mr.Zdy on 2017/8/27.
 */

class ChatRoomView(private val mContext: Context, mContainer: ViewGroup, private val pageType: Int) : ChatRoomContract.View {

    private lateinit var mLongClickListener: Any

    private lateinit var mPresenter: ChatRoomContract.Presenter
    private lateinit var swipeBackLayout: MySwipeBackLayout

    private val mainView: LinearLayout
    private val mRecyclerView: RecyclerView
    private lateinit var mToolbarContainer: ViewGroup
    private lateinit var mToolbar: Toolbar

    private lateinit var mAdapter: ChatRoomRecyclerViewAdapter

    private val notifyHandler = NotifyHandler(mContext.mainLooper)
    private var refreshFlag = 0L

    private var uuid = "0"

    override val isShowing: Boolean get() = !swipeBackLayout.isOpen

    init {
        val params = ViewGroup.MarginLayoutParams(
                ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.MATCH_PARENT)

        mainView = LinearLayout(mContext)
        mainView.layoutParams = ViewGroup.LayoutParams(ScreenUtils.getScreenWidth(mContext),
                ViewGroup.LayoutParams.MATCH_PARENT)
        mainView.orientation = LinearLayout.VERTICAL


        mRecyclerView = object : RecyclerView(mContext) {
            override fun dispatchTouchEvent(event: MotionEvent): Boolean {

                val rawX = event.rawX
                val rawY = event.rawY
                val coordinate = intArrayOf(rawX.toInt(), rawY.toInt())
                XposedHelpers.setObjectField(mLongClickListener, MainAdapterLongClick.CoordinateField.name, coordinate)

                return super.dispatchTouchEvent(event)
            }
        }
        mRecyclerView.id = android.R.id.list
        mRecyclerView.layoutManager = LinearLayoutManager(mContext)
        mRecyclerView.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mRecyclerView.setBackgroundColor(Color.parseColor("#" + AppSaveInfo.helperColorInfo(mContext)))


        mainView.addView(initToolbar())
        mainView.addView(mRecyclerView)
        mainView.isClickable = true
        mainView.setPadding(0, ScreenUtils.getStatusHeight(mContext), 0, 0)

        initSwipeBack()

        mContainer.addView(swipeBackLayout, params)

        try {
            uuid = Settings.Secure.getString(mainView.context.contentResolver, Settings.Secure.ANDROID_ID)
            ApiManager.sendRequestForUserStatistics("init", uuid, Build.MODEL)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

    }


    private fun initSwipeBack() {
        swipeBackLayout = MySwipeBackLayout(mContext)
        swipeBackLayout.attachToView(mainView, mContext)
        swipeBackLayout.setPanelSlideListener(object : BGASwipeBackLayout2.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
            }

            override fun onPanelOpened(panel: View) {
                RuntimeInfo.currentPage = PageType.MAIN
            }

            override fun onPanelClosed(panel: View) {

            }
        })
    }


    override fun setOnItemActionListener(listener: ChatRoomRecyclerViewAdapter.OnItemActionListener) {
        mAdapter.setOnItemActionListener(listener)
    }


    override fun show() {
        LogUtils.log("TrackHelperCan'tOpen, ChatRoomView -> show no params")
        show(ScreenUtils.getScreenWidth(mContext))
    }

    override fun dismiss() {
        dismiss(0)
    }

    override fun show(offest: Int) {
        LogUtils.log("TrackHelperCan'tOpen, ChatRoomView -> show, offest = ${offest}, swipeBackLayout = ${swipeBackLayout}")

        mRecyclerView
                .viewTreeObserver
                .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        LogUtils.log("showMessageRefresh RecyclerView notify finish , pageType = " + PageType.printPageType(pageType))
                        mRecyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        if (swipeBackLayout.mSlideableView == null) {
                            MainLauncherUI.restartMainActivity()
                        } else {
                            swipeBackLayout.closePane()
                        }
                    }
                })
        if (swipeBackLayout.mSlideableView == null) {
            MainLauncherUI.restartMainActivity()
            return
        }

        refreshInner()
    }

    override fun dismiss(offest: Int) {
        swipeBackLayout.openPane()
    }

    override fun getCurrentData(): ArrayList<ChatInfoModel> {
        return mAdapter.data
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun init() {
        mAdapter = ChatRoomRecyclerViewAdapter(mContext)
        LogUtils.log("mRecyclerView = $mRecyclerView, mAdapter = $mAdapter")
        mRecyclerView.adapter = mAdapter

        mLongClickListener = MainAdapterLongClick.getConversationLongClickClassConstructor()
                .newInstance(MainAdapter.originAdapter, MainAdapter.listView, MainLauncherUI.launcherUI, intArrayOf(300, 300))
        setOnItemActionListener(object : ChatRoomRecyclerViewAdapter.OnItemActionListener {
            override fun onItemClick(view: View, relativePosition: Int, chatInfoModel: ChatInfoModel) {

                /**
                 * 通过位运算确定当前回话是企业号
                 */
                if (chatInfoModel.field_attrflag.and(0x200000) == 0x200000) {
                    val intent = Intent(mContext, RuntimeInfo.classloader.loadClass("com.tencent.mm.ui.conversation.EnterpriseConversationUI"))
                    intent.putExtra("enterprise_biz_name", chatInfoModel.field_username)
                    intent.putExtra("enterprise_biz_display_name", chatInfoModel.nickname)
                    intent.putExtra("enterprise_from_scene", 1)
                    mContext.startActivity(intent)
                } else {
                    XposedHelpers.callMethod(MainLauncherUI.launcherUI, WXObject.MainUI.M.StartChattingOfLauncherUI, chatInfoModel.field_username, null, true)
                }
            }

            override fun onItemLongClick(view: View, relativePosition: Int, chatInfoModel: ChatInfoModel): Boolean {
                MainAdapterLongClick.onItemLongClickMethodInvokeGetItemFlagNickName = chatInfoModel.field_username.toString()
                XposedHelpers.callMethod(mLongClickListener, "onItemLongClick",
                        arrayOf(AdapterView::class.java, View::class.java, Int::class.java, Long::class.java),
                        MainAdapter.listView, view, 100000 + relativePosition + MainAdapter.listView.headerViewsCount, 0)
                return true
            }
        })


    }


    override fun refreshList(isForce: Boolean, data: Any?) {
        //强制刷新 直接刷新
        if (isForce) {
            refreshInner()
            return
        }

        LogUtils.log("MessageHook 2021-09-18, refresh, RuntimeInfo.currentPage = ${RuntimeInfo.currentPage}")

        //在主页的时候不刷
        if (RuntimeInfo.currentPage == PageType.MAIN) return


        val currentTimeMillis = System.currentTimeMillis()

        //上次刷新距离现在的时间间隔
        val interval = currentTimeMillis - refreshFlag

        LogUtils.log("MessageHook 2021-09-18, refresh, currentTimeMillis = $currentTimeMillis, refreshFlag = $refreshFlag, interval = $interval")

        //刷新小于三秒，安排三秒后刷新
        if (interval < 3000) {
            notifyHandler.removeCallbacksAndMessages(null)//先清空之前的安排消息
            notifyHandler.sendEmptyMessageDelayed(1, 3000 - interval)
        } else {
            refreshInner()
        }

    }

    private fun refreshInner() {
        LogUtils.log("MessageHook 2021-09-18, refreshInner")
        mainView.post {
            val newData =
                    if (pageType == PageType.CHAT_ROOMS) MessageFactory.getSpecChatRoom()
                    else MessageFactory.getSpecOfficial()

            mAdapter.data = newData
            mAdapter.notifyDataSetChanged()
            refreshFlag = System.currentTimeMillis()
        }
    }

    inner class NotifyHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            refreshList(false, null)
        }
    }

    private fun initToolbar(): View {
        mToolbarContainer = RelativeLayout(mContext)

        mToolbar = Toolbar(mContext)

        val height = ScreenUtils.dip2px(mContext, 48f)
        val tintColor = Color.parseColor("#" + AppSaveInfo.nicknameColorInfo(mContext))

        mToolbar.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        mToolbar.navigationIcon = BitmapDrawable(mContext.resources, DrawableMaker.getArrowBitMapForBack(tintColor))

        mToolbar.setNavigationOnClickListener { mPresenter.dismiss() }
        mToolbar.setBackgroundColor(Color.parseColor("#" + AppSaveInfo.toolbarColorInfo(mContext)))

//        when (pageType) {
//            PageType.CHAT_ROOMS -> mToolbar.title = "群聊"
//            PageType.OFFICIAL -> mToolbar.title = "服务号"
//        }
//        mToolbar.setTitleTextColor(Color.parseColor("#" + AppSaveInfo.nicknameColorInfo(mContext)))
//
        mToolbar.addView(TextView(mContext).apply {
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).apply {
                setMargins(0, 0, ScreenUtils.dip2px(mContext, 72f), 0)
            }
            setTextColor(Color.parseColor("#" + AppSaveInfo.nicknameColorInfo(mContext)))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            typeface = Typeface.DEFAULT_BOLD
            when (pageType) {
                PageType.CHAT_ROOMS -> text = "群聊"
                PageType.OFFICIAL -> text = "服务号"
            }
        })

        val clazz: Class<*>
        try {
            clazz = Class.forName("android.widget.Toolbar")
//            val mTitleTextView = clazz.getDeclaredField("mTitleTextView")
//            mTitleTextView.isAccessible = true
//            val textView = mTitleTextView.get(mToolbar) as TextView
//            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
//            textView.setTextColor(tintColor)

            val mNavButtonView = clazz.getDeclaredField("mNavButtonView")
            mNavButtonView.isAccessible = true
            val imageButton = mNavButtonView.get(mToolbar) as ImageButton
            val layoutParams = imageButton.layoutParams
            layoutParams.height = height
            layoutParams.width = ScreenUtils.dip2px(mContext, 56f)
            imageButton.layoutParams = layoutParams
            imageButton.scaleType = ImageView.ScaleType.FIT_CENTER

        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }

        val imageView = ImageView(mContext)

        val params = RelativeLayout.LayoutParams(height, height)
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)

        imageView.layoutParams = params
        val padding = height / 8
        imageView.setPadding(padding, padding, padding, padding)
        imageView.setImageDrawable(DrawableMaker.handleAvatarDrawable(mContext, pageType, 0x00000000, tintColor))

        imageView.setOnClickListener {
            val whiteListDialogBuilder = WhiteListDialogBuilder()
            when (pageType) {
                PageType.OFFICIAL -> whiteListDialogBuilder.pageType = PageType.OFFICIAL
                PageType.CHAT_ROOMS -> whiteListDialogBuilder.pageType = PageType.CHAT_ROOMS
            }
            val dialog = whiteListDialogBuilder.getWhiteListDialog(mContext)
            dialog.show()
            dialog.setOnDismissListener {
                when (pageType) {
                    PageType.OFFICIAL -> RuntimeInfo.officialViewPresenter?.refreshList(false, Any())
                    PageType.CHAT_ROOMS -> RuntimeInfo.chatRoomViewPresenter?.refreshList(false, Any())
                }
                MainLauncherUI.refreshListMainUI()
            }
        }

        mToolbarContainer.addView(mToolbar)
        mToolbarContainer.addView(imageView)

        return mToolbarContainer
    }

    override fun setPresenter(presenter: ChatRoomContract.Presenter) {
        mPresenter = presenter
    }
}
