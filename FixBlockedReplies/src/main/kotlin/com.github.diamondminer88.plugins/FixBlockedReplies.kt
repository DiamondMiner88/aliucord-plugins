package com.github.diamondminer88.plugins

import android.content.Context
import android.os.Bundle
import android.view.View
import com.aliucord.Utils
import com.aliucord.Utils.createCheckedSetting
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.SettingsAPI
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.settings.delegate
import com.aliucord.widgets.BottomSheet
import com.discord.models.member.GuildMember
import com.discord.models.user.CoreUser
import com.discord.models.user.User
import com.discord.stores.StoreMessageReplies
import com.discord.views.CheckedSetting.ViewType.SWITCH
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.adapter.`WidgetChatListAdapterItemMessage$configureReplyPreview$1`
import com.discord.widgets.chat.list.entries.MessageEntry

@Suppress("unused")
@AliucordPlugin
class FixBlockedReplies : Plugin() {
	private val msgBlockedStrId = Utils.getResId("reply_quote_message_blocked", "string")

	private val configReplyContentWithId =
		WidgetChatListAdapterItemMessage::class.java.getDeclaredMethod(
			"configureReplyContentWithResourceId",
			Int::class.javaPrimitiveType
		).apply { isAccessible = true }

	private val configReplyAuthor = WidgetChatListAdapterItemMessage::class.java.getDeclaredMethod(
		"configureReplyAuthor",
		User::class.java,
		GuildMember::class.java,
		MessageEntry::class.java
	).apply { isAccessible = true }

	private val replyHolder = WidgetChatListAdapterItemMessage::class.java.getDeclaredField("replyHolder")
		.apply { isAccessible = true }

	init {
		settingsTab = SettingsTab(
			FixBlockedRepliesSettings::class.java,
			SettingsTab.Type.BOTTOM_SHEET
		).withArgs(settings)
	}

	override fun start(ctx: Context) {
		patcher.before<MessageEntry.ReplyData>(
			StoreMessageReplies.MessageState::class.java,
			MessageEntry::class.java,
			Boolean::class.javaPrimitiveType!!
		) {
			if (settings.getBool("showContent", false))
				it.args[2] = false
		}

		patcher.after<WidgetChatListAdapterItemMessage>(
			"configureReplyPreview",
			MessageEntry::class.java
		) {
			val entry = it.args[0] as MessageEntry
			if (entry.replyData == null || !entry.replyData.isRepliedUserBlocked)
				return@after

			val replyEntry = entry.replyData.messageEntry
			configReplyAuthor.invoke(
				it.thisObject,
				CoreUser(replyEntry.message.author),
				replyEntry.author,
				replyEntry
			)

			val replyHolder = replyHolder.get(it.thisObject) as View
			replyHolder.setOnClickListener(
				`WidgetChatListAdapterItemMessage$configureReplyPreview$1`(
					replyEntry.message
				)
			)

			configReplyContentWithId.invoke(
				it.thisObject,
				msgBlockedStrId
			)
		}
	}

	override fun stop(context: Context) {
		patcher.unpatchAll()
	}
}

class FixBlockedRepliesSettings(settings: SettingsAPI) : BottomSheet() {
	private var showContent: Boolean by settings.delegate(false)

	override fun onViewCreated(view: View, bundle: Bundle?) {
		super.onViewCreated(view, bundle)
		val ctx = view.context

		addView(
			createCheckedSetting(
				ctx, SWITCH,
				"Reply content",
				"Show reply content"
			).apply {
				isChecked = showContent
				setOnCheckedListener { showContent = it }
			})
	}
}
