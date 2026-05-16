package arc.mysteryloot.classes;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class MysteryLootTableItem {
  @Nonnull
  public String ItemId = "";
  public double DropChance = 1.0;
  public int Amount = 1;

  public MysteryLootTableItem() {}
  
  public MysteryLootTableItem(MysteryLootTableItem other) {
    if (other != null) {
      this.ItemId = other.ItemId;
      this.DropChance = other.DropChance;
      this.Amount = other.Amount;
    }
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
      new KeyedCodec<>("DropChance", Codec.DOUBLE),
      (config, value) -> config.DropChance = value,
      config -> config.DropChance
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
