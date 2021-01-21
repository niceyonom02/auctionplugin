import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class EasyAuction extends JavaPlugin {
    public static EasyAuction Main;
    private AuctionManager auctionManager;
    private CustomEconomy economyPlugin;
    @Override
    public void onEnable(){
        Main = this;
        economyPlugin = (CustomEconomy) Bukkit.getPluginManager().getPlugin("customeconomy");

        if(economyPlugin == null){
            Bukkit.getLogger().severe("Economy not found!");
        }
        auctionManager = new AuctionManager();
        getCommand("경매").setExecutor(auctionManager);
    }

    public CustomEconomy getEconomyPlugin(){
        return economyPlugin;
    }

    public AuctionManager getAuctionManager(){
        return auctionManager;
    }
}
