package dada.command

import dada.Game
import dada.ReversedTictactoe
import dada.event.QuitEvent
import kotlinx.coroutines.*
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content

object Command : CompositeCommand(
    ReversedTictactoe,
    "t",
) {
    @SubCommand("inv")
    suspend fun MemberCommandSender.inv(target: Member) {
        subject.sendMessage("成功发送邀请")
        val scope = CoroutineScope(SupervisorJob())

        val scopedChannel = coroutineScope {
            globalEventChannel().parentScope(scope)
        }

        val waiting = GlobalScope.launch {
            delay(60000)
            subject.sendMessage(At(user) + PlainText("超过一分钟没有人回答你哦，你的邀请自动取消啦"))
            scope.cancel()
        }

        scopedChannel.subscribeAlways<GroupMessageEvent> {
            if (sender == target && message.content == "ok") {
                ReversedTictactoe.gameList.add(Game(sender, user))
                subject.sendMessage("同意成功,发送”开始游戏“就可以开始反井字棋啦")
                waiting.cancel()
                scope.cancel()
            }
        }
    }

    @SubCommand("help")
    suspend fun MemberCommandSender.help() {
        subject.sendMessage(
            """
            反井字棋2.0：由星提供游戏规则，dada进行程序设计
            更新时间：2021/6/28
            游戏规则：玩家将在4*4的棋盘中依次落子，谁先横、竖、或斜方向连成三子谁就落败。为避免模仿棋，会在棋局开始之初自动落两子
            操作方法：
                输入”/t bet <群员(昵称或名片或at)>“即可向指定群友发起一次反井字棋挑战
                被挑战者只需发发送”ok“即可接受挑战。
                棋局开始后，发送”/play <纵坐标> <横坐标>“即可落子
                若想放弃棋局，发送“/t quit”即可退出
        """.trimIndent()
        )
    }

    @SubCommand("quit")
    suspend fun MemberCommandSender.quit() {
        QuitEvent(user).broadcast()
    }
}