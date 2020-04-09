package com.wynprice.tabulataxidermy;

import com.wynprice.tabulataxidermy.network.C0UploadData;
import com.wynprice.tabulataxidermy.network.C3SetBlockProperties;
import com.wynprice.tabulataxidermy.network.C5RequestHeaders;
import com.wynprice.tabulataxidermy.network.C7SetBlockUUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import net.dumbcode.dumblibrary.client.gui.GuiConstants;
import net.dumbcode.dumblibrary.client.gui.GuiDropdownBox;
import net.dumbcode.dumblibrary.client.gui.GuiTaxidermy;
import net.dumbcode.dumblibrary.client.gui.SelectListEntry;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.network.SplitNetworkHandler;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import javax.imageio.ImageIO;
import javax.vecmath.Vector3f;
import javax.xml.ws.RequestWrapper;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuiTTBlock extends GuiScreen implements GuiSlider.ISlider {

    @Getter
    private final TTBlockEntity blockEntity;

    private GuiTextField modelLocation;
    private GuiTextField textureLocation;

    private GuiDropdownBox<DataHeaderEntry> selectionBox;

    private GuiSlider xPosition;
    private GuiSlider yPosition;
    private GuiSlider zPosition;

    private GuiSlider xRotation;
    private GuiSlider yRotation;
    private GuiSlider zRotation;

    private GuiSlider scaleSlider;

    private boolean slidersDirty = false;

    @Getter
    private final List<DataHeaderEntry> entries = new ArrayList<>();

    public GuiTTBlock(TTBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public void initGui() {
        TabulaTaxidermy.NETWORK.sendToServer(new C5RequestHeaders());
        this.selectionBox = new GuiDropdownBox<>(this.width/2-175, this.height/4-40, 350, 20, 10, () -> this.entries);
        this.modelLocation = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, this.width/2-150, this.height/4-10, 300, 20);
        this.textureLocation = new GuiTextField(1, Minecraft.getMinecraft().fontRenderer, this.width/2-150, this.height/4+20, 300, 20);
        this.addButton(new GuiButton(2, this.width/2-100, this.height/2-15, 200, 20, "Upload"));
        this.addButton(new GuiButton(4, this.width/2-105, this.height-30, 100, 20, "Animate"));
        this.addButton(new GuiButton(5, this.width/2+5, this.height-30, 100, 20, "Done"));

        int sliderWidth = 100;
        Vector3f translation = this.blockEntity.getTranslation();
        this.addButton(this.xPosition = new GuiSlider(6, this.width/2 - 3*sliderWidth/2 - 10, this.height/2+ 30, sliderWidth, 20, "X: ", "", -2, 2, translation.x, true, true, this));
        this.addButton(this.yPosition = new GuiSlider(7, this.width/2 - sliderWidth/2, this.height/2 + 30, sliderWidth, 20, "Y: ", "", -2, 2, translation.y, true, true, this));
        this.addButton(this.zPosition = new GuiSlider(8, this.width/2 + sliderWidth/2 + 10, this.height/2 + 30, sliderWidth, 20, "Z: ", "", -2, 2, translation.z, true, true, this));

        Vector3f rotation = this.blockEntity.getRotation();
        this.addButton(this.xRotation = new GuiSlider(9, this.width/2 - 3*sliderWidth/2 - 10, this.height/2 + 60, sliderWidth, 20, "X: ", "", -180, 180, rotation.x, true, true, this));
        this.addButton(this.yRotation = new GuiSlider(10, this.width/2 - sliderWidth/2, this.height/2 + 60, sliderWidth, 20, "Y: ", "", -180, 180, rotation.y, true, true, this));
        this.addButton(this.zRotation = new GuiSlider(11, this.width/2 + sliderWidth/2 + 10, this.height/2 + 60, sliderWidth, 20, "Z: ", "", -180, 180, rotation.z, true, true, this));

        this.addButton(this.scaleSlider = new GuiSlider(12, this.width/2 - sliderWidth/2, this.height/2+7, sliderWidth, 20, "Scale: ", "", -5, 5, Math.log(this.blockEntity.getScale()) / Math.log(2), true, true, this));


        this.modelLocation.setMaxStringLength(1000);
        this.textureLocation.setMaxStringLength(1000);

        super.initGui();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button.id == 2) {
            try {
                File f = new File(this.modelLocation.getText());
                TabulaModelInformation information = TabulaUtils.getModelInformation(new FileInputStream(f));
                try {
                    BufferedImage image = ImageIO.read(new File(this.textureLocation.getText()));
                    UUID uuid = UUID.randomUUID();
                    SplitNetworkHandler.sendSplitMessage(new C0UploadData(this.blockEntity.getPos(), uuid, f.getName(), information, image), SimpleNetworkWrapper::sendToServer);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        if(button.id == 3) {
            //transform
        }
        if(button.id == 4) {
            TabulaModel model = this.blockEntity.getModel();
            ResourceLocation texture = this.blockEntity.getTexture();
            if(model != null && texture != null) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiTaxidermy(model, texture, new TextComponentString("Taxidermy Block"), this.blockEntity));
            }
        }
        if(button.id == 5) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.modelLocation.drawTextBox();
        this.textureLocation.drawTextBox();
        this.selectionBox.render(mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.modelLocation.mouseClicked(mouseX, mouseY, mouseButton);
        this.textureLocation.mouseClicked(mouseX, mouseY, mouseButton);
        this.selectionBox.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        this.selectionBox.handleMouseInput();
        super.handleMouseInput();
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        this.selectionBox.handleKeyboardInput();
        super.handleKeyboardInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if(GuiScreen.isKeyComboCtrlV(keyCode)) {
            this.convertCopiedFileToString();
        }
        this.modelLocation.textboxKeyTyped(typedChar, keyCode);
        this.textureLocation.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void convertCopiedFileToString() {
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if(transferable != null && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            try {
                List<File> fileList = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                if(fileList.size() == 1) {
                    GuiScreen.setClipboardString(fileList.get(0).getAbsolutePath());
                }
            } catch (Throwable ignored) {
                //ignore
            }
        }
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
        this.xPosition.setValue(translation.x);
        this.yPosition.setValue(translation.y);
        this.zPosition.setValue(translation.z);

        this.xRotation.setValue(angles.x);
        this.yRotation.setValue(angles.y);
        this.zRotation.setValue(angles.z);

        this.scaleSlider.setValue(Math.log(scale) / Math.log(2));
    }

    public void setList(List<DataHeader> headers) {
        this.entries.clear();
        for (DataHeader header : headers) {
            this.entries.add(new DataHeaderEntry(header));
        }
    }

    @RequiredArgsConstructor
    private class DataHeaderEntry implements SelectListEntry {

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
        public void onClicked(int relMouseX, int relMouseY, int mouseX, int mouseY) {
            TabulaTaxidermy.NETWORK.sendToServer(new C7SetBlockUUID(blockEntity.getPos(), this.header.getUuid()));
        }
    }
}
