package github.mczme.ruralroutes.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import github.mczme.ruralroutes.block.DisplayCaseBlock;
import github.mczme.ruralroutes.blockentity.DisplayCaseBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 展示柜方块实体渲染器
 * 将展示物品摆放在展示柜斜面上。
 */
public class DisplayCaseBlockEntityRenderer implements BlockEntityRenderer<DisplayCaseBlockEntity> {

    private static final float SURFACE_CENTER_Y = 13.18F / 16.0F;
    private static final float SURFACE_TILT = 45.0F;
    private static final float ITEM_Z_OFFSET_3D = -0.1F;
    private static final float ITEM_SCALE_3D = 0.52F;
    private static final float ITEM_Z_OFFSET_FLAT = -0.09F;
    private static final float ITEM_SCALE_FLAT = 0.38F;

    public DisplayCaseBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DisplayCaseBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack displayItem = blockEntity.getDisplayItem();
        if (displayItem.isEmpty()) {
            return;
        }

        Level level = blockEntity.getLevel();
        if (level == null) {
            level = Minecraft.getInstance().level;
        }
        if (level == null) {
            return;
        }

        Direction facing = blockEntity.getBlockState().getValue(DisplayCaseBlock.FACING);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel bakedModel = itemRenderer.getModel(displayItem, level, null, 0);
        boolean isGui3d = bakedModel.isGui3d();

        poseStack.pushPose();
        poseStack.translate(0.5D, SURFACE_CENTER_Y, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(getBlockRotation(facing)));
        poseStack.translate(0.0D, 0.0D, isGui3d ? ITEM_Z_OFFSET_3D : ITEM_Z_OFFSET_FLAT);
        poseStack.mulPose(Axis.XP.rotationDegrees(SURFACE_TILT));
        float scale = isGui3d ? ITEM_SCALE_3D : ITEM_SCALE_FLAT;
        poseStack.scale(scale, scale, scale);

        itemRenderer.renderStatic(
            displayItem,
            ItemDisplayContext.FIXED,
            packedLight,
            packedOverlay,
            poseStack,
            bufferSource,
            level,
            0
        );

        poseStack.popPose();
    }

    private float getBlockRotation(Direction facing) {
        return switch (facing) {
            case EAST -> 270.0F;
            case SOUTH -> 180.0F;
            case WEST -> 90.0F;
            default -> 0.0F;
        };
    }
}
