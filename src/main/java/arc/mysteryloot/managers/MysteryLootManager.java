package arc.mysteryloot.managers;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.hypixel.hytale.builtin.adventure.shop.GiveItemInteraction;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemQuality;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import arc.core.components.Msg;
import arc.core.loggers.Logger;
import arc.mysteryloot.classes.MysteryLootTable;
import arc.mysteryloot.classes.MysteryLootTableItem;
import arc.mysteryloot.configs.MysteryLootTablesConfig;

public class MysteryLootManager {
  @Nonnull
  public final MysteryLootUIManager UI;

  private final Config<MysteryLootTablesConfig> TablesConfig;

  public MysteryLootManager(Config<MysteryLootTablesConfig> tablesConfig) {
    this.TablesConfig = tablesConfig;
    this.UI = new MysteryLootUIManager(this);
  }

  @Nullable
  public MysteryLootTable GetLootTable(String tableId) {
    return TablesConfig.get().GetRewardPool(tableId);
  }

  @Nonnull
  public Map<String, MysteryLootTable> GetAllLootTables() {
    return TablesConfig.get().LootTablePools;
  }

  public boolean CreateMysteryLootTable(String tableId) {
    MysteryLootTablesConfig config = TablesConfig.get();
    if (config.LootTablePools.containsKey(tableId)) return false;
    config.SaveRewardPool(tableId, new MysteryLootTable());
    TablesConfig.save();
    return true;
  }

  public boolean DeleteMysteryLootTable(String tableId) {
    MysteryLootTablesConfig config = TablesConfig.get();
    if (!config.LootTablePools.containsKey(tableId)) return false;
    config.DeleteRewardPool(tableId);
    TablesConfig.save();
    return true;
  }

  public boolean UpdateMysteryLootTable(String tableId, MysteryLootTable newTable) {
    MysteryLootTablesConfig config = TablesConfig.get();
    if (!config.LootTablePools.containsKey(tableId)) return false;
    config.SaveRewardPool(tableId, newTable);
    TablesConfig.save();
    return true;
  }

  public boolean RenameMysteryLootTable(String oldId, String newId, MysteryLootTable table) {
    MysteryLootTablesConfig config = TablesConfig.get();
    if (!config.LootTablePools.containsKey(oldId)) return false;
    config.RenameRewardPool(oldId, newId, table);
    TablesConfig.save();
    return true;
  }

  @Nullable
  public MysteryLootTableItem GetLootTableItem(String tableId, int itemIndex) {
    MysteryLootTable table = GetLootTable(tableId);
    if (table == null || itemIndex < 0 || itemIndex >= table.Items.size()) return null;
    return table.Items.get(itemIndex);
  }

  public boolean CreateLootTableItem(String tableId, MysteryLootTableItem item) {
    MysteryLootTablesConfig config = TablesConfig.get();
    MysteryLootTable table = config.GetRewardPool(tableId);
    if (table == null) return false;
    table.Items.add(item);
    TablesConfig.save();
    return true;
  }

  public boolean UpdateLootTableItem(String tableId, int itemIndex, MysteryLootTableItem updatedItem) {
    MysteryLootTablesConfig config = TablesConfig.get();
    MysteryLootTable table = config.GetRewardPool(tableId);
    if (table == null || itemIndex < 0 || itemIndex >= table.Items.size()) return false;
    table.Items.set(itemIndex, updatedItem);
    TablesConfig.save();
    return true;
  }

  public boolean DeleteLootTableItem(String tableId, int itemIndex) {
    MysteryLootTablesConfig config = TablesConfig.get();
    MysteryLootTable table = config.GetRewardPool(tableId);
    if (table == null || itemIndex < 0 || itemIndex >= table.Items.size()) return false;
    table.Items.remove(itemIndex);
    TablesConfig.save();
    return true;
  }

  /**
   * Picks a random item from the table using weighted selection.
   * Returns null if the table doesn't exist or has no items.
   */
  @Nullable
  public MysteryLootTableItem RollLootTable(String tableId) {
    MysteryLootTable table = GetLootTable(tableId);
    if (table == null || table.Items.isEmpty()) return null;

    double totalWeight = table.Items.stream().mapToDouble(i -> i.DropWeight).sum();
    if (totalWeight <= 0) return null;

    double roll = Math.random() * totalWeight;
    double cumulative = 0;
    for (MysteryLootTableItem item : table.Items) {
      cumulative += item.DropWeight;
      if (roll < cumulative) return item;
    }
    return table.Items.get(table.Items.size() - 1);
  }

  /**
   * Gives a mystery loot item to the player and broadcasts a win announcement
   * if AnnounceWin is configured on the item. The tableId is used as the badge label.
   */
  public void GiveMysteryItem(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull PlayerRef playerRef,
    @Nonnull Store<EntityStore> store,
    @Nonnull String tableId,
    @Nonnull MysteryLootTableItem item
  ) {
    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null) return;

    var asset = Item.getAssetMap().getAsset(item.ItemId);
    if (asset == null) {
      Logger.Error("MysteryLootManager - GiveMysteryItem - Could not find Item Asset" + item.ItemId);
      return;
    }

    var itemStack = new ItemStack(item.ItemId, item.Amount);
    player.notifyPickupItem(ref, itemStack, null, store);

    var giveItem = new GiveItemInteraction(item.ItemId, item.Amount);
    giveItem.run(store, ref, playerRef);

    var quality = ItemQuality.getAssetMap().getAsset(asset.getQualityIndex());
    if (quality == null) {
      Logger.Error("MysteryLootManager - GiveMysteryItem - Could not find ItemQuality Asset" + asset.getQualityIndex());
      return;
    }
    
    if (item.AnnounceWin == false) {
      return;
    }

    var itemColor = quality.getTextColor();
    var itemHexColor = String.format("#%02x%02x%02x", itemColor.red & 0xFF, itemColor.green & 0xFF, itemColor.blue & 0xFF);

    String tableName = tableId.replace("_", " ");
    String itemName = item.ItemId.replace("_", " ");
    Msg msg = new Msg().Raw("");
    msg.Append(new Msg().Raw("[" + tableName + "] ").Color("#d51d6a").Bold());
    msg.Append(new Msg().Raw(playerRef.getUsername()).Color("#ffffff").Bold());
    msg.Append(new Msg().Raw(" won ").Color("#aaaaaa"));
    if (item.Amount > 1) {
      msg.Append(new Msg().Raw(item.Amount + " ").Color("#fcdf99").Bold());
    }
    msg.Append(new Msg().Raw(itemName).Color(itemHexColor).Bold());
    msg.Append(new Msg().Raw("!").Color("#aaaaaa"));
    Universe.get().sendMessage(msg.Build());
  }
}
