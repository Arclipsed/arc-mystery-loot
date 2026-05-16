package arc.mysteryloot.systems;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import arc.mysteryloot.MysteryLootPlugin;
import arc.mysteryloot.components.LootRollViewer;
import arc.mysteryloot.pages.MysteryLootRollAnimationPage;

/**
 * Ticks every frame for any player with a LootRollViewer component.
 * Flashes a random item every 100ms; after TotalTime seconds, reveals
 * the real result and opens MysteryLootTableResultPage.
 */
public class LootRollSystem extends EntityTickingSystem<EntityStore> {

  private static final float FLASH_INTERVAL = 0.1f; // 100ms between flashes

  @Override
  public Query<EntityStore> getQuery() {
    return Query.and(LootRollViewer.GetComponentType());
  }

  @Override
  public void tick(
    float dt,
    int index,
    @Nonnull ArchetypeChunk<EntityStore> chunk,
    @Nonnull Store<EntityStore> store,
    @Nonnull CommandBuffer<EntityStore> commandBuffer
  ) {
    var viewerType = LootRollViewer.GetComponentType();
    if (viewerType == null) return;

    var ref = chunk.getReferenceTo(index);
    if (ref == null || !ref.isValid()) return;

    var playerRef = chunk.getComponent(index, PlayerRef.getComponentType());
    if (playerRef == null || !playerRef.isValid()) return;

    var viewer = chunk.getComponent(index, viewerType);
    if (viewer == null) return;

    viewer.AccumulatedTime += dt;

    // Flash a new random item every FLASH_INTERVAL seconds
    var page = MysteryLootRollAnimationPage.OpenPages.get(playerRef.getUuid());
    if (page != null && viewer.AccumulatedTime % FLASH_INTERVAL < dt) {
      page.ShowNextRandomItem();
    }

    // Animation done — reveal result
    if (viewer.AccumulatedTime >= viewer.TotalTime) {
      commandBuffer.removeComponent(ref, viewerType);

      // Close animation page and open result page
      var player = store.getComponent(ref, Player.getComponentType());
      if (player != null) {
        player.getPageManager().setPage(ref, commandBuffer.getStore(), Page.None);
      }

      MysteryLootPlugin.INSTANCE.Manager.UI.OpenResultPage(
        ref, commandBuffer.getStore(), viewer.ItemId, viewer.Amount
      );
    }
  }
}
