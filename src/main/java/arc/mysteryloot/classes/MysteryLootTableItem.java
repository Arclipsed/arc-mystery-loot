package arc.mysteryloot.classes;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class MysteryLootTableItem {
  @Nonnull
  public String ItemId = "";
  public double DropWeight = 1.0;
  public int Amount = 1;
  /** If true, broadcasts a server-wide message when this item is won. */
  public boolean AnnounceWin = false;
  /** If true, plays the special reward sound (SFX_Mystery_Loot_Reward_2) when this item is won. */
  public boolean PlaySound = false;

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
      this.PlaySound = other.PlaySound;
    }
  }

  public boolean Matches(MysteryLootTableItem other) {
    if (other == null) return false;
    return java.util.Objects.equals(this.ItemId, other.ItemId)
      && this.DropWeight == other.DropWeight
      && this.Amount == other.Amount
      && this.AnnounceWin == other.AnnounceWin
      && this.PlaySound == other.PlaySound;
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
      new KeyedCodec<>("PlaySound", Codec.BOOLEAN),
      (config, value) -> config.PlaySound = value != null && value,
      config -> config.PlaySound
    )
    .add()
    .build();
}
