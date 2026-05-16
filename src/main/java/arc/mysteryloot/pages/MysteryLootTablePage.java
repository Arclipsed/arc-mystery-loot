package arc.mysteryloot.pages;

import java.util.ArrayList;
import java.util.Comparator;
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
 * Player-facing Mystery Loot page.
 * Shows all possible items in the table with their drop %, then lets the player roll.
 */
public class MysteryLootTablePage extends InteractiveCustomUIPage<MysteryLootTablePage.MysteryLootTablePageEventData> {

  @Nonnull private final String TableId;
  @Nonnull private final PlayerRef Player;
  private boolean HasRolled = false;

  public MysteryLootTablePage(@Nonnull PlayerRef playerRef, @Nonnull String tableId) {
    super(playerRef, CustomPageLifetime.CanDismiss, MysteryLootTablePageEventData.CODEC);
    this.Player = playerRef;
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
    cmd.append("MysteryLoot/Pages/MysteryLootTablePage.ui");

    cmd.set("#TableNameLabel.Text", TableId.replace("_", " "));
    cmd.set("#RollButton.Disabled", HasRolled);

    renderItems(cmd);

    events.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("Action", "Close"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#RollButton", EventData.of("Action", "Roll"), false);
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private static final int ITEMS_PER_ROW = 5;

  private void renderItems(UICommandBuilder cmd) {
    MysteryLootTable table = MysteryLootPlugin.INSTANCE.Manager.GetLootTable(TableId);
    if (table == null) return;

    // Sort rarest first (lowest DropWeight = rarest)
    List<MysteryLootTableItem> sorted = new ArrayList<>(table.Items);
    sorted.sort(Comparator.comparingDouble(i -> i.DropWeight));

    double totalWeight = table.Items.stream().mapToDouble(i -> i.DropWeight).sum();

    int rowIndex = 0;

    for (int i = 0; i < sorted.size(); i++) {
      MysteryLootTableItem item = sorted.get(i);
      String pct = totalWeight > 0
        ? String.format("%.3f%%", (item.DropWeight / totalWeight) * 100.0)
        : "0%";

      // Start a new row every 5 items
      if (i % ITEMS_PER_ROW == 0) {
        rowIndex = i / ITEMS_PER_ROW;
        cmd.append("#LootItemsContainer", "MysteryLoot/Pages/Components/LootCardRow.ui");
      }

      String rowSel = "#LootItemsContainer[" + rowIndex + "]";
      cmd.append(rowSel, "MysteryLoot/Pages/Components/LootItemCard.ui");

      String cardSel = rowSel + "[" + (i % ITEMS_PER_ROW) + "]";
      cmd.set(cardSel + " #LootItemCardSlot.ItemId", item.ItemId);
      cmd.set(cardSel + " #LootItemCardSlot.Quantity", item.Amount);
      cmd.set(cardSel + " #LootItemPctLabel.Text", pct);
    }
  }


  // ── Events ─────────────────────────────────────────────────────────────────

  @Override
  public void handleDataEvent(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nonnull MysteryLootTablePageEventData data
  ) {
    if (data.Action == null) return;

    switch (data.Action) {
      case "Close":
        this.close();
        return;

      case "Roll":
        if (HasRolled) break;
        var rolled = MysteryLootPlugin.INSTANCE.Manager.RollAndGiveLootTable(TableId, ref, store, Player);
        HasRolled = true;
        if (rolled != null) {
          var table = MysteryLootPlugin.INSTANCE.Manager.GetLootTable(TableId);
          var items = table != null ? table.Items : java.util.List.of(rolled);
          MysteryLootPlugin.INSTANCE.Manager.UI.OpenRollAnimationPage(ref, store, items, rolled.ItemId, rolled.Amount);
        }
        return;
    }
  }

  @Override
  public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {}

  // ── Event Data ─────────────────────────────────────────────────────────────

  public static class MysteryLootTablePageEventData {
    @Nullable public String Action;

    public MysteryLootTablePageEventData() {}

    @Nonnull
    public static final BuilderCodec<MysteryLootTablePageEventData> CODEC = BuilderCodec
      .builder(MysteryLootTablePageEventData.class, MysteryLootTablePageEventData::new)
      .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.Action = v, d -> d.Action).add()
      .build();
  }
}
