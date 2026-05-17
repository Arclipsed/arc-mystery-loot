package arc.mysteryloot.managers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import arc.mysteryloot.pages.MysteryLootRollAnimationPage;
import arc.mysteryloot.pages.MysteryLootSimulatePage;
import arc.mysteryloot.pages.MysteryLootTableEditorPage;
import arc.mysteryloot.pages.MysteryLootTablePage;
import arc.mysteryloot.pages.MysteryLootTableResultPage;
import arc.mysteryloot.classes.MysteryLootTableItem;
import arc.mysteryloot.components.LootRollViewer;

public class MysteryLootUIManager {
  @Nonnull
  private final MysteryLootManager Manager;

  public MysteryLootUIManager(@Nonnull MysteryLootManager manager) {
    this.Manager = manager;
  }

  public void OpenCreateLootTablePage(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store
  ) {
    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
    if (playerRef == null || !playerRef.isValid()) return;

    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null) return;

    // Open with no table pre-selected → "-- Create New --" mode
    var page = new MysteryLootTableEditorPage(playerRef, null);
    player.getPageManager().openCustomPage(ref, store, page);
  }

  public void OpenUpdateLootTablePage(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nonnull String tableId
  ) {
    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
    if (playerRef == null || !playerRef.isValid()) return;

    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null) return;

    // Open with a specific table pre-selected
    var page = new MysteryLootTableEditorPage(playerRef, tableId);
    player.getPageManager().openCustomPage(ref, store, page);
  }
  
  public void OpenSimulatePage(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nullable String initialTableId
  ) {
    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
    if (playerRef == null || !playerRef.isValid()) return;

    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null) return;

    var page = new MysteryLootSimulatePage(playerRef, initialTableId);
    player.getPageManager().openCustomPage(ref, store, page);
  }

  public void OpenMysteryLootTablePage(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nonnull String tableId
  ) {
    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
    if (playerRef == null || !playerRef.isValid()) return;

    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null) return;

    var page = new MysteryLootTablePage(playerRef, tableId);
    player.getPageManager().openCustomPage(ref, store, page);
  }

  public void OpenResultPage(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nonnull String itemId,
    int amount
  ) {
    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
    if (playerRef == null || !playerRef.isValid()) return;

    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null) return;

    var page = new MysteryLootTableResultPage(playerRef, itemId, amount);
    player.getPageManager().openCustomPage(ref, store, page);
  }

  public void OpenRollAnimationPage(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nonnull String tableId,
    @Nonnull String realItemId,
    int realAmount
  ) {
    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
    if (playerRef == null || !playerRef.isValid()) return;

    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null) return;

    // Attach the LootRollViewer component so LootRollSystem can drive the animation
    var viewer = new LootRollViewer();
    viewer.TableId = tableId;
    viewer.ItemId = realItemId;
    viewer.Amount = realAmount;
    store.addComponent(ref, LootRollViewer.GetComponentType(), viewer);

    // Page looks up the table items itself
    var table = Manager.GetLootTable(tableId);
    var items = table != null ? table.Items : new java.util.ArrayList<MysteryLootTableItem>();
    var page = new MysteryLootRollAnimationPage(playerRef, items, realItemId);
    player.getPageManager().openCustomPage(ref, store, page);
  }
}

