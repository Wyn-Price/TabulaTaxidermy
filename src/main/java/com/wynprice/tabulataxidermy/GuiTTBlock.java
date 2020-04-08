package com.wynprice.tabulataxidermy;

import com.wynprice.tabulataxidermy.network.C0UploadData;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.network.SplitNetworkHandler;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class GuiTTBlock extends GuiScreen {

    private GuiTextField modelLocation;
    private GuiTextField textureLocation;
    private GuiButton doneButton;

    private final BlockPos pos;

    public GuiTTBlock(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void initGui() {
        this.modelLocation = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, this.width/2-200, this.height/4-10, 400, 20);
        this.textureLocation = new GuiTextField(1, Minecraft.getMinecraft().fontRenderer, this.width/2-200, 3*this.height/4-10, 400, 20);
        this.doneButton = this.addButton(new GuiButton(2, this.width/2-100, this.height-25, 200, 20, "Done"));

        this.modelLocation.setMaxStringLength(1000);
        this.textureLocation.setMaxStringLength(1000);

        super.initGui();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button == this.doneButton) {
            System.out.println(this.modelLocation.getText());
            System.out.println(this.textureLocation.getText());

            try {
                TabulaModelInformation information = TabulaUtils.getModelInformation(new FileInputStream(this.modelLocation.getText()));
                try {
                    BufferedImage image = ImageIO.read(new File(this.textureLocation.getText()));
                    UUID uuid = UUID.randomUUID();
                    SplitNetworkHandler.sendSplitMessage(new C0UploadData(this.pos, uuid, information, image), SimpleNetworkWrapper::sendToServer);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.modelLocation.drawTextBox();
        this.textureLocation.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.modelLocation.mouseClicked(mouseX, mouseY, mouseButton);
        this.textureLocation.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
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
}
