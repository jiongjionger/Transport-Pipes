package de.robotricker.transportpipes.manager.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.pipes.GoldenPipe;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.PipeUtils;

public class GoldenPipeInv implements Listener {

	private static HashMap<GoldenPipe, Inventory> pipe_invs = new HashMap<GoldenPipe, Inventory>();

	public static void openGoldenPipeInv(Player p, GoldenPipe pipe) {
		Inventory inv;
		if (pipe_invs.containsKey(pipe)) {
			inv = pipe_invs.get(pipe);
		} else {
			inv = Bukkit.createInventory(null, 6 * 9, "�rGolden Pipe");
			pipe_invs.put(pipe, inv);
		}

		ItemStack glass_pane = SettingsUtils.changeDisplayNameAndLore(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), "�r");
		List<PipeDirection> pipeConnections = PipeUtils.getPipeConnections(pipe.blockLoc);

		inv.setItem(0 * 9, SettingsUtils.changeDisplayNameAndLore(new ItemStack(Material.WOOL, 1, (short) 0), "�fWhite"));
		inv.setItem(1 * 9, SettingsUtils.changeDisplayNameAndLore(new ItemStack(Material.WOOL, 1, (short) 4), "�eYellow"));
		inv.setItem(2 * 9, SettingsUtils.changeDisplayNameAndLore(new ItemStack(Material.WOOL, 1, (short) 5), "�aGreen"));
		inv.setItem(3 * 9, SettingsUtils.changeDisplayNameAndLore(new ItemStack(Material.WOOL, 1, (short) 11), "�1Blue"));
		inv.setItem(4 * 9, SettingsUtils.changeDisplayNameAndLore(new ItemStack(Material.WOOL, 1, (short) 14), "�cRed"));
		inv.setItem(5 * 9, SettingsUtils.changeDisplayNameAndLore(new ItemStack(Material.WOOL, 1, (short) 15), "�8Black"));

		for (int line = 0; line < 6; line++) {
			if (!pipe.isPipeNeighborBlock(PipeDirection.values()[line]) && !pipeConnections.contains(PipeDirection.values()[line])) {
				for (int i = 1; i < 9; i++) {
					inv.setItem(line * 9 + i, glass_pane);
				}
			} else {
				ItemData[] items = pipe.getOutputItems(PipeDirection.fromID(line));
				for (int i = 1; i < 9; i++) {
					if (items[i - 1] != null) {
						inv.setItem(line * 9 + i, items[i - 1].toItemStack());
					} else {
						inv.setItem(line * 9 + i, null);
					}
				}
			}
		}

		p.openInventory(inv);
	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (e.getClickedInventory() != null && pipe_invs.containsValue(e.getClickedInventory())) {
			Player p = (Player) e.getWhoClicked();
			//clicked on glass pane
			if (SettingsUtils.hasDisplayName(e.getCurrentItem(), "�r")) {
				e.setCancelled(true);
			}
			//clicked on wool block
			if (e.getRawSlot() >= 0 && e.getRawSlot() <= 5 * 9 && e.getRawSlot() % 9 == 0) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		if (e.getInventory() != null && pipe_invs.containsValue(e.getInventory())) {
			GoldenPipe pipe = null;
			//get pipe with inventory
			for (GoldenPipe gp : pipe_invs.keySet()) {
				if (pipe_invs.get(gp).equals(e.getInventory())) {
					pipe = gp;
					break;
				}
			}
			//cache new items in golden pipe
			linefor: for (int line = 0; line < 6; line++) {
				List<ItemData> items = new ArrayList<ItemData>();
				for (int i = 1; i < 9; i++) {
					ItemStack is = e.getInventory().getItem(line * 9 + i);
					//make sure the glass pane won't be saved
					if (is != null && !SettingsUtils.hasDisplayName(is, "�r")) {
						items.add(new ItemData(is));
						if (is.getAmount() > 1) {
							ItemStack clone = is.clone();
							clone.setAmount(is.getAmount() - 1);
							e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), clone);
						}
					} else if (SettingsUtils.hasDisplayName(is, "�r")) {
						//skip this save-sequenz if this line is not available (not a pipe or block as neighbor)
						continue linefor;
					}
				}
				pipe.changeOutputItems(PipeDirection.fromID(line), items);
			}
		}
	}

}
