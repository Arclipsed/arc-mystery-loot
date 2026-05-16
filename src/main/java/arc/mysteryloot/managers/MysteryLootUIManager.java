package arc.mysteryloot.managers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

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
}
