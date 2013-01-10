/**
 * SakuraCmd - Package: net.syamn.sakuracmd.utils.plugin
 * Created: 2013/01/10 16:19:43
 */
package net.syamn.sakuracmd.utils.plugin;

import net.syamn.sakuracmd.permission.Perms;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * SakuraCmdUtil (SakuraCmdUtil.java)
 * @author syam(syamn)
 */
public class SakuraCmdUtil {
    /**
     * 権限によってTabリストの表示色を変更する
     * @param player
     */
    public static void changeTabColor(final Player player){
        ChatColor color = null;
        if (Perms.TAB_RED.has(player)){
            color = ChatColor.RED;
        }
        else if (Perms.TAB_PURPLE.has(player)){
            color = ChatColor.LIGHT_PURPLE;
        }
        else if (Perms.TAB_AQUA.has(player)){
            color = ChatColor.AQUA;
        }
        else if (Perms.TAB_NONE.has(player)){
            color = null;
        }
        else if (Perms.TAB_GRAY.has(player)){
            color = ChatColor.GRAY;
        }
        
        if (color != null){
            String newName = color.toString() + player.getDisplayName();
            if (newName.length() > 16){
                newName = newName.substring(0, 12) + ChatColor.WHITE + "..";
            }
            player.setPlayerListName(newName);
        }else{
            player.setPlayerListName(player.getDisplayName());
        }
    }
}