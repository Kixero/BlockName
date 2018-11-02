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
    private static final int DISTANCE_TO_BLOCK = 5;  // The max distance to the nearest block
    private static boolean BOLD_TEXT = true;         // Display the text in bold
    
    /**
     * OnEnable() method.
     */
    @Override
    public void onEnable()
    {
        this.saveDefaultConfig();
        
        getServer().getPluginManager().registerEvents(this, this);
        
        // Load the Settings
        BOLD_TEXT = this.getConfig().getBoolean("settings.bold-text");

        // Reload the names
        ConfigurationSection names = this.getConfig().getConfigurationSection("names");
    
        for (String id : names.getKeys(false))
        {
            ConfigurationSection section = this.getConfig().getConfigurationSection("names." + id);
            
            Location loc = new Location(this.getServer().getWorld(section.getString("world")),
                                        section.getInt("x"),
                                        section.getInt("y"),
                                        section.getInt("z"));
            
            this.addBlockName(loc.getBlock(), section.getString("name"));
        }
    }
    
    /**
     * OnCommand() Listener
     *
     * Listens to the command 'blockname' to set a name to a block. and to
     * the command 'blockname remove' to remove a block's name.
     *
     * @param sender The sender of the command.
     * @param cmd The command sent.
     * @param label The label of the command.
     * @param args The arguments of the command.
     * @return True if the command has been handled, else otherwise.
     */
    public boolean onCommand(CommandSender sender, @NotNull Command cmd, String label, String[] args)
    {
        // Checks for the 'blockname' command.
        if (cmd.getName().equalsIgnoreCase("blockname"))
        {
            // Check if the sender is a player
            if (!(sender instanceof Player))
            {
                sender.sendMessage("This command can only be run by a player.");
            }
            else
            {
                Player player = (Player) sender;
    
                Block block = player.getTargetBlock(null, DISTANCE_TO_BLOCK);
                
                // Check if the player really aims at a block
                if (block.getType() == Material.AIR)
                {
                    sender.sendMessage(ChatColor.GOLD + "You are not targeting a block.");
                }
                else
                {
                    // Check for the 'blockname remove' command
                    if (args[0].equalsIgnoreCase("remove"))
                    {
                        this.removeBlockName(player, block);
                    }
                    else
                    {
                        // Builds the name from the multiple arguments (separated with a space)
                        String blockName = "";
        
                        for (String str : args)
                        {
                            blockName = blockName.concat(str);
                            blockName = blockName.concat(" ");
                        }
        
                        blockName = blockName.substring(0, blockName.length() - 1);
        
                        // Add the name to the block
                        this.addBlockName(player, block, blockName);
                    }
                }
            
                return true;
            }
        }
        return false;
    }
    
    /**
     * A PlayerMoveEvent Listener to check if the player looks at a named block
     *
     * @param event The playerMoveEvent caught.
     * @return Always false.
     */
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
    
    /**
     * A BlockBreak Listener to check if a named block is destroyed
     * and remove its name.
     *
     * @param event The BlockBreakEvent caught.
     * @return Always false.
     */
    @EventHandler
    public boolean onBlockDestroy(@NotNull BlockBreakEvent event)
    {
        if (event.getBlock().hasMetadata("Name"))
        {
            this.removeBlockName(event.getBlock());
        }
        
        return false;
    }
    
    /**
     * Displays the message in the player's actionbar.
     *
     * @param player The player to display the message.
     * @param message The message to display as a String.
     */
    private static void sendMessage(@NotNull Player player, String message)
    {
        if (BOLD_TEXT)
        {
            message = translateAlternateColorCodesBold('&', message);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        }
        else
        {
            message = ChatColor.translateAlternateColorCodes('&', message);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        }
    }
    
    /**
     * A method to remove a block's name in the metadata and the config.
     *
     * @param block The block we want to remove the name.
     */
    private void removeBlockName(@NotNull Block block)
    {
        if (block.hasMetadata("Name"))
        {
            this.getConfig().set("names." + locToId(block.getLocation()), null);
            block.removeMetadata("Name", this);
        }
        this.saveConfig();
    }
    
    /**
     * A method to remove a block's name in the metadata and the config,
     * and send a message to the player.
     *
     * @param player The player to send the message.
     * @param block The block we want to remove the name.
     */
    private void removeBlockName(Player player, @NotNull Block block)
    {
        if (block.hasMetadata("Name"))
        {
            this.removeBlockName(block);
            
            player.sendMessage(ChatColor.GOLD + "Name removed.");
        }
    }
    
    /**
     * A method to add (or change if name already set) a block's name
     * in the metadata and in the config.
     *
     * @param block The block we want to send the message.
     * @param blockName The name to set to the block.
     */
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
    
    /**
     * A method to add (or change if name already set) a block's name
     * in the metadata and in the config and send a message to the player.
     *
     * @param player The player to send the message.
     * @param block The block we want to send the message.
     * @param blockName The name to set to the block.
     */
    private void addBlockName(@NotNull Player player, Block block, String blockName)
    {
        this.addBlockName(block, blockName);
        
        player.sendMessage(ChatColor.GOLD + "Name set.");
    }
    
    /**
     * A method to create a unique id based on a location coordinates.
     *
     * @param loc The location to base the id on.
     * @return The id as a String.
     */
    @NotNull
    private static String locToId(@NotNull Location loc)
    {
        return Integer.toString(loc.getBlockX()) + "0" + Integer.toString(loc.getBlockY()) + "0" + Integer.toString(loc.getBlockZ());
    }
    
    /**
     * Method copied and tweaked from the ChatColor class to set all text in bold.
     *
     * Translates a string using an alternate color code character into a
     * string that uses the internal ChatColor.COLOR_CODE color code
     * character. The alternate color code character will only be replaced if
     * it is immediately followed by 0-9, A-F, a-f, K-O, k-o, R or r.
     *
     * @param altColorChar The alternate color code character to replace. Ex: &
     * @param textToTranslate Text containing the alternate color code character.
     * @return Text containing the ChatColor.COLOR_CODE color code character and set to bold.
     */
    private static String translateAlternateColorCodesBold(char altColorChar, @NotNull String textToTranslate)
    {
        char[] b = textToTranslate.toCharArray();
        
        for (int i = 0; i < b.length - 1; i++)
        {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1)
            {
                b[i] = ChatColor.COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
                
                b = insert(b, i + 2, ChatColor.COLOR_CHAR);
                b = insert(b, i + 3, 'l');
            }
        }
        b = insert(b, 0, ChatColor.COLOR_CHAR);
        b = insert(b, 1, 'l');
        
        return new String(b);
    }
    
    /**
     * A method to insert a char in a char array at an index and shift
     * the other elements after the index.
     *
     * @param array The array to insert the char into.
     * @param index The index to insert the char at.
     * @param c The char to insert.
     * @return The array with the char inserted.
     */
    private static char[] insert(@NotNull char[] array, int index, char c)
    {
        char[] result = new char[array.length + 1];
        
        System.arraycopy(array, 0, result, 0, index);
        
        result[index] = c;
        
        System.arraycopy(array, index, result, index + 1, array.length - index);
        
        return result;
    }
    
    /**
     * OnDisable() method.
     */
    @Override
    public void onDisable()
    {
        this.saveConfig();
    }
}
