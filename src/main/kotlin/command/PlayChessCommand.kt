package dada.command

import dada.ReversedTictactoe
import dada.event.PlayChessEvent
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.event.broadcast

/*
收到落子指令后广播一个落子事件。此事件被Game中的监听器接收后，就能进行落子
这么做的目的是利用mirai的command系统，自动解析玩家的指令，懒得构造新的指令解析方式来确定玩家落子位置
 */
object PlayChessCommand : SimpleCommand(
    ReversedTictactoe,
    "play"
) {
    @Handler
    suspend fun MemberCommandSender.lay(posX: Int, posY: Int) {
        PlayChessEvent(user, posX - 1, posY - 1).broadcast()
    }
}