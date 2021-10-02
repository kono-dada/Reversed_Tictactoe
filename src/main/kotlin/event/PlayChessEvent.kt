package dada.event

import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.AbstractEvent

class PlayChessEvent(val ply: Member, val pX: Int, val pY: Int) : AbstractEvent()