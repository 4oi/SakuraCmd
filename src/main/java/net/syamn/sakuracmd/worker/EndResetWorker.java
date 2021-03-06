/**
 * SakuraCmd - Package: net.syamn.sakuracmd.worker
 * Created: 2013/02/09 5:24:32
 */
package net.syamn.sakuracmd.worker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import net.syamn.sakuracmd.SakuraCmd;
import net.syamn.sakuracmd.events.EndResetEvent;
import net.syamn.sakuracmd.events.EndResettingEvent;
import net.syamn.sakuracmd.listener.feature.EndResetListener;
import net.syamn.sakuracmd.serial.EndResetWorldData;
import net.syamn.utils.LogUtil;
import net.syamn.utils.TimeUtil;
import net.syamn.utils.Util;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * EndResetWorker (EndResetWorker.java)
 * @author syam(syamn)
 */
public class EndResetWorker{
    private static EndResetWorker instance = null;
    public static EndResetWorker getInstance(){
        return instance;
    }
    public static void dispose(){
        instance.onDispose();
        instance = null;
    }
    public static void createInstance(final SakuraCmd plugin){
        instance = new EndResetWorker();
        instance.plugin  = plugin;
        instance.init();
    }

    //--
    private SakuraCmd plugin;
    private File endResetData;
    private List<Integer> tasks = new ArrayList<Integer>();

    // data start
    private final HashMap<String, HashMap<String, Long>> resetChunks = new HashMap<String, HashMap<String, Long>>();
    private final HashMap<String, Long> cvs = new HashMap<String, Long>();
    private final HashMap<String, EndResetWorldData> worldData = new HashMap<String, EndResetWorldData>();

    private boolean save = false;
    private final AtomicBoolean saveLock = new AtomicBoolean(false);
    // data end

    @SuppressWarnings("unchecked")
    private void init(){
        endResetData = new File(plugin.getDataFolder(), "endResetData.dat");

        if (!endResetData.exists()){
            // file not exists, check directory
            plugin.getDataFolder().mkdir();
        }else{
            // file exists, load data
            ObjectInputStream in = null;
            try{
                in = new ObjectInputStream(new FileInputStream(endResetData));;

                /* dropped version structure
                int fileVersion;
                Object[] sa = null;
                try {
                    Object o = in.readObject();
                    if (o == null || !(o instanceof Object[])) {
                        LogUtil.warning("Could not read EndReset save data!");
                        return;
                    }
                    sa = (Object[]) o;
                    fileVersion = (Integer) sa[0];
                } catch (OptionalDataException ex) {
                    fileVersion = in.readInt();
                }
                 */

                // load data
                for (Entry<String, HashMap<String, Long>> e : ((HashMap<String, HashMap<String, Long>>) in.readObject()).entrySet()){
                    resetChunks.put(e.getKey(), e.getValue());
                }
                for (Entry<String, Long> e : ((HashMap<String, Long>) in.readObject()).entrySet()){
                    cvs.put(e.getKey(), e.getValue());
                }
                for (Entry<String, EndResetWorldData> e : ((HashMap<String, EndResetWorldData>) in.readObject()).entrySet()){
                    worldData.put(e.getKey(), e.getValue());
                }
            }
            catch(Exception ex){
                LogUtil.warning("Could not read EndReset save data!");
                ex.printStackTrace();
            }
            finally{
                if (in != null){ try{ in.close(); }catch(Exception ignore){} }
            }

            BukkitScheduler scheduler = Bukkit.getScheduler();
            tasks.add(scheduler.runTaskTimer(plugin, new CheckThread(), 100L, 24000L).getTaskId()); // 20分毎
            tasks.add(scheduler.runTaskTimer(plugin, new SaveThread(), 48000L, 48000L).getTaskId()); // 40分毎
        }
    }

    public void callWorldLoad(){
        for (World world : Bukkit.getServer().getWorlds()){
            if (world.getEnvironment() != Environment.THE_END) continue;
            EndResetListener.getInstance().onWorldLoad(new WorldLoadEvent(world));
        }
    }

    private void onDispose(){
        for (int taskID : tasks){
            Bukkit.getScheduler().cancelTask(taskID);
        }
        tasks.clear();

        if (save){
            new SaveThread().run();
        }
    }

    public void regen(World world){
        regen(world, false);
    }

