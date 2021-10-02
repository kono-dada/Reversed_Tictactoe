package dada

import dada.event.PlayChessEvent
import dada.event.QuitEvent
import kotlinx.coroutines.*
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage

class Game constructor(player1: Member, player2: Member, Wager: Int = 0) {
    private var nowTurn = player1 //这储存了本回合的人
    private var chessBoard = ChessBoard()
    private var chessColor = Chess.WHITE
    private var running = false
    val p1 = player1
    val p2 = player2
    private val wager = Wager

    /*
    游戏中会开启一个job，实时监听群里的消息，以此推进棋局进行
     */
    private val game = GlobalScope.launch {
        val scope = CoroutineScope(SupervisorJob())
        val scopedChannel = coroutineScope {
            globalEventChannel().parentScope(scope)
        }

        scopedChannel.subscribeAlways<GroupMessageEvent> {
            if (message.content == "开始游戏" && (sender == p1 || sender == p2)) {
                beginning()
                scope.cancel()
            }
        }

        //---------------以上行为确定棋局的开始，并在收到开始的指令后关闭监听开始指令的监听器--------------

        globalEventChannel().subscribeAlways<PlayChessEvent> {
            if (ply == nowTurn && running) {
                try {
                    play(pX, pY)
                    nextTurn()
                } catch (e: Exception) {
                    ply.group.sendMessage(At(ply) + "QAQ明乃搞不懂你要下哪里")
                }
            }
        }

        globalEventChannel().subscribeAlways<QuitEvent> {
            if (ply == p1 || ply == p2) {
                ply.group.sendMessage(PlainText("退出成功\n")+ At(ply)
                        + PlainText("输给了")
                        + At(theOtherPlayer(ply))
                        + PlainText("${wager}个明乃币"))
                settle(ply)
            }
        }
    }

    /*
    下棋
     */
    private fun play(x: Int, y: Int) {
        if (chessBoard.chessBoard[y][x] == Chess.NONE.value) {
            chessBoard.chessBoard[y][x] = chessColor.value
        } else {
            throw Exception("chessExists")
        }
    }

    /*
    开局。为了防止模仿棋的出现，会随机在棋盘上安置两枚棋子，不失公平性。
     */
    private suspend fun beginning() {
        chessBoard = ChessBoard()
        running = true
        when ((1..8).random()) {
            1 -> {
                chessBoard.chessBoard[0][2] = 1
                chessBoard.chessBoard[1][1] = 2
            }
            2 -> {
                chessBoard.chessBoard[2][0] = 1
                chessBoard.chessBoard[1][1] = 2
            }
            3 -> {
                chessBoard.chessBoard[0][1] = 1
                chessBoard.chessBoard[1][2] = 2
            }
            4 -> {
                chessBoard.chessBoard[2][3] = 1
                chessBoard.chessBoard[1][2] = 2
            }
            5 -> {
                chessBoard.chessBoard[1][0] = 1
                chessBoard.chessBoard[2][1] = 2
            }
            6 -> {
                chessBoard.chessBoard[3][2] = 1
                chessBoard.chessBoard[2][1] = 2
            }
            7 -> {
                chessBoard.chessBoard[1][3] = 1
                chessBoard.chessBoard[2][2] = 2
            }
            8 -> {
                chessBoard.chessBoard[3][1] = 1
                chessBoard.chessBoard[2][2] = 2
            }
        }
        nextTurn()
    }

    private suspend fun nextTurn() {
        /*
        每次前进一个回合, 都判断一次胜负.
         */
        when (judge()) {
            0 -> {
                nowTurn.group.sendMessage(
                    PlainText("游戏结束\n")
                            + At(nowTurn)
                            + PlainText("输给了")
                            + At(theOtherPlayer())
                            + PlainText("${wager}个明乃币")
                            + chessBoard.board().uploadAsImage(nowTurn.group)
                )
                settle(nowTurn)
            }
            1 -> {
                nowTurn = theOtherPlayer()
                chessColor = theOtherColor(chessColor)
                nowTurn.group.sendMessage(
                    At(nowTurn)
                            + "轮到你落子了哦（你是\""
                            + (if (chessColor == Chess.BLACK) "Ｘ" else "Ｏ")
                            + "\"）"
                            + chessBoard.board().uploadAsImage(nowTurn.group)
                )

            }
            2 -> {
                nowTurn.group.sendMessage("平局了哦")
                running = false
                chessBoard = ChessBoard()
                game.cancel()
            }
        }
    }

    private fun settle(loser:Member){
        loser.pay(wager)
        theOtherPlayer(loser).addPoints(wager)
        this.running = false
        this.chessBoard = ChessBoard()
        game.cancel()
    }

    private fun theOtherPlayer(ply: Member = nowTurn): Member {
        return if (p1 == ply) p2 else p1
    }

    private fun judge(): Int {
        return chessBoard.judge(chessColor.value)
    }

    private fun theOtherColor(color: Chess): Chess {
        return if (color == Chess.BLACK) Chess.WHITE else Chess.BLACK
    }
}

