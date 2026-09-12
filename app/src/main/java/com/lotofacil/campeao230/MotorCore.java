package com.lotofacil.campeao230;

import java.util.*;
import java.util.regex.*;

/**
 * Port direto do MOTOR_REFERENCIA_PYDROID_V2.py.
 * Mantém o mesmo estudo dos 230 grupos, mapa forte/falha, projeção e desempates.
 * V2 acrescenta somente uma configuração de filtro: FIXO ou ALTERNATIVO.
 */
public final class MotorCore {

    public static final int REPETIDAS_EXATAS = 9;
    public static final int PARES_EXATOS = 7;
    public static final int PRIMOS_MIN = 5;
    public static final int PRIMOS_MAX = 6;
    public static final int MAGICOS_MIN = 5;
    public static final int MAGICOS_MAX = 6;
    public static final int MOLDURA_EXATA = 10;
    public static final int SOMA_MIN = 201;
    public static final int SOMA_MAX = 204;

    public static final class FilterConfig {
        public String label;
        public int repeatedExact;
        public int evenExact;
        public int oddExact;
        public int primesMin, primesMax;
        public int magicsMin, magicsMax;
        public int borderExact;
        public int sumMin, sumMax;

        public static FilterConfig fixed() {
            FilterConfig f = new FilterConfig();
            f.label = "FILTRO FIXO";
            f.repeatedExact = REPETIDAS_EXATAS;
            f.evenExact = PARES_EXATOS;
            f.oddExact = 8;
            f.primesMin = PRIMOS_MIN;
            f.primesMax = PRIMOS_MAX;
            f.magicsMin = MAGICOS_MIN;
            f.magicsMax = MAGICOS_MAX;
            f.borderExact = MOLDURA_EXATA;
            f.sumMin = SOMA_MIN;
            f.sumMax = SOMA_MAX;
            return f;
        }

        public static FilterConfig custom(String label, int repeated, int even, int odd,
                                          int primesMin, int primesMax,
                                          int magicsMin, int magicsMax,
                                          int border, int sumMin, int sumMax) {
            FilterConfig f = new FilterConfig();
            f.label = label == null ? "FILTRO ALTERNATIVO" : label;
            f.repeatedExact = repeated;
            f.evenExact = even;
            f.oddExact = odd;
            f.primesMin = primesMin;
            f.primesMax = primesMax;
            f.magicsMin = magicsMin;
            f.magicsMax = magicsMax;
            f.borderExact = border;
            f.sumMin = sumMin;
            f.sumMax = sumMax;
            return f;
        }

        public String summary() {
            return "rep=" + repeatedExact +
                    " | P/I=" + evenExact + "/" + oddExact +
                    " | primos=" + range(primesMin, primesMax) +
                    " | mágicos=" + range(magicsMin, magicsMax) +
                    " | moldura=" + borderExact +
                    " | soma=" + sumMin + ".." + sumMax;
        }

        private static String range(int a, int b) {
            return a == b ? String.valueOf(a) : (a + ".." + b);
        }
    }

    private static final int UNIVERSE_MASK = (1 << 25) - 1;

    private static final boolean[] PRIMOS = boolSet(2,3,5,7,11,13,17,19,23);
    private static final boolean[] MAGICOS = boolSet(5,6,7,12,13,14,19,20,21);
    private static final boolean[] MOLDURA = boolSet(1,2,3,4,5,6,10,11,15,16,20,21,22,23,24,25);

    private MotorCore() {}

    private static boolean[] boolSet(int... values) {
        boolean[] a = new boolean[26];
        for (int v : values) a[v] = true;
        return a;
    }

    public interface ProgressCallback {
        void update(String stage, int done, int total, int overallPercent, String detail);
    }

    public static final class BaseData {
        public final List<Integer> contests;
        public final List<int[]> draws;
        public final int[] drawMasks;
        public final int[] last;

        BaseData(List<Integer> contests, List<int[]> draws) {
            this.contests = contests;
            this.draws = draws;
            this.drawMasks = new int[draws.size()];
            for (int i = 0; i < draws.size(); i++) this.drawMasks[i] = mask(draws.get(i));
            this.last = draws.get(draws.size() - 1).clone();
        }
    }

    public static final class GroupInfo {
        public int[] group22;
        public int[] outside3;
        public int times15;
        public Integer lastContest15;
        public int currentDelay;
        public double meanInterval;
        public int maxInterval;
        public double pressure;
    }

