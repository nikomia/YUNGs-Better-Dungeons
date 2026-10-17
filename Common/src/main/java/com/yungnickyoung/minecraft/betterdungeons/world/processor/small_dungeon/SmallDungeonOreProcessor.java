package com.yungnickyoung.minecraft.betterdungeons.world.processor.small_dungeon;

import net.minecraft.tags.BlockItemTags;
import com.mojang.serialization.MapCodec;
import com.yungnickyoung.minecraft.betterdungeons.BetterDungeonsCommon;
import com.yungnickyoung.minecraft.betterdungeons.module.StructureProcessorTypeModule;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.function.Predicate;

/**
 * Replaces ore in props with cobblestone if ores are disabled in the config.
 */


public class SmallDungeonOreProcessor implements StructureProcessor {
    public static final SmallDungeonOreProcessor INSTANCE = new SmallDungeonOreProcessor();
    public static final MapCodec<SmallDungeonOreProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    private static final Predicate<BlockState> isOre = blockState ->
            blockState.is(BlockTags.GOLD_ORES) ||
            blockState.is(BlockTags.IRON_ORES) ||
            blockState.is(BlockItemTags.DIAMOND_ORES.block()) ||
            blockState.is(BlockItemTags.REDSTONE_ORES.block()) ||
            blockState.is(BlockItemTags.LAPIS_ORES.block()) ||
            blockState.is(BlockItemTags.COAL_ORES.block()) ||
            blockState.is(BlockItemTags.EMERALD_ORES.block()) ||
            blockState.is(BlockTags.COPPER_ORES);

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader,
                                                             BlockPos jigsawPiecePos,
                                                             BlockPos jigsawPieceBottomCenterPos,
                                                             BlockPos templateRelativePos,
                                                             StructureTemplate.StructureBlockInfo blockInfoGlobal,
                                                             StructurePlaceSettings structurePlacementData) {
        if (isOre.test(blockInfoGlobal.state())) {
            if (!BetterDungeonsCommon.CONFIG.smallDungeons.enableOreProps) {
                blockInfoGlobal = new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), Blocks.CAVE_AIR.defaultBlockState(), null);
            }
        }
        return blockInfoGlobal;
    }

    @Override
    public MapCodec<? extends StructureProcessor> codec() {
        return StructureProcessorTypeModule.SMALL_DUNGEON_ORE_PROCESSOR;
    }
}
