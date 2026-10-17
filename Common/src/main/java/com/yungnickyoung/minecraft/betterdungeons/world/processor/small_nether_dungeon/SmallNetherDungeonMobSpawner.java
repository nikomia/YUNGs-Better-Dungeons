package com.yungnickyoung.minecraft.betterdungeons.world.processor.small_nether_dungeon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yungnickyoung.minecraft.betterdungeons.BetterDungeonsCommon;
import com.yungnickyoung.minecraft.betterdungeons.module.StructureProcessorTypeModule;
import com.yungnickyoung.minecraft.yungsapi.world.spawner.MobSpawnerData;

import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.DropChances;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;



public class SmallNetherDungeonMobSpawner implements StructureProcessor {
    public static final MapCodec<SmallNetherDungeonMobSpawner> CODEC = RecordCodecBuilder.mapCodec(codecBuilder -> codecBuilder
            .group(
                    Identifier.CODEC
                            .fieldOf("spawner_mob")
                            .forGetter(SmallNetherDungeonMobSpawner::getSpawnerMob))
            .apply(codecBuilder, codecBuilder.stable(SmallNetherDungeonMobSpawner::new)));

    private SmallNetherDungeonMobSpawner(Identifier spawnerMob) {
        this.spawnerMob = spawnerMob;
    }

    private final Identifier spawnerMob;

    public Identifier getSpawnerMob() {
        return this.spawnerMob;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader,
                                                             BlockPos jigsawPiecePos,
                                                             BlockPos jigsawPieceBottomCenterPos,
                                                             BlockPos templateRelativePos,
                                                             StructureTemplate.StructureBlockInfo blockInfoGlobal,
                                                             StructurePlaceSettings structurePlacementData) {
        if (blockInfoGlobal.state().getBlock() instanceof SpawnerBlock) {
            // Create spawner & populate with data
            MobSpawnerData spawner = MobSpawnerData.builder()
                    .spawnPotentials(WeightedList.of(new SpawnData(
                            Util.make(new CompoundTag(), (compoundTag) -> {
                                compoundTag.putString("id", spawnerMob.toString());
                                if (spawnerMob.toString().equals("minecraft:wither_skeleton")) {
                                    addWitherSkeletonEquipment(compoundTag, levelReader);
                                    if (!BetterDungeonsCommon.CONFIG.smallNetherDungeons.witherSkeletonsDropWitherSkulls) {
                                        compoundTag.putString("DeathLootTable", "minecraft:empty");
                                    }
                                } else if (spawnerMob.toString().equals("minecraft:blaze")) {
                                    if (!BetterDungeonsCommon.CONFIG.smallNetherDungeons.blazesDropBlazeRods) {
                                        compoundTag.putString("DeathLootTable", "minecraft:empty");
                                    }
                                }
                            }),
                            Optional.empty(),
                            Optional.empty())))
                    .setEntityType(BuiltInRegistries.ENTITY_TYPE
                            .get(spawnerMob)
                            .orElseGet(() -> {
                                BetterDungeonsCommon.LOGGER.error("Unable to find entity type for spawner: {}. Defaulting to zombie...", spawnerMob);
                                return BuiltInRegistries.ENTITY_TYPE.get(Identifier.withDefaultNamespace("zombie")).get();
                            })
                            .value())
                    .build();
            if (spawnerMob.toString().equals("minecraft:wither_skeleton")) {
                addWitherSkeletonEquipment(spawner.nextSpawnData.getEntityToSpawn(), levelReader);
                if (!BetterDungeonsCommon.CONFIG.smallNetherDungeons.witherSkeletonsDropWitherSkulls) {
                    spawner.nextSpawnData.getEntityToSpawn().putString("DeathLootTable", "minecraft:empty");
                }
            } else if (spawnerMob.toString().equals("minecraft:blaze")) {
                if (!BetterDungeonsCommon.CONFIG.smallNetherDungeons.blazesDropBlazeRods) {
                    spawner.nextSpawnData.getEntityToSpawn().putString("DeathLootTable", "minecraft:empty");
                }
            }
            CompoundTag nbt = spawner.save();
            blockInfoGlobal = new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.SPAWNER.defaultBlockState(), nbt);
        }
        return blockInfoGlobal;
    }

    private static void addWitherSkeletonEquipment(CompoundTag entityTag, LevelReader levelReader) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, levelReader.registryAccess());
        EntityEquipment equipment = new EntityEquipment();
        equipment.set(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
        equipment.set(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
        equipment.set(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
        equipment.set(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
        equipment.set(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
        entityTag.put("equipment", EntityEquipment.CODEC.encodeStart(ops, equipment).getOrThrow());

        DropChances dropChances = DropChances.DEFAULT
                .withEquipmentChance(EquipmentSlot.FEET, 0)
                .withEquipmentChance(EquipmentSlot.LEGS, 0)
                .withEquipmentChance(EquipmentSlot.CHEST, 0)
                .withEquipmentChance(EquipmentSlot.HEAD, 0);
        entityTag.put("drop_chances", DropChances.CODEC.encodeStart(ops, dropChances).getOrThrow());
    }

    @Override
    public MapCodec<? extends StructureProcessor> codec() {
        return StructureProcessorTypeModule.SMALL_NETHER_DUNGEON_MOB_SPAWNER_PROCESSOR;
    }
}
