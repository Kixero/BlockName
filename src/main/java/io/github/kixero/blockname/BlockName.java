package io.github.kixero.blockname;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class BlockName extends JavaPlugin implements Listener
{
    private final int DISTANCE_TO_BLOCK = 5;
    
    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
        
        for (String name : this.getConfig().getKeys(false))
        {
            ConfigurationSection section = this.getConfig().getConfigurationSection(name);
            String world = section.getString("world");
            int x = section.getInt("x");
            int y = section.getInt("y");
            int z = section.getInt("z");
            
            Location loc = new Location(this.getServer().getWorld(world), x, y, z);
            
            Block block = loc.getBlock();
            
            this.addBlockName(block, name);
        }
        
    }
    
    private static void sendMessage(@NotNull Player player, String message)
    {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }
    
    public boolean onCommand(CommandSender sender, @NotNull Command cmd, String label, String[] args)
    {
        if (cmd.getName().equalsIgnoreCase("blockname"))
        {
            if (!(sender instanceof Player))
            {
                sender.sendMessage("This command can only be run by a player.");
            }
            else
            {
                Player player = (Player) sender;
    
                Block block = player.getTargetBlock(null, DISTANCE_TO_BLOCK);
                
                if (args[0].equalsIgnoreCase("remove"))
                {
                    this.removeBlockName(player, block);
                }
                else
                {
                    String blockName = "";
    
                    for (String str : args)
                    {
                        blockName = blockName.concat(str);
                        blockName = blockName.concat(" ");
                    }
                    
                    this.addBlockName(player, block, blockName);
                }
            
                return true;
            }
        }
        return false;
    }
    
    public void removeBlockName(Block block)
    {
        if (block.hasMetadata("Name"))
        {
            this.getConfig().set(block.getMetadata("Name").get(0).asString(), null);
            block.removeMetadata("Name", this);
        }
    }
    
    public void removeBlockName(Player player, Block block)
    {
        if (block.hasMetadata("Name"))
        {
            this.removeBlockName(block);
            
            player.sendMessage(ChatColor.GOLD + "Name removed.");
        }
    }
    
    public void addBlockName(Block block, String blockName)
    {
        this.removeBlockName(block);
        
        block.setMetadata("Name", new FixedMetadataValue(this, blockName));
        
        Location loc = block.getLocation();
        
        this.getConfig().set(blockName.concat(".world"), block.getWorld().getName());
        this.getConfig().set(blockName.concat(".x"), loc.getBlockX());
        this.getConfig().set(blockName.concat(".y"), loc.getBlockY());
        this.getConfig().set(blockName.concat(".z"), loc.getBlockZ());
    }
    
    public void addBlockName(Player player, Block block, String blockName)
    {
        this.addBlockName(block, blockName);
        
        player.sendMessage(ChatColor.GOLD + "Name set.");
    }
    
    @EventHandler
    public boolean onPlayerMoveEvent(@NotNull PlayerMoveEvent event)
    {
        Player player = event.getPlayer();
        Block block = player.getTargetBlock(null, DISTANCE_TO_BLOCK);
        
        if (block.hasMetadata("Name"))
        {
            sendMessage(player, ChatColor.BOLD + block.getMetadata("Name").get(0).asString());
        }
        
        return false;
    }
    
    @Override
    public void onDisable()
    {
        this.saveConfig();
    }
}
