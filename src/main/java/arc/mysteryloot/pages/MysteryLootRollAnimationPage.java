package arc.mysteryloot.pages;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import arc.mysteryloot.classes.MysteryLootTableItem;

/**
 * Displays a slot-machine style animation before revealing the real loot result.
 * The system ticks this page via LootRollSystem — not interactive.
 */
public class MysteryLootRollAnimationPage extends InteractiveCustomUIPage<MysteryLootRollAnimationPage.RollAnimPageEventData> {

  /** Static registry so LootRollSystem can find the open page by player UUID. */
  public static final ConcurrentHashMap<UUID, MysteryLootRollAnimationPage> OpenPages = new ConcurrentHashMap<>();

  /** All items in the table — used to pick random ones for the flash animation. */
  @Nonnull private final List<MysteryLootTableItem> TableItems;

  /** Current item being displayed (changes each tick). */
  @Nonnull private String CurrentItemId;
  private int CurrentAmount;

  private int FlashIndex = 0;

  @Nonnull private final PlayerRef Player;

  public MysteryLootRollAnimationPage(
    @Nonnull PlayerRef playerRef,
    @Nonnull List<MysteryLootTableItem> tableItems,
    @Nonnull String initialItemId
  ) {
    super(playerRef, CustomPageLifetime.CanDismiss, RollAnimPageEventData.CODEC);
    this.Player = playerRef;
    this.TableItems = tableItems;
    this.CurrentItemId = initialItemId;
    this.CurrentAmount = 1;
    OpenPages.put(playerRef.getUuid(), this);
  }

  // ── Build ──────────────────────────────────────────────────────────────────

  @Override
  public void build(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull UICommandBuilder cmd,
    @Nonnull UIEventBuilder events,
    @Nonnull Store<EntityStore> store
  ) {
    cmd.append("MysteryLoot/Pages/MysteryLootRollAnimationPage.ui");
    cmd.set("#RollItemSlot.ItemId", CurrentItemId);
    cmd.set("#RollItemSlot.Quantity", CurrentAmount);
  }

  // ── Called by LootRollSystem each tick ────────────────────────────────────

  public void ShowNextRandomItem() {
    if (TableItems.isEmpty()) return;
    FlashIndex = (FlashIndex + 1) % TableItems.size();
    var item = TableItems.get(FlashIndex);
    CurrentItemId = item.ItemId;
    CurrentAmount = item.Amount;
    this.rebuild();
  }

  // ── Cleanup ───────────────────────────────────────────────────────────────

  @Override
  public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
    OpenPages.remove(Player.getUuid());
  }

  @Override
  public void handleDataEvent(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nonnull RollAnimPageEventData data
  ) {}

  // ── Event Data ─────────────────────────────────────────────────────────────

  public static class RollAnimPageEventData {
    public RollAnimPageEventData() {}

    @Nonnull
    public static final BuilderCodec<RollAnimPageEventData> CODEC = BuilderCodec
      .builder(RollAnimPageEventData.class, RollAnimPageEventData::new)
      .build();
  }
}
