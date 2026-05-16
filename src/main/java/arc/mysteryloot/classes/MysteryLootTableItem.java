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

  public MysteryLootTableItem() {}
  
  public MysteryLootTableItem(String itemId, double dropWeight, int amount) {
    this.ItemId = itemId;
    this.DropWeight = dropWeight;
    this.Amount = amount;
  }
  
  public MysteryLootTableItem(MysteryLootTableItem other) {
    if (other != null) {
      this.ItemId = other.ItemId;
      this.DropWeight = other.DropWeight;
      this.Amount = other.Amount;
    }
  }

  public boolean Matches(MysteryLootTableItem other) {
    if (other == null) return false;
    return java.util.Objects.equals(this.ItemId, other.ItemId)
      && this.DropWeight == other.DropWeight
      && this.Amount == other.Amount;
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
    .build();
}