    public void regen(World world, boolean silent){
        if (world == null || world.getEnvironment() != Environment.THE_END){
            throw new IllegalArgumentException("world must be end world");
        }
        final String worldName = world.getName();

        short dragonAmount = 1;
        String message = (silent) ? null : "&c[SakuraServer] &dエンドワールド'&6" + worldName + "&d'はリセットされました！";

        // call event
        EndResettingEvent resettingEvent = new EndResettingEvent(world, dragonAmount, message);
        plugin.getServer().getPluginManager().callEvent(resettingEvent);
        if (resettingEvent.isCancelled()){
            return;
        }
        dragonAmount = resettingEvent.getDragonAmount();
        if (dragonAmount < 1) dragonAmount = 1;
        message = resettingEvent.getCompleteMessage();

        LogUtil.info("Resetting world " + world.getName() + " ...");

        // reset
        for (final Player p : world.getPlayers()){
            p.teleport(Bukkit.getServer().getWorlds().get(0).getSpawnLocation(), TeleportCause.PLUGIN);
            Util.message(p, "&d このワールドはリセットされます！");
        }

        long cv = cvs.get(worldName) + 1;
        if (cv == Long.MAX_VALUE) cv = Long.MIN_VALUE;
        cvs.put(worldName, cv);

        EndResetListener listener = EndResetListener.getInstance();
        if (listener == null){
            throw new IllegalStateException("EndResetListener is null!");
        }

        for (Chunk chunk : world.getLoadedChunks()){
            listener.onChunkLoad(new ChunkLoadEvent(chunk, false));
        }

        if (dragonAmount > 1) {
            dragonAmount--;
            Location loc = world.getSpawnLocation();
            loc.setY(world.getMaxHeight() - 1);
            for (short i = 0; i < dragonAmount; i++){
                world.spawnEntity(loc, EntityType.ENDER_DRAGON);
            }
        }

        LogUtil.info("World " + world.getName() + " was reset!");

        save = true;
        if (message != null){
            Util.broadcastMessage(message);
        }

        EndResetWorldData data = worldData.get(world.getName());
        if(data != null){
            data.updateLastReset();
        }

        // Call complete event
        plugin.getServer().getPluginManager().callEvent(new EndResetEvent(world, dragonAmount, message));
    }

    private class CheckThread implements Runnable {
        @Override
        public void run() {
            if (worldData.isEmpty()) return;

            final long now = TimeUtil.getCurrentUnixSec();
            Server server = Bukkit.getServer();
            EndResetWorldData resetWorld;

            for (Entry<String, EndResetWorldData> entry : worldData.entrySet()) {
                resetWorld = entry.getValue();
                if (resetWorld.getNextReset() <= now) {
                    World world = server.getWorld(entry.getKey());
                    if (world != null) regen(world);
                    resetWorld.updateLastReset();
                    save = true;
                }
            }
        }
    }

    private class SaveThread implements Runnable {
        @Override
        public void run() {
            if (!save) return;
            save = false;

            while (!saveLock.compareAndSet(false, true)){
                continue;
            }

            try {
                if (!endResetData.exists()) endResetData.createNewFile();
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(endResetData));

                //out.writeInt(0); // file version -- dropped
                out.writeObject(resetChunks);
                out.writeObject(cvs);
                out.writeObject(worldData);

                Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new AsyncSaveThread(out));
            } catch (Exception ex) {
                saveLock.set(false);
                LogUtil.warning("Cannot write end reset save data!");
                ex.printStackTrace();
            }
        }
    }
    private class AsyncSaveThread implements Runnable {
        private final ObjectOutputStream out;

        private AsyncSaveThread(ObjectOutputStream out) {
            this.out = out;
        }

        @Override
        public void run() {
            try {
                out.flush();
                out.close();
            } catch (Exception ex) {
                LogUtil.warning("Cannot write end reset save data!");
                ex.printStackTrace();
            } finally {
                saveLock.set(false);
            }
        }
    }

    /* getter / setter */
    public void updateSaveFlag(){
        this.save = true;
    }

    // worldData
    public void putWorldData(final String worldName, final EndResetWorldData data){
        this.worldData.put(worldName, data);
        this.save = true;
    }
    public void removeWorldData(final String worldName){
        this.worldData.remove(worldName);
        this.save = true;
    }
    public Map<String, EndResetWorldData> getWorldDataMap(){
        return Collections.unmodifiableMap(this.worldData);
    }

    // cvs
    public void putCvs(final String worldName, final Long hashKey){
        this.cvs.put(worldName, hashKey);
        this.save = true;
    }
    public long getCvs(final String worldName){
        return this.cvs.get(worldName);
    }
    public Map<String, Long> getCvsMap(){
        return Collections.unmodifiableMap(this.cvs);
    }

    // resetChunks
    public void putResetChunks(final String worldName, final HashMap<String, Long> value){
        this.resetChunks.put(worldName, value);
        this.save = true;
    }
    public HashMap<String, Long> getResetChunks(final String worldName){
        return this.resetChunks.get(worldName);
    }
    public Map<String, HashMap<String, Long>> getResetChunkMap(){
        return Collections.unmodifiableMap(this.resetChunks);
    }
}