package com.familiasantana.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Mod(modid = "familiasantana", name = "Familia Santana Client", version = "2.0",
        clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class FamiliaSantanaClient {

    private static final String CATEGORY = "Familia Santana Client";
    private static final SimpleDateFormat CLOCK = new SimpleDateFormat("HH:mm:ss");

    private KeyBinding menuKey;
    private KeyBinding zoomKey;

    private final Minecraft mc = Minecraft.getMinecraft();
    private final List<Long> leftClicks = new ArrayList<Long>();
    private final List<Long> rightClicks = new ArrayList<Long>();

    private float oldGamma = 1.0F;
    private boolean fullbrightApplied = false;
    private boolean zooming = false;
    private boolean oldSmooth = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Config.load();
        menuKey = new KeyBinding("Abrir menu", Keyboard.KEY_RSHIFT, CATEGORY);
        zoomKey = new KeyBinding("Zoom", Keyboard.KEY_C, CATEGORY);
        ClientRegistry.registerKeyBinding(menuKey);
        ClientRegistry.registerKeyBinding(zoomKey);
        MinecraftForge.EVENT_BUS.register(this);
    }

    // ---------- Entrada ----------

    @SubscribeEvent
    public void onMouse(MouseEvent e) {
        if (!e.buttonstate) return;
        long now = System.currentTimeMillis();
        if (e.button == 0) leftClicks.add(now);
        else if (e.button == 1) rightClicks.add(now);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent e) {
        if (menuKey.isPressed()) {
            mc.displayGuiScreen(new SettingsGui());
        }
    }

    // ---------- Tick: sprint, fullbright, zoom ----------

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END || mc.thePlayer == null) return;

        // Toggle Sprint (sprint automatico ao andar para frente)
        if (Config.get("Toggle Sprint")
                && mc.gameSettings.keyBindForward.isKeyDown()
                && !mc.thePlayer.isSneaking()
                && !mc.thePlayer.isUsingItem()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }

        // Fullbright
        if (Config.get("Fullbright")) {
            if (!fullbrightApplied) {
                oldGamma = mc.gameSettings.gammaSetting;
                fullbrightApplied = true;
            }
            mc.gameSettings.gammaSetting = 100.0F;
        } else if (fullbrightApplied) {
            mc.gameSettings.gammaSetting = oldGamma;
            fullbrightApplied = false;
        }

        // Camera suave durante o zoom
        boolean wantZoom = Config.get("Zoom") && zoomKey.isKeyDown() && mc.currentScreen == null;
        if (wantZoom && !zooming) {
            oldSmooth = mc.gameSettings.smoothCamera;
            mc.gameSettings.smoothCamera = true;
            zooming = true;
        } else if (!wantZoom && zooming) {
            mc.gameSettings.smoothCamera = oldSmooth;
            zooming = false;
        }
    }

    @SubscribeEvent
    public void onFov(FOVUpdateEvent e) {
        if (zooming) {
            e.newfov = e.fov * 0.25F;
        }
    }

    // ---------- HUD ----------

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post e) {
        if (e.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (mc.thePlayer == null || mc.gameSettings.showDebugInfo) return;

        long now = System.currentTimeMillis();
        prune(leftClicks, now);
        prune(rightClicks, now);

        ScaledResolution sr = e.resolution;

        drawInfoLines(sr);
        if (Config.get("Keystrokes")) drawKeystrokes(sr);
        if (Config.get("Armor HUD")) drawArmor(sr);
        if (Config.get("Efeitos")) drawEffects(sr);

        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    private void prune(List<Long> list, long now) {
        Iterator<Long> it = list.iterator();
        while (it.hasNext()) {
            if (now - it.next() > 1000) it.remove();
        }
    }

    private void drawInfoLines(ScaledResolution sr) {
        List<String> lines = new ArrayList<String>();
        if (Config.get("Marca")) lines.add("\u00a76\u00a7lFamilia Santana Client");
        if (Config.get("FPS")) lines.add("\u00a7fFPS: \u00a7e" + Minecraft.getDebugFPS());
        if (Config.get("CPS")) lines.add("\u00a7fCPS: \u00a7e" + leftClicks.size() + " \u00a77| \u00a7e" + rightClicks.size());
        if (Config.get("Ping")) lines.add("\u00a7fPing: \u00a7e" + getPing() + "ms");
        if (Config.get("Coordenadas")) {
            lines.add("\u00a7fXYZ: \u00a7e" + MathHelper.floor_double(mc.thePlayer.posX) + " "
                    + MathHelper.floor_double(mc.thePlayer.posY) + " "
                    + MathHelper.floor_double(mc.thePlayer.posZ));
        }
        if (Config.get("Direcao")) lines.add("\u00a7fDirecao: \u00a7e" + mc.thePlayer.getHorizontalFacing().getName());
        if (Config.get("Relogio")) lines.add("\u00a7fHora: \u00a7e" + CLOCK.format(new Date()));

        int y = 4;
        for (String line : lines) {
            mc.fontRendererObj.drawStringWithShadow(line, 4, y, 0xFFFFFF);
            y += 10;
        }
    }

    private int getPing() {
        if (mc.getNetHandler() == null) return 0;
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        return info == null ? 0 : info.getResponseTime();
    }

    // ---------- Keystrokes ----------

    private void drawKeystrokes(ScaledResolution sr) {
        int s = 20;
        int gap = 2;
        int x = 4;
        int y = sr.getScaledHeight() - 4 - (s * 4 + gap * 3);

        drawKey("W", mc.gameSettings.keyBindForward.isKeyDown(), x + s + gap, y, s, s, null);
        drawKey("A", mc.gameSettings.keyBindLeft.isKeyDown(), x, y + s + gap, s, s, null);
        drawKey("S", mc.gameSettings.keyBindBack.isKeyDown(), x + s + gap, y + s + gap, s, s, null);
        drawKey("D", mc.gameSettings.keyBindRight.isKeyDown(), x + (s + gap) * 2, y + s + gap, s, s, null);

        int mouseW = (s * 3 + gap * 2 - gap) / 2;
        int my = y + (s + gap) * 2;
        drawKey("LMB", Mouse.isButtonDown(0), x, my, mouseW, s, leftClicks.size() + " CPS");
        drawKey("RMB", Mouse.isButtonDown(1), x + mouseW + gap, my, mouseW, s, rightClicks.size() + " CPS");

        drawKey("-----", mc.gameSettings.keyBindJump.isKeyDown(), x, y + (s + gap) * 3, s * 3 + gap * 2, 12, null);
    }

    private void drawKey(String label, boolean pressed, int x, int y, int w, int h, String sub) {
        int bg = pressed ? 0xAAFFFFFF : 0x88000000;
        int fg = pressed ? 0x000000 : 0xFFFFFF;
        Gui.drawRect(x, y, x + w, y + h, bg);
        int lw = mc.fontRendererObj.getStringWidth(label);
        if (sub == null) {
            mc.fontRendererObj.drawString(label, x + (w - lw) / 2, y + (h - 8) / 2, fg);
        } else {
            mc.fontRendererObj.drawString(label, x + (w - lw) / 2, y + 3, fg);
            int sw = mc.fontRendererObj.getStringWidth(sub);
            mc.fontRendererObj.drawString(sub, x + (w - sw) / 2, y + 11, pressed ? 0x444444 : 0xAAAAAA);
        }
    }

    // ---------- Armor HUD ----------

    private void drawArmor(ScaledResolution sr) {
        int x = sr.getScaledWidth() - 22;
        int y = sr.getScaledHeight() / 2 - 45;

        List<ItemStack> stacks = new ArrayList<ItemStack>();
        stacks.add(mc.thePlayer.getHeldItem());
        for (int i = 3; i >= 0; i--) stacks.add(mc.thePlayer.inventory.armorInventory[i]);

        for (ItemStack stack : stacks) {
            if (stack != null) {
                GlStateManager.pushMatrix();
                RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
                mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, x, y);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();

                if (stack.isItemStackDamageable()) {
                    String dur = String.valueOf(stack.getMaxDamage() - stack.getItemDamage());
                    int w = mc.fontRendererObj.getStringWidth(dur);
                    mc.fontRendererObj.drawStringWithShadow(dur, x - w - 3, y + 4, 0xFFFFFF);
                }
            }
            y += 18;
        }
        GlStateManager.disableLighting();
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    // ---------- Efeitos de pocao ----------

    private void drawEffects(ScaledResolution sr) {
        Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
        if (effects.isEmpty()) return;

        int y = 4;
        for (PotionEffect effect : effects) {
            String name = I18n.format(effect.getEffectName());
            if (effect.getAmplifier() > 0) name += " " + (effect.getAmplifier() + 1);
            String text = "\u00a7f" + name + " \u00a77" + Potion.getDurationString(effect);
            int w = mc.fontRendererObj.getStringWidth(text);
            mc.fontRendererObj.drawStringWithShadow(text, sr.getScaledWidth() - w - 4, y, 0xFFFFFF);
            y += 10;
        }
    }
}
