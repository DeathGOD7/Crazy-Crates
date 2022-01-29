package com.badbones69.crazycrates.cratetypes;

import com.badbones69.crazycrates.Methods;
import com.badbones69.crazycrates.api.CrazyManager;
import com.badbones69.crazycrates.func.enums.KeyType;
import com.badbones69.crazycrates.api.events.PlayerPrizeEvent;
import com.badbones69.crazycrates.api.objects.Crate;
import com.badbones69.crazycrates.api.objects.ItemBuilder;
import com.badbones69.crazycrates.api.objects.Prize;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.ArrayList;

public class Wonder implements Listener {
    
    public static void startWonder(final Player player, Crate crate, KeyType keyType, boolean checkHand) {

        if (!CrazyManager.getInstance().takeKeys(1, player, crate, keyType, checkHand)) {
            Methods.failedToTakeKey(player, crate);
            CrazyManager.getInstance().removePlayerFromOpeningList(player);
            return;
        }

        final Inventory inv = CrazyManager.getJavaPlugin().getServer().createInventory(null, 45, crate.getCrateInventoryName());
        final ArrayList<String> slots = new ArrayList<>();

        for (int i = 0; i < 45; i++) {
            Prize prize = crate.pickPrize(player);
            slots.add(i + "");
            inv.setItem(i, prize.getDisplayItem());
        }

        player.openInventory(inv);
        CrazyManager.getInstance().addCrateTask(player, new BukkitRunnable() {
            int fullTime = 0;
            int timer = 0;
            int slot1 = 0;
            int slot2 = 44;
            final ArrayList<Integer> Slots = new ArrayList<>();
            Prize prize = null;
            
            @Override
            public void run() {

                if (timer >= 2 && fullTime <= 65) {
                    slots.remove(slot1 + "");
                    slots.remove(slot2 + "");
                    Slots.add(slot1);
                    Slots.add(slot2);
                    inv.setItem(slot1, new ItemBuilder().setMaterial(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build());
                    inv.setItem(slot2, new ItemBuilder().setMaterial(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build());
                    for (String slot : slots) {
                        prize = crate.pickPrize(player);
                        inv.setItem(Integer.parseInt(slot), prize.getDisplayItem());
                    }
                    slot1++;
                    slot2--;
                }

                if (fullTime > 67) {
                    ItemStack item = Methods.getRandomPaneColor().setName(" ").build();
                    for (int slot : Slots) {
                        inv.setItem(slot, item);
                    }
                }

                player.openInventory(inv);

                if (fullTime > 100) {
                    CrazyManager.getInstance().endCrate(player);
                    player.closeInventory();
                    CrazyManager.getInstance().givePrize(player, prize);

                    if (prize.useFireworks()) Methods.fireWork(player.getLocation().add(0, 1, 0));

                    CrazyManager.getJavaPlugin().getServer().getPluginManager().callEvent(new PlayerPrizeEvent(player, crate, crate.getName(), prize));
                    CrazyManager.getInstance().removePlayerFromOpeningList(player);
                    return;
                }

                fullTime++;
                timer++;
                if (timer > 2) {
                    timer = 0;
                }
            }
        }.runTaskTimer(CrazyManager.getJavaPlugin(), 0, 2));
    }
}