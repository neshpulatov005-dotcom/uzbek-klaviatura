package uz.onlinefizika.klaviatura;

import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UzKeyboardService extends InputMethodService {
    private boolean shift = false, lock = false;
    private long lastShift = 0;
    private final List<TextView> letters = new ArrayList<>();
    private TextView shiftKey;
    private int cBg, cKey, cFn, cInk, cNew, cNewInk, cOn;
    private float d;
    private final Handler h = new Handler(Looper.getMainLooper());

    private int dp(int v) { return (int) (v * d + 0.5f); }

    @Override public View onCreateInputView() {
        d = getResources().getDisplayMetrics().density;
        boolean night = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (night) { cBg = 0xFF0F161C; cKey = 0xFF2A3742; cFn = 0xFF1F2A33; cInk = 0xFFEAF0F5; cNew = 0xFF2BB3DF; cNewInk = 0xFF06222C; cOn = 0xFFEAF0F5; }
        else { cBg = 0xFFE9EEF2; cKey = 0xFFFFFFFF; cFn = 0xFFCFD8DF; cInk = 0xFF14202B; cNew = 0xFF0A7FA6; cNewInk = 0xFFFFFFFF; cOn = 0xFF14202B; }
        letters.clear();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(cBg);
        root.setPadding(dp(3), dp(4), dp(3), dp(10));

        LinearLayout r0 = row(root);
        for (String c : new String[]{"ö", "ğ", "ş", "ç"}) letter(r0, c, true);

        LinearLayout r1 = row(root);
        for (char c : "1234567890".toCharArray()) plain(r1, String.valueOf(c), 1f, String.valueOf(c));

        LinearLayout r2 = row(root);
        for (char c : "qwertyuiop".toCharArray()) letter(r2, String.valueOf(c), false);

        LinearLayout r3 = row(root);
        r3.addView(spacer(0.5f));
        for (char c : "asdfghjkl".toCharArray()) letter(r3, String.valueOf(c), false);
        r3.addView(spacer(0.5f));

        LinearLayout r4 = row(root);
        shiftKey = fn(r4, "⇧", 1.4f);
        shiftKey.setOnClickListener(v -> {
            long now = SystemClock.uptimeMillis();
            if (shift && !lock && now - lastShift < 350) lock = true;
            else if (shift || lock) { shift = false; lock = false; }
            else shift = true;
            lastShift = now;
            refresh();
        });
        for (char c : "zxcvbnm".toCharArray()) letter(r4, String.valueOf(c), false);
        TextView del = fn(r4, "⌫", 1.4f);
        del.setOnTouchListener((v, e) -> {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setAlpha(0.6f); v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    deleteOne(); h.postDelayed(repeat, 400); return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1f); h.removeCallbacks(repeat); afterCommit(); return true;
            }
            return true;
        });

        LinearLayout r5 = row(root);
        TextView globe = fn(r5, "🌐", 1.2f);
        globe.setOnClickListener(v -> ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker());
        plain(r5, "ʻ", 1f, "ʻ");
        plain(r5, ",", 1f, ",");
        TextView sp = plain(r5, "Oʻzbek", 4f, " ");
        sp.setTextSize(14);
        plain(r5, ".", 1f, ".");
        TextView enter = fn(r5, "↵", 1.4f);
        enter.setOnClickListener(v -> enter());
        refresh();
        return root;
    }

    private final Runnable repeat = new Runnable() {
        @Override public void run() { deleteOne(); h.postDelayed(this, 50); }
    };

    @Override public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        if (!lock) autoCaps();
        refresh();
    }

    private void autoCaps() {
        InputConnection ic = getCurrentInputConnection();
        EditorInfo ei = getCurrentInputEditorInfo();
        shift = ic != null && ei != null && ei.inputType != 0 && ic.getCursorCapsMode(ei.inputType) != 0;
    }

    private void afterCommit() { if (!lock) { autoCaps(); } refresh(); }

    private void type(String s) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.commitText(s, 1);
        afterCommit();
    }

    private void deleteOne() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        CharSequence sel = ic.getSelectedText(0);
        if (sel != null && sel.length() > 0) ic.commitText("", 1);
        else ic.deleteSurroundingText(1, 0);
    }

    private void enter() {
        EditorInfo ei = getCurrentInputEditorInfo();
        InputConnection ic = getCurrentInputConnection();
        if (ei != null && ic != null) {
            int a = ei.imeOptions & EditorInfo.IME_MASK_ACTION;
            if ((ei.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) == 0 && a != EditorInfo.IME_ACTION_NONE && a != EditorInfo.IME_ACTION_UNSPECIFIED) {
                ic.performEditorAction(a); return;
            }
        }
        sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
    }

    private void refresh() {
        for (TextView t : letters) {
            String c = (String) t.getTag();
            t.setText(shift ? c.toUpperCase(Locale.ROOT) : c);
        }
        if (shiftKey != null) {
            boolean on = shift || lock;
            ((GradientDrawable) shiftKey.getBackground()).setColor(lock ? 0xFFC8323C : on ? cOn : cFn);
            shiftKey.setTextColor(on ? (lock ? 0xFFFFFFFF : cBg) : cInk);
        }
    }

    private LinearLayout row(LinearLayout root) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER);
        root.addView(r, new LinearLayout.LayoutParams(-1, -2));
        return r;
    }

    private View spacer(float w) { return new View(this) {{ setLayoutParams(new LinearLayout.LayoutParams(0, 1, w)); }}; }

    private TextView key(LinearLayout r, String label, int bg, int fg, float w, int hDp) {
        TextView t = new TextView(this);
        t.setText(label); t.setGravity(Gravity.CENTER); t.setTextColor(fg); t.setTextSize(20);
        GradientDrawable g = new GradientDrawable(); g.setColor(bg); g.setCornerRadius(dp(9));
        t.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(hDp), w);
        lp.setMargins(dp(3), dp(3), dp(3), dp(3));
        t.setLayoutParams(lp);
        t.setClickable(true);
        t.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN) { v.setAlpha(0.6f); v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); }
            else if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL) v.setAlpha(1f);
            return false;
        });
        r.addView(t);
        return t;
    }

    private void letter(LinearLayout r, String ch, boolean special) {
        TextView t = key(r, ch, special ? cNew : cKey, special ? cNewInk : cInk, 1f, special ? 52 : 46);
        if (special) t.setTextSize(26);
        t.setTag(ch);
        t.setOnClickListener(v -> type(shift ? ch.toUpperCase(Locale.ROOT) : ch));
        letters.add(t);
    }

    private TextView plain(LinearLayout r, String label, float w, String out) {
        TextView t = key(r, label, cKey, cInk, w, 46);
        t.setOnClickListener(v -> type(out));
        return t;
    }

    private TextView fn(LinearLayout r, String label, float w) {
        TextView t = key(r, label, cFn, cInk, w, 46);
        t.setTextSize(18);
        return t;
    }
}
