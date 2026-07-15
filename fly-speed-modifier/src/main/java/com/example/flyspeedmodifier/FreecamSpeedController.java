package com.example.flyspeedmodifier;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class FreecamSpeedController {
    private static final String FREECAM_MOD_ID = "freecam";

    private enum SpeedTarget {
        NONE,
        FREECAM,
        DIRECT_FLIGHT
    }

    private static KeyMapping adjustSpeedKey;
    private static boolean hasTemporaryMultiplier;
    private static double temporaryMultiplier = 1.0D;
    private static boolean wasAdjustSpeedKeyDown;

    private static boolean hasDirectOriginalFlyingSpeed;
    private static float directOriginalFlyingSpeed;

    private FreecamSpeedController() {}

    public static void setAdjustSpeedKey(KeyMapping keyMapping) {
        adjustSpeedKey = keyMapping;
    }

    public static void onEndClientTick(Minecraft client) {
        updateAdjustKeyState();

        SpeedTarget activeTarget = resolveActiveTarget(client);

        if (activeTarget == SpeedTarget.DIRECT_FLIGHT) {
            applyDirectFlyingSpeed(client);
            return;
        }

        if (hasDirectOriginalFlyingSpeed) {
            restoreDirectFlyingSpeed(client);
        }
    }

    public static void onConfigChanged() {
        resetTemporaryMultiplierSilently();
    }

    public static boolean handleMouseScroll(double verticalScroll) {
        if (verticalScroll == 0.0D) {
            return false;
        }

        if (!isAdjustKeyDown()) {
            return false;
        }

        Minecraft client = Minecraft.getInstance();
        SpeedTarget activeTarget = resolveActiveTarget(client);
        if (activeTarget == SpeedTarget.NONE) {
            return false;
        }

        if (!hasTemporaryMultiplier) {
            temporaryMultiplier = clampMultiplier(FlySpeedModifierConfig.initialSpeed());
            hasTemporaryMultiplier = true;
        }

        temporaryMultiplier =
                adjustMultiplierNonLinearly(
                        temporaryMultiplier, verticalScroll, FlySpeedModifierConfig.scrollStep());

        if (activeTarget == SpeedTarget.DIRECT_FLIGHT) {
            applyDirectFlyingSpeed(client);
        }

        showMultiplierOverlay(activeTarget, temporaryMultiplier);
        return true;
    }

    public static double applyFreecamSpeed(double originalSpeed) {
        if (!shouldUseTemporaryFreecamMultiplier()) {
            return originalSpeed;
        }

        return originalSpeed * temporaryMultiplier;
    }

    public static float applyFreecamCreativeFlyingSpeed(float originalFlyingSpeed) {
        if (!shouldUseTemporaryFreecamMultiplier()) {
            return originalFlyingSpeed;
        }

        return (float) (originalFlyingSpeed * temporaryMultiplier);
    }

    private static boolean shouldUseTemporaryFreecamMultiplier() {
        return isFreecamEnabled() && hasTemporaryMultiplier;
    }

    private static SpeedTarget resolveActiveTarget(Minecraft client) {
        if (isFreecamEnabled()) {
            return SpeedTarget.FREECAM;
        }

        if (isDirectFlyingActive(client)) {
            return SpeedTarget.DIRECT_FLIGHT;
        }

        return SpeedTarget.NONE;
    }

    private static boolean isDirectFlyingActive(Minecraft client) {
        return client != null && client.player != null && client.player.getAbilities().flying;
    }

    private static void applyDirectFlyingSpeed(Minecraft client) {
        if (!isDirectFlyingActive(client) || !hasTemporaryMultiplier) {
            return;
        }

        captureDirectOriginalFlyingSpeed(client);
        if (!hasDirectOriginalFlyingSpeed) {
            return;
        }

        client.player
                .getAbilities()
                .setFlyingSpeed((float) (directOriginalFlyingSpeed * temporaryMultiplier));
    }

    private static void captureDirectOriginalFlyingSpeed(Minecraft client) {
        if (hasDirectOriginalFlyingSpeed || client == null || client.player == null) {
            return;
        }

        directOriginalFlyingSpeed = client.player.getAbilities().getFlyingSpeed();
        hasDirectOriginalFlyingSpeed = true;
    }

    private static void restoreDirectFlyingSpeed(Minecraft client) {
        if (!hasDirectOriginalFlyingSpeed) {
            return;
        }

        if (client != null && client.player != null) {
            client.player.getAbilities().setFlyingSpeed(directOriginalFlyingSpeed);
        }

        hasDirectOriginalFlyingSpeed = false;
        directOriginalFlyingSpeed = 0.0F;
    }

    private static void updateAdjustKeyState() {
        boolean isDown = isAdjustKeyDown();
        if (isDown && !wasAdjustSpeedKeyDown) {
            showCurrentMultiplierOnKeyPress();
        }

        wasAdjustSpeedKeyDown = isDown;
    }

    private static void showCurrentMultiplierOnKeyPress() {
        if (FlySpeedModifierConfig.resetOnAdjust() || !hasTemporaryMultiplier) {
            temporaryMultiplier = clampMultiplier(FlySpeedModifierConfig.initialSpeed());
            hasTemporaryMultiplier = true;
        } else {
            temporaryMultiplier = clampMultiplier(temporaryMultiplier);
        }

        Minecraft client = Minecraft.getInstance();
        SpeedTarget activeTarget = resolveActiveTarget(client);
        if (activeTarget == SpeedTarget.NONE) {
            return;
        }

        if (activeTarget == SpeedTarget.DIRECT_FLIGHT) {
            applyDirectFlyingSpeed(client);
        }

        showMultiplierOverlay(activeTarget, temporaryMultiplier);
    }

    private static void resetTemporaryMultiplierSilently() {
        temporaryMultiplier = clampMultiplier(FlySpeedModifierConfig.initialSpeed());
        hasTemporaryMultiplier = true;

        Minecraft client = Minecraft.getInstance();
        SpeedTarget activeTarget = resolveActiveTarget(client);
        if (activeTarget == SpeedTarget.DIRECT_FLIGHT) {
            applyDirectFlyingSpeed(client);
        }
    }

    private static boolean isAdjustKeyDown() {
        return adjustSpeedKey != null && adjustSpeedKey.isDown();
    }

    private static boolean isFreecamEnabled() {
        if (!FabricLoader.getInstance().isModLoaded(FREECAM_MOD_ID)) {
            return false;
        }

        try {
            Class<?> freecamClass = Class.forName("net.xolt.freecam.Freecam");
            Object value = freecamClass.getMethod("isEnabled").invoke(null);
            return Boolean.TRUE.equals(value);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private static double adjustMultiplierNonLinearly(
            double currentMultiplier, double scrollAmount, double scrollStep) {
        double result = clampMultiplier(currentMultiplier);
        int operationCount = (int) Math.round(Math.abs(10.0D * scrollAmount * scrollStep));
        if (operationCount <= 0) {
            return result;
        }

        boolean increase = scrollAmount > 0.0D;
        for (int i = 0; i < operationCount; i++) {
            result =
                    increase
                            ? increaseOneSignificantStep(result)
                            : decreaseOneSignificantStep(result);
            result = clampMultiplier(result);
        }

        return clampMultiplier(alignToHundredthTowardChangeDirection(result, increase));
    }

    private static double increaseOneSignificantStep(double value) {
        double place = significantPlace(value, 1);
        double units = Math.floor(value / place + 1.0E-9D);
        return truncateToPlace((units + 1.0D) * place, place);
    }

    private static double decreaseOneSignificantStep(double value) {
        double place = significantPlace(value, 1);
        double units = Math.floor(value / place + 1.0E-9D);
        double candidate = truncateToPlace((units - 1.0D) * place, place);

        if (wouldBorrowToLowerMagnitude(value, candidate)) {
            double fallbackPlace = significantPlace(value, 2);
            double fallbackUnits = Math.floor(value / fallbackPlace + 1.0E-9D);
            return truncateToPlace((fallbackUnits - 1.0D) * fallbackPlace, fallbackPlace);
        }

        return candidate;
    }

    private static boolean wouldBorrowToLowerMagnitude(double originalValue, double nextValue) {
        if (!Double.isFinite(originalValue)
                || !Double.isFinite(nextValue)
                || originalValue <= 0.0D) {
            return false;
        }

        double highestPlace = highestSignificantPlace(originalValue);
        return nextValue > 0.0D && nextValue < highestPlace;
    }

    private static double highestSignificantPlace(double value) {
        double abs = Math.abs(value);
        if (!Double.isFinite(abs) || abs <= 0.0D) {
            return 0.01D;
        }

        int highestPower = (int) Math.floor(Math.log10(abs));
        double place = Math.pow(10.0D, highestPower);
        if (!Double.isFinite(place) || place <= 0.0D) {
            return 0.01D;
        }

        return place;
    }

    private static double significantPlace(double value, int offsetFromHighest) {
        double abs = Math.abs(value);
        if (!Double.isFinite(abs) || abs <= 0.0D) {
            return 0.01D;
        }

        int highestPower = (int) Math.floor(Math.log10(abs));
        double place = Math.pow(10.0D, highestPower - offsetFromHighest);
        if (!Double.isFinite(place) || place <= 0.0D) {
            return 0.01D;
        }

        return place;
    }

    private static double alignToHundredthTowardChangeDirection(double value, boolean increase) {
        if (!Double.isFinite(value)) {
            return value;
        }

        double place = 0.01D;
        double scaled = value / place;
        double units = increase ? Math.ceil(scaled - 1.0E-9D) : Math.floor(scaled + 1.0E-9D);
        return Math.round(units * place * 100.0D) / 100.0D;
    }

    private static double truncateToPlace(double value, double place) {
        if (!Double.isFinite(value) || !Double.isFinite(place) || place <= 0.0D) {
            return value;
        }

        double units = Math.floor(value / place + 1.0E-9D);
        return units * place;
    }

    private static double clampMultiplier(double value) {
        if (!Double.isFinite(value)) {
            return FlySpeedModifierConfig.minSpeed();
        }

        double min = FlySpeedModifierConfig.minSpeed();
        double max = FlySpeedModifierConfig.maxSpeed();

        if (value < min) {
            return min;
        }

        if (value > max) {
            return max;
        }

        return value;
    }

    private static void showMultiplierOverlay(SpeedTarget target, double multiplier) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        String label =
                target == SpeedTarget.FREECAM
                        ? "Freecam speed multiplier"
                        : "Flight speed multiplier";
        String text = String.format(Locale.ROOT, "%s: %.2fx", label, multiplier);
        client.player.sendOverlayMessage(Component.literal(text));
    }
}
