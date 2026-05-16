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
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import arc.mysteryloot.MysteryLootPlugin;
import arc.mysteryloot.classes.MysteryLootTable;
import arc.mysteryloot.classes.MysteryLootTableItem;

/**
 * Slot machine viewer for a loot table.
 * Lets you pick a table from the dropdown, see all its items with their %,
 * then hit "Roll!" to simulate a weighted draw and highlight the result.
 */
public class MysteryLootRollPage extends InteractiveCustomUIPage<MysteryLootRollPage.MysteryLootRollPageEventData> {

  @Nullable private String SelectedTableId;
  @Nullable private MysteryLootTableItem RolledItem;
  private int RollCount = 0;

  public MysteryLootRollPage(@Nonnull PlayerRef playerRef, @Nullable String initialTableId) {
    super(playerRef, CustomPageLifetime.CanDismiss, MysteryLootRollPageEventData.CODEC);

    var tables = MysteryLootPlugin.INSTANCE.Manager.GetAllLootTables();
    if (initialTableId != null && tables.containsKey(initialTableId)) {
      SelectedTableId = initialTableId;
    } else if (!tables.isEmpty()) {
      SelectedTableId = tables.keySet().stream().sorted().findFirst().orElse(null);
    }
  }

  // ── Build ──────────────────────────────────────────────────────────────────

  @Override
  public void build(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull UICommandBuilder cmd,
    @Nonnull UIEventBuilder events,
    @Nonnull Store<EntityStore> store
  ) {
    cmd.append("MysteryLoot/Pages/MysteryLootRollPage.ui");

    populateTableDropdown(cmd);
    renderItemList(cmd);
    renderRollResult(cmd);

    events.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("Action", "Close"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#RollButton", EventData.of("Action", "Roll"), false);
    events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TableDropdown",
      new EventData().append("Action", "SelectTable").append("@SelectedType", "#TableDropdown.Value"), false);
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private void populateTableDropdown(UICommandBuilder cmd) {
    var tables = MysteryLootPlugin.INSTANCE.Manager.GetAllLootTables();
    List<DropdownEntryInfo> entries = new ArrayList<>();
    tables.keySet().stream().sorted().forEach(key ->
      entries.add(new DropdownEntryInfo(LocalizableString.fromString(key), key))
    );
    cmd.set("#TableDropdown.Entries", entries);
    cmd.set("#TableDropdown.Value", SelectedTableId != null ? SelectedTableId : "");
    boolean hasTable = SelectedTableId != null;
    cmd.set("#RollButton.Disabled", !hasTable);
  }

  private void renderItemList(UICommandBuilder cmd) {
    if (SelectedTableId == null) return;

    MysteryLootTable table = MysteryLootPlugin.INSTANCE.Manager.GetLootTable(SelectedTableId);
    if (table == null || table.Items.isEmpty()) return;

    double totalWeight = table.Items.stream().mapToDouble(i -> i.DropWeight).sum();

    for (int i = 0; i < table.Items.size(); i++) {
      MysteryLootTableItem item = table.Items.get(i);
      String pct = totalWeight > 0
        ? String.format("%.3f%%", (item.DropWeight / totalWeight) * 100.0)
        : "0%";
      boolean isRolled = RolledItem != null && item.ItemId.equals(RolledItem.ItemId);

      String sel = "#ItemListContainer[" + i + "]";
      cmd.append("#ItemListContainer", "MysteryLoot/Pages/Components/LootItemRow.ui");
      cmd.set(sel + " #LootItemSlot.ItemId", item.ItemId);
      cmd.set(sel + " #LootItemSlot.Quantity", item.Amount);
      cmd.set(sel + " #ItemIdLabel.Text", item.ItemId);
      cmd.set(sel + " #ItemChanceLabel.Text", "x" + item.Amount + "  |  w" + String.format("%.3f", item.DropWeight) + "  →  " + pct);
      cmd.set(sel + " #RolledIndicator.Visible", isRolled);
    }
  }

  private void renderRollResult(UICommandBuilder cmd) {
    boolean hasResult = RolledItem != null;
    cmd.set("#ResultPanel.Visible", hasResult);
    if (hasResult) {
      cmd.set("#ResultItemSlot.ItemId", RolledItem.ItemId);
      cmd.set("#ResultItemSlot.Quantity", RolledItem.Amount);
      cmd.set("#ResultItemIdLabel.Text", RolledItem.ItemId);
      cmd.set("#RollCountLabel.Text", "Roll #" + RollCount);
    }
  }

  // ── Events ─────────────────────────────────────────────────────────────────

  @Override
  public void handleDataEvent(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nonnull MysteryLootRollPageEventData data
  ) {
    if (data.Action == null) return;

    switch (data.Action) {
      case "Close":
        this.close();
        return;

      case "SelectTable":
        var tables = MysteryLootPlugin.INSTANCE.Manager.GetAllLootTables();
        SelectedTableId = (data.SelectedType != null && tables.containsKey(data.SelectedType))
          ? data.SelectedType : null;
        RolledItem = null;
        RollCount = 0;
        // rebuild to re-render item list for new table
        this.rebuild();
        return;

      case "Roll":
        if (SelectedTableId == null) break;
        RolledItem = MysteryLootPlugin.INSTANCE.Manager.RollLootTable(SelectedTableId);
        RollCount++;
        break;
    }

    UICommandBuilder update = new UICommandBuilder();
    // re-render item list to update the highlight
    renderItemList(update);
    renderRollResult(update);
    sendUpdate(update);
  }

  @Override
  public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {}

  // ── Event Data ─────────────────────────────────────────────────────────────

  public static class MysteryLootRollPageEventData {
    @Nullable public String Action;
    @Nullable public String SelectedType;

    public MysteryLootRollPageEventData() {}

    @Nonnull
    public static final BuilderCodec<MysteryLootRollPageEventData> CODEC = BuilderCodec
      .builder(MysteryLootRollPageEventData.class, MysteryLootRollPageEventData::new)
      .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.Action = v, d -> d.Action).add()
      .append(new KeyedCodec<>("@SelectedType", Codec.STRING), (d, v) -> d.SelectedType = v, d -> d.SelectedType).add()
      .build();
  }
}
