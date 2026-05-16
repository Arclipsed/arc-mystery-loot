package arc.mysteryloot.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

public class MysteryLootTable {
  @Nonnull
  public List<MysteryLootTableItem> Items = new ArrayList<>();

  public MysteryLootTable() {}

  public MysteryLootTable(MysteryLootTable other) {
    if (other != null && other.Items != null) {
      for (MysteryLootTableItem item : other.Items) {
        this.Items.add(new MysteryLootTableItem(item));
      }
    }
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
    .build();
}
