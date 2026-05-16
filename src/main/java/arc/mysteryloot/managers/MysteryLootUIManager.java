package arc.mysteryloot.managers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import arc.mysteryloot.pages.MysteryLootTableEditorPage;
import arc.mysteryloot.pages.MysteryLootPage;

public class MysteryLootUIManager {
  @Nonnull
  private final MysteryLootManager Manager;

  public MysteryLootUIManager(@Nonnull MysteryLootManager manager) {
    this.Manager = manager;
  }

  public void OpenLootTableManagerPage(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nullable String initialTableId
  ) {
    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
    if (playerRef == null || !playerRef.isValid()) return;

    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null) return;

    var page = new MysteryLootPage(playerRef, initialTableId);
    player.getPageManager().openCustomPage(ref, store, page);
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
}

