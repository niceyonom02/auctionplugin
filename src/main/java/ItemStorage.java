import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Random;

public class ItemStorage {
    public ArrayList<ItemStack> itemList = new ArrayList<>();

    public ItemStack getRandomItemStackClone(){
        int random = new Random().nextInt(itemList.size());
        return itemList.get(random).clone();
    }
}
