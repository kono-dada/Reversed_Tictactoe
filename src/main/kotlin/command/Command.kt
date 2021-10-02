package dada.command

import dada.Game
import dada.ReversedTictactoe
import dada.enough
import dada.event.QuitEvent
import kotlinx.coroutines.*
import net.mamoe.mirai.console.command.CommandSender
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
    @SubCommand("bet")
    suspend fun MemberCommandSender.bet(target: Member, wager: Int) {
        if (user.enough(wager) && target.enough(wager)) {
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
                    ReversedTictactoe.gameList.add(Game(sender, user, wager))
                    subject.sendMessage("同意成功,发送”开始游戏“就可以开始反井字棋啦")
                    waiting.cancel()
                    scope.cancel()
                }
            }
        } else {
            subject.sendMessage("发送邀请失败……可能是你已经进入游戏了，要不然就是你或你的邀请人明乃币不足")
        }
    }

    @SubCommand("help")
    suspend fun MemberCommandSender.help() {
        subject.sendMessage(
            """
            反井字棋2.0：由星提供游戏规则，dada进行程序设计
            更新时间：2021/6/28
            更新内容：在竞技中引入了明乃币的奖励机制，优化了操作方法
            操作方法：
                输入”/t bet <群员(昵称或名片或at)> <明乃币金额>“即可向指定群友发起一次反井字棋挑战，并押上指定的金额
                被挑战者只需发发送”ok“即可接受挑战。
                棋局开始后，输入”/play <纵坐标> <横坐标>“即可落子
        """.trimIndent()
        )
    }

    @SubCommand("quit")
    suspend fun MemberCommandSender.quit(){
        QuitEvent(user).broadcast()
    }

    private fun Member.playerExists(): Boolean {
        for (game in ReversedTictactoe.gameList) {
            if ((game.p1 == this || game.p2 == this)) return true
        }
        return false
    }
}