    public static final class NumberInfo {
        public int number;
        public double hist, r120, r60, r30, r15, r8, pond30, trend;
        public int delay, delayMax, seq, seqMax;
        public double delayMean, returnPressure, seqMean, excessSeq;
        public double strength, failure;
        public int level;
        public String clazz;
    }

    public static final class Metrics {
        public int rep, even, odd, primes, magics, border, sum;
    }

    public static final class Candidate {
        public double failureMean, failureMin, gameStrength, stalk;
        public int[] game;
        public int[] failures10;
        public Metrics metrics;
        public long order;
    }

    public static final class Result {
        public BaseData base;
        public List<GroupInfo> groupRanking;
        public GroupInfo winner;
        public NumberInfo[] map;
        public Candidate best;
        public List<Candidate> top20;
        public int totalGenerated;
        public int totalClassified;
        public FilterConfig filter;
    }

    public static BaseData parseText(String text) {
        TreeMap<Integer, int[]> results = new TreeMap<>();
        Pattern p = Pattern.compile("\\d+");
        String[] lines = text.split("\\R");

        for (String line : lines) {
            Matcher m = p.matcher(line);
            ArrayList<Integer> values = new ArrayList<>();
            while (m.find()) values.add(Integer.parseInt(m.group()));
            if (values.size() < 15) continue;

            Integer contest = null;
            int start = 0;
            if (values.size() >= 16 && values.get(0) > 25) {
                contest = values.get(0);
                start = 1;
            }

            ArrayList<Integer> nums = new ArrayList<>();
            for (int i = start; i < values.size(); i++) {
                int v = values.get(i);
                if (v >= 1 && v <= 25) nums.add(v);
            }

            if (nums.size() >= 15) {
                int[] draw = new int[15];
                HashSet<Integer> unique = new HashSet<>();
                int off = nums.size() - 15;
                for (int i = 0; i < 15; i++) {
                    draw[i] = nums.get(off + i);
                    unique.add(draw[i]);
                }
                if (unique.size() == 15) {
                    Arrays.sort(draw);
                    if (contest == null) contest = results.size() + 1;
                    results.put(contest, draw);
                }
            }
        }

        if (results.size() < 50) {
            throw new IllegalArgumentException("Poucos concursos da Lotofácil foram identificados.");
        }

        ArrayList<Integer> contests = new ArrayList<>(results.keySet());
        ArrayList<int[]> draws = new ArrayList<>();
        for (Integer c : contests) draws.add(results.get(c));
        return new BaseData(contests, draws);
    }

    public static Result run(BaseData base, ProgressCallback cb) {
        return run(base, FilterConfig.fixed(), cb);
    }

    public static Result run(BaseData base, FilterConfig filter, ProgressCallback cb) {
        Result r = new Result();
        r.base = base;
        r.filter = filter == null ? FilterConfig.fixed() : filter;

        r.groupRanking = analyzeAllGroups(base, cb);
        r.winner = r.groupRanking.get(0);

        if (cb != null) cb.update("Construindo mapa forte/falha", 0, 25, 18, "Histórico completo");
        r.map = buildMap(base, cb);

        GenerationResult gr = generateAndClassify(r.winner.group22, base, r.map, r.filter, cb);
        r.totalGenerated = gr.totalGenerated;
        r.totalClassified = gr.totalClassified;
        r.top20 = gr.top20;
        r.best = gr.top20.isEmpty() ? null : gr.top20.get(0);

        if (cb != null) cb.update("Concluído", 1, 1, 100, r.best == null ? "Nenhum jogo passou nos filtros" : "Melhor jogo encontrado");
        return r;
    }

