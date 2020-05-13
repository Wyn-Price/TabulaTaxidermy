package com.wynprice.taxidermy;

import com.wynprice.taxidermy.network.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.gui.*;
import net.dumbcode.dumblibrary.client.gui.filebox.FileDropboxFrame;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.network.SplitNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.lwjgl.input.Mouse;

import javax.vecmath.Vector3f;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class GuiTaxidermyBlock extends GuiScreen {

    @Getter
    private final TaxidermyBlockEntity blockEntity;

    private FileDropboxFrame dropboxFrame;

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

    @Getter
    private final List<SelectListEntry> modelEntries = new ArrayList<>();

    public GuiTaxidermyBlock(TaxidermyBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public void initGui() {
        TabulaTaxidermy.NETWORK.sendToServer(new C5RequestHeaders());

        this.modelSelectionBox = new GuiDropdownBox<>(this.width/2-175, this.height/4-40, 170, 20, this.height/80, () -> this.modelEntries);
        this.textureSelectionBox = new GuiDropdownBox<>(this.width/2+5, this.height/4-40, 170, 20, this.height/80, () -> this.textureEntries);

        int sliderWidth = 100;

        this.addButton(new GuiButton(4, this.width/2 - 3*sliderWidth/2 - 10, this.height-30, sliderWidth, 20, "Animate"));
        this.addButton(new GuiButton(5, this.width/2 - sliderWidth/2, this.height-30, sliderWidth, 20, "Toggle Hidden"));
        this.addButton(new GuiButton(6, this.width/2 + sliderWidth/2 + 10, this.height-30, sliderWidth, 20, "Done"));

        Vector3f translation = this.blockEntity.getTranslation();
        this.xPosition = new GuiNumberEntry(7, translation.x, 1/4F, 2, this.width/4, this.height/2+ 30, sliderWidth, 20, this::addButton, this::onChange);
        this.yPosition = new GuiNumberEntry(8, translation.y, 1/4F, 2, this.width/2, this.height/2 + 30, sliderWidth, 20, this::addButton, this::onChange);
        this.zPosition = new GuiNumberEntry(9, translation.z, 1/4F, 2, 3*this.width/4, this.height/2 + 30, sliderWidth, 20, this::addButton, this::onChange);

        Vector3f rotation = this.blockEntity.getRotation();
        this.xRotation = new GuiNumberEntry(10, rotation.x, 22.5F, 2, this.width/4, this.height/2 + 60, sliderWidth, 20, this::addButton, this::onChange);
        this.yRotation = new GuiNumberEntry(11, rotation.y, 22.5F, 2, this.width/2, this.height/2 + 60, sliderWidth, 20, this::addButton, this::onChange);
        this.zRotation = new GuiNumberEntry(12, rotation.z, 22.5F, 2, 3*this.width/4, this.height/2 + 60, sliderWidth, 20, this::addButton, this::onChange);

        this.scaleSlider = new GuiNumberEntry(13, this.blockEntity.getScale(), 1/4F, 2, this.width/2, this.height/2, sliderWidth, 20, this::addButton, this::onChange);

        this.allSliders = new GuiNumberEntry[]{ this.xPosition, this.yPosition, this.zPosition, this.xRotation, this.yRotation, this.zRotation, this.scaleSlider };

        super.initGui();
    }

    private void runOnSliders(Consumer<GuiNumberEntry> consumer) {
        for (GuiNumberEntry slider : this.allSliders) {
            consumer.accept(slider);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button.id == 4) {
            TabulaModel model = this.blockEntity.getModel();
            ResourceLocation texture = this.blockEntity.getTexture();
            if(model != null && texture != null) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiTaxidermy(model, texture, new TextComponentString("Taxidermy Block"), this.blockEntity));
            }
        }
        if(button.id == 5) {
            TabulaTaxidermy.NETWORK.sendToServer(new C9ToggleHidden(this.blockEntity.getPos()));
        }
        if(button.id == 6) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
        this.runOnSliders(e -> e.buttonClicked(button));
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.runOnSliders(GuiNumberEntry::render);
        this.modelSelectionBox.render(mouseX, mouseY);
        this.textureSelectionBox.render(mouseX, mouseY);

        FontRenderer renderer = Minecraft.getMinecraft().fontRenderer;
        this.drawCenteredString(renderer, "Model", this.width/2-85, this.height/4-60, GuiConstants.NICE_WHITE);
        this.drawCenteredString(renderer, "Texture", this.width/2+85, this.height/4-60, GuiConstants.NICE_WHITE);

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.modelSelectionBox.mouseClicked(mouseX, mouseY, mouseButton);
        this.textureSelectionBox.mouseClicked(mouseX, mouseY, mouseButton);
        this.runOnSliders(e -> e.mouseClicked(mouseX, mouseY, mouseButton));
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        if(Mouse.getEventButtonState() && this.dropboxFrame != null) {
            this.dropboxFrame.dispose();
            this.dropboxFrame = null;
        }
        this.modelSelectionBox.handleMouseInput();
        this.textureSelectionBox.handleMouseInput();
        this.runOnSliders(e -> e.handleMouseInput(this.width, this.height));
        super.handleMouseInput();
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        this.modelSelectionBox.handleKeyboardInput();
        this.textureSelectionBox.handleKeyboardInput();
        super.handleKeyboardInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.runOnSliders(e -> e.keyTyped(typedChar, keyCode));
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void updateScreen() {
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
        this.runOnSliders(GuiNumberEntry::updateEntry);
        super.updateScreen();
    }

    public void onChange(GuiNumberEntry entry, int id) {
        TabulaTaxidermy.NETWORK.sendToServer(new C3SetBlockProperties(this.blockEntity.getPos(), id-7, (float) entry.getValue()));
    }

    public void setProperties(int index, float value) {
        this.allSliders[index].setValue(value, false);
    }

    public void setList(DataHandler<?> handler, List<DataHeader> headers) {
        List<SelectListEntry> entries = (handler == DataHandler.TEXTURE ? this.textureEntries : this.modelEntries);
        entries.clear();
        entries.add(new UploadEntryEntry(handler));
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
        public void draw(int x, int y, int mouseX, int mouseY) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            fontRenderer.drawString(this.header.getName(), x + 3F, y + 6F, 0xFFF0F0F0, false);
            fontRenderer.drawString(this.header.getUploader(), x + fontRenderer.getStringWidth(this.header.getName()) + 10F, y + 6F, 0xFF707070, false);
        }

        @Override
        public boolean onClicked(int relMouseX, int relMouseY, int mouseX, int mouseY) {
            TabulaTaxidermy.NETWORK.sendToServer(new C7S8SetBlockUUID(blockEntity.getPos(), this.header.getUuid(), this.handler));
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
        public void draw(int x, int y, int mouseX, int mouseY) {
            String text = this.file == null ? "Upload new file" : "Click to upload " + this.file.getName();
            Minecraft.getMinecraft().fontRenderer.drawString(text, x + 3, y + 4, 0xFFFAFAFA);
        }

        @Override
        public boolean onClicked(int relMouseX, int relMouseY, int mouseX, int mouseY) {
            if(this.file != null) {
                dropboxFrame = null;
                this.handler.createHandler(this.file).ifPresent(h ->
                    SplitNetworkHandler.sendSplitMessage(new C0UploadData(blockEntity.getPos(), UUID.randomUUID(), this.file.getName(), h), SimpleNetworkWrapper::sendToServer)
                );
                this.file = null;
            } else {
                dropboxFrame = new FileDropboxFrame("Upload " + this.handler.getTypeName(), (dir, name) -> name.endsWith(this.handler.getExtension()), f -> this.file = f);
            }
            return false;
        }
    }
}
