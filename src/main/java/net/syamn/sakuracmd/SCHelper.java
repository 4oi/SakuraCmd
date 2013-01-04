/**
 * SakuraCmd - Package: net.syamn.sakuracmd
 * Created: 2013/01/04 13:02:22
 */
package net.syamn.sakuracmd;

import net.syamn.sakuracmd.permission.PermissionManager;
import net.syamn.sakuracmd.worker.AFKWorker;
import net.syamn.utils.LogUtil;

/**
 * SCHelper (SCHelper.java)
 * @author syam(syamn)
 */
public class SCHelper {
    private static long mainThreadID;
    private static long pluginStarted;
    private static SCHelper instance = new SCHelper();
    
    public static SCHelper getInstance(){
        return instance;
    }
    public static void dispose(){
        instance = null;
    }
    
    private SakuraCmd plugin;
    private ConfigurationManager config;
    
    /**
     * プラスグインの初期化時と有効化時に呼ばれる
     */
    private void init(){
        AFKWorker.getInstance();
        this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                this.plugin, AFKWorker.getInstance().getAfkChecker(), 0, config.getAfkCheckIntervalInSec() * 20);
        
        // loadconfig
        try {
            config.loadConfig(true);
        } catch (Exception ex) {
            LogUtil.warning(SakuraCmd.logPrefix + "an error occured while trying to load the config file.");
            ex.printStackTrace();
        }
        
        PermissionManager.setupPermissions(plugin); // init permission
        
    }
    
    public void setMainPlugin(final SakuraCmd plugin){
        mainThreadID = Thread.currentThread().getId();
        this.plugin = plugin;
        this.config = new ConfigurationManager(plugin);
        
        // database
        //database = new Database(this);
        //database.createStructure();
        
        init();
    }
    
    /**
     * プラグインをリロードする
     */
    public synchronized void reload(){
        AFKWorker.dispose();
        
        System.gc();
        init();
    }
    
    /**
     * 設定マネージャを返す
     *
     * @return ConfigurationManager
     */
    public ConfigurationManager getConfig() {
        return config;
    }
}
