package com.wynprice.taxidermy;

import com.wynprice.taxidermy.network.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.client.gui.GuiConstants;
import net.dumbcode.dumblibrary.client.gui.GuiDropdownBox;
import net.dumbcode.dumblibrary.client.gui.GuiTaxidermy;
import net.dumbcode.dumblibrary.client.gui.SelectListEntry;
import net.dumbcode.dumblibrary.client.gui.filebox.FileDropboxFrame;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.network.SplitNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.lwjgl.input.Mouse;

import javax.vecmath.Vector3f;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuiTaxidermyBlock extends GuiScreen implements GuiSlider.ISlider {

    @Getter
    private final TaxidermyBlockEntity blockEntity;

    private FileDropboxFrame dropboxFrame;

    private GuiDropdownBox<SelectListEntry> modelSelectionBox;
    private GuiDropdownBox<SelectListEntry> textureSelectionBox;

    private GuiSlider xPosition;
    private GuiSlider yPosition;
    private GuiSlider zPosition;

    private GuiSlider xRotation;
    private GuiSlider yRotation;
    private GuiSlider zRotation;

    private GuiSlider scaleSlider;

    private GuiSlider[] allSliders;
    private int selectedIndex = -1;

    private boolean slidersDirty = false;

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
        (this.xPosition = this.addButton(new GuiSlider(7, this.width/2 - 3*sliderWidth/2 - 10, this.height/2+ 30, sliderWidth, 20, "X: ", "", -2, 2, translation.x, true, true, this))).updateSlider();
        (this.yPosition = this.addButton(new GuiSlider(8, this.width/2 - sliderWidth/2, this.height/2 + 30, sliderWidth, 20, "Y: ", "", -2, 2, translation.y, true, true, this))).updateSlider();
        (this.zPosition = this.addButton(new GuiSlider(9, this.width/2 + sliderWidth/2 + 10, this.height/2 + 30, sliderWidth, 20, "Z: ", "", -2, 2, translation.z, true, true, this))).updateSlider();

        Vector3f rotation = this.blockEntity.getRotation();
        (this.xRotation = this.addButton(new GuiSlider(10, this.width/2 - 3*sliderWidth/2 - 10, this.height/2 + 60, sliderWidth, 20, "X: ", "", -180, 180, rotation.x, true, true, this))).updateSlider();
        (this.yRotation = this.addButton(new GuiSlider(11, this.width/2 - sliderWidth/2, this.height/2 + 60, sliderWidth, 20, "Y: ", "", -180, 180, rotation.y, true, true, this))).updateSlider();
        (this.zRotation = this.addButton(new GuiSlider(12, this.width/2 + sliderWidth/2 + 10, this.height/2 + 60, sliderWidth, 20, "Z: ", "", -180, 180, rotation.z, true, true, this))).updateSlider();

        this.scaleSlider = this.addButton(new GuiSlider(13, this.width/2 - sliderWidth/2, this.height/2+7, sliderWidth, 20, "Scale: ", "", -5, 5, Math.log(this.blockEntity.getScale()) / Math.log(2), true, true, this));

        this.allSliders = new GuiSlider[]{ this.xPosition, this.yPosition, this.zPosition, this.xRotation, this.yRotation, this.zRotation, this.scaleSlider };

        super.initGui();
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
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
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
        for (int i = 0; i < this.allSliders.length; i++) {
            if(GuiConstants.mouseOn(this.allSliders[i], mouseX, mouseY)) {
                this.selectedIndex = i;
                break;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        this.selectedIndex = -1;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        if(Mouse.getEventButtonState() && this.dropboxFrame != null) {
            this.dropboxFrame.dispose();
            this.dropboxFrame = null;
        }
        this.modelSelectionBox.handleMouseInput();
        this.textureSelectionBox.handleMouseInput();
        super.handleMouseInput();
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        this.modelSelectionBox.handleKeyboardInput();
        this.textureSelectionBox.handleKeyboardInput();
        super.handleKeyboardInput();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void updateScreen() {
        if(this.slidersDirty) {
            this.slidersDirty = false;
            TabulaTaxidermy.NETWORK.sendToServer(new C3SetBlockProperties(this.blockEntity.getPos(),
                new Vector3f((float) this.xPosition.getValue(), (float) this.yPosition.getValue(), (float) this.zPosition.getValue()),
                new Vector3f((float) this.xRotation.getValue(), (float) this.yRotation.getValue(), (float) this.zRotation.getValue()),
                (float) Math.pow(2, this.scaleSlider.getValue())
            ));
        }

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
        super.updateScreen();
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        this.slidersDirty = true;
        if(slider.id == 12) {
            slider.parent = null;
            double value = Math.pow(2, slider.getValue());
            slider.displayString = slider.dispString + (Math.round(value * 100F) / 100F) + slider.suffix;
            slider.parent = this;
        }
    }

    public void setProperties(Vector3f translation, Vector3f angles, float scale) {
        if(this.selectedIndex != 0) {
            this.xPosition.setValue(translation.x);
        }
        if(this.selectedIndex != 1) {
            this.yPosition.setValue(translation.y);
        }
        if(this.selectedIndex != 2) {
            this.zPosition.setValue(translation.z);
        }

        if(this.selectedIndex != 3) {
            this.xRotation.setValue(angles.x);
        }
        if(this.selectedIndex != 4) {
            this.yRotation.setValue(angles.y);
        }
        if(this.selectedIndex != 5) {
            this.zRotation.setValue(angles.z);
        }

        if(this.selectedIndex != 6) {
            this.scaleSlider.setValue(Math.log(scale) / Math.log(2));
        }
    }

    public void setList(DataHandler handler, List<DataHeader> headers) {
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
