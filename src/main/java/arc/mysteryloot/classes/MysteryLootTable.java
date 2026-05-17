package arc.mysteryloot.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

public class MysteryLootTable {
  @Nonnull
  public MysteryLootKey RequiredKey = new MysteryLootKey();

  @Nonnull
  public List<MysteryLootTableItem> Items = new ArrayList<>();

  

  public MysteryLootTable() {}

  public MysteryLootTable(MysteryLootTable other) {
    if (other != null && other.Items != null) {
      for (MysteryLootTableItem item : other.Items) {
        this.Items.add(new MysteryLootTableItem(item));
      }
    }
    if (other != null) {
      this.RequiredKey = new MysteryLootKey(other.RequiredKey);
    }
  }

  public boolean Matches(MysteryLootTable other) {
    if (other == null) return false;
    if (this.Items.size() != other.Items.size()) return false;
    for (int i = 0; i < this.Items.size(); i++) {
      if (!this.Items.get(i).Matches(other.Items.get(i))) return false;
    }
    return true;
  }

  @Nonnull
  public static final BuilderCodec<MysteryLootTable> CODEC = BuilderCodec
    .builder(MysteryLootTable.class, MysteryLootTable::new)
    .append(
      new KeyedCodec<>("Items", new ArrayCodec<>(MysteryLootTableItem.CODEC, MysteryLootTableItem[]::new)),
      (config, value) -> config.Items = value != null ? new ArrayList<>(Arrays.asList(value)) : new ArrayList<>(),
      config -> config.Items.toArray(new MysteryLootTableItem[0])
    )
    .add()
    .append(
      new KeyedCodec<>("RequiredKey", MysteryLootKey.CODEC),
      (config, value) -> config.RequiredKey = value != null ? value : new MysteryLootKey(),
      config -> config.RequiredKey
    )
    .add()
    .build();
}
