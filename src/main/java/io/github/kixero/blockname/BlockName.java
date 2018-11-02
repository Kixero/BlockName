package io.github.kixero.blockname;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlockName extends JavaPlugin
{
    @Override
    public void onEnable()
    {
    
    }
    
    private static void sendMessage(Player player, String message)
    {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (cmd.getName().equalsIgnoreCase("blockname"))
        {
            if (!(sender instanceof Player))
            {
                sender.sendMessage("This command can only be run by a player.");
            }
            else
            {
                String blockName = "";
                
                for (String str : args)
                {
                    blockName = blockName.concat(str);
                    blockName = blockName.concat(" ");
                }
                
                Player player = (Player) sender;
                
                Block block = player.getTargetBlock(null, 10);
                

                if (block.hasMetadata("Name"))
                {
                    block.removeMetadata("Name", this);
                }
                block.setMetadata("Name", new FixedMetadataValue(this, blockName));
            
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void onDisable()
    {
    
    }
}
