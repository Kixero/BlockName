package io.github.kixero.blockname;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class BlockName extends JavaPlugin implements Listener
{
    private static final int DISTANCE_TO_BLOCK = 5;
    private static boolean BOLD_TEXT = true;
    
    @Override
    public void onEnable()
    {
        this.saveDefaultConfig();
        
        getServer().getPluginManager().registerEvents(this, this);
        
        BOLD_TEXT = this.getConfig().getBoolean("settings.bold-text");

        ConfigurationSection names = this.getConfig().getConfigurationSection("names");
    
        for (String id : names.getKeys(false))
        {
            ConfigurationSection section = this.getConfig().getConfigurationSection("names." + id);
    
            String name = section.getString("name");
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
        if (BOLD_TEXT)
        {
            message = ChatColor.translateAlternateColorCodes('&', "&l" + message);
        }
        else
        {
            message = ChatColor.translateAlternateColorCodes('&', message);
        }
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
                
                if (block.getType() == Material.AIR)
                {
                    sender.sendMessage(ChatColor.GOLD + "You are not targeting a block.");
                }
                else
                {
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
        
                        blockName = blockName.substring(0, blockName.length() - 1);
        
                        this.addBlockName(player, block, blockName);
                    }
                }
            
                return true;
            }
        }
        return false;
    }
    
    private void removeBlockName(@NotNull Block block)
    {
        if (block.hasMetadata("Name"))
        {
            this.getConfig().set("names." + locToId(block.getLocation()), null);
            block.removeMetadata("Name", this);
        }
        this.saveConfig();
    }
    
    private void removeBlockName(Player player, @NotNull Block block)
    {
        if (block.hasMetadata("Name"))
        {
            this.removeBlockName(block);
            
            player.sendMessage(ChatColor.GOLD + "Name removed.");
        }
    }
    
    private void addBlockName(Block block, String blockName)
    {
        this.removeBlockName(block);
        
        block.setMetadata("Name", new FixedMetadataValue(this, blockName));
        
        Location loc = block.getLocation();
        String id = locToId(loc);
        
        this.getConfig().set("names." + id + ".name", blockName);
        this.getConfig().set("names." + id + ".world", block.getWorld().getName());
        this.getConfig().set("names." + id + ".x", loc.getBlockX());
        this.getConfig().set("names." + id + ".y", loc.getBlockY());
        this.getConfig().set("names." + id + ".z", loc.getBlockZ());
        
        this.saveConfig();
    }
    
    private void addBlockName(@NotNull Player player, Block block, String blockName)
    {
        this.addBlockName(block, blockName);
        
        player.sendMessage(ChatColor.GOLD + "Name set.");
    }
    
    @NotNull
    private static String locToId(@NotNull Location loc)
    {
        return Integer.toString(loc.getBlockX()) + "0" + Integer.toString(loc.getBlockY()) + "0" + Integer.toString(loc.getBlockZ());
    }
    
    @EventHandler
    public boolean onPlayerMoveEvent(@NotNull PlayerMoveEvent event)
    {
        Player player = event.getPlayer();
        Block block = player.getTargetBlock(null, DISTANCE_TO_BLOCK);
        
        if (block.hasMetadata("Name"))
        {
            sendMessage(player, block.getMetadata("Name").get(0).asString());
        }
        
        return false;
    }
    
    @EventHandler
    public boolean onBlockDestroy(@NotNull BlockBreakEvent event)
    {
        if (event.getBlock().hasMetadata("Name"))
        {
            this.removeBlockName(event.getBlock());
        }
        
        return false;
    }
    
    @Override
    public void onDisable()
    {
        this.saveConfig();
    }
}
