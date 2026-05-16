package arc.mysteryloot.pages;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
 * Admin-only loot table simulator.
 * Pick a table, view all items + drop %, roll 1/5/10 times, see history.
 * All history is in-memory only — nothing is saved.
 */
public class MysteryLootSimulatePage extends InteractiveCustomUIPage<MysteryLootSimulatePage.MysteryLootSimulatePageEventData> {

  private static final int MAX_HISTORY = 50;

  @Nullable private String SelectedTableId;
  @Nullable private MysteryLootTableItem LastRolledItem;
  private int TotalRolls = 0;

  // In-memory history: most recent first
  @Nonnull private final Deque<String> History = new ArrayDeque<>();

  public MysteryLootSimulatePage(@Nonnull PlayerRef playerRef, @Nullable String initialTableId) {
    super(playerRef, CustomPageLifetime.CanDismiss, MysteryLootSimulatePageEventData.CODEC);
    var tables = MysteryLootPlugin.INSTANCE.Manager.GetAllLootTables();
    if (initialTableId != null && tables.containsKey(initialTableId)) {
      SelectedTableId = initialTableId;
    } else if (!tables.isEmpty()) {
      SelectedTableId = tables.keySet().stream().sorted().findFirst().orElse(null);
    }
  }

  // ── Build (called fresh on every rebuild()) ────────────────────────────────

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
    renderHistory(cmd);
    renderLastResult(cmd);

    events.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("Action", "Close"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#Roll1Button", EventData.of("Action", "Roll").append("Count", "1"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#Roll5Button", EventData.of("Action", "Roll").append("Count", "5"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#Roll10Button", EventData.of("Action", "Roll").append("Count", "10"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#ClearHistoryButton", EventData.of("Action", "ClearHistory"), false);
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
    cmd.set("#Roll1Button.Disabled", SelectedTableId == null);
    cmd.set("#Roll5Button.Disabled", SelectedTableId == null);
    cmd.set("#Roll10Button.Disabled", SelectedTableId == null);
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
      boolean isLast = LastRolledItem != null && item.ItemId.equals(LastRolledItem.ItemId);

      String sel = "#ItemListContainer[" + i + "]";
      cmd.append("#ItemListContainer", "MysteryLoot/Pages/Components/LootItemRow.ui");
      cmd.set(sel + " #LootItemSlot.ItemId", item.ItemId);
      cmd.set(sel + " #LootItemSlot.Quantity", item.Amount);
      cmd.set(sel + " #ItemIdLabel.Text", item.ItemId);
      cmd.set(sel + " #ItemChanceLabel.Text", "x" + item.Amount + "  |  w" + String.format("%.3f", item.DropWeight) + "  →  " + pct);
      cmd.set(sel + " #RolledIndicator.Visible", isLast);
    }
  }

  private void renderHistory(UICommandBuilder cmd) {
    cmd.set("#TotalRollsLabel.Text", "Total Rolls: " + TotalRolls);
    cmd.set("#ClearHistoryButton.Visible", !History.isEmpty());
    int i = 0;
    for (String entry : History) {
      String sel = "#HistoryListContainer[" + i + "]";
      cmd.append("#HistoryListContainer", "MysteryLoot/Pages/Components/HistoryRow.ui");
      cmd.set(sel + " #HistoryEntryLabel.Text", entry);
      i++;
    }
  }

  private void renderLastResult(UICommandBuilder cmd) {
    boolean has = LastRolledItem != null;
    cmd.set("#ResultPanel.Visible", has);
    if (has) {
      cmd.set("#ResultItemSlot.ItemId", LastRolledItem.ItemId);
      cmd.set("#ResultItemSlot.Quantity", LastRolledItem.Amount);
      cmd.set("#ResultItemIdLabel.Text", LastRolledItem.ItemId + "  x" + LastRolledItem.Amount);
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
        LastRolledItem = null;
        History.clear();
        TotalRolls = 0;
        this.rebuild();
        return;

      case "ClearHistory":
        History.clear();
        LastRolledItem = null;
        TotalRolls = 0;
        this.rebuild();
        return;

      case "Roll":
        if (SelectedTableId == null) break;
        int count = 1;
        try { if (data.Count != null) count = Integer.parseInt(data.Count); }
        catch (Exception ignored) {}

        MysteryLootTableItem lastResult = null;
        for (int i = 0; i < count; i++) {
          MysteryLootTableItem rolled = MysteryLootPlugin.INSTANCE.Manager.RollLootTable(SelectedTableId);
          if (rolled == null) continue;
          lastResult = rolled;
          TotalRolls++;
          String entry = "#" + TotalRolls + "  →  " + rolled.ItemId + "  x" + rolled.Amount;
          History.addFirst(entry);
          if (History.size() > MAX_HISTORY) History.removeLast();
        }
        if (lastResult != null) LastRolledItem = lastResult;
        // Always rebuild so item list containers reset (avoids duplicate rows)
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
