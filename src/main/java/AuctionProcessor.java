import java.util.UUID;

public interface AuctionProcessor {
    void requestStartAuction(ItemStorage itemStorage, String code);
    void finishAuction(Auction auction,String code, UUID uuid);
}
