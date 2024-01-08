package mom.blahaj.splashedvd;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Calendar;

@Mixin(SplashTextRenderer.class)
public class SplashTextMixin {
    @Final
    @Shadow
    private String text;

    @Unique
    int x = 0;
    @Unique
    int y = 0;

    @Unique
    private static int velocityX = 1;
    @Unique
    private static int velocityY = 1;

    @Unique
    long lastMillis = System.currentTimeMillis();

    @Unique
    String resolved;

    @Unique
    private String getText() {
        // This is a really weird work-around to the lack of a constructor. I'm too tired to fix it though.
        if (resolved == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            // It's splash-text related. I feel obligated to have an Easter-egg here.
            resolved = calendar.get(Calendar.MONTH) == Calendar.JUNE && calendar.get(Calendar.DAY_OF_MONTH) == 12 ? "happy birthday, sylvxa!" : text;
        }
        return resolved;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(DrawContext context, int screenWidth, TextRenderer textRenderer, int alpha, CallbackInfo ci) {
        ci.cancel();
        // Scaling
        int charLength = textRenderer.getWidth(getText());
        float scale = 1.8F * (100 / (float) (charLength + 32));

        // "Ticking" code
        if ((System.currentTimeMillis() - lastMillis) > 1000/60) { // Only tick at 60fps to keep things consistent.
            int textWidth = (int) (charLength * scale);
            int textHeight = (int) (textRenderer.fontHeight * scale);

            float scaledX = x * scale;
            float scaledY = y * scale;
            x += velocityX;
            y += velocityY;

            // Collision
            if (scaledX < 0) velocityX = 1; // Left wall bounce
            if (scaledX + textWidth > screenWidth) velocityX = -1; // Right wall bounce
            if (scaledY < 0) velocityY = 1; // Top wall bounce
            if (scaledY + textHeight > context.getScaledWindowHeight()) velocityY = -1; // Bottom wall bounce

            lastMillis = System.currentTimeMillis();
        }


        // Scale and render.
        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, scale);
        context.drawTextWithShadow(textRenderer, getText(), x, y, 16776960 | alpha);
        context.getMatrices().pop();
    }
}
