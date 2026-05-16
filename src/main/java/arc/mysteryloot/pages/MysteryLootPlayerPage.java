package arc.mysteryloot.pages;

import java.util.ArrayList;
import java.util.List;

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
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import arc.mysteryloot.MysteryLootPlugin;
import arc.mysteryloot.classes.MysteryLootTable;
import arc.mysteryloot.classes.MysteryLootTableItem;

/**
 * Player-facing loot roll page.
 * Shows all possible items in the table, then lets the player roll.
 * After rolling, the result item is revealed.
 */
public class MysteryLootPlayerPage extends InteractiveCustomUIPage<MysteryLootPlayerPage.MysteryLootPlayerPageEventData> {

  @Nonnull private final String TableId;
  @Nullable private MysteryLootTableItem RolledItem;
  private boolean HasRolled = false;

  public MysteryLootPlayerPage(@Nonnull PlayerRef playerRef, @Nonnull String tableId) {
    super(playerRef, CustomPageLifetime.CanDismiss, MysteryLootPlayerPageEventData.CODEC);
    this.TableId = tableId;
  }

  // ── Build ──────────────────────────────────────────────────────────────────

  @Override
  public void build(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull UICommandBuilder cmd,
    @Nonnull UIEventBuilder events,
    @Nonnull Store<EntityStore> store
  ) {
    cmd.append("MysteryLoot/Pages/MysteryLootPlayerPage.ui");

    cmd.set("#TableNameLabel.Text", TableId.replace("_", " "));
    cmd.set("#RollButton.Disabled", HasRolled);

    renderItems(cmd);
    renderResult(cmd);

    events.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("Action", "Close"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#RollButton", EventData.of("Action", "Roll"), false);
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private void renderItems(UICommandBuilder cmd) {
    MysteryLootTable table = MysteryLootPlugin.INSTANCE.Manager.GetLootTable(TableId);
    if (table == null) return;

    for (int i = 0; i < table.Items.size(); i++) {
      MysteryLootTableItem item = table.Items.get(i);
      String sel = "#LootItemsContainer[" + i + "]";
      cmd.append("#LootItemsContainer", "MysteryLoot/Pages/Components/PlayerLootItemSlot.ui");
      cmd.set(sel + " #PlayerLootItemSlot.ItemId", item.ItemId);
      cmd.set(sel + " #PlayerLootItemSlot.Quantity", item.Amount);
    }
  }

  private void renderResult(UICommandBuilder cmd) {
    cmd.set("#ResultPanel.Visible", HasRolled && RolledItem != null);
    if (HasRolled && RolledItem != null) {
      cmd.set("#ResultItemSlot.ItemId", RolledItem.ItemId);
      cmd.set("#ResultItemSlot.Quantity", RolledItem.Amount);
      cmd.set("#ResultItemNameLabel.Text", RolledItem.ItemId.replace("_", " "));
      cmd.set("#ResultAmountLabel.Text", "x" + RolledItem.Amount);
    }
  }

  // ── Events ─────────────────────────────────────────────────────────────────

  @Override
  public void handleDataEvent(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nonnull MysteryLootPlayerPageEventData data
  ) {
    if (data.Action == null) return;

    switch (data.Action) {
      case "Close":
        this.close();
        return;

      case "Roll":
        if (HasRolled) break;
        RolledItem = MysteryLootPlugin.INSTANCE.Manager.RollLootTable(TableId);
        HasRolled = true;
        this.rebuild();
        return;
    }
  }

  @Override
  public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {}

  // ── Event Data ─────────────────────────────────────────────────────────────

  public static class MysteryLootPlayerPageEventData {
    @Nullable public String Action;

    public MysteryLootPlayerPageEventData() {}

    @Nonnull
    public static final BuilderCodec<MysteryLootPlayerPageEventData> CODEC = BuilderCodec
      .builder(MysteryLootPlayerPageEventData.class, MysteryLootPlayerPageEventData::new)
      .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.Action = v, d -> d.Action).add()
      .build();
  }
}