    public static List<GroupInfo> analyzeAllGroups(BaseData base, ProgressCallback cb) {
        ArrayList<GroupInfo> ranking = new ArrayList<>();
        int pos = 0;
        int totalGroups = 230;

        for (int a = 1; a <= 23; a++) {
            for (int b = a + 1; b <= 24; b++) {
                for (int c = b + 1; c <= 25; c++) {
                    pos++;
                    int outsideMask = (1 << (a - 1)) | (1 << (b - 1)) | (1 << (c - 1));
                    int times15 = 0;
                    int lastIdx = -1;
                    int prevIdx = -1;
                    long intervalSum = 0;
                    int intervalCount = 0;
                    int maxInterval = 0;

                    for (int i = 0; i < base.drawMasks.length; i++) {
                        if ((outsideMask & base.drawMasks[i]) == 0) {
                            times15++;
                            if (prevIdx >= 0) {
                                int gap = i - prevIdx;
                                intervalSum += gap;
                                intervalCount++;
                                if (gap > maxInterval) maxInterval = gap;
                            }
                            prevIdx = i;
                            lastIdx = i;
                        }
                    }

                    GroupInfo g = new GroupInfo();
                    g.outside3 = new int[]{a,b,c};
                    g.group22 = complement3(a,b,c);
                    g.times15 = times15;

                    if (lastIdx >= 0) {
                        g.lastContest15 = base.contests.get(lastIdx);
                        g.currentDelay = (base.draws.size() - 1) - lastIdx;
                        if (intervalCount > 0) {
                            g.meanInterval = intervalSum / (double) intervalCount;
                            g.maxInterval = maxInterval;
                        } else {
                            g.meanInterval = base.draws.size();
                            g.maxInterval = base.draws.size();
                        }
                    } else {
                        g.lastContest15 = null;
                        g.currentDelay = base.draws.size();
                        g.meanInterval = base.draws.size();
                        g.maxInterval = base.draws.size();
                    }

                    g.pressure = g.currentDelay / Math.max(1.0, g.meanInterval);
                    ranking.add(g);

                    if (cb != null && (pos == 1 || pos % 5 == 0 || pos == totalGroups)) {
                        int overall = (int)Math.round(15.0 * pos / totalGroups);
                        cb.update("Analisando os 230 grupos de 22", pos, totalGroups, overall,
                                "Maior atraso atual sem 15 pontos");
                    }
                }
            }
        }

        ranking.sort((x,y) -> {
            int z = Integer.compare(y.currentDelay, x.currentDelay);
            if (z != 0) return z;
            z = Double.compare(y.pressure, x.pressure);
            if (z != 0) return z;
            return Integer.compare(y.times15, x.times15);
        });
        return ranking;
    }

    private static int[] complement3(int a, int b, int c) {
        int[] out = new int[22];
        int k = 0;
        for (int n = 1; n <= 25; n++) if (n != a && n != b && n != c) out[k++] = n;
        return out;
    }

    public static NumberInfo[] buildMap(BaseData base, ProgressCallback cb) {
        int total = base.draws.size();
        NumberInfo[] data = new NumberInfo[26];

        int s120 = total - Math.min(120, total);
        int s60 = total - Math.min(60, total);
        int s30 = total - Math.min(30, total);
        int s15 = total - Math.min(15, total);
        int s8 = total - Math.min(8, total);
        int antStart = total >= 30 ? total - 30 : total;
        int antEnd = total >= 30 ? total - 15 : total;

        for (int n = 1; n <= 25; n++) {
            NumberInfo d = new NumberInfo();
            d.number = n;
            d.hist = rate(n, base.drawMasks, 0, total);
            d.r120 = rate(n, base.drawMasks, s120, total);
            d.r60 = rate(n, base.drawMasks, s60, total);
            d.r30 = rate(n, base.drawMasks, s30, total);
            d.r15 = rate(n, base.drawMasks, s15, total);
            d.r8 = rate(n, base.drawMasks, s8, total);
            d.pond30 = weightedRate(n, base.drawMasks, s30, total);
            double previous15 = total >= 30 ? rate(n, base.drawMasks, antStart, antEnd) : d.hist;
            d.trend = d.r15 - previous15;

            d.delay = currentDelay(n, base.drawMasks);
            int[] ba = delayBlocks(n, base.drawMasks);
            d.delayMean = mean(ba);
            d.delayMax = max(ba);
            d.returnPressure = d.delay / Math.max(1.0, d.delayMean);

            d.seq = currentSequence(n, base.drawMasks);
            int[] bs = sequenceBlocks(n, base.drawMasks);
            d.seqMean = mean(bs);
            d.seqMax = max(bs);
            d.excessSeq = Math.max(0.0, d.seq - Math.max(1.0, d.seqMean));
            data[n] = d;

            if (cb != null) {
                int overall = 15 + (int)Math.round(10.0 * n / 25.0);
                cb.update("Construindo mapa forte/falha", n, 25, overall, "Dezena " + String.format(Locale.US, "%02d", n));
            }
        }

        double[] nhist = minmax(data, x -> x.hist);
        double[] n120 = minmax(data, x -> x.r120);
        double[] n60 = minmax(data, x -> x.r60);
        double[] n30 = minmax(data, x -> x.r30);
        double[] n15 = minmax(data, x -> x.r15);
        double[] npond = minmax(data, x -> x.pond30);
        double[] ntend = minmax(data, x -> x.trend);
        double[] nret = minmax(data, x -> Math.min(x.returnPressure, 2.5));
        double[] nexcess = minmax(data, x -> x.excessSeq);

        for (int n = 1; n <= 25; n++) {
            NumberInfo d = data[n];
            double force = 100.0 * (
                    0.12 * nhist[n] +
                    0.12 * n120[n] +
                    0.10 * n60[n] +
                    0.16 * n30[n] +
                    0.12 * n15[n] +
                    0.13 * npond[n] +
                    0.10 * ntend[n] +
                    0.10 * nret[n] +
                    0.05 * (1.0 - nexcess[n])
            );
            double drop = 1.0 - ntend[n];
            double fail = 100.0 * (
                    0.62 * (1.0 - force / 100.0) +
                    0.18 * drop +
                    0.12 * nexcess[n] +
                    0.08 * (1.0 - nret[n])
            );
            d.strength = clamp(force, 0, 100);
            d.failure = clamp(fail, 0, 100);
        }

        ArrayList<NumberInfo> order = new ArrayList<>();
        for (int n = 1; n <= 25; n++) order.add(data[n]);
        order.sort((a,b) -> Double.compare(b.strength, a.strength));
        for (int pos = 0; pos < order.size(); pos++) {
            NumberInfo d = order.get(pos);
            int level = 10 - (int)(pos * 10.0 / 25.0);
            level = Math.max(1, Math.min(10, level));
            d.level = level;
            if (level >= 9) d.clazz = "FORTE";
            else if (level >= 7) d.clazz = "BOA";
            else if (level >= 5) d.clazz = "NEUTRA";
            else if (level >= 3) d.clazz = "FRACA";
            else d.clazz = "MUITO FRACA";
        }

        return data;
    }

