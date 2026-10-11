package com.example.dimtowers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class TowerStructure extends Structure {
    public static final MapCodec<TowerStructure> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            settingsCodec(i),
            Codec.STRING.fieldOf("theme").forGetter(s -> s.themeId)
    ).apply(i, TowerStructure::new));

    final String themeId;

    public TowerStructure(StructureSettings settings, String themeId) {
        super(settings);
        this.themeId = themeId;
    }

    @Override
    public StructureType<?> type() { return DimensionalTowers.TOWER_STRUCTURE.get(); }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        int cx = ctx.chunkPos().getMinBlockX() + 8;
        int cz = ctx.chunkPos().getMinBlockZ() + 8;
        int s0 = findGround(ctx, Theme.byName(themeId), cx, cz);
        if (s0 == Integer.MIN_VALUE) return Optional.empty();
        final String id = themeId;
        return Optional.of(new GenerationStub(new BlockPos(cx, s0, cz),
                builder -> builder.addPiece(new TowerPiece(id, cx, cz, s0))));
    }

    private static int findGround(GenerationContext ctx, Theme theme, int cx, int cz) {
        ChunkGenerator gen = ctx.chunkGenerator();
        LevelHeightAccessor ha = ctx.heightAccessor();
        RandomState rs = ctx.randomState();

        if (theme.nether) {
            NoiseColumn col = gen.getBaseColumn(cx, cz, ha, rs);
            // tower is ~70 blocks tall, so start low enough to stay under the bedrock roof
            for (int y = 60; y >= 34; y--) {
                BlockState st = col.getBlock(y);
                if (st.isAir() || !st.getFluidState().isEmpty()) continue;
                boolean clear = true;
                for (int dy = 1; dy <= 12 && clear; dy++) clear = col.getBlock(y + dy).isAir();
                if (clear) return y;
            }
            return Integer.MIN_VALUE;
        }
        boolean end = theme == Theme.END;
        int[] off = {0, 0, 10, 10, -10, 10, 10, -10, -10, -10};
        int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
        for (int i = 0; i < off.length; i += 2) {
            int x = cx + off[i], z = cz + off[i + 1];
            int h = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, ha, rs);
            if (end) {
                if (h <= 10) return Integer.MIN_VALUE;
            } else if (!gen.getBaseColumn(x, z, ha, rs).getBlock(h - 1).getFluidState().isEmpty()) {
                return Integer.MIN_VALUE;
            }
            max = Math.max(max, h);
            min = Math.min(min, h);
        }
        if (max - min > (end ? 6 : 10) || max > 230) return Integer.MIN_VALUE;
        return max - 1;
    }
}
