package arc.mysteryloot.classes;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/**
 * Defines an item (and amount) required to open a Mystery Loot table.
 * If ItemId is empty, no item is required.
 */
public class MysteryLootKey {

  /** The item ID the player must have. Empty = no requirement. */
  @Nonnull
  public String ItemId = "";

  /** How many of the item are required (and consumed on roll). */
  public int Amount = 1;

  public MysteryLootKey() {}

  public MysteryLootKey(MysteryLootKey other) {
    if (other != null) {
      this.ItemId = other.ItemId != null ? other.ItemId : "";
      this.Amount = other.Amount;
    }
  }

  /** Returns true if this key actually requires an item. */
  public boolean IsRequired() {
    return ItemId != null && !ItemId.isEmpty();
  }

  @Nonnull
  public static final BuilderCodec<MysteryLootKey> CODEC = BuilderCodec
    .builder(MysteryLootKey.class, MysteryLootKey::new)
    .append(
      new KeyedCodec<>("ItemId", Codec.STRING),
      (k, v) -> k.ItemId = v != null ? v : "",
      k -> k.ItemId
    ).add()
    .append(
      new KeyedCodec<>("Amount", Codec.INTEGER),
      (k, v) -> k.Amount = v,
      k -> k.Amount
    ).add()
    .build();
}
