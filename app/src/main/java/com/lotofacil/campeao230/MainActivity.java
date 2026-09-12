package com.lotofacil.campeao230;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainActivity extends Activity {

    private static final int PICK_TXT = 1001;
    private static final int PURPLE = Color.rgb(80, 24, 145);
    private static final int PURPLE_DARK = Color.rgb(55, 12, 104);
    private static final int PURPLE_LIGHT = Color.rgb(246, 239, 252);
    private static final int GREEN = Color.rgb(46,125,50);
    private static final int RED = Color.rgb(198,40,40);

    private Button btnLoad, btnFixed, btnAlt, btnPdf;
    private TextView status, progressText, detail, output, currentFilter;
    private ProgressBar progress;

    private EditText edRep, edEven, edOdd, edPrimes, edMagics, edBorder, edSumMin, edSumMax;

    private MotorCore.BaseData base;
    private MotorCore.Result result;
    private long startMs;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
    }

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, float sp) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(Color.rgb(35,25,40));
        return t;
    }

    private GradientDrawable rounded(int fill, int stroke, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radiusDp));
        if (stroke != 0) g.setStroke(dp(1), stroke);
        return g;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(16);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setBackgroundResource(R.drawable.bg_button);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(58));
        lp.setMargins(0, dp(6), 0, dp(6));
        b.setLayoutParams(lp);
        return b;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(14), dp(14), dp(14), dp(14));
        c.setBackground(rounded(Color.WHITE, Color.rgb(220,202,235), 16));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(8), 0, dp(8));
        c.setLayoutParams(lp);
        return c;
    }

    private EditText field(String label, String value) {
        EditText e = new EditText(this);
        e.setHint(label);
        e.setText(value);
        e.setTextSize(15);
        e.setSingleLine(true);
        e.setInputType(InputType.TYPE_CLASS_TEXT);
        e.setPadding(dp(10), dp(8), dp(10), dp(8));
        e.setBackground(rounded(Color.WHITE, Color.rgb(190,160,215), 10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(52), 1f);
        lp.setMargins(dp(3), dp(4), dp(3), dp(4));
        e.setLayoutParams(lp);
        return e;
    }

    private LinearLayout row(EditText a, EditText b) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.addView(a);
        r.addView(b);
        return r;
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PURPLE_LIGHT);
        scroll.addView(root);

        FrameLayout hero = new FrameLayout(this);
        hero.setBackgroundColor(PURPLE);
        hero.setPadding(dp(12), dp(18), dp(12), dp(18));
        root.addView(hero, new LinearLayout.LayoutParams(-1, dp(205)));

        ImageView clover = new ImageView(this);
        clover.setImageResource(R.drawable.ic_clover_white);
        clover.setAlpha(0.13f);
        clover.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        FrameLayout.LayoutParams cip = new FrameLayout.LayoutParams(dp(175), dp(175), Gravity.CENTER);
        hero.addView(clover, cip);

        TextView title = text("LOTO FÁCIL CAMPEÃO\n230 GRUPOS DE 22", 27);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setShadowLayer(3, 0, 2, PURPLE_DARK);
        hero.addView(title, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(13), dp(12), dp(13), dp(30));
        root.addView(body);

        TextView subtitle = text("230 grupos • grupo mais atrasado em 15 • 170.544 combinações • mapa forte/falha", 14);
        subtitle.setTextColor(PURPLE_DARK);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setTypeface(Typeface.DEFAULT_BOLD);
        subtitle.setPadding(0, dp(2), 0, dp(8));
        body.addView(subtitle);

        btnLoad = button("1. CARREGAR TXT DA LOTOFÁCIL");
        body.addView(btnLoad);

        LinearLayout fixedCard = card();
        TextView ftitle = text("FILTRO FIXO", 19);
        ftitle.setTextColor(PURPLE_DARK);
        ftitle.setTypeface(Typeface.DEFAULT_BOLD);
        fixedCard.addView(ftitle);
        TextView fdesc = text("9 repetidas • 7 pares / 8 ímpares • primos 5–6 • mágicos 5–6 • moldura 10 • soma 201–204", 14);
        fdesc.setPadding(0, dp(7), 0, dp(6));
        fixedCard.addView(fdesc);
        btnFixed = button("GERAR JOGO — FILTRO FIXO");
        btnFixed.setEnabled(false);
        fixedCard.addView(btnFixed);
        body.addView(fixedCard);

        LinearLayout altCard = card();
        altCard.setBackground(rounded(Color.rgb(251,248,254), Color.rgb(176,135,208), 16));
        TextView atitle = text("FILTRO ALTERNATIVO", 19);
        atitle.setTextColor(PURPLE_DARK);
        atitle.setTypeface(Typeface.DEFAULT_BOLD);
        altCard.addView(atitle);
        TextView adesc = text("Monte o padrão que você quiser. Em primos e mágicos pode digitar 6 ou uma faixa como 5-6.", 13);
        adesc.setPadding(0, dp(5), 0, dp(8));
        altCard.addView(adesc);

        edRep = field("Repetidas", "8");
        edEven = field("Pares", "8");
        edOdd = field("Ímpares", "7");
        edPrimes = field("Primos", "6");
        edMagics = field("Mágicos", "5");
        edBorder = field("Moldura", "9");
        edSumMin = field("Soma mínima", "192");
        edSumMax = field("Soma máxima", "210");

        altCard.addView(row(edRep, edEven));
        altCard.addView(row(edOdd, edPrimes));
        altCard.addView(row(edMagics, edBorder));
        altCard.addView(row(edSumMin, edSumMax));

        btnAlt = button("GERAR JOGO — FILTRO ALTERNATIVO");
        btnAlt.setEnabled(false);
        altCard.addView(btnAlt);
        body.addView(altCard);

        currentFilter = text("Filtro atual: nenhum", 14);
        currentFilter.setTextColor(PURPLE_DARK);
        currentFilter.setTypeface(Typeface.DEFAULT_BOLD);
        currentFilter.setPadding(dp(12), dp(10), dp(12), dp(10));
        currentFilter.setBackground(rounded(Color.rgb(238,226,248), Color.rgb(205,177,226), 12));
        body.addView(currentFilter);

        btnPdf = button("GERAR PDF DO ÚLTIMO JOGO");
        btnPdf.setEnabled(false);
        body.addView(btnPdf);

        status = text("Aguardando base histórica...", 15);
        status.setPadding(dp(14), dp(14), dp(14), dp(14));
        status.setBackground(rounded(Color.WHITE, Color.rgb(220,202,235), 14));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.setMargins(0, dp(9), 0, dp(8));
        body.addView(status, sp);

        progressText = text("0%", 22);
        progressText.setGravity(Gravity.CENTER);
        progressText.setTextColor(PURPLE);
        progressText.setTypeface(Typeface.DEFAULT_BOLD);
        body.addView(progressText);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgress(0);
        if (Build.VERSION.SDK_INT >= 21) progress.setProgressTintList(ColorStateList.valueOf(PURPLE));
        body.addView(progress, new LinearLayout.LayoutParams(-1, dp(18)));

        detail = text("ETAPA: aguardando\nPROCESSAMENTO: 0 / 0\nTEMPO: 0 s", 14);
        detail.setTypeface(Typeface.MONOSPACE);
        detail.setPadding(0, dp(8), 0, dp(8));
        body.addView(detail);

        output = text("", 16);
        output.setTypeface(Typeface.MONOSPACE);
        output.setTextIsSelectable(true);
        output.setPadding(0, dp(12), 0, dp(20));
        body.addView(output);

        btnLoad.setOnClickListener(v -> openTxt());
        btnFixed.setOnClickListener(v -> runMotor(MotorCore.FilterConfig.fixed()));
        btnAlt.setOnClickListener(v -> {
            try {
                runMotor(readAlternativeFilter());
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        btnPdf.setOnClickListener(v -> generatePdf());

        setContentView(scroll);
    }

    private MotorCore.FilterConfig readAlternativeFilter() {
        int rep = parseInt(edRep, "Repetidas");
        int even = parseInt(edEven, "Pares");
        int odd = parseInt(edOdd, "Ímpares");
        int[] primes = parseRange(edPrimes.getText().toString(), "Primos");
        int[] magics = parseRange(edMagics.getText().toString(), "Mágicos");
        int border = parseInt(edBorder, "Moldura");
        int sumMin = parseInt(edSumMin, "Soma mínima");
        int sumMax = parseInt(edSumMax, "Soma máxima");

        if (even + odd != 15) throw new IllegalArgumentException("Pares + ímpares precisa dar 15.");
        if (rep < 0 || rep > 15) throw new IllegalArgumentException("Repetidas deve ficar entre 0 e 15.");
        if (border < 0 || border > 15) throw new IllegalArgumentException("Moldura deve ficar entre 0 e 15.");
        if (sumMin > sumMax) throw new IllegalArgumentException("Soma mínima não pode ser maior que a soma máxima.");
        if (primes[0] < 0 || primes[1] > 15 || magics[0] < 0 || magics[1] > 15)
            throw new IllegalArgumentException("Primos/Mágicos fora da faixa válida.");

        return MotorCore.FilterConfig.custom(
                "FILTRO ALTERNATIVO", rep, even, odd,
                primes[0], primes[1], magics[0], magics[1],
                border, sumMin, sumMax
        );
    }

    private int parseInt(EditText e, String name) {
        String s = e.getText().toString().trim();
        if (s.isEmpty()) throw new IllegalArgumentException("Preencha: " + name);
        try { return Integer.parseInt(s); }
        catch (Exception ex) { throw new IllegalArgumentException("Valor inválido em " + name); }
    }

    private int[] parseRange(String raw, String name) {
        String s = raw.trim().replace(" ", "").replace("..", "-");
        if (s.isEmpty()) throw new IllegalArgumentException("Preencha: " + name);
        try {
            if (s.contains("-")) {
                String[] p = s.split("-", 2);
                int a = Integer.parseInt(p[0]);
                int b = Integer.parseInt(p[1]);
                if (a > b) { int t = a; a = b; b = t; }
                return new int[]{a,b};
            }
            int v = Integer.parseInt(s);
            return new int[]{v,v};
        } catch (Exception ex) {
            throw new IllegalArgumentException(name + ": use um número (6) ou faixa (5-6).");
        }
    }

    private void openTxt() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(i, PICK_TXT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_TXT || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            String txt = readAll(uri);
            base = MotorCore.parseText(txt);
            result = null;
            btnFixed.setEnabled(true);
            btnAlt.setEnabled(true);
            btnPdf.setEnabled(false);
            progress.setProgress(0);
            progressText.setText("0%");
            output.setText("");
            currentFilter.setText("Filtro atual: nenhum jogo calculado");
            status.setText(
                    "BASE CARREGADA\n" +
                    "Concursos: " + base.draws.size() + "\n" +
                    "Último concurso: " + base.contests.get(base.contests.size()-1) + "\n" +
                    "Último resultado: " + MotorCore.fmt(base.last) + "\n\n" +
                    "Motor preservado: 230 grupos → grupo mais atrasado → 170.544 → projeção de falha"
            );
        } catch (Exception e) {
            status.setText("ERRO AO CARREGAR: " + e.getMessage());
            btnFixed.setEnabled(false);
            btnAlt.setEnabled(false);
        }
    }

    private String readAll(Uri uri) throws IOException {
        InputStream in = getContentResolver().openInputStream(uri);
        if (in == null) throw new IOException("Arquivo não pôde ser aberto.");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) >= 0) out.write(buf, 0, n);
        in.close();
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    private void runMotor(MotorCore.FilterConfig filter) {
        if (base == null) return;

        btnLoad.setEnabled(false);
        btnFixed.setEnabled(false);
        btnAlt.setEnabled(false);
        btnPdf.setEnabled(false);
        result = null;
        startMs = System.currentTimeMillis();
        progress.setProgress(0);
        progressText.setText("0%");
        output.setText("");
        currentFilter.setText(filter.label + " • " + filter.summary());
        status.setText("Executando motor original com " + filter.label.toLowerCase(Locale.ROOT) + "...");

        new Thread(() -> {
            try {
                MotorCore.Result r = MotorCore.run(base, filter, (stage, done, total, overall, extra) -> {
                    long sec = (System.currentTimeMillis() - startMs) / 1000;
                    runOnUiThread(() -> {
                        progress.setProgress(overall);
                        progressText.setText(overall + "%");
                        detail.setText(
                                "ETAPA: " + stage + "\n" +
                                "PROCESSAMENTO: " + done + " / " + total + "\n" +
                                "TEMPO: " + sec + " s\n" +
                                extra
                        );
                    });
                });

                result = r;
                runOnUiThread(() -> {
                    btnLoad.setEnabled(true);
                    btnFixed.setEnabled(true);
                    btnAlt.setEnabled(true);
                    progress.setProgress(100);
                    progressText.setText("100%");
                    if (r.best != null) {
                        btnPdf.setEnabled(true);
                        status.setText("CONCLUÍDO — " + r.filter.label + " encontrou jogo.");
                    } else {
                        btnPdf.setEnabled(false);
                        status.setText("CONCLUÍDO — grupo vencedor encontrado, mas nenhum dos 170.544 jogos passou nesse filtro.");
                    }
                    output.setText(MotorCore.report(r));
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnLoad.setEnabled(true);
                    btnFixed.setEnabled(true);
                    btnAlt.setEnabled(true);
                    status.setText("ERRO NO MOTOR: " + e.getMessage());
                });
            }
        }).start();
    }

    private void generatePdf() {
        if (result == null || result.best == null) return;
        try {
            String suffix = result.filter != null && result.filter.label.contains("ALTERNATIVO") ? "ALTERNATIVO" : "FIXO";
            String name = "LOTOFACIL_CAMPEAO_230_" + suffix + "_CONCURSO_" +
                    (result.base.contests.get(result.base.contests.size()-1) + 1) + ".pdf";

            PdfDocument doc = new PdfDocument();
            PdfDocument.PageInfo pi = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = doc.startPage(pi);
            Canvas c = page.getCanvas();
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

            p.setColor(PURPLE);
            c.drawRect(0, 0, 595, 92, p);
            p.setColor(Color.WHITE);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            p.setTextSize(20);
            c.drawText("LOTO FÁCIL CAMPEÃO - 230 GRUPOS DE 22", 28, 34, p);
            p.setTextSize(13);
            c.drawText(result.filter.label, 28, 57, p);
            p.setTypeface(Typeface.DEFAULT);
            p.setTextSize(10);
            c.drawText(result.filter.summary(), 28, 77, p);

            MotorCore.Candidate best = result.best;
            MotorCore.GroupInfo g = result.winner;

            p.setColor(Color.BLACK);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            p.setTextSize(12);
            c.drawText("Grupo 22 vencedor:", 35, 116, p);
            p.setTypeface(Typeface.DEFAULT);
            p.setTextSize(10.5f);
            c.drawText(MotorCore.fmt(g.group22), 35, 134, p);
            c.drawText("Fora do grupo: " + MotorCore.fmt(g.outside3) +
                    " | atraso sem 15: " + g.currentDelay +
                    " | vezes 15: " + g.times15, 35, 153, p);

            p.setTypeface(Typeface.DEFAULT_BOLD);
            p.setTextSize(14);
            c.drawText("VOLANTE 01-25", 35, 180, p);

            HashSet<Integer> game = new HashSet<>();
            for (int n : best.game) game.add(n);

            float left = 48, top = 194, cw = 96, ch = 49, gap = 4;
            p.setTextAlign(Paint.Align.CENTER);
            for (int n = 1; n <= 25; n++) {
                int row = (n - 1) / 5;
                int col = (n - 1) % 5;
                float x = left + col * (cw + gap);
                float y = top + row * (ch + gap);
                p.setColor(game.contains(n) ? GREEN : RED);
                RectF box = new RectF(x, y, x + cw, y + ch);
                c.drawRoundRect(box, 8, 8, p);
                p.setColor(Color.WHITE);
                p.setTypeface(Typeface.DEFAULT_BOLD);
                p.setTextSize(19);
                c.drawText(String.format(Locale.US, "%02d", n), x + cw/2, y + 31, p);
            }
            p.setTextAlign(Paint.Align.LEFT);

            float y = 480;
            p.setColor(Color.BLACK);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            p.setTextSize(13);
            c.drawText("JOGO FINAL — VERDE", 35, y, p);
            y += 21;
            p.setColor(GREEN);
            p.setTextSize(16);
            c.drawText(MotorCore.fmt(best.game), 35, y, p);

            y += 31;
            p.setColor(Color.BLACK);
            p.setTextSize(13);
            c.drawText("10 FALHAS PROJETADAS — VERMELHO", 35, y, p);
            y += 21;
            p.setColor(RED);
            p.setTextSize(15);
            c.drawText(MotorCore.fmt(best.failures10), 35, y, p);

            y += 32;
            p.setColor(Color.BLACK);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            p.setTextSize(12.5f);
            c.drawText("RESUMO DO PADRÃO", 35, y, p);
            p.setTypeface(Typeface.DEFAULT);
            p.setTextSize(11.5f);
            y += 19;
            c.drawText("Repetidas: " + best.metrics.rep +
                    " | Pares/Ímpares: " + best.metrics.even + "/" + best.metrics.odd +
                    " | Primos: " + best.metrics.primes, 35, y, p);
            y += 18;
            c.drawText("Mágicos: " + best.metrics.magics +
                    " | Moldura: " + best.metrics.border +
                    " | Soma: " + best.metrics.sum, 35, y, p);
            y += 18;
            c.drawText("Falha média: " + String.format(Locale.US, "%.4f", best.failureMean) +
                    " | Falha mínima: " + String.format(Locale.US, "%.4f", best.failureMin), 35, y, p);
            y += 18;
            c.drawText("Força das 15: " + String.format(Locale.US, "%.4f", best.gameStrength) +
                    " | Talo: " + String.format(Locale.US, "%.4f", best.stalk), 35, y, p);
            y += 18;
            c.drawText("Gerados: " + result.totalGenerated +
                    " | Classificados: " + result.totalClassified +
                    " | Base: " + result.base.draws.size(), 35, y, p);

            p.setColor(Color.DKGRAY);
            p.setTextSize(9);
            c.drawText("Estudo estatístico - não há garantia de premiação.", 35, 815, p);

            doc.finishPage(page);
            savePdfDocument(doc, name);
            doc.close();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao gerar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void savePdfDocument(PdfDocument doc, String name) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.Downloads.DISPLAY_NAME, name);
            cv.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            cv.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
            if (uri == null) throw new IOException("Não foi possível criar o PDF em Downloads.");
            OutputStream os = getContentResolver().openOutputStream(uri);
            if (os == null) throw new IOException("Não foi possível abrir o PDF para gravação.");
            doc.writeTo(os);
            os.close();
            Toast.makeText(this, "PDF salvo em Download/" + name, Toast.LENGTH_LONG).show();
        } else {
            File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (dir == null) throw new IOException("Pasta de downloads indisponível.");
            File f = new File(dir, name);
            FileOutputStream os = new FileOutputStream(f);
            doc.writeTo(os);
            os.close();
            Toast.makeText(this, "PDF salvo em " + f.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }
}
