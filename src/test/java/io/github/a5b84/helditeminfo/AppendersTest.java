package io.github.a5b84.helditeminfo;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig;
import io.github.a5b84.helditeminfo.mixin.BrushableBlockEntityAccessor;
import io.github.a5b84.helditeminfo.mixin.TrialSpawnerStateDataAccessor;
import io.github.a5b84.helditeminfo.util.ItemEnchantmentsBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.StringSplitter;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.network.Filterable;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypeIds;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.decoration.painting.PaintingVariants;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Instruments;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.Bees;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.InstrumentComponent;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

class AppendersTest {

  private static HolderLookup.@Nullable Provider lookupProvider;
  private static HolderLookup.@Nullable RegistryLookup<Item> itemRegistryLookup;

  @BeforeAll
  public static void setup() {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
    lookupProvider = spyLookupProvider(VanillaRegistries.createWorldLookup());
    BuiltInRegistries.DATA_COMPONENT_INITIALIZERS
        .build(lookupProvider)
        .forEach(DataComponentInitializers.PendingComponents::apply);
    itemRegistryLookup = lookupProvider.lookupOrThrow(Registries.ITEM);
  }

  @ParameterizedTest
  @MethodSource("getTestBuildTooltipArguments")
  void testBuildTooltip(
      ResourceKey<Item> item, DataComponentPatch componentPatch, List<Component> expectedTooltip) {
    assert lookupProvider != null;
    assert itemRegistryLookup != null;

    ItemStack stack = new ItemStack(itemRegistryLookup.getOrThrow(item), 1, componentPatch);
    TooltipBuilder builder =
        new TooltipBuilder(
            stack,
            new TestConfig(),
            Item.TooltipContext.of(lookupProvider),
            new StringSplitter((_, _) -> (int) Util.AVERAGE_CHARACTER_WIDTH),
            false);
    Appenders.appendAll(builder);
    List<Component> actualTooltip = builder.build();
    assertThat(actualTooltip).containsExactlyElementsOf(expectedTooltip);
  }

  private static Stream<Arguments> getTestBuildTooltipArguments() {
    assert lookupProvider != null;
    assert itemRegistryLookup != null;

    HolderLookup.RegistryLookup<PaintingVariant> paintingVariantRegistryLookup =
        lookupProvider.lookupOrThrow(Registries.PAINTING_VARIANT);
    HolderLookup.RegistryLookup<Instrument> instrumentRegistryLookup =
        lookupProvider.lookupOrThrow(Registries.INSTRUMENT);

    ItemStackTemplate intangibleSpectralArrow =
        new ItemStackTemplate(
            Items.SPECTRAL_ARROW,
            DataComponentPatch.builder()
                .set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE)
                .build());

