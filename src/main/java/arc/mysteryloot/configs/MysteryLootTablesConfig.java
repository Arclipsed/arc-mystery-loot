package arc.mysteryloot.configs;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;

import arc.mysteryloot.classes.MysteryLootTable;
import arc.mysteryloot.classes.MysteryLootTableItem;

public class MysteryLootTablesConfig {

  @Nonnull
  public Map<String, MysteryLootTable> LootTablePools = new HashMap<>();

  public MysteryLootTablesConfig() {}

  public void Init() {
    if (LootTablePools == null) {
      LootTablePools = new HashMap<>();
    }
    var exampleLootTable = new MysteryLootTable();

    exampleLootTable.Items.add(
      new MysteryLootTableItem(
        "Plant_Fruit_Berries_Red",
        1.0,
        5
      )
    );

    exampleLootTable.Items.add(
      new MysteryLootTableItem(
        "Plant_Fruit_Apple",
        1.0,
        2
      )
    );

    exampleLootTable.Items.add(
      new MysteryLootTableItem(
        "Plant_Fruit_Pinkberry",
        1.0,
        1
      )
    );

    exampleLootTable.Items.add(
      new MysteryLootTableItem(
        "Plant_Fruit_Coconut",
        1.0,
        1
      )
    );

    exampleLootTable.Items.add(
      new MysteryLootTableItem(
        "Food_Bread",
        1.0,
        1
      )
    );

    exampleLootTable.Items.add(
      new MysteryLootTableItem(
        "Food_Fish_Raw",
        2.0,
        1
      )
    );

    LootTablePools.computeIfAbsent("Food_Loot_Table", id -> exampleLootTable);
  }

  @Nullable
  public MysteryLootTable GetRewardPool(String tableId) {
    if (tableId == null || tableId.isEmpty()) return null;
    return LootTablePools.get(tableId);
  }

  public void SaveRewardPool(String tableId, MysteryLootTable config) {
    LootTablePools.put(tableId, config);
  }

  public void RenameRewardPool(String oldId, String newId, MysteryLootTable config) {
    LootTablePools.remove(oldId);
    LootTablePools.put(newId, config);
  }

  public void DeleteRewardPool(String tableId) {
    LootTablePools.remove(tableId);
  }

  @Nonnull
  public static final BuilderCodec<MysteryLootTablesConfig> CODEC = BuilderCodec
    .builder(MysteryLootTablesConfig.class, MysteryLootTablesConfig::new)
    .append(
      new KeyedCodec<>("LootTablePools", new MapCodec<>(MysteryLootTable.CODEC, HashMap::new)),
      (config, value) -> config.LootTablePools = value != null ? new HashMap<>(value) : new HashMap<>(),
      config -> config.LootTablePools
    )
    .add()
    .build();
}
