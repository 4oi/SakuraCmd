/**
 * SakuraCmd - Package: net.syamn.sakuracmd.player
 * Created: 2013/01/01 22:29:06
 */
package net.syamn.sakuracmd.player;

import java.io.File;
import java.util.ArrayList;

import net.syamn.sakuracmd.SakuraCmd;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * PlayerData (PlayerData.java)
 * @author syam(syamn)
 */
public class PlayerData{
    private final static String SEPARATOR = System.getProperty("file.separator");
    private final static String dataDir = "userData";
    
    private final String playerName;
    private YamlConfiguration conf = new YamlConfiguration();
    private File file;
    private ArrayList<Power> powers = new ArrayList<Power>();
    private boolean saved = true;
    
    /* Transient status */
    // --> moved to SakuraPlayer
    
    /* Saves values*/
    
    /* ************************** */

    public PlayerData(final String playerName){
        this.playerName = playerName;
        String fileName = SakuraCmd.getInstance().getDataFolder() + SEPARATOR + dataDir + SEPARATOR + playerName + ".yml";
        load(new File(fileName));
    }
    private PlayerData(final String playerName, final File file){
        this.playerName = playerName;
        load(file);
    }
    
    private boolean load(final File file){
        this.file = file;
        if (!file.exists()){
            if (!file.getParentFile().exists()){
                file.getParentFile().mkdir();
            }
            if (!save(true)){
                throw new IllegalStateException("Could not create plaer data file: " + file.getPath());
            }
        }
        
        try {
            conf = new YamlConfiguration();
            conf.load(file);
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    private boolean save(final boolean force){
        if (!saved || force){
            try{
                conf.save(file);
            }catch (Exception ex){
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public String getPlayerName(){
        return this.playerName;
    }
    
    public static PlayerData getDataIfExists(final String playerName){
        final String fileName = SakuraCmd.getInstance().getDataFolder() + SEPARATOR + dataDir + SEPARATOR + playerName + ".yml";
        final File file = new File(fileName);
        return (file.exists()) ? new PlayerData(playerName, file) : null;
    }
    
    /* Getter/Setter */
    public boolean hasPower(final Power power){
        return powers.contains(power);
    }
    public void addPower(final Power power/*, final int level*/){
        if (!hasPower(power)){
            powers.add(power);
            saved = false;
        }
    }
    public void removePower(final Power power){
        powers.remove(power);
        saved = false;
    }
}
