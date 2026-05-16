package arc.mysteryloot.managers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import arc.mysteryloot.pages.CreateLootTablePage;
import arc.mysteryloot.pages.MysteryLootPage;
import arc.mysteryloot.pages.UpdateLootTablePage;

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

    var page = new CreateLootTablePage(playerRef);
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

    var page = new UpdateLootTablePage(playerRef, tableId);
    player.getPageManager().openCustomPage(ref, store, page);
  }
}

