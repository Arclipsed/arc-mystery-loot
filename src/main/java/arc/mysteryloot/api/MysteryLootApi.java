package arc.mysteryloot.api;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import arc.mysteryloot.MysteryLootPlugin;
import arc.mysteryloot.classes.MysteryLootTable;

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

  /**
   * Opens the player-facing Mystery Loot table page for the given table.
   * Call this from your own plugin when a player interacts with a loot source.
  */
  public static void OpenMysteryLootTablePage(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nonnull String tableId
  ) {
    MysteryLootPlugin.INSTANCE.Manager.UI.OpenMysteryLootTablePage(ref, store, tableId);
  }
}
