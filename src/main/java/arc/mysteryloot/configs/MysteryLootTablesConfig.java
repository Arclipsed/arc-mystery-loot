package arc.mysteryloot.configs;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;

import arc.mysteryloot.classes.MysteryLootTable;

public class MysteryLootTablesConfig {

  @Nonnull
  public Map<String, MysteryLootTable> RewardPools = new HashMap<>();

  public MysteryLootTablesConfig() {}

  public void Init() {
    if (RewardPools == null) {
      RewardPools = new HashMap<>();
    }
    RewardPools.computeIfAbsent("Default_Loot", id -> new MysteryLootTable());
  }

  @Nullable
  public MysteryLootTable GetRewardPool(String tableId) {
    if (tableId == null || tableId.isEmpty()) return null;
    return RewardPools.get(tableId);
  }

  public void SaveRewardPool(String tableId, MysteryLootTable config) {
    RewardPools.put(tableId, config);
  }

  public void RenameRewardPool(String oldId, String newId, MysteryLootTable config) {
    RewardPools.remove(oldId);
    RewardPools.put(newId, config);
  }

  public void DeleteRewardPool(String tableId) {
    RewardPools.remove(tableId);
  }

  @Nonnull
  public static final BuilderCodec<MysteryLootTablesConfig> CODEC = BuilderCodec
    .builder(MysteryLootTablesConfig.class, MysteryLootTablesConfig::new)
    .append(
      new KeyedCodec<>("RewardPools", new MapCodec<>(MysteryLootTable.CODEC, HashMap::new)),
      (config, value) -> config.RewardPools = value != null ? new HashMap<>(value) : new HashMap<>(),
      config -> config.RewardPools
    )
    .add()
    .build();
}
