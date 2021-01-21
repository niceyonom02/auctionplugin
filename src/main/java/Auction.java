import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Auction {
    private int expireSecond;
    private int threadID;
    private String currentAuctionCode;
    private ItemStack winItem;
    boolean isOneMinuteNotified = false;
    boolean isProgressing = false;
    private String currentMaxOfferMember = null;
    private HashMap<String, AuctionPlayer> payMembers = new HashMap<>();
    private ItemStorage itemStorage;

    Auction(int expireMinute,  ItemStorage itemStorage, String code){
        this.expireSecond = expireMinute * 60;
        this.itemStorage = itemStorage;
        currentAuctionCode = code;
    }

    public void start(){
        winItem = pickItem();
        notifyItem();
        isProgressing = true;

        threadID = Bukkit.getScheduler().scheduleSyncRepeatingTask(EasyAuction.Main, new Runnable() {

            @Override
            public void run() {
                Bukkit.getLogger().info(expireSecond + "");
                expireSecond--;

                if(expireSecond <= 60 && !isOneMinuteNotified){
                    isOneMinuteNotified = true;
                    for(Player p : Bukkit.getOnlinePlayers()){
                        p.sendMessage("경매 1분 잔여시간");
                    }
                }

                if(expireSecond <= 0){
                    isProgressing = false;
                    Bukkit.getScheduler().cancelTask(threadID);
                    finishAuction();
                }
            }
        }, 0L, 20L);
    }

    public void finishAuction(){
        String name = pickWinner();
        for(Player p : Bukkit.getOnlinePlayers()){
            if(name.equalsIgnoreCase("NONE")){
                p.sendMessage("이번 경매는 승자가 없습니다!");
            } else{
                p.sendMessage("이번 경매의 승자는 " + name + "님입니다!");
            }
        }

        if(!name.equalsIgnoreCase("NONE")){
            if(Bukkit.getPlayer(name) != null){
                Bukkit.getPlayer(name).getInventory().addItem(winItem.clone());
            }

            EasyAuction.Main.getAuctionManager().finishAuction(this, currentAuctionCode, Bukkit.getPlayer(name).getUniqueId());
        } else{
            EasyAuction.Main.getAuctionManager().finishAuction(this, "NONE", null);
        }

        winItem = null;
        payMembers.clear();

    }

    private String pickWinner(){
        List<String> keySetList = new ArrayList<>(payMembers.keySet());
        Collections.sort(keySetList, (o1, o2) -> (payMembers.get(o2).currentPrice.compareTo(payMembers.get(o1).currentPrice)));


        for(int i = 0; i < keySetList.size(); i++){
            if(EasyAuction.Main.getEconomyPlugin().getCashManager().hasBalance(payMembers.get(keySetList.get(i)).uuid, payMembers.get(keySetList.get(i)).currentPrice)){
                EasyAuction.Main.getEconomyPlugin().getCashManager().withdraw(payMembers.get(keySetList.get(i)).uuid, payMembers.get(keySetList.get(i)).currentPrice);
                return keySetList.get(i);
            } else{
                if(Bukkit.getPlayer(keySetList.get(i)) != null){
                    Bukkit.getPlayer(keySetList.get(i)).sendMessage("자금이 부족하여 낙찰이 취소되었습니다!");
                }
            }
        }

        return "NONE";
    }

    public boolean hasBalance(String name, long price){
        return true;
    }

    public String getAuctionCode(){
        return currentAuctionCode;
    }

    public ItemStack pickItem(){
        return itemStorage.getRandomItemStackClone();
    }

    public void notifyItem(){
        for(Player p : Bukkit.getOnlinePlayers()){
            p.sendMessage("이번 경매 아이템은 " + winItem.toString() + "인니다");
        }
    }

    public void offer(Player sender, long price ) {
        if(currentMaxOfferMember == null){
            currentMaxOfferMember = sender.getName();
            AuctionPlayer plnew = new AuctionPlayer();
            plnew.currentPrice = price;
            plnew.uuid = sender.getUniqueId();
            payMembers.put(sender.getName(), plnew);
            sender.sendMessage(currentAuctionCode + "경매에 " + price + "원으로 입찰하였습니다.");
        } else{
            if(price <= payMembers.get(currentMaxOfferMember).currentPrice){
                sender.sendMessage("현재 최고 제시 금액" + payMembers.get(currentMaxOfferMember).currentPrice + "N 이상을 제시해야 합니다.");

            } else{
                currentMaxOfferMember = sender.getName();
                if(payMembers.containsKey(sender.getName())){
                    AuctionPlayer pl = payMembers.get(sender.getName());
                    pl.currentPrice = price;
                    payMembers.put(sender.getName(), pl);
                } else{
                    AuctionPlayer plnew = new AuctionPlayer();
                    plnew.currentPrice = price;
                    plnew.uuid = sender.getUniqueId();
                    payMembers.put(sender.getName(), plnew);
                }
                sender.sendMessage(currentAuctionCode + "경매에 " + price + "원으로 입찰하였습니다.");
            }
        }
    }
}
