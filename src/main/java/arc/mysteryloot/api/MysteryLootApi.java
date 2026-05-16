package arc.mysteryloot.api;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import arc.mysteryloot.MysteryLootPlugin;
import arc.mysteryloot.classes.MysteryLootTable;
import arc.mysteryloot.classes.MysteryLootTableItem;

/**
 * Public API for the Arc Mystery Loot plugin.
 * External plugins should use this class — it delegates to MysteryLootManager and contains no logic itself.
 */
public class MysteryLootApi {

  @Nullable
  public static MysteryLootTable GetLootTable(String tableId) {
    return MysteryLootPlugin.INSTANCE.Manager.GetLootTable(tableId);
  }

  @Nonnull
  public static Map<String, MysteryLootTable> GetAllLootTables() {
    return MysteryLootPlugin.INSTANCE.Manager.GetAllLootTables();
  }

  public static boolean CreateMysteryLootTable(String tableId) {
    return MysteryLootPlugin.INSTANCE.Manager.CreateMysteryLootTable(tableId);
  }

  public static boolean DeleteMysteryLootTable(String tableId) {
    return MysteryLootPlugin.INSTANCE.Manager.DeleteMysteryLootTable(tableId);
  }

  public static boolean UpdateMysteryLootTable(String tableId, MysteryLootTable newTable) {
    return MysteryLootPlugin.INSTANCE.Manager.UpdateMysteryLootTable(tableId, newTable);
  }

  public static boolean RenameMysteryLootTable(String oldId, String newId, MysteryLootTable table) {
    return MysteryLootPlugin.INSTANCE.Manager.RenameMysteryLootTable(oldId, newId, table);
  }

  @Nullable
  public static MysteryLootTableItem GetLootTableItem(String tableId, int itemIndex) {
    return MysteryLootPlugin.INSTANCE.Manager.GetLootTableItem(tableId, itemIndex);
  }

  public static boolean CreateLootTableItem(String tableId, MysteryLootTableItem item) {
    return MysteryLootPlugin.INSTANCE.Manager.CreateLootTableItem(tableId, item);
  }

  public static boolean UpdateLootTableItem(String tableId, int itemIndex, MysteryLootTableItem updatedItem) {
    return MysteryLootPlugin.INSTANCE.Manager.UpdateLootTableItem(tableId, itemIndex, updatedItem);
  }

  public static boolean DeleteLootTableItem(String tableId, int itemIndex) {
    return MysteryLootPlugin.INSTANCE.Manager.DeleteLootTableItem(tableId, itemIndex);
  }

  /**
   * Performs a weighted random roll on the given loot table.
   * Returns the rolled item, or null if the table doesn't exist or is empty.
   */
  @Nullable
  public static MysteryLootTableItem RollLootTable(String tableId) {
    return MysteryLootPlugin.INSTANCE.Manager.RollLootTable(tableId);
  }

  /**
   * Opens the player-facing loot roll dialog for the given table.
   * Call this from your own plugin when a player interacts with a loot source.
   */
  public static void OpenPlayerLootPage(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nonnull String tableId
  ) {
    MysteryLootPlugin.INSTANCE.Manager.UI.OpenPlayerPage(ref, store, tableId);
  }
}
