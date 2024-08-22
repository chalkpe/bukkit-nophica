package pe.chalk.bukkit.nophica

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.plugin.java.JavaPlugin

class Nophica : JavaPlugin(), Listener {
    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
    }

    @EventHandler
    fun onEntityChangeBlock(event: EntityChangeBlockEvent) {
        if (event.block.type == Material.FARMLAND && event.to == Material.DIRT) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        // only player has permission
        val player = event.player
        if (!player.hasPermission("nophica.harvest")) return

        // only hoe is in main hand
        val item = player.inventory.itemInMainHand
        if (!item.type.name.endsWith("_HOE")) return

        // only on right click block
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        // check for clicked block type
        val block = event.clickedBlock ?: return
        when (block.type) {
            Material.WHEAT -> harvestAgeable(player, block, Material.WHEAT_SEEDS, Material.WHEAT)
            Material.BEETROOTS -> harvestAgeable(player, block, Material.BEETROOT_SEEDS, Material.BEETROOT)
            Material.POTATOES -> harvestAgeable(player, block, Material.POTATO, null)
            Material.CARROTS -> harvestAgeable(player, block, Material.CARROT, null)
            else -> return
        }
    }

    private fun harvestAgeable(player: Player, block: Block, seeds: Material, output: Material?) {
        val blockData = block.blockData

        // only maximum age crop
        if (blockData !is Ageable) return
        if (blockData.age < blockData.maximumAge) return

        blockData.age = 0
        block.blockData = blockData

        val item = player.inventory.itemInMainHand
        val itemMeta = item.itemMeta ?: return
        val fortuneLevel = itemMeta.getEnchantLevel(Enchantment.FORTUNE)

        if (output != null) {
            val fortuneChance = (1..100).random() <= (fortuneLevel * 25)
            val fortuneTarget = arrayOf("output", "seeds").random()

            val outputAmount = if (fortuneChance && fortuneTarget == "output") {
                2
            } else {
                1
            }
            block.world.dropItemNaturally(block.location, ItemStack(output, outputAmount))

            val seedsAmount = if (fortuneChance && fortuneTarget == "seeds") {
                (1..2).random()
            } else {
                0
            }
            block.world.dropItemNaturally(block.location, ItemStack(seeds, seedsAmount))
        } else {
            val seedsAmount = (1..(3 + fortuneLevel)).count { (1..100).random() <= 57 }
            block.world.dropItemNaturally(block.location, ItemStack(seeds, seedsAmount))
        }

        if (player.gameMode != GameMode.CREATIVE) {
            useItemInHand(player)
        }
    }

    private fun useItemInHand(player: Player) {
        val item = player.inventory.itemInMainHand
        val itemMeta = item.itemMeta ?: return
        val unbreakingLevel = itemMeta.getEnchantLevel(Enchantment.UNBREAKING)

        if (itemMeta.isUnbreakable || itemMeta !is Damageable) return
        if ((1..100).random() >= (100 / (unbreakingLevel + 1))) return

        itemMeta.damage += 1
        item.itemMeta = itemMeta

        player.inventory.setItemInMainHand(
            if (itemMeta.damage >= item.type.maxDurability) {
                player.playSound(player.location, Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f)
                ItemStack(Material.AIR)
            } else {
                item
            }
        )
    }
}
