package arc.mysteryloot.managers;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.hypixel.hytale.server.core.util.Config;
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
    return TablesConfig.get().RewardPools;
  }

  public boolean CreateMysteryLootTable(String tableId) {
    MysteryLootTablesConfig config = TablesConfig.get();
    if (config.RewardPools.containsKey(tableId)) {
      return false;
    }
    config.SaveRewardPool(tableId, new MysteryLootTable());
    TablesConfig.save();
    return true;
  }

  public boolean DeleteMysteryLootTable(String tableId) {
    MysteryLootTablesConfig config = TablesConfig.get();
    if (!config.RewardPools.containsKey(tableId)) {
      return false;
    }
    config.DeleteRewardPool(tableId);
    TablesConfig.save();
    return true;
  }

  public boolean UpdateMysteryLootTable(String tableId, MysteryLootTable newTable) {
    MysteryLootTablesConfig config = TablesConfig.get();
    if (!config.RewardPools.containsKey(tableId)) {
      return false;
    }
    config.SaveRewardPool(tableId, newTable);
    TablesConfig.save();
    return true;
  }

  public boolean RenameMysteryLootTable(String oldId, String newId, MysteryLootTable table) {
    MysteryLootTablesConfig config = TablesConfig.get();
    if (!config.RewardPools.containsKey(oldId)) {
      return false;
    }
    config.RenameRewardPool(oldId, newId, table);
    TablesConfig.save();
    return true;
  }


  @Nullable
  public MysteryLootTableItem GetLootTableItem(String tableId, int itemIndex) {
    MysteryLootTable table = GetLootTable(tableId);
    if (table == null || itemIndex < 0 || itemIndex >= table.Items.size()) {
      return null;
    }
    return table.Items.get(itemIndex);
  }

  public boolean CreateLootTableItem(String tableId, MysteryLootTableItem item) {
    MysteryLootTablesConfig config = TablesConfig.get();
    MysteryLootTable table = config.GetRewardPool(tableId);
    if (table == null) {
      return false;
    }
    table.Items.add(item);
    TablesConfig.save();
    return true;
  }

  public boolean UpdateLootTableItem(String tableId, int itemIndex, MysteryLootTableItem updatedItem) {
    MysteryLootTablesConfig config = TablesConfig.get();
    MysteryLootTable table = config.GetRewardPool(tableId);
    if (table == null) {
      return false;
    }
    if (itemIndex < 0 || itemIndex >= table.Items.size()) {
      return false;
    }
    table.Items.set(itemIndex, updatedItem);
    TablesConfig.save();
    return true;
  }

  public boolean DeleteLootTableItem(String tableId, int itemIndex) {
    MysteryLootTablesConfig config = TablesConfig.get();
    MysteryLootTable table = config.GetRewardPool(tableId);
    if (table == null) {
      return false;
    }
    if (itemIndex < 0 || itemIndex >= table.Items.size()) {
      return false;
    }
    table.Items.remove(itemIndex);
    TablesConfig.save();
    return true;
  }

}
