package arc.mysteryloot.pages;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Displayed after a player rolls a loot table.
 * Shows the item they received in a clean, centered dialog.
 */
public class MysteryLootTableResultPage extends InteractiveCustomUIPage<MysteryLootTableResultPage.ResultPageEventData> {

  @Nonnull private final String ItemId;
  private final int Amount;

  public MysteryLootTableResultPage(
    @Nonnull PlayerRef playerRef,
    @Nonnull String itemId,
    int amount
  ) {
    super(playerRef, CustomPageLifetime.CanDismiss, ResultPageEventData.CODEC);
    this.ItemId = itemId;
    this.Amount = amount;
  }

  // ── Build ──────────────────────────────────────────────────────────────────

  @Override
  public void build(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull UICommandBuilder cmd,
    @Nonnull UIEventBuilder events,
    @Nonnull Store<EntityStore> store
  ) {
    cmd.append("MysteryLoot/Pages/MysteryLootTableResultPage.ui");

    cmd.set("#ResultItemSlot.ItemId", ItemId);
    cmd.set("#ResultItemSlot.Quantity", Amount);
    cmd.set("#ResultItemNameLabel.Text", ItemId.replace("_", " "));
    cmd.set("#ResultAmountLabel.Text", "x" + Amount);

    events.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", com.hypixel.hytale.server.core.ui.builder.EventData.of("Action", "Close"), false);
  }

  // ── Events ─────────────────────────────────────────────────────────────────

  @Override
  public void handleDataEvent(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nonnull ResultPageEventData data
  ) {
    if ("Close".equals(data.Action)) {
      this.close();
    }
  }

  @Override
  public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {}

  // ── Event Data ─────────────────────────────────────────────────────────────

  public static class ResultPageEventData {
    @Nullable public String Action;

    public ResultPageEventData() {}

    public static ResultPageEventData of(String key, String value) {
      ResultPageEventData d = new ResultPageEventData();
      d.Action = value;
      return d;
    }

    @Nonnull
    public static final BuilderCodec<ResultPageEventData> CODEC = BuilderCodec
      .builder(ResultPageEventData.class, ResultPageEventData::new)
      .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.Action = v, d -> d.Action).add()
      .build();
  }
}
