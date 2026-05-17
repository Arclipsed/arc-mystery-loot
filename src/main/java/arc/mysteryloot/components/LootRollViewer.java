package arc.mysteryloot.components;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Attached to a player while the loot roll animation is playing.
 * Stores accumulated time and the real result to reveal when done.
 */
public class LootRollViewer implements Component<EntityStore> {

  private static ComponentType<EntityStore, LootRollViewer> TYPE;

  public float AccumulatedTime = 0f;
  public float TotalTime = 2.0f;

  /** The real rolled item — shown at the end of the animation. */
  public String ItemId = "";
  public int Amount = 1;

  /** The table this roll came from — used at animation end to look up item properties. */
  public String TableId = "";

  public LootRollViewer() {}

  // ── Component registry ────────────────────────────────────────────────────

  public static void SetComponentType(ComponentType<EntityStore, LootRollViewer> type) {
    TYPE = type;
  }

  public static ComponentType<EntityStore, LootRollViewer> GetComponentType() {
    return TYPE;
  }

  @Override
  public LootRollViewer clone() {
    LootRollViewer copy = new LootRollViewer();
    copy.AccumulatedTime = this.AccumulatedTime;
    copy.TotalTime = this.TotalTime;
    copy.ItemId = this.ItemId;
    copy.Amount = this.Amount;
    copy.TableId = this.TableId;
    return copy;
  }

  @Nonnull
  public static final BuilderCodec<LootRollViewer> CODEC = BuilderCodec
    .builder(LootRollViewer.class, LootRollViewer::new)
    .append(new KeyedCodec<>("AccumulatedTime", Codec.FLOAT), (c, v) -> c.AccumulatedTime = v, c -> c.AccumulatedTime).add()
    .append(new KeyedCodec<>("TotalTime", Codec.FLOAT), (c, v) -> c.TotalTime = v, c -> c.TotalTime).add()
    .append(new KeyedCodec<>("ItemId", Codec.STRING), (c, v) -> c.ItemId = v, c -> c.ItemId).add()
    .append(new KeyedCodec<>("Amount", Codec.INTEGER), (c, v) -> c.Amount = v, c -> c.Amount).add()
    .append(new KeyedCodec<>("TableId", Codec.STRING), (c, v) -> c.TableId = v != null ? v : "", c -> c.TableId).add()
    .build();
}
