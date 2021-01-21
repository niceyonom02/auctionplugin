import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.util.*;

public class AuctionManager implements AuctionProcessor, CommandExecutor {
    int minutesPerAuction = 2;
    List<AuctionData> auctionData = new ArrayList<>();
    ArrayList<Auction> currentOngoingAuctions = new ArrayList<>();
    HashMap<String, UUID> previousWinners = new HashMap<>();


    public AuctionManager(){
        loadAuctionDatas();
    }

    public void loadAuctionDatas(){
        HashMap<Integer, Integer> map = new HashMap<>();
        ItemStorage it = new ItemStorage();
        it.itemList.add(new ItemStack(Material.GLASS));
        it.itemList.add(new ItemStack(Material.ANVIL));
        it.itemList.add(new ItemStack(Material.BOAT));

        HashMap<Integer, Integer> map1 = new HashMap<>();

    }

    public void startAuction(ItemStorage itemStorage, String code){
        notifyAuction();
        Auction auction = new Auction(minutesPerAuction, itemStorage, code);
        currentOngoingAuctions.add(auction);
        auction.start();
    }

    public void notifyAuction(){
        for(Player p : Bukkit.getOnlinePlayers()){
            p.sendMessage("곧 경매 시작함");
        }
    }

    private Auction getAuction(String code){
        for(Auction auction : currentOngoingAuctions){
            if(auction.getAuctionCode().equalsIgnoreCase(code)){
                return auction;
            }
        }

        return null;
    }

    @Override
    public void requestStartAuction(ItemStorage itemStorage, String code) {
        startAuction(itemStorage, code);
    }

    public void finishAuction(Auction auction, String code, UUID winner){
        if(!code.equalsIgnoreCase("NONE")){
            previousWinners.put(code, winner);
        } else{
            previousWinners.remove(code);
        }
        currentOngoingAuctions.remove(auction);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player)) return false;

        Player sender = (Player) commandSender;

        if(s.equalsIgnoreCase("경매")){
            if(strings.length > 0){
                switch (strings[0]){
                    case "목록":
                        printCurrentAuction(sender);
                        break;
                    default:
                        if(strings.length > 1){
                            if(getAuction(strings[0]) != null){
                                if(previousWinners.containsKey(strings[0])){
                                    if(previousWinners.get(strings[0]).equals(sender.getUniqueId())){
                                        sender.sendMessage("이전 경매에서의 승자는 바로 다음 동일 경매에 참가할 수 없습니다.");
                                        return false;
                                    }
                                }

                                try{
                                    long offerPrice = Long.parseLong(strings[1]);
                                    if(offerPrice > 0){
                                        if(EasyAuction.Main.getEconomyPlugin().getCashManager().hasBalance(sender.getUniqueId(), offerPrice)){
                                            getAuction(strings[0]).offer(sender, offerPrice);
                                        } else{
                                            sender.sendMessage("자금이 부족합니다!");
                                            return false;
                                        }
                                    } else{
                                        sender.sendMessage("0이상의 정수만 입력 가능합니다!");
                                        return false;
                                    }
                                } catch (NumberFormatException e){
                                    sender.sendMessage("0이상의 정수만 입력 가능합니다!");
                                    return false;
                                }
                            } else{
                                sender.sendMessage("해당 경매는 진행중이 아닙니다!");
                                return false;
                            }
                        } else{
                            sender.sendMessage("/경매 [경매코드] [가격]");
                            return false;
                        }

                }
            } else{
                helpMessage(sender);
            }
        }
        return false;
    }

    public void printCurrentAuction(Player player){
        if(currentOngoingAuctions.isEmpty()){
            player.sendMessage("진행중인 경매가 없습니다!");
            return;
        }

        for(Auction auction : currentOngoingAuctions){
            player.sendMessage(auction.toString());
        }
    }

    public boolean hasBalance(String name, long price){
        return true;
    }

    public void helpMessage(Player player){
        player.sendMessage("/경매 [경매코드] [제시가격]");
        player.sendMessage("/경매 목록 -> 경매 현황 및 경매 코드 확인");
    }
}
