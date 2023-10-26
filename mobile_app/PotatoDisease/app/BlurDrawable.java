import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.graphics.Bitmap;

public class BlurDrawable extends BitmapDrawable {
    public BlurDrawable(Context context, Bitmap bitmap, float blurRadius) {
        super(context.getResources(), blurBitmap(bitmap, context, blurRadius));
    }

    private static Bitmap blurBitmap(Bitmap bitmap, Context context, float blurRadius) {
        Bitmap blurredBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        Allocation input = Allocation.createFromBitmap(rs, bitmap);
        Allocation output = Allocation.createFromBitmap(rs, blurredBitmap);

        script.setRadius(blurRadius);
        script.setInput(input);
        script.forEach(output);

        output.copyTo(blurredBitmap);

        rs.destroy();

        return blurredBitmap;
    }
}
