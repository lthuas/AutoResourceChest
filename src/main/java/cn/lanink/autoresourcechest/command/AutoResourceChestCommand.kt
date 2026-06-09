package cn.lanink.autoresourcechest.command

import cn.lanink.autoresourcechest.AutoResourceChest
import cn.lanink.autoresourcechest.chest.ChestManager
import cn.lanink.autoresourcechest.utils.Utils
import cn.nukkit.Player
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.item.StringItem
import cn.nukkit.utils.Config
import java.io.File
import java.util.*

/**
 * @author LT_Name
 */
class AutoResourceChestCommand : Command("AutoResourceChest") {

    private val autoResourceChest: AutoResourceChest = AutoResourceChest.instance!!

    init {
        this.aliases = arrayOf("arc")
        this.usage = "/ARC help 查看命令帮助"
        this.description = "AutoResourceChest 管理命令"
        this.permission = "autoresourcechest.admin"

        this.commandParameters.clear()
        this.commandParameters["create"] = arrayOf(
            CommandParameter.newEnum("create", arrayOf("create")),
            CommandParameter.newType("资源箱名称", CommandParamType.TEXT)
        )
        this.commandParameters["place"] = arrayOf(
            CommandParameter.newEnum("place", arrayOf("place")),
            CommandParameter.newType("资源箱名称", false, CommandParamType.TEXT)
        )
        this.commandParameters["saveitem"] = arrayOf(
            CommandParameter.newEnum("saveitem", arrayOf("saveitem")),
            CommandParameter.newType("物品名称", false, CommandParamType.TEXT)
        )
        this.commandParameters["reload"] = arrayOf(
            CommandParameter.newEnum("reload", arrayOf("reload"))
        )
    }

    override fun execute(player: CommandSender?, command: String?, args: Array<out String>?): Boolean {
        player ?: return false
        command ?: return false
        if ((command == "autoresourcechest") || (command == "arc")) {
            if (player !is Player) {
                player.sendMessage("§e>> §c请在游戏内使用此命令")
                return true
            }
            if (args.isNullOrEmpty()) {
                this.sendCommandHelp(player)
                return true
            }

            when(args[0].lowercase(Locale.getDefault())) {
                "create" -> {
                    if (args.size > 1) {
                        val name = args[1]
                        if (File("${autoResourceChest.dataFolder}/Chests/$name.yml").exists()) {
                            player.sendMessage("§e>> §c已存在名为 $name 的资源箱配置！")
                            return true
                        }
                        autoResourceChest.saveResource("chest.yml", "Chests/$name.yml", true)
                        autoResourceChest.chestConfigMap[name] = ChestManager(name, Config("${autoResourceChest.dataFolder}/Chests/$name.yml", Config.YAML))
                        player.sendMessage("§e>> §a新的资源箱配置 $name 创建成功！")
                    } else {
                        player.sendMessage("§e>> §c请输入资源箱名字！")
                    }
                }

                "place" -> {
                    if (args.size > 1) {
                        val name = args[1]
                        val chestManager = autoResourceChest.chestConfigMap[name]
                        if (chestManager == null) {
                            player.sendMessage("§e>> §c不存在名为 $name 的资源箱配置，请先创建！")
                            return true
                        }
                        autoResourceChest.placeChestPlayer[player] = chestManager
                        player.sendMessage("§e>> §a请放置一个箱子作为资源箱！")
                    } else {
                        player.sendMessage("§e>> §c请输入资源箱名字！")
                    }
                }

                "saveItem".lowercase(Locale.getDefault()) -> {
                    if (args.size > 1) {
                        val name = args[1]
                        val item = player.inventory.itemInHand
                        if (item.id == 0 || !item.hasCompoundTag()) {
                            player.sendMessage("普通物品无需保存！可直接使用物品ID：${if (item is StringItem) item.getNamespaceId() else "${item.id}:${item.damage}"}")
                            return true
                        }
                        val config = autoResourceChest.getNbtConfig()
                        if (config.keys.contains(name)) {
                            player.sendMessage("NBT物品：$name 已存在！换个名字吧！")
                        } else {
                            var itemString = if (item is StringItem) {
                                "${item.getNamespaceId()}:${Utils.bytesToBase64(item.compoundTag)}"
                            } else {
                                "${item.id}:${item.damage}:${Utils.bytesToBase64(item.compoundTag)}"
                            }
                            config.set(name, itemString)
                            config.save()
                            player.sendMessage("NBT物品：$name 保存成功！")
                        }
                    }else{
                        player.sendMessage("请输入名称")
                    }
                }

                "reload" -> {
                    for (chestManager in autoResourceChest.chestConfigMap.values) {
                        chestManager.closeAllChest()
                    }
                    autoResourceChest.chestConfigMap.clear()
                    autoResourceChest.loadAllChests()
                    player.sendMessage("已重载资源箱配置！请在后台查看详情！")
                }

                else -> {
                    this.sendCommandHelp(player)
                }
            }
            return true
        }
        return false
    }

    private fun sendCommandHelp(sender: CommandSender) {
        sender.sendMessage("§a/arc create <配置名称> §e创建一个资源箱配置\n" +
                "§a/arc place <配置名称> §e放置一个资源箱\n" +
                "§a/arc saveItem <物品名称> §e保存手上的物品\n" +
                "§a/arc reload §e从配置文件重新加载资源箱配置\n")
    }

}