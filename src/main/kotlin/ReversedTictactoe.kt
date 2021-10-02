package dada

import dada.command.Command
import dada.command.PlayChessCommand
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.utils.info
import java.lang.reflect.Member

object ReversedTictactoe : KotlinPlugin(
    JvmPluginDescription(
        id = "dada.reversedTictactoe",
        version = "1.0-SNAPSHOT",
    )
) {
    val gameList = mutableListOf<Game>()

    override fun onEnable() {
        Command.register()
        PlayChessCommand.register()

        AbstractPermitteeId.AnyUser.permit(Command.permission)
        AbstractPermitteeId.AnyUser.permit(PlayChessCommand.permission)

        logger.info { "Plugin loaded" }
        globalEventChannel()
    }

}
