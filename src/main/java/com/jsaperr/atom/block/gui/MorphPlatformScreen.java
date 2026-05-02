package com.jsaperr.atom.block.gui;

import com.jsaperr.atom.block.MorphRequestPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class MorphPlatformScreen extends AbstractContainerScreen<MorphPlatformMenu> {

    private static final int GUI_WIDTH  = 176;
    private static final int GUI_HEIGHT = 176;
    private static final int SLOT_SIZE  = 24;
    private static final int SLOT_GAP   = 6;
    private static final int SLOTS_PER_ROW = 5;
    private static final int GRID_OFFSET_X = 10;
    private static final int GRID_OFFSET_Y = 22;

    private static final int BTN_W = 60;
    private static final int BTN_H = 16;
    private static final int BTN_X = GUI_WIDTH - BTN_W - 8;
    private static final int BTN_Y = GUI_HEIGHT - BTN_H - 8;

    private static final ResourceLocation DEMORPH_SENTINEL =
            ResourceLocation.fromNamespaceAndPath("atom", "demorph");

    private static final int COLOR_SLOT_BORDER = 0xFF4A5A6A;
    private static final int COLOR_SLOT_BG     = 0xFF1E2A35;

    private List<ResourceLocation> availableMobs = new ArrayList<>();

    public MorphPlatformScreen(MorphPlatformMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth  = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    public void setAvailableMobs(List<ResourceLocation> mobs) {
        this.availableMobs = new ArrayList<>(mobs);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = GUI_WIDTH / 2;
        this.titleLabelY = -10;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        graphics.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, 0xFF2D2D2D);
        graphics.fill(x + 1, y + 1, x + GUI_WIDTH - 1, y + GUI_HEIGHT - 1, 0xFF3D3D3D);

        // title
        Component title = Component.translatable("block.atom.morph_platform");
        graphics.drawCenteredString(font, title, x + GUI_WIDTH / 2, y + 4, 0xFFFFFF);

        graphics.fill(x, y + 12, x + GUI_WIDTH, y + 13, 0xFF888888);

        // separator above button row
        graphics.fill(x, y + BTN_Y - 6, x + GUI_WIDTH, y + BTN_Y - 5, 0xFF888888);

        // demorph button
        int bx = x + BTN_X;
        int by = y + BTN_Y;
        boolean hovered = mouseX >= bx && mouseX < bx + BTN_W && mouseY >= by && mouseY < by + BTN_H;
        graphics.fill(bx - 1, by - 1, bx + BTN_W + 1, by + BTN_H + 1, COLOR_SLOT_BORDER);
        graphics.fill(bx, by, bx + BTN_W, by + BTN_H, hovered ? 0xFF3A4A5A : COLOR_SLOT_BG);
        graphics.drawCenteredString(font, "Demorph", bx + BTN_W / 2, by + 4, 0xFFFFFF);

        if (availableMobs.isEmpty()) {
            Component hint = Component.literal("Complete a category at the Research Station");
            graphics.drawCenteredString(font, hint, x + GUI_WIDTH / 2, y + GUI_HEIGHT / 2 - 4, 0x888888);
            return;
        }

        for (int i = 0; i < availableMobs.size(); i++) {
            ResourceLocation mobId = availableMobs.get(i);
            int col = i % SLOTS_PER_ROW;
            int row = i / SLOTS_PER_ROW;
            int sx = x + GRID_OFFSET_X + col * (SLOT_SIZE + SLOT_GAP);
            int sy = y + GRID_OFFSET_Y + row * (SLOT_SIZE + SLOT_GAP);

            graphics.fill(sx - 1, sy - 1, sx + SLOT_SIZE + 1, sy + SLOT_SIZE + 1, COLOR_SLOT_BORDER);
            graphics.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, COLOR_SLOT_BG);

            ItemStack egg = getSpawnEgg(mobId);
            if (!egg.isEmpty()) {
                graphics.renderItem(egg, sx + 4, sy + 4);
            }
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int i = 0; i < availableMobs.size(); i++) {
            ResourceLocation mobId = availableMobs.get(i);
            int col = i % SLOTS_PER_ROW;
            int row = i / SLOTS_PER_ROW;
            int sx = leftPos + GRID_OFFSET_X + col * (SLOT_SIZE + SLOT_GAP);
            int sy = topPos + GRID_OFFSET_Y + row * (SLOT_SIZE + SLOT_GAP);

            if (mouseX >= sx && mouseX < sx + SLOT_SIZE && mouseY >= sy && mouseY < sy + SLOT_SIZE) {
                String name = mobId.getPath().replace("_", " ");
                name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                graphics.renderComponentTooltip(font, List.of(Component.literal(name)), mouseX, mouseY);
                return;
            }
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int bx = leftPos + BTN_X;
        int by = topPos + BTN_Y;
        if (mouseX >= bx && mouseX < bx + BTN_W && mouseY >= by && mouseY < by + BTN_H) {
            PacketDistributor.sendToServer(new MorphRequestPayload(DEMORPH_SENTINEL));
            this.onClose();
            return true;
        }

        for (int i = 0; i < availableMobs.size(); i++) {
            ResourceLocation mobId = availableMobs.get(i);
            int col = i % SLOTS_PER_ROW;
            int row = i / SLOTS_PER_ROW;
            int sx = leftPos + GRID_OFFSET_X + col * (SLOT_SIZE + SLOT_GAP);
            int sy = topPos + GRID_OFFSET_Y + row * (SLOT_SIZE + SLOT_GAP);

            if (mouseX >= sx && mouseX < sx + SLOT_SIZE && mouseY >= sy && mouseY < sy + SLOT_SIZE) {
                PacketDistributor.sendToServer(new MorphRequestPayload(mobId));
                this.onClose();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // suppress default label rendering
    }

    private static ItemStack getSpawnEgg(ResourceLocation mobId) {
        ResourceLocation eggId = ResourceLocation.withDefaultNamespace(mobId.getPath() + "_spawn_egg");
        Item item = BuiltInRegistries.ITEM.get(eggId);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }
}
