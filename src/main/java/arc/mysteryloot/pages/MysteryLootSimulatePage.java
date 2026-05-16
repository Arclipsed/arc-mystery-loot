package arc.mysteryloot.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Admin-only loot table simulator.
 * Rolls 1x / 10x / 100x and shows an aggregated result overview.
 * All data is in-memory only — nothing is saved.
 */
public class MysteryLootSimulatePage extends InteractiveCustomUIPage<MysteryLootSimulatePage.MysteryLootSimulatePageEventData> {

  @Nullable private String SelectedTableId;
  @Nonnull private final Map<String, Integer> RollCounts = new HashMap<>();
  @Nonnull private final Map<String, Integer> ItemAmounts = new HashMap<>();
  private int TotalRolls = 0;

  public MysteryLootSimulatePage(@Nonnull PlayerRef playerRef, @Nullable String initialTableId) {
    super(playerRef, CustomPageLifetime.CanDismiss, MysteryLootSimulatePageEventData.CODEC);
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
    cmd.append("MysteryLoot/Pages/MysteryLootSimulatePage.ui");

    populateTableDropdown(cmd);
    renderItemList(cmd);
    renderStats(cmd);

    events.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("Action", "Close"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#Roll1Button",   EventData.of("Action", "Roll").append("Count", "1"),   false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#Roll10Button",  EventData.of("Action", "Roll").append("Count", "10"),  false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#Roll100Button", EventData.of("Action", "Roll").append("Count", "100"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#ResetButton",   EventData.of("Action", "Reset"),  false);
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
    boolean has = SelectedTableId != null;
    cmd.set("#Roll1Button.Disabled", !has);
    cmd.set("#Roll10Button.Disabled", !has);
    cmd.set("#Roll100Button.Disabled", !has);
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
      String sel = "#ItemListContainer[" + i + "]";
      cmd.append("#ItemListContainer", "MysteryLoot/Pages/Components/LootItemRow.ui");
      cmd.set(sel + " #LootItemSlot.ItemId", item.ItemId);
      cmd.set(sel + " #LootItemSlot.Quantity", item.Amount);
      cmd.set(sel + " #ItemIdLabel.Text", item.ItemId);
      cmd.set(sel + " #ItemChanceLabel.Text", "x" + item.Amount + "  |  w" + String.format("%.3f", item.DropWeight) + "  ->  " + pct);
    }
  }

  private void renderStats(UICommandBuilder cmd) {
    boolean hasData = TotalRolls > 0;
    cmd.set("#StatsPanel.Visible", hasData);
    cmd.set("#ResetButton.Visible", hasData);
    cmd.set("#TotalRollsLabel.Text", TotalRolls + " Total Rolls");

    if (!hasData) return;

    // Sort by most rolled descending
    List<Map.Entry<String, Integer>> sorted = new ArrayList<>(RollCounts.entrySet());
    sorted.sort((a, b) -> b.getValue() - a.getValue());

    for (int i = 0; i < sorted.size(); i++) {
      String itemId = sorted.get(i).getKey();
      int count = sorted.get(i).getValue();
      int amount = ItemAmounts.getOrDefault(itemId, 1);
      double pct = TotalRolls > 0 ? (count / (double) TotalRolls) * 100.0 : 0;

      String sel = "#StatsListContainer[" + i + "]";
      cmd.append("#StatsListContainer", "MysteryLoot/Pages/Components/StatsRow.ui");
      cmd.set(sel + " #StatsItemSlot.ItemId", itemId);
      cmd.set(sel + " #StatsItemSlot.Quantity", amount);
      cmd.set(sel + " #StatsItemIdLabel.Text", itemId);
      cmd.set(sel + " #StatsCountLabel.Text", count + "x rolled  (" + String.format("%.2f", pct) + "%)");
    }
  }

  // ── Events ─────────────────────────────────────────────────────────────────

  @Override
  public void handleDataEvent(
    @Nonnull Ref<EntityStore> ref,
    @Nonnull Store<EntityStore> store,
    @Nonnull MysteryLootSimulatePageEventData data
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
        RollCounts.clear();
        ItemAmounts.clear();
        TotalRolls = 0;
        this.rebuild();
        return;

      case "Reset":
        RollCounts.clear();
        ItemAmounts.clear();
        TotalRolls = 0;
        this.rebuild();
        return;

      case "Roll":
        if (SelectedTableId == null) break;
        int count = 1;
        try { if (data.Count != null) count = Integer.parseInt(data.Count); }
        catch (Exception ignored) {}

        for (int i = 0; i < count; i++) {
          MysteryLootTableItem rolled = MysteryLootPlugin.INSTANCE.Manager.RollLootTable(SelectedTableId);
          if (rolled == null) continue;
          TotalRolls++;
          RollCounts.merge(rolled.ItemId, 1, Integer::sum);
          ItemAmounts.putIfAbsent(rolled.ItemId, rolled.Amount);
        }
        this.rebuild();
        return;
    }
  }

  @Override
  public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {}

  // ── Event Data ─────────────────────────────────────────────────────────────

  public static class MysteryLootSimulatePageEventData {
    @Nullable public String Action;
    @Nullable public String SelectedType;
    @Nullable public String Count;

    public MysteryLootSimulatePageEventData() {}

    @Nonnull
    public static final BuilderCodec<MysteryLootSimulatePageEventData> CODEC = BuilderCodec
      .builder(MysteryLootSimulatePageEventData.class, MysteryLootSimulatePageEventData::new)
      .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.Action = v, d -> d.Action).add()
      .append(new KeyedCodec<>("@SelectedType", Codec.STRING), (d, v) -> d.SelectedType = v, d -> d.SelectedType).add()
      .append(new KeyedCodec<>("Count", Codec.STRING), (d, v) -> d.Count = v, d -> d.Count).add()
      .build();
  }
}
