import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class AuctionData {
    public HashMap<Integer, Integer> timeRange;
    public ItemStorage itemStorage;
    public String code;
    public HashMap<Integer, Boolean> currentHourSchedule = new HashMap<>();
    private int cachedHour;
    private AuctionProcessor processor;

    public AuctionData(HashMap<Integer, Integer> timeRange, ItemStorage itemStorage, String code, AuctionProcessor auctionProcessor){
        this.timeRange = timeRange;
        this.itemStorage = itemStorage;
        this.code = code;
        this.processor = auctionProcessor;

        cachedHour = LocalDateTime.now().getHour();
        setFirstCase();
        hourSchedule();
    }

    public void addRange(int min, int max){
        if(min < 0 || max >= 60) return;

        timeRange.put(min, max);
    }

    public void setFirstCase(){
        int currentMinute = LocalDateTime.now().getMinute();

        for(int minTime : timeRange.keySet()){
            if(currentMinute < timeRange.get(minTime)){
                Bukkit.getLogger().info(code + " " + timeRange.get(minTime).toString());
                if(currentMinute < minTime){
                    int randomMinute = minTime + new Random().nextInt(timeRange.get(minTime) - minTime);
                    currentHourSchedule.put(randomMinute, false);
                } else{
                    int randomMinute = currentMinute + new Random().nextInt(timeRange.get(minTime) - currentMinute);
                    currentHourSchedule.put(randomMinute, false);
                }
            }
        }

        for(int min : currentHourSchedule.keySet()){
            Bukkit.getLogger().info(code + "mincheck" +" " + min + "");
        }
    }

    public void hourSchedule(){
        Bukkit.getScheduler().scheduleSyncRepeatingTask(EasyAuction.Main, new Runnable() {

            @Override
            public void run() {
                for(int executeTime : currentHourSchedule.keySet()){
                    if(!currentHourSchedule.get(executeTime)){
                        if(LocalDateTime.now().getMinute() >= executeTime){
                            Bukkit.getLogger().info(executeTime + "");
                            Bukkit.getLogger().info(LocalDateTime.now().toString());
                            currentHourSchedule.put(executeTime, true);
                            processor.requestStartAuction(itemStorage, code);
                        }
                    }
                }

                if(cachedHour != LocalDateTime.now().getHour()){
                        scheduleNewHour();
                }

                cachedHour = LocalDateTime.now().getHour();
            }
        }, 0L, 20L);
    }

    public void scheduleNewHour(){
        for(int minTime : timeRange.keySet()){
            int randomMinute = minTime + new Random().nextInt(timeRange.get(minTime) - minTime);
            currentHourSchedule.put(randomMinute, false);
        }
    }

    @Override
    public String toString(){
        return code;
    }
}