    private interface ValueGetter { double get(NumberInfo x); }

    private static double[] minmax(NumberInfo[] data, ValueGetter getter) {
        double lo = Double.POSITIVE_INFINITY, hi = Double.NEGATIVE_INFINITY;
        for (int n = 1; n <= 25; n++) {
            double v = getter.get(data[n]);
            if (v < lo) lo = v;
            if (v > hi) hi = v;
        }
        double[] out = new double[26];
        if (Math.abs(hi - lo) < 1e-12) {
            for (int n = 1; n <= 25; n++) out[n] = 0.5;
        } else {
            for (int n = 1; n <= 25; n++) out[n] = (getter.get(data[n]) - lo) / (hi - lo);
        }
        return out;
    }

    private static double rate(int n, int[] masks, int start, int end) {
        if (end <= start) return 0.0;
        int bit = 1 << (n - 1), count = 0;
        for (int i = start; i < end; i++) if ((masks[i] & bit) != 0) count++;
        return count / (double)(end - start);
    }

    private static double weightedRate(int n, int[] masks, int start, int end) {
        if (end <= start) return 0.0;
        int bit = 1 << (n - 1);
        long num = 0, den = 0;
        int weight = 1;
        for (int i = start; i < end; i++, weight++) {
            den += weight;
            if ((masks[i] & bit) != 0) num += weight;
        }
        return num / (double)den;
    }

    private static int currentDelay(int n, int[] masks) {
        int bit = 1 << (n - 1), delay = 0;
        for (int i = masks.length - 1; i >= 0; i--) {
            if ((masks[i] & bit) != 0) return delay;
            delay++;
        }
        return masks.length;
    }

    private static int currentSequence(int n, int[] masks) {
        int bit = 1 << (n - 1), count = 0;
        for (int i = masks.length - 1; i >= 0; i--) {
            if ((masks[i] & bit) != 0) count++;
            else break;
        }
        return count;
    }

    private static int[] delayBlocks(int n, int[] masks) {
        int bit = 1 << (n - 1);
        ArrayList<Integer> list = new ArrayList<>();
        int cur = 0;
        for (int mask : masks) {
            if ((mask & bit) == 0) cur++;
            else {
                if (cur > 0) list.add(cur);
                cur = 0;
            }
        }
        if (cur > 0) list.add(cur);
        return toIntArray(list);
    }

