/**
 * SakuraCmd - Package: net.syamn.sakuracmd.commands.tp
 * Created: 2012/12/29 8:30:44
 */
package net.syamn.sakuracmd.commands.tp;

import net.syamn.sakuracmd.Perms;
import net.syamn.sakuracmd.commands.BaseCommand;
import net.syamn.utils.Util;
import net.syamn.utils.exception.CommandException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

/**
 * Tp (Tp.java)
 * @author syam(syamn)
 */
public class Tp extends BaseCommand{
    public Tp(){
        bePlayer = true;
        name = "tp";
        perm = Perms.TP;
        argLength = 1;
        usage = "[player] <- tp ";
    }
    
    public void execute() throws CommandException{
        final Player target = Bukkit.getPlayer(args.get(0));
        if (target == null || !target.isOnline()){
            throw new CommandException("&cプレイヤーが見つかりません！");
        }
        
        player.teleport(target, TeleportCause.COMMAND);
        
        Util.message(sender, "&aプレイヤー " + target.getName() + " にテレポートしました！");
    }
}
