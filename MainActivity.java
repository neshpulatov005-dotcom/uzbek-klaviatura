package uz.onlinefizika.klaviatura;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        float d = getResources().getDisplayMetrics().density;
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding((int)(20*d), (int)(40*d), (int)(20*d), (int)(20*d));
        TextView t = new TextView(this);
        t.setText("Oʻzbek klaviatura\nÖ  Ğ  Ş  Ç");
        t.setTextSize(24);
        l.addView(t);
        TextView s = new TextView(this);
        s.setTextSize(16);
        s.setPadding(0, (int)(16*d), 0, (int)(16*d));
        s.setText("1. «Klaviaturani yoqish» tugmasini bosing va roʻyxatdan “Oʻzbek (Ö Ğ Ş Ç)” ni yoqing.\n\n2. «Klaviaturani tanlash» tugmasini bosib, uni faol qiling.\n\n3. Istalgan dasturda yozayotganda 🌐 tugmasi bilan klaviaturalar orasida almashtiring.");
        l.addView(s);
        Button b1 = new Button(this);
        b1.setText("Klaviaturani yoqish");
        b1.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
        l.addView(b1);
        Button b2 = new Button(this);
        b2.setText("Klaviaturani tanlash");
        b2.setOnClickListener(v -> ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker());
        l.addView(b2);
        EditText e = new EditText(this);
        e.setHint("Sinab koʻrish uchun shu yerga yozing");
        l.addView(e);
        setContentView(l);
    }
}
