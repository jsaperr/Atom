package com.jsaperr.atom.block.gui;

import com.jsaperr.atom.MorphCategories;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class ResearchStationScreen extends AbstractContainerScreen<ResearchStationMenu> {

    private static final int GUI_WIDTH = 196;
    private static final int GUI_HEIGHT = 160;
    private static final int TAB_HEIGHT = 22;
    private static final int SLOT_SIZE = 24;
    private static final int SLOT_GAP = 6;
    private static final int SLOTS_PER_ROW = 5;
    private static final int GRID_OFFSET_X = 10;
    private static final int GRID_OFFSET_Y = 36;

    // slot bg colors
    private static final int COLOR_SLOT_RESEARCHED_BORDER = 0xFF4A6741;
    private static final int COLOR_SLOT_RESEARCHED_BG    = 0xFF2A3D28;
    private static final int COLOR_SLOT_LOCKED_BORDER    = 0xFF555555;
    private static final int COLOR_SLOT_LOCKED_BG        = 0xFF1F1F1F;
    private static final int COLOR_SLOT_LOCKED_OVERLAY   = 0xBB000000;

    private static final List<ResourceLocation> CATEGORY_KEYS =
            new ArrayList<>(MorphCategories.CATEGORIES.keySet());

    private static final List<Component> TAB_NAMES = CATEGORY_KEYS.stream()
            .map(id -> (Component) Component.translatable("gui.atom.tab." + id.getPath()))
            .toList();

    private static final List<List<ResourceLocation>> CATEGORY_MOBS =
            CATEGORY_KEYS.stream().map(MorphCategories.CATEGORIES::get).toList();

    private Set<ResourceLocation> researched = new HashSet<>();
    private int activeTab = 0;

    public ResearchStationScreen(ResearchStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    public void setResearched(Set<ResourceLocation> researched) {
        this.researched = new HashSet<>(researched);
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

        int tabWidth = GUI_WIDTH / TAB_NAMES.size();
        for (int i = 0; i < TAB_NAMES.size(); i++) {
            int tx = x + i * tabWidth;
            boolean active = i == activeTab;
            int border = active ? 0xFF888888 : 0xFF555555;
            int bg     = active ? 0xFF555555 : 0xFF444444;
            graphics.fill(tx, y, tx + tabWidth, y + TAB_HEIGHT, border);
            graphics.fill(tx + 1, y + 1, tx + tabWidth - 1, y + TAB_HEIGHT, bg);
            graphics.drawCenteredString(font, TAB_NAMES.get(i), tx + tabWidth / 2, y + 7, active ? 0xFFFFFF : 0xAAAAAA);
        }

        graphics.fill(x, y + TAB_HEIGHT, x + GUI_WIDTH, y + TAB_HEIGHT + 1, 0xFF888888);

        List<ResourceLocation> mobs = CATEGORY_MOBS.get(activeTab);
        int totalResearched = 0;

        for (int i = 0; i < mobs.size(); i++) {
            ResourceLocation mobId = mobs.get(i);
            boolean done = researched.contains(mobId);
            if (done) totalResearched++;

            int col = i % SLOTS_PER_ROW;
            int row = i / SLOTS_PER_ROW;
            int sx = x + GRID_OFFSET_X + col * (SLOT_SIZE + SLOT_GAP);
            int sy = y + GRID_OFFSET_Y + row * (SLOT_SIZE + SLOT_GAP);

            int border = done ? COLOR_SLOT_RESEARCHED_BORDER : COLOR_SLOT_LOCKED_BORDER;
            int bg     = done ? COLOR_SLOT_RESEARCHED_BG     : COLOR_SLOT_LOCKED_BG;
            graphics.fill(sx - 1, sy - 1, sx + SLOT_SIZE + 1, sy + SLOT_SIZE + 1, border);
            graphics.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, bg);

            ItemStack egg = getSpawnEgg(mobId);
            if (!egg.isEmpty()) {
                graphics.renderItem(egg, sx + 4, sy + 4);
            }

            if (!done) {
                graphics.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, COLOR_SLOT_LOCKED_OVERLAY);
            }
        }

        String progress = totalResearched + " / " + mobs.size();
        graphics.drawString(font, progress, x + GUI_WIDTH - 8 - font.width(progress), y + TAB_HEIGHT + 7, 0xAAAAAA, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        List<ResourceLocation> mobs = CATEGORY_MOBS.get(activeTab);
        for (int i = 0; i < mobs.size(); i++) {
            ResourceLocation mobId = mobs.get(i);
            int col = i % SLOTS_PER_ROW;
            int row = i / SLOTS_PER_ROW;
            int sx = leftPos + GRID_OFFSET_X + col * (SLOT_SIZE + SLOT_GAP);
            int sy = topPos + GRID_OFFSET_Y + row * (SLOT_SIZE + SLOT_GAP);

            if (mouseX >= sx && mouseX < sx + SLOT_SIZE && mouseY >= sy && mouseY < sy + SLOT_SIZE) {
                boolean done = researched.contains(mobId);
                String mobName = mobId.getPath().replace("_", " ");
                mobName = Character.toUpperCase(mobName.charAt(0)) + mobName.substring(1);

                List<Component> lines = done
                        ? List.of(Component.literal(mobName), Component.literal("Researched").withStyle(s -> s.withColor(0x55FF55)))
                        : List.of(Component.literal(mobName), Component.literal("Not researched").withStyle(s -> s.withColor(0xAAAAAA)));

                graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
                return;
            }
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = leftPos;
        int y = topPos;
        int tabWidth = GUI_WIDTH / TAB_NAMES.size();
        for (int i = 0; i < TAB_NAMES.size(); i++) {
            int tx = x + i * tabWidth;
            if (mouseX >= tx && mouseX < tx + tabWidth && mouseY >= y && mouseY < y + TAB_HEIGHT) {
                activeTab = i;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // suppress default title/inventory label rendering
    }

    private static ItemStack getSpawnEgg(ResourceLocation mobId) {
        ResourceLocation eggId = ResourceLocation.withDefaultNamespace(mobId.getPath() + "_spawn_egg");
        Item item = BuiltInRegistries.ITEM.get(eggId);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }
}
