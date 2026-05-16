package arc.mysteryloot.configs;

import javax.annotation.Nonnull;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class MysteryLootConfig {
  @Nonnull
  public String RewardPoolId = "";

  public MysteryLootConfig() {}

  public MysteryLootConfig(MysteryLootConfig other) {
    if (other != null) {
      this.RewardPoolId = other.RewardPoolId;
    }
  }

  @Nonnull
  public static final BuilderCodec<MysteryLootConfig> CODEC = BuilderCodec
    .builder(MysteryLootConfig.class, MysteryLootConfig::new)
    .append(
      new KeyedCodec<>("RewardPoolId", Codec.STRING),
      (config, value) -> config.RewardPoolId = value,
      config -> config.RewardPoolId
    )
    .add()
    .build();
}