    return Stream.of(
        testBuildTooltipArguments(
            ItemIds.DISC_FRAGMENT_5,
            DataComponentPatch.EMPTY,
            List.of(
                Component.translatable("item.minecraft.disc_fragment_5.desc")
                    .withStyle(TooltipBuilder.DEFAULT_COLOR))),
        testBuildTooltipArguments(
            ItemIds.PAINTING,
            DataComponentPatch.builder()
                .set(
                    DataComponents.PAINTING_VARIANT,
                    paintingVariantRegistryLookup.getOrThrow(PaintingVariants.WANDERER))
                .build(),
            List.of(
                Component.translatable("painting.minecraft.wanderer.title")
                    .withStyle(TooltipBuilder.DEFAULT_COLOR),
                Component.translatable("painting.minecraft.wanderer.author")
                    .withStyle(TooltipBuilder.DEFAULT_COLOR),
                Component.translatable("painting.dimensions", 1, 2)
                    .withStyle(TooltipBuilder.DEFAULT_COLOR))),
        testBuildTooltipArguments(
            BlockItemIds.SUSPICIOUS_SAND.item(),
            DataComponentPatch.builder()
                .set(
                    DataComponents.BLOCK_ENTITY_DATA,
                    TypedEntityData.of(
                        BlockEntityTypes.BRUSHABLE_BLOCK,
                        buildCompoundTag(output ->
                            output.store(
                                BrushableBlockEntityAccessor.getItemTagKey(),
                                ItemStack.CODEC,
                                new ItemStack(Items.GOLD_BLOCK, 2)))))
                .build(),
            List.of(itemCountComponent(2, "block.minecraft.gold_block"))),
        testBuildTooltipArguments(
            BlockItemIds.COMMAND_BLOCK.item(),
            DataComponentPatch.builder()
                .set(
                    DataComponents.BLOCK_ENTITY_DATA,
                    TypedEntityData.of(
                        BlockEntityTypes.COMMAND_BLOCK,
                        buildCompoundTag(output ->
                            output.putString(
                                "Command",
                                "say Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."))))
                .build(),
            List.of(
                Component.literal("say Lorem ipsum dolor sit amet, consectetur")
                    .withStyle(TooltipBuilder.DEFAULT_COLOR),
                Component.literal("adipiscing elit, sed do eiusmod tempor")
                    .append("...")
                    .withStyle(TooltipBuilder.DEFAULT_COLOR))),
        testBuildTooltipArguments(
            BlockItemIds.SPAWNER.item(),
            DataComponentPatch.builder()
                .set(
                    DataComponents.BLOCK_ENTITY_DATA,
                    TypedEntityData.of(
                        BlockEntityTypes.MOB_SPAWNER,
                        buildCompoundTag(output -> {
                          CompoundTag entityTag = new CompoundTag();
                          entityTag.putString(
                              Entity.TAG_ID, EntityTypeIds.PIG.identifier().toString());
                          output.store(
                              BaseSpawner.SPAWN_DATA_TAG,
                              SpawnData.CODEC,
                              new SpawnData(entityTag, Optional.empty(), Optional.empty()));
                        })))
                .build(),
            List.of(
                Component.translatable("entity.minecraft.pig")
                    .withStyle(TooltipBuilder.DEFAULT_COLOR))),
        testBuildTooltipArguments(
            BlockItemIds.TRIAL_SPAWNER.item(),
            DataComponentPatch.builder()
                .set(
                    DataComponents.BLOCK_ENTITY_DATA,
                    TypedEntityData.of(
                        BlockEntityTypes.TRIAL_SPAWNER,
                        buildCompoundTag(output -> {
                          CompoundTag entityTag = new CompoundTag();
                          entityTag.putString(
                              Entity.TAG_ID, EntityTypeIds.PIG.identifier().toString());
                          output.store(
                              TrialSpawnerStateDataAccessor.getSpawnDataKey(),
                              SpawnData.CODEC,
                              new SpawnData(entityTag, Optional.empty(), Optional.empty()));
                        })))
                .build(),
            List.of(
                Component.translatable("entity.minecraft.pig")
                    .withStyle(TooltipBuilder.DEFAULT_COLOR))),
        testBuildTooltipArguments(
            ItemIds.TROPICAL_FISH_BUCKET,
            DataComponentPatch.builder()
                .set(DataComponents.TROPICAL_FISH_BASE_COLOR, DyeColor.RED)
                .set(DataComponents.TROPICAL_FISH_PATTERN, TropicalFish.Pattern.FLOPPER)
                .set(DataComponents.TROPICAL_FISH_PATTERN_COLOR, DyeColor.BLUE)
                .build(),
            List.of(
                Component.translatable("entity.minecraft.tropical_fish.type.flopper")
                    .withStyle(
                        Style.EMPTY.withColor(TooltipBuilder.DEFAULT_COLOR).withItalic(true)),
                Component.translatable("color.minecraft.red")
                    .append(", ")
                    .append(Component.translatable("color.minecraft.blue"))
                    .withStyle(
                        Style.EMPTY.withColor(TooltipBuilder.DEFAULT_COLOR).withItalic(true)))),
        testBuildTooltipArguments(
            ItemIds.GOAT_HORN,
            DataComponentPatch.builder()
                .set(
                    DataComponents.INSTRUMENT,
                    new InstrumentComponent(
                        instrumentRegistryLookup.getOrThrow(Instruments.PONDER_GOAT_HORN)))
                .build(),
            List.of(
                Component.translatable("instrument.minecraft.ponder_goat_horn")
                    .withStyle(TooltipBuilder.DEFAULT_COLOR))),
        testBuildTooltipArguments(
            ItemIds.FILLED_MAP,
            DataComponentPatch.builder().set(DataComponents.MAP_ID, new MapId(123)).build(),
            List.of(
                Component.translatable("filled_map.unknown")
                    .withStyle(TooltipBuilder.DEFAULT_COLOR))),
        testBuildTooltipArguments(
            BlockItemIds.BEE_NEST.item(),
            DataComponentPatch.builder()
                .set(
                    DataComponents.BEES,
                    new Bees(
                        List.of(
                            new BeehiveBlockEntity.Occupant(
                                TypedEntityData.of(EntityTypes.BEE, new CompoundTag()), 0, 200))))
                .set(
                    DataComponents.BLOCK_STATE,
                    new BlockItemStateProperties(Map.of(BeehiveBlock.HONEY_LEVEL.getName(), "3")))
                .build(),
            List.of(
                Component.translatable("container.beehive.bees", 1, 3)
                    .withStyle(TooltipBuilder.DEFAULT_COLOR),
                Component.translatable("container.beehive.honey", 3, 5)
                    .withStyle(TooltipBuilder.DEFAULT_COLOR))),
        testBuildTooltipArguments(
            BlockItemIds.SHULKER_BOX.item(),
            DataComponentPatch.builder()
                .set(
                    DataComponents.CONTAINER,
                    ItemContainerContents.fromItems(
                        List.of(
                            new ItemStack(Items.REDSTONE_ORE, 32),
                            new ItemStack(Items.STONE, 32),
                            new ItemStack(Items.STONE, 32),
                            new ItemStack(
                                itemRegistryLookup.getOrThrow(ItemIds.IRON_SWORD),
                                1,
                                DataComponentPatch.builder()
                                    .set(
                                        DataComponents.ENCHANTMENTS,
                                        new ItemEnchantmentsBuilder(lookupProvider)
                                            .set(Enchantments.SHARPNESS, 5)
                                            .build())
                                    .build()),
                            new ItemStack(Items.STONE, 32),
                            new ItemStack(Items.STONE, 32))))
                .build(),
            List.of(
                itemCountComponent(32, "block.minecraft.redstone_ore"),
                itemCountComponent(128, "block.minecraft.stone"),
                itemCountComponent(1, "item.minecraft.iron_sword"))),
        testBuildTooltipArguments(
            BlockItemIds.CHISELED_BOOKSHELF.item(),
            DataComponentPatch.builder()
                .set(
                    DataComponents.CONTAINER,
                    ItemContainerContents.fromItems(
                        List.of(
                            new ItemStack(Items.BOOK),
                            ItemStack.EMPTY,
                            new ItemStack(Items.BOOK),
                            ItemStack.EMPTY,
                            new ItemStack(Items.BOOK))))
                .build(),
            List.of(itemCountComponent(3, "item.minecraft.book"))),
        testBuildTooltipArguments(
            ItemIds.BUNDLE,
            DataComponentPatch.builder()
                .set(
                    DataComponents.BUNDLE_CONTENTS,
                    new BundleContents(
                        List.of(
                            new ItemStackTemplate(Items.GOLD_BLOCK, 3),
                            new ItemStackTemplate(Items.IRON_BLOCK, 2))))
                .build(),
            List.of(
                itemCountComponent(3, "block.minecraft.gold_block"),
                itemCountComponent(2, "block.minecraft.iron_block"))),
        testBuildTooltipArguments(
            ItemIds.WRITTEN_BOOK,
            DataComponentPatch.builder()
                .set(
                    DataComponents.WRITTEN_BOOK_CONTENT,
                    new WrittenBookContent(
                        Filterable.passThrough("Title title"), "Me", 3, List.of(), false))
                .build(),
            List.of(
                Component.translatable("book.byAuthor", "Me")
                    .withStyle(TooltipBuilder.DEFAULT_COLOR),
                Component.translatable("book.generation.3")
                    .withStyle(TooltipBuilder.DEFAULT_COLOR))),
        testBuildTooltipArguments(
            ItemIds.CROSSBOW,
            DataComponentPatch.builder()
                .set(
                    DataComponents.CHARGED_PROJECTILES,
                    ChargedProjectiles.of(intangibleSpectralArrow))
                .build(),
            List.of(
                Component.translatable(
                        "item.minecraft.crossbow.projectile.single",
                        Component.translatable(
                                "chat.square_brackets",
                                Component.empty()
                                    .append(
                                        Component.translatable("item.minecraft.spectral_arrow")))
                            .withStyle(
                                Style.EMPTY
                                    .withColor(TextColor.WHITE)
                                    .withHoverEvent(
                                        new HoverEvent.ShowItem(intangibleSpectralArrow))))
                    .withStyle(TooltipBuilder.DEFAULT_COLOR))),
        testBuildTooltipArguments(
            ItemIds.FIREWORK_ROCKET,
            DataComponentPatch.builder()
                .set(
                    DataComponents.FIREWORKS,
                    new Fireworks(
                        2,
                        List.of(
                            new FireworkExplosion(
                                FireworkExplosion.Shape.LARGE_BALL,
                                IntList.of(0xd88198),
                                IntList.of(0xb3312c),
                                false,
                                false))))
                .build(),
            List.of(
                Component.translatable("item.minecraft.firework_rocket.flight_duration", 2)
                    .withStyle(TooltipBuilder.DEFAULT_COLOR),
                Component.translatable(
                        "item.minecraft.firework_rocket.single_star",
                        Component.translatable("item.minecraft.firework_star.shape.large_ball"))
                    .withStyle(TooltipBuilder.DEFAULT_COLOR),
                Component.literal("  ")
                    .append(
                        Component.empty()
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.translatable("item.minecraft.firework_star.pink"))),
                Component.literal("  ")
                    .append(
                        Component.translatable("item.minecraft.firework_star.fade_to")
                            .withStyle(ChatFormatting.GRAY)
                            .append(" ")
                            .append(Component.translatable("item.minecraft.firework_star.red"))))),
        testBuildTooltipArguments(
            ItemIds.POTION,
            DataComponentPatch.builder()
                .set(
                    DataComponents.POTION_CONTENTS,
                    new PotionContents(Potions.STRONG_TURTLE_MASTER))
                .build(),
            List.of(
                Component.translatable(
                        "potion.withDuration",
                        Component.translatable(
                            "potion.withAmplifier",
                            Component.translatable("effect.minecraft.slowness"),
                            Component.translatable("potion.potency.5")),
                        Component.literal("00:20"))
                    .withStyle(ChatFormatting.RED),
                Component.translatable(
                        "potion.withDuration",
                        Component.translatable(
                            "potion.withAmplifier",
                            Component.translatable("effect.minecraft.resistance"),
                            Component.translatable("potion.potency.3")),
                        Component.literal("00:20"))
                    .withStyle(ChatFormatting.BLUE))),
        testBuildTooltipArguments(
            ItemIds.MUSIC_DISC_13,
            DataComponentPatch.EMPTY,
            List.of(
                Component.translatable("jukebox_song.minecraft.13")
                    .withStyle(TooltipBuilder.DEFAULT_COLOR))),
        testBuildTooltipArguments(
            ItemIds.DIAMOND_SWORD,
            DataComponentPatch.builder()
                .set(
                    DataComponents.ENCHANTMENTS,
                    new ItemEnchantmentsBuilder(lookupProvider)
                        .set(Enchantments.SHARPNESS, 5)
                        .build())
                .build(),
            List.of(
                Component.translatable("enchantment.minecraft.sharpness")
                    .append(" ")
                    .append(Component.translatable("enchantment.level.5"))
                    .withStyle(TooltipBuilder.DEFAULT_COLOR))),
        testBuildTooltipArguments(
            ItemIds.BOOK,
            DataComponentPatch.builder()
                .set(
                    DataComponents.LORE,
                    new ItemLore(
                        List.of(
                            Component.literal("Lorem ipsum dolor sit amet, ")
                                .append(
                                    Component.literal("consectetur adipiscing elit.")
                                        .withStyle(ChatFormatting.BLUE)),
                            Component.literal(
                                    "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
                                .withStyle(Style.EMPTY.withItalic(true)))))
                .build(),
            List.of(
                Component.literal("Lorem ipsum dolor sit amet, consectetur")
                    .withStyle(
                        Style.EMPTY.withColor(TooltipBuilder.DEFAULT_COLOR).withItalic(true)),
                Component.literal("adipiscing elit.")
                    .withStyle(
                        Style.EMPTY.withColor(TooltipBuilder.DEFAULT_COLOR).withItalic(true)),
                Component.literal("Sed do eiusmod tempor incididunt ut labore et")
                    .append("...")
                    .withStyle(
                        Style.EMPTY.withColor(TooltipBuilder.DEFAULT_COLOR).withItalic(true)))),
        testBuildTooltipArguments(
            ItemIds.DIAMOND_SWORD,
            DataComponentPatch.builder().set(DataComponents.UNBREAKABLE, Unit.INSTANCE).build(),
            List.of(
                Component.translatable("item.unbreakable")
                    .withStyle(TooltipBuilder.DEFAULT_COLOR))),
        testBuildTooltipArguments(
            BlockItemIds.OAK_SIGN.item(),
            DataComponentPatch.builder()
                .set(
                    DataComponents.SIGN_TEXT_FRONT,
                    createSignText(List.of(Component.literal("Front")), DyeColor.BLACK, false))
                .build(),
            List.of(Component.literal("Front").withStyle(TooltipBuilder.DEFAULT_COLOR))),
        testBuildTooltipArguments(
            BlockItemIds.OAK_SIGN.item(),
            DataComponentPatch.builder()
                .set(
                    DataComponents.SIGN_TEXT_BACK,
                    createSignText(List.of(Component.literal("Back")), DyeColor.BLACK, false))
                .build(),
            List.of(Component.literal("Back").withStyle(TooltipBuilder.DEFAULT_COLOR))),
        testBuildTooltipArguments(
            BlockItemIds.OAK_SIGN.item(),
            DataComponentPatch.builder()
                .set(
                    DataComponents.SIGN_TEXT_FRONT,
                    createSignText(
                        List.of(
                            Component.literal("Front line 1"),
                            Component.empty(),
                            Component.literal("Front line ")
                                .append(
                                    Component.literal("3")
                                        .withStyle(
                                            Style.EMPTY
                                                .withColor(TextColor.LIGHT_PURPLE)
                                                .withBold(true))),
                            Component.empty()),
                        DyeColor.BLACK,
                        false))
                .set(
                    DataComponents.SIGN_TEXT_BACK,
                    createSignText(
                        List.of(
                            Component.empty(),
                            Component.literal("Back line 2"),
                            Component.empty(),
                            Component.literal("Back line 4").withStyle(ChatFormatting.GOLD)),
                        DyeColor.YELLOW,
                        true))
                .build(),
            List.of(
                Component.literal("Front line 1").withStyle(TooltipBuilder.DEFAULT_COLOR),
                Component.literal("Front line 3").withStyle(TooltipBuilder.DEFAULT_COLOR),
                Component.translatable("held_item_info.tooltip.sign_side_separator")
                    .withStyle(TooltipBuilder.DEFAULT_COLOR),
                Component.literal("Back line 2").withStyle(TooltipBuilder.DEFAULT_COLOR),
                Component.literal("Back line 4").withStyle(TooltipBuilder.DEFAULT_COLOR))),
        testBuildTooltipArguments(
            ItemIds.OMINOUS_BOTTLE,
            DataComponentPatch.EMPTY,
            List.of(
                Component.translatable(
                        "potion.withDuration",
                        Component.translatable("effect.minecraft.bad_omen"),
                        Component.literal("01:40:00"))
                    .withStyle(ChatFormatting.BLUE))));
  }

  private static Arguments testBuildTooltipArguments(
      ResourceKey<Item> item, DataComponentPatch componentPatch, List<Component> expectedTooltip) {
    return Arguments.of(
        Named.of(item.identifier().toShortString(), item),
        componentPatch,
        Named.of(expectedTooltip.size() + " lines", expectedTooltip));
  }

  private static MutableComponent itemCountComponent(int count, String itemKey) {
    return Component.translatable(
            "item.container.item_count", count, Component.translatable(itemKey))
        .withStyle(TooltipBuilder.DEFAULT_COLOR);
  }

  private static CompoundTag buildCompoundTag(Consumer<TagValueOutput> outputConsumer) {
    assert lookupProvider != null;
    try (ProblemReporter.ScopedCollector reporter =
        new ProblemReporter.ScopedCollector(HeldItemInfo.LOGGER)) {
      TagValueOutput output = TagValueOutput.createWithContext(reporter, lookupProvider);
      outputConsumer.accept(output);
      return output.buildResult();
    }
  }

  private static SignText createSignText(
      List<Component> messages, DyeColor color, boolean hasGlowingText) {
    return new SignText(messages, messages, color, hasGlowingText);
  }

  private static HolderLookup.Provider spyLookupProvider(HolderLookup.Provider provider) {
    HolderLookup.Provider spiedProvider = Mockito.spy(provider);

    HolderLookup.RegistryLookup<Enchantment> enchantmentRegistryLookup =
        spyEnchantmentRegistryLookup(spiedProvider.lookupOrThrow(Registries.ENCHANTMENT));
    Mockito.doReturn(Optional.of(enchantmentRegistryLookup))
        .when(spiedProvider)
        .lookup(Registries.ENCHANTMENT);

    return spiedProvider;
  }

  @SuppressWarnings("unchecked")
  private static HolderLookup.RegistryLookup<Enchantment> spyEnchantmentRegistryLookup(
      HolderLookup.RegistryLookup<Enchantment> registryLookup) {
    HolderLookup.RegistryLookup<Enchantment> spiedLookup = Mockito.spy(registryLookup);

    Mockito.doAnswer(invocation ->
            ((Optional<Holder.Reference<Enchantment>>) invocation.callRealMethod())
                .map(enchantment -> {
                  Holder.Reference<Enchantment> spiedEnchantment = Mockito.spy(enchantment);
                  Mockito.doReturn(false).when(spiedEnchantment).is(EnchantmentTags.CURSE);
                  return spiedEnchantment;
                }))
        .when(spiedLookup)
        .get(Mockito.<ResourceKey<Enchantment>>any());

    Mockito.when(spiedLookup.get(EnchantmentTags.TOOLTIP_ORDER)).thenReturn(Optional.empty());

    return spiedLookup;
  }

  private static class TestConfig extends HeldItemInfoConfig {
    @Override
    public boolean showName() {
      return false;
    }

    @Override
    public boolean showFilledMapId() {
      return true;
    }
  }
}
