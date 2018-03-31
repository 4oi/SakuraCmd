/**
 * SakuraCmd - Package: net.syamn.sakuracmd
 * Created: 2012/12/28 13:35:03
 */
package net.syamn.sakuracmd;

import net.syamn.sakuracmd.listener.PlayerListener;
import net.syamn.sakuracmd.listener.feature.AprilFoolsListener;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SakuraCmd (SakuraCmd.java)
 * @author syam(syamn)
 */
public class SakuraCmd extends JavaPlugin{
    // ** Logger **
    public final static String logPrefix = "[SakuraCmd] ";
    public final static String msgPrefix = "&c[SakuraCmd] &f";

    // ** Instance **
    private static SakuraCmd instance;

    /**
     * プラグイン起動処理
     */
    @Override
    public void onEnable() {
        instance = this;

        // Regist Listeners
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new AprilFoolsListener(), this);
    }

    /**
     * プラグイン停止処理
     */
    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

    /**
     * インスタンスを返す
     *
     * @return SakuraCmdインスタンス
     */
    public static SakuraCmd getInstance() {
        return instance;
    }
}