    private static int[] sequenceBlocks(int n, int[] masks) {
        int bit = 1 << (n - 1);
        ArrayList<Integer> list = new ArrayList<>();
        int cur = 0;
        for (int mask : masks) {
            if ((mask & bit) != 0) cur++;
            else {
                if (cur > 0) list.add(cur);
                cur = 0;
            }
        }
        if (cur > 0) list.add(cur);
        return toIntArray(list);
    }

    private static int[] toIntArray(List<Integer> list) {
        int[] a = new int[list.size()];
        for (int i = 0; i < a.length; i++) a[i] = list.get(i);
        return a;
    }

    private static double mean(int[] a) {
        if (a.length == 0) return 0.0;
        long s = 0;
        for (int v : a) s += v;
        return s / (double)a.length;
    }

    private static int max(int[] a) {
        int m = 0;
        for (int v : a) if (v > m) m = v;
        return m;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static final class GenerationResult {
        int totalGenerated;
        int totalClassified;
        List<Candidate> top20;
    }

    private static GenerationResult generateAndClassify(int[] group22, BaseData base, NumberInfo[] map, FilterConfig filter, ProgressCallback cb) {
        final int total = nCr(22, 15);
        ArrayList<Candidate> classified = new ArrayList<>();
        long[] order = {0};
        int[] done = {0};
        int[] classifiedCount = {0};
        int[] nextPct = {0};
        int[] buf = new int[15];

        combineGroup(group22, 15, 0, 0, buf, game -> {
            done[0]++;
            Metrics m = metrics(game, base.last);
            if (passes(m, filter)) {
                Candidate c = evaluateFailure(game, map);
                c.metrics = m;
                c.order = order[0]++;
                classified.add(c);
                classifiedCount[0]++;
            }

            int pct = (int)(done[0] * 100.0 / total);
            if (cb != null && (done[0] == 1 || pct >= nextPct[0] || done[0] == total)) {
                int overall = 25 + (int)Math.round(70.0 * done[0] / total);
                cb.update("Gerando 170.544 jogos e aplicando filtros", done[0], total, overall,
                        "Classificados: " + classifiedCount[0] + " | " + filter.label);
                nextPct[0] = pct + 2;
            }
        });

        classified.sort(MotorCore::compareCandidates);

        GenerationResult gr = new GenerationResult();
        gr.totalGenerated = total;
        gr.totalClassified = classified.size();
        gr.top20 = new ArrayList<>(classified.subList(0, Math.min(20, classified.size())));
        return gr;
    }

    private interface GameConsumer { void accept(int[] game); }

    private static void combineGroup(int[] a, int k, int start, int depth, int[] buf, GameConsumer consumer) {
        if (depth == k) {
            consumer.accept(buf.clone());
            return;
        }
        int remaining = k - depth;
        for (int i = start; i <= a.length - remaining; i++) {
            buf[depth] = a[i];
            combineGroup(a, k, i + 1, depth + 1, buf, consumer);
        }
    }

    private static int compareCandidates(Candidate a, Candidate b) {
        int z = Double.compare(b.failureMean, a.failureMean);
        if (z != 0) return z;
        z = Double.compare(b.failureMin, a.failureMin);
        if (z != 0) return z;
        z = Double.compare(b.gameStrength, a.gameStrength);
        if (z != 0) return z;
        z = Double.compare(b.stalk, a.stalk);
        if (z != 0) return z;
        return Long.compare(a.order, b.order);
    }

    private static Metrics metrics(int[] game, int[] last) {
        Metrics m = new Metrics();
        int lastMask = mask(last), gameMask = mask(game);
        m.rep = Integer.bitCount(lastMask & gameMask);
        for (int n : game) {
            m.sum += n;
            if (n % 2 == 0) m.even++; else m.odd++;
            if (PRIMOS[n]) m.primes++;
            if (MAGICOS[n]) m.magics++;
            if (MOLDURA[n]) m.border++;
        }
        return m;
    }

    private static boolean passes(Metrics m, FilterConfig f) {
        return m.rep == f.repeatedExact &&
                m.even == f.evenExact &&
                m.odd == f.oddExact &&
                m.primes >= f.primesMin && m.primes <= f.primesMax &&
                m.magics >= f.magicsMin && m.magics <= f.magicsMax &&
                m.border == f.borderExact &&
                m.sum >= f.sumMin && m.sum <= f.sumMax;
    }

    private static Candidate evaluateFailure(int[] game, NumberInfo[] map) {
        Candidate c = new Candidate();
        c.game = game.clone();
        int gameMask = mask(game);
        int failuresMask = UNIVERSE_MASK & ~gameMask;
        c.failures10 = new int[10];
        int k = 0;
        double failSum = 0, failMin = Double.POSITIVE_INFINITY, strengthSum = 0, stalkSum = 0;

        for (int n = 1; n <= 25; n++) {
            if ((failuresMask & (1 << (n - 1))) != 0) {
                c.failures10[k++] = n;
                double f = map[n].failure;
                failSum += f;
                if (f < failMin) failMin = f;
            } else {
                strengthSum += map[n].strength;
                stalkSum += 55 * map[n].pond30 + 20 * map[n].r30 + 15 * Math.max(0.0, map[n].trend);
            }
        }

        c.failureMean = failSum / 10.0;
        c.failureMin = failMin;
        c.gameStrength = strengthSum / 15.0;
        c.stalk = stalkSum / 15.0;
        return c;
    }

    public static int mask(int[] nums) {
        int m = 0;
        for (int n : nums) m |= (1 << (n - 1));
        return m;
    }

    public static int nCr(int n, int r) {
        if (r < 0 || r > n) return 0;
        r = Math.min(r, n - r);
        long v = 1;
        for (int i = 1; i <= r; i++) v = v * (n - r + i) / i;
        return (int)v;
    }

    public static String fmt(int[] nums) {
        int[] c = nums.clone();
        Arrays.sort(c);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < c.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(String.format(Locale.US, "%02d", c[i]));
        }
        return sb.toString();
    }

