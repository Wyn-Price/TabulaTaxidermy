package com.wynprice.taxidermy;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynprice.taxidermy.network.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.client.gui.*;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.server.network.SplitNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;
import net.minecraftforge.fml.network.PacketDistributor;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class GuiTaxidermyBlock extends Screen {

    private static boolean fileDialogOpen = false;

    @Getter
    private final TaxidermyBlockEntity blockEntity;

    private GuiDropdownBox<SelectListEntry> modelSelectionBox;
    private GuiDropdownBox<SelectListEntry> textureSelectionBox;

    private GuiNumberEntry xPosition;
    private GuiNumberEntry yPosition;
    private GuiNumberEntry zPosition;

    private GuiNumberEntry xRotation;
    private GuiNumberEntry yRotation;
    private GuiNumberEntry zRotation;

    private GuiNumberEntry scaleSlider;

    private GuiNumberEntry[] allSliders;

    @Getter
    private final List<SelectListEntry> textureEntries = new ArrayList<>();
    private UploadEntryEntry textureUpload;

    @Getter
    private final List<SelectListEntry> modelEntries = new ArrayList<>();
    private UploadEntryEntry modelUpload;

    public GuiTaxidermyBlock(TaxidermyBlockEntity blockEntity) {
        super(new StringTextComponent("Taxidermy :)"));
        this.blockEntity = blockEntity;
    }

    @Override
    public void init() {
        Taxidermy.NETWORK.sendToServer(new C5RequestHeaders());

        this.addButton(this.modelSelectionBox = new GuiDropdownBox<>(this.width/2-175, this.height/4-40, 170, 20, this.height/80, () -> this.modelEntries));
        this.addButton(this.textureSelectionBox = new GuiDropdownBox<>(this.width/2+5, this.height/4-40, 170, 20, this.height/80, () -> this.textureEntries));

        int sliderWidth = 100;

        this.addButton(new ExtendedButton(this.width/2 - 3*sliderWidth/2 - 10, this.height-30, sliderWidth, 20, new StringTextComponent("Animate"), b -> {
            DCMModel model = this.blockEntity.getModel();
            ResourceLocation texture = this.blockEntity.getTexture();
            if(model != null && texture != null) {
                Minecraft.getInstance().setScreen(new GuiTaxidermy(model, texture, new StringTextComponent("Taxidermy Block"), this.blockEntity));
            }
        }));

        this.addButton(new ExtendedButton(this.width/2 - sliderWidth/2, this.height-30, sliderWidth, 20, new StringTextComponent("Toggle Hidden"), b ->
            Taxidermy.NETWORK.sendToServer(new C8ToggleHidden(this.blockEntity.getBlockPos()))
        ));


        this.addButton(new ExtendedButton(this.width/2 + sliderWidth/2 + 10, this.height-30, sliderWidth, 20, new StringTextComponent("Done"), b -> {
            Minecraft.getInstance().setScreen(null);
        }));

        Vector3f translation = this.blockEntity.getTranslation();
        this.addButton(this.xPosition = new GuiNumberEntry(0, translation.x(), 1/4F, 2, this.width/4, this.height/2+ 30, sliderWidth, 20, this::onChange));
        this.addButton(this.yPosition = new GuiNumberEntry(1, translation.y(), 1/4F, 2, this.width/2, this.height/2 + 30, sliderWidth, 20, this::onChange));
        this.addButton(this.zPosition = new GuiNumberEntry(2, translation.z(), 1/4F, 2, 3*this.width/4, this.height/2 + 30, sliderWidth, 20, this::onChange));

        Vector3f rotation = this.blockEntity.getRotation();
        this.addButton(this.xRotation = new GuiNumberEntry(3, rotation.x(), 22.5F, 2, this.width/4, this.height/2 + 60, sliderWidth, 20, this::onChange));
        this.addButton(this.yRotation = new GuiNumberEntry(4, rotation.y(), 22.5F, 2, this.width/2, this.height/2 + 60, sliderWidth, 20, this::onChange));
        this.addButton(this.zRotation = new GuiNumberEntry(5, rotation.z(), 22.5F, 2, 3*this.width/4, this.height/2 + 60, sliderWidth, 20, this::onChange));

        this.addButton(this.scaleSlider = new GuiNumberEntry(6, this.blockEntity.getScale(), 1/4F, 2, this.width/2, this.height/2, sliderWidth, 20, this::onChange));

        this.allSliders = new GuiNumberEntry[]{ this.xPosition, this.yPosition, this.zPosition, this.xRotation, this.yRotation, this.zRotation, this.scaleSlider };

        super.init();
    }

    private void runOnSliders(Consumer<GuiNumberEntry> consumer) {
        for (GuiNumberEntry slider : this.allSliders) {
            consumer.accept(slider);
        }
    }


    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);

        FontRenderer renderer = Minecraft.getInstance().font;
        AbstractGui.drawCenteredString(stack, renderer, "Model", this.width/2-85, this.height/4-60, GuiConstants.NICE_WHITE);
        AbstractGui.drawCenteredString(stack, renderer, "Texture", this.width/2+85, this.height/4-60, GuiConstants.NICE_WHITE);
    }

    @Override
    public void onFilesDrop(List<Path> files) {
        Minecraft instance = Minecraft.getInstance();
        MouseHelper handler = instance.mouseHandler;
        double x = handler.xpos() * instance.getWindow().getGuiScaledWidth() / instance.getWindow().getScreenWidth();
        boolean model = x < this.width/2D;

        UploadEntryEntry entry = model ? this.modelUpload : this.textureUpload;
        if(entry != null) {
            for (Path file : files) {
                if (file.getFileName().endsWith("."+(model ? DataHandler.MODEL : DataHandler.TEXTURE).getExtension())) {
                    try {
                        entry.file = file.toFile();
                        GuiDropdownBox<SelectListEntry> box = model ? this.modelSelectionBox : this.textureSelectionBox;
                        box.setOpen(true);
                        box.setActive(entry);
                        break;
                    } catch (Exception ignored) {

                    }
                }

            }
        }

    }

    @Override
    public void tick() {
        for (SelectListEntry entry : this.modelEntries) {
            if(entry instanceof DataHeaderEntry && ((DataHeaderEntry) entry).header.getUuid().equals(this.blockEntity.getModelUUID())) {
                this.modelSelectionBox.setActive(entry);
                break;
            }
        }
        for (SelectListEntry entry : this.textureEntries) {
            if(entry instanceof DataHeaderEntry && ((DataHeaderEntry) entry).header.getUuid().equals(this.blockEntity.getTextureUUID())) {
                this.textureSelectionBox.setActive(entry);
                break;
            }
        }
        this.runOnSliders(GuiNumberEntry::tick);
        super.tick();
    }

    public void onChange(GuiNumberEntry entry, int id) {
        Taxidermy.NETWORK.sendToServer(new C3SetBlockProperties(this.blockEntity.getBlockPos(), id, (float) entry.getValue()));
    }

    public void setProperties(int index, float value) {
        this.allSliders[index].setValue(value, false);
    }

    public void setList(DataHandler<?> handler, List<DataHeader> headers) {
        List<SelectListEntry> entries = (handler == DataHandler.TEXTURE ? this.textureEntries : this.modelEntries);
        entries.clear();
        UploadEntryEntry entry = new UploadEntryEntry(handler);
        if(handler == DataHandler.TEXTURE) {
            this.textureUpload = entry;
        } else {
            this.modelUpload = entry;
        }
        entries.add(entry);
        for (DataHeader header : headers) {
            entries.add(new DataHeaderEntry(handler, header));
        }
    }

    @RequiredArgsConstructor
    private class DataHeaderEntry implements SelectListEntry {

        private final DataHandler<?> handler;
        private final DataHeader header;

        @Override
        public String getSearch() {
            return this.header.getName();
        }

        @Override
        public void draw(MatrixStack stack, int x, int y, int mouseX, int mouseY, boolean mouseOver) {
            FontRenderer fontRenderer = Minecraft.getInstance().font;
            fontRenderer.draw(stack, this.header.getName(), x + 3F, y + 6F, 0xFFF0F0F0);
            fontRenderer.draw(stack, this.header.getUploader(), x + fontRenderer.width(this.header.getName()) + 10F, y + 6F, 0xFF707070);
        }


        @Override
        public boolean onClicked(double relMouseX, double relMouseY, double mouseX, double mouseY) {
            Taxidermy.NETWORK.sendToServer(new C7S7SetBlockUUID(blockEntity.getBlockPos(), this.header.getUuid(), this.handler));
            return true;
        }
    }

    private class UploadEntryEntry implements SelectListEntry {

        private final DataHandler<?> handler;
        private File file;

        private UploadEntryEntry(DataHandler<?> handler) {
            this.handler = handler;
        }

        @Override
        public String getSearch() {
            return "";
        }

        @Override
        public void draw(MatrixStack stack, int x, int y, int mouseX, int mouseY, boolean mouseOver) {
            String text = this.file == null ? "Upload new file" : "Click to upload " + this.file.getName();
            Minecraft.getInstance().font.draw(stack, text, x + 3, y + 4, 0xFFFAFAFA);
        }

        @Override
        public boolean onClicked(double relMouseX, double relMouseY, double mouseX, double mouseY) {
            if(this.file != null) {
                this.handler.createHandler(this.file.toPath(), true).ifPresent(h ->
                    SplitNetworkHandler.sendSplitMessage(new C0UploadData(blockEntity.getBlockPos(), UUID.randomUUID(), this.file.getName(), h), PacketDistributor.SERVER.noArg())
                );
                this.file = null;
            } else {
                PointerBuffer path = MemoryUtil.memAllocPointer(1);
                try {
                    int result = NativeFileDialog.NFD_OpenDialog(this.handler.getExtension(), this.handler.getFolderCache(), path);
                    switch (result) {
                        case NativeFileDialog.NFD_OKAY:
                            this.file = new File(path.getStringUTF8(0));
                            break;
                        case NativeFileDialog.NFD_CANCEL:
                            break;
                        default:
                            Taxidermy.getLogger().error("File Dialog Error: {}", NativeFileDialog.NFD_GetError());
                    }
                } finally {
                    MemoryUtil.memFree(path);
                }
            }
            return false;
        }
    }
}
