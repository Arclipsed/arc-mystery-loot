package arc.mysteryloot.classes;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class MysteryLootTableItem {
  @Nonnull
  public String ItemId = "Plant_Fruit_Berries_Red";
  public double DropWeight = 1.0;
  public int Amount = 1;
  public boolean AnnounceWin = false;
  @Nonnull
  public String RewardSound = "SFX_Mystery_Loot_Reward_T1";

  public MysteryLootTableItem() {}
  
  public MysteryLootTableItem(String itemId, double dropWeight, int amount) {
    this.ItemId = itemId;
    this.DropWeight = dropWeight;
    this.Amount = amount;
    this.AnnounceWin = false;
  }
  
  public MysteryLootTableItem(MysteryLootTableItem other) {
    if (other != null) {
      this.ItemId = other.ItemId;
      this.DropWeight = other.DropWeight;
      this.Amount = other.Amount;
      this.AnnounceWin = other.AnnounceWin;
      this.RewardSound = other.RewardSound != null && !other.RewardSound.isEmpty() ? other.RewardSound : "SFX_Mystery_Loot_Reward_T1";
    }
  }

  public boolean Matches(MysteryLootTableItem other) {
    if (other == null) return false;
    return java.util.Objects.equals(this.ItemId, other.ItemId)
      && this.DropWeight == other.DropWeight
      && this.Amount == other.Amount
      && this.AnnounceWin == other.AnnounceWin
      && java.util.Objects.equals(this.RewardSound, other.RewardSound);
  }

  @Nonnull
  public static final BuilderCodec<MysteryLootTableItem> CODEC = BuilderCodec
    .builder(MysteryLootTableItem.class, MysteryLootTableItem::new)
    .append(
      new KeyedCodec<>("ItemId", Codec.STRING),
      (config, value) -> config.ItemId = value,
      config -> config.ItemId
    )
    .add()
    .append(
      new KeyedCodec<>("DropWeight", Codec.DOUBLE),
      (config, value) -> config.DropWeight = value,
      config -> config.DropWeight
    )
    .add()
    .append(
      new KeyedCodec<>("Amount", Codec.INTEGER),
      (config, value) -> config.Amount = value,
      config -> config.Amount
    )
    .add()
    .append(
      new KeyedCodec<>("AnnounceWin", Codec.BOOLEAN),
      (config, value) -> config.AnnounceWin = value != null && value,
      config -> config.AnnounceWin
    )
    .add()
    .append(
      new KeyedCodec<>("RewardSound", Codec.STRING),
      (config, value) -> config.RewardSound = value != null ? value : "",
      config -> config.RewardSound
    )
    .add()
    .build();
}