    public static String report(Result r) {
        StringBuilder s = new StringBuilder();
        s.append("RESULTADO FINAL\n\n");
        if (r.filter != null) {
            s.append(r.filter.label).append("\n");
            s.append(r.filter.summary()).append("\n\n");
        }
        s.append("GRUPO 22 ESCOLHIDO:\n").append(fmt(r.winner.group22)).append("\n");
        s.append("3 DEZENAS FORA: ").append(fmt(r.winner.outside3)).append("\n");
        s.append("Atraso sem 15: ").append(r.winner.currentDelay).append(" concurso(s)\n");
        s.append("Último 15 do grupo: ").append(r.winner.lastContest15 == null ? "NUNCA" : r.winner.lastContest15).append("\n");
        s.append("Vezes 15: ").append(r.winner.times15).append(" | média intervalo: ")
                .append(String.format(Locale.US, "%.2f", r.winner.meanInterval))
                .append(" | pressão: ").append(String.format(Locale.US, "%.3f", r.winner.pressure)).append("\n\n");

        if (r.best == null) {
            s.append("Nenhum jogo passou simultaneamente nos filtros rígidos.\n");
            return s.toString();
        }

        Candidate c = r.best;
        s.append("MELHOR JOGO DE 15:\n>>> ").append(fmt(c.game)).append(" <<<\n\n");
        s.append("10 FALHAS PROJETADAS:\n>>> ").append(fmt(c.failures10)).append(" <<<\n\n");
        s.append("Projeção média de falha: ").append(String.format(Locale.US, "%.4f", c.failureMean)).append("\n");
        s.append("Menor score nas 10 falhas: ").append(String.format(Locale.US, "%.4f", c.failureMin)).append("\n");
        s.append("Força média das 15: ").append(String.format(Locale.US, "%.4f", c.gameStrength)).append("\n");
        s.append("Talo médio: ").append(String.format(Locale.US, "%.4f", c.stalk)).append("\n\n");
        s.append("PADRÃO DO JOGO\n");
        s.append("Repetidas: ").append(c.metrics.rep).append("\n");
        s.append("Pares / ímpares: ").append(c.metrics.even).append(" / ").append(c.metrics.odd).append("\n");
        s.append("Primos: ").append(c.metrics.primes).append("\n");
        s.append("Mágicos: ").append(c.metrics.magics).append("\n");
        s.append("Moldura: ").append(c.metrics.border).append("\n");
        s.append("Soma: ").append(c.metrics.sum).append("\n");
        s.append("Gerados: ").append(r.totalGenerated).append(" | classificados: ").append(r.totalClassified).append("\n\n");
        s.append("TOP 10\n");
        for (int i = 0; i < Math.min(10, r.top20.size()); i++) {
            Candidate x = r.top20.get(i);
            s.append(String.format(Locale.US, "%02d. %s | falha=%.3f | soma=%d\n", i + 1, fmt(x.game), x.failureMean, x.metrics.sum));
        }
        return s.toString();
    }
}
