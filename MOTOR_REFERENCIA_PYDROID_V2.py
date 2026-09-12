# -*- coding: utf-8 -*-
"""
LOTOFÁCIL - TODOS OS 230 GRUPOS DE 22 -> MAIS ATRASADO EM 15 PONTOS
-> C(22,15)=170.544 -> FILTROS RÍGIDOS -> MELHOR PROJEÇÃO DE FALHA
PYDROID V2

ENGRENAGEM
1) Lê TODOS os concursos da Lotofácil disponíveis na base.
2) Gera TODOS os grupos de 22 dezenas do universo 01..25:
      C(25,22) = C(25,3) = 230 grupos.
3) Para cada grupo de 22, percorre todo o histórico e mede:
   - quantas vezes o resultado de 15 ficou 100% dentro do grupo (15 pontos);
   - último concurso em que o grupo fez 15;
   - atraso atual desde o último 15;
   - intervalo médio entre ocorrências de 15;
   - maior intervalo histórico entre ocorrências de 15;
   - pressão atual = atraso atual / intervalo médio.
4) Escolhe PRIMEIRO o grupo de 22 com MAIOR ATRASO ATUAL sem fazer 15.
   Desempates: maior pressão, depois maior quantidade histórica de 15.
5) Dentro do grupo vencedor, gera TODAS as C(22,15)=170.544 combinações.
6) Separa somente jogos com os filtros já estabelecidos:
   - exatamente 9 repetidas do último resultado;
   - exatamente 7 pares e 8 ímpares;
   - 5 ou 6 primos;
   - 5 ou 6 números mágicos;
   - exatamente 10 dezenas da moldura;
   - soma entre 201 e 204, inclusive.
7) Para cada jogo classificado, calcula as 10 dezenas que ficaram de fora
   e usa o mesmo MAPA FORTE/FALHA do código anterior, construído com TODO
   o histórico carregado (ex.: 3.779 concursos).
8) O melhor jogo é o classificado cuja FALHAS10 tem a MAIOR projeção
   média de falha. Desempates: maior menor-score das 10 falhas, maior
   força média das 15 mantidas e maior talo médio.

NÚMEROS MÁGICOS: 05 06 07 12 13 14 19 20 21.
MOLDURA: 01 02 03 04 05 06 10 11 15 16 20 21 22 23 24 25.

ESTUDO ESTATÍSTICO. NÃO HÁ GARANTIA DE PREMIAÇÃO.
"""

import os
import re
import math
import time
import itertools
from collections import Counter

APP = "LOTOFÁCIL - 230 GRUPOS22 + ATRASO15 + FILTROS + FALHA - V2"

DOWNLOADS = [
    "/storage/emulated/0/Download",
    "/storage/emulated/0/Downloads",
    os.path.expanduser("~/Download"),
    ".",
]

UNIVERSO = set(range(1, 26))

GRUPO22_PADRAO = (
    1,2,3,4,6,7,8,9,10,11,12,
    13,14,15,17,18,19,20,22,23,24,25
)

PRIMOS = {2,3,5,7,11,13,17,19,23}
MAGICOS = {5,6,7,12,13,14,19,20,21}
MOLDURA = {1,2,3,4,5,6,10,11,15,16,20,21,22,23,24,25}

# Filtros rígidos pedidos.
REPETIDAS_EXATAS = 9
PARES_EXATOS = 7
PRIMOS_MIN = 5
PRIMOS_MAX = 6
MAGICOS_MIN = 5
MAGICOS_MAX = 6
MOLDURA_EXATA = 10
SOMA_MIN = 201
SOMA_MAX = 204


def hr(c="=", n=90):
    print(c*n)


def fmt(nums):
    return " ".join(f"{n:02d}" for n in sorted(nums))


def barra(atual, total, largura=28):
    p = atual / max(1,total)
    p = max(0.0,min(1.0,p))
    cheio = int(largura*p)
    return "[" + "█"*cheio + "░"*(largura-cheio) + "] " + f"{p*100:5.1f}%"


def pasta_download():
    for p in DOWNLOADS:
        if os.path.isdir(p):
            return p
    return "."


def escolher_arquivo(pasta):
    arquivos = sorted(
        x for x in os.listdir(pasta)
        if x.lower().endswith((".txt",".csv"))
    )
    if not arquivos:
        raise RuntimeError("Nenhum TXT/CSV encontrado na pasta Download.")

    hr()
    print("ARQUIVOS DA PASTA DOWNLOAD")
    hr()
    for i,nome in enumerate(arquivos,1):
        print(f"{i:03d} - {nome}")

    while True:
        op = input("\nEscolha a base da LOTOFÁCIL pelo número: ").strip()
        if op.isdigit() and 1 <= int(op) <= len(arquivos):
            return os.path.join(pasta,arquivos[int(op)-1])
        print("Número inválido.")


def escolher_grupo22():
    hr()
    print("GRUPO DE 22 DEZENAS")
    hr()
    print("Grupo padrão:")
    print(fmt(GRUPO22_PADRAO))
    print()
    print("ENTER = usar esse grupo.")
    print("Ou cole outras 22 dezenas separadas por espaço.")

    entrada = input("\n22 dezenas: ").strip()
    if not entrada:
        return tuple(sorted(GRUPO22_PADRAO))

    nums = sorted(set(
        int(x) for x in re.findall(r"\d+", entrada)
        if 1 <= int(x) <= 25
    ))

    if len(nums) != 22:
        raise RuntimeError(
            f"Foram identificadas {len(nums)} dezenas. Precisa de EXATAMENTE 22."
        )

    return tuple(nums)


def ler_base(path):
    texto = None
    for enc in ("utf-8-sig","utf-8","latin-1"):
        try:
            with open(path,"r",encoding=enc) as f:
                texto = f.read()
            break
        except Exception:
            pass

    if texto is None:
        raise RuntimeError("Não consegui abrir a base.")

    resultados = {}

    for linha in texto.splitlines():
        vals = [int(x) for x in re.findall(r"\d+",linha)]
        if len(vals) < 15:
            continue

        concurso = None
        if len(vals) >= 16 and vals[0] > 25:
            concurso = vals[0]
            vals = vals[1:]

        dezenas = [x for x in vals if 1 <= x <= 25]
        if len(dezenas) >= 15:
            jogo = tuple(sorted(dezenas[-15:]))
            if len(set(jogo)) == 15:
                if concurso is None:
                    concurso = len(resultados)+1
                resultados[concurso] = jogo

    if len(resultados) < 50:
        raise RuntimeError("Poucos concursos da Lotofácil foram identificados.")

    return dict(sorted(resultados.items()))



# ======================================================================
# TODOS OS C(25,22) = 230 GRUPOS
# ESCOLHER O QUE ESTÁ HÁ MAIS TEMPO SEM FAZER 15
# ======================================================================

def analisar_todos_grupos22(draws, concursos, pasta):
    total_grupos = math.comb(25,22)
    draw_sets = [set(d) for d in draws]
    ranking = []

    hr()
    print("ETAPA 1 - ANALISANDO TODOS OS 230 GRUPOS DE 22")
    hr()
    print("Critério principal: MAIOR ATRASO ATUAL desde o último 15 pontos.")
    print("Histórico carregado:", len(draws), "concursos")
    print()

    inicio = time.time()
    prox = 0

    for pos, fora3 in enumerate(itertools.combinations(range(1,26),3),1):
        fora = set(fora3)
        grupo22 = tuple(sorted(UNIVERSO - fora))

        ocorrencias = []
        for idx, d in enumerate(draw_sets):
            # O grupo de 22 faz 15 pontos quando nenhuma das 3 excluídas
            # aparece no resultado oficial de 15 dezenas.
            if fora.isdisjoint(d):
                ocorrencias.append(idx)

        vezes15 = len(ocorrencias)

        if ocorrencias:
            ultimo_idx = ocorrencias[-1]
            ultimo_concurso15 = concursos[ultimo_idx]
            atraso_atual = (len(draws)-1) - ultimo_idx

            intervalos = [
                ocorrencias[i] - ocorrencias[i-1]
                for i in range(1,len(ocorrencias))
            ]

            if intervalos:
                media_intervalo = sum(intervalos)/len(intervalos)
                max_intervalo = max(intervalos)
            else:
                media_intervalo = float(len(draws))
                max_intervalo = len(draws)
        else:
            ultimo_idx = -1
            ultimo_concurso15 = None
            atraso_atual = len(draws)
            media_intervalo = float(len(draws))
            max_intervalo = len(draws)

        pressao = atraso_atual / max(1.0, media_intervalo)

        ranking.append({
            "grupo22": grupo22,
            "fora3": tuple(sorted(fora3)),
            "vezes15": vezes15,
            "ultimo_concurso15": ultimo_concurso15,
            "atraso_atual": atraso_atual,
            "media_intervalo": media_intervalo,
            "max_intervalo": max_intervalo,
            "pressao": pressao,
        })

        pct = int(pos*100/total_grupos)
        if pct >= prox:
            vel = pos/max(.001,time.time()-inicio)
            eta = int((total_grupos-pos)/max(.001,vel))
            print(
                "\r" + barra(pos,total_grupos) +
                f" | {pos}/{total_grupos}" +
                f" | ETA {eta}s",
                end="", flush=True
            )
            prox += 5

    print()

    # REGRA PRINCIPAL PEDIDA:
    # 1) maior atraso atual sem 15;
    # 2) em empate, maior pressão x intervalo médio;
    # 3) em novo empate, mais ocorrências históricas de 15.
    ranking.sort(
        key=lambda x:(
            x["atraso_atual"],
            x["pressao"],
            x["vezes15"]
        ),
        reverse=True
    )

    caminho = os.path.join(
        pasta,
        "LOTOFACIL_230_GRUPOS22_ATRASO15_V2.txt"
    )

    with open(caminho,"w",encoding="utf-8") as f:
        f.write("LOTOFÁCIL - TODOS OS 230 GRUPOS DE 22 - ATRASO DE 15 PONTOS\n")
        f.write("="*110 + "\n")
        f.write(f"Concursos estudados: {len(draws)}\n")
        f.write(f"Último concurso da base: {concursos[-1]}\n")
        f.write("Regra: maior atraso atual sem 15; desempate por pressão e vezes15.\n")
        f.write("="*110 + "\n\n")

        for i,x in enumerate(ranking,1):
            ult = x["ultimo_concurso15"] if x["ultimo_concurso15"] is not None else "NUNCA"
            f.write(
                f"{i:03d}. FORA3={fmt(x['fora3'])}"
                f" | GRUPO22={fmt(x['grupo22'])}"
                f" | vezes15={x['vezes15']}"
                f" | último15={ult}"
                f" | atraso={x['atraso_atual']}"
                f" | média_int={x['media_intervalo']:.2f}"
                f" | máx_int={x['max_intervalo']}"
                f" | pressão={x['pressao']:.3f}\n"
            )

    return ranking, caminho


def mostrar_top_grupos22(ranking, qtd=15):
    hr()
    print("TOP GRUPOS DE 22 - MAIOR TEMPO SEM 15 PONTOS")
    hr()
    for i,x in enumerate(ranking[:qtd],1):
        ult = x["ultimo_concurso15"] if x["ultimo_concurso15"] is not None else "NUNCA"
        print(
            f"{i:02d}. FORA {fmt(x['fora3'])}"
            f" | atraso={x['atraso_atual']}"
            f" | último15={ult}"
            f" | vezes15={x['vezes15']}"
            f" | média={x['media_intervalo']:.2f}"
            f" | pressão={x['pressao']:.2f}"
        )

# ======================================================================
# MESMO MAPA FORTE/FALHA DO CÓDIGO ANTERIOR
# ======================================================================

def slope(v):
    n = len(v)
    if n < 2:
        return 0.0
    xm = (n-1)/2.0
    ym = sum(v)/n
    num = sum((i-xm)*(x-ym) for i,x in enumerate(v))
    den = sum((i-xm)**2 for i in range(n))
    return num/den if den else 0.0


def atraso_atual(n,draws):
    atraso = 0
    for d in reversed(draws):
        if n in d:
            return atraso
        atraso += 1
    return len(draws)


def blocos_atraso(n,draws):
    blocos = []
    atual = 0
    for d in draws:
        if n not in d:
            atual += 1
        else:
            if atual:
                blocos.append(atual)
            atual = 0
    if atual:
        blocos.append(atual)
    return blocos


def sequencia_atual(n,draws):
    qtd = 0
    for d in reversed(draws):
        if n in d:
            qtd += 1
        else:
            break
    return qtd


def blocos_sequencia(n,draws):
    blocos = []
    atual = 0
    for d in draws:
        if n in d:
            atual += 1
        else:
            if atual:
                blocos.append(atual)
            atual = 0
    if atual:
        blocos.append(atual)
    return blocos


def taxa(n,janela):
    if not janela:
        return 0.0
    return sum(1 for d in janela if n in d)/len(janela)


def taxa_ponderada(n,janela):
    if not janela:
        return 0.0
    pesos = list(range(1,len(janela)+1))
    return sum((1 if n in d else 0)*p for d,p in zip(janela,pesos))/sum(pesos)


def minmax_map(valores):
    lo = min(valores.values())
    hi = max(valores.values())
    if abs(hi-lo) < 1e-12:
        return {k:0.5 for k in valores}
    return {k:(v-lo)/(hi-lo) for k,v in valores.items()}


def construir_mapa(draws):
    total = len(draws)

    j120 = draws[-min(120,total):]
    j60  = draws[-min(60,total):]
    j30  = draws[-min(30,total):]
    j15  = draws[-min(15,total):]
    ant15 = draws[-min(30,total):-15] if total >= 30 else []
    j8 = draws[-min(8,total):]

    dados = {}

    for n in range(1,26):
        hist = taxa(n,draws)
        r120 = taxa(n,j120)
        r60 = taxa(n,j60)
        r30 = taxa(n,j30)
        r15 = taxa(n,j15)
        r8 = taxa(n,j8)
        pond30 = taxa_ponderada(n,j30)
        anterior15 = taxa(n,ant15) if ant15 else hist
        tendencia = r15-anterior15

        atr = atraso_atual(n,draws)
        ba = blocos_atraso(n,draws)
        atr_med = sum(ba)/len(ba) if ba else 0.0
        atr_max = max(ba) if ba else 0
        pressao_retorno = atr/max(1.0,atr_med)

        seq = sequencia_atual(n,draws)
        bs = blocos_sequencia(n,draws)
        seq_med = sum(bs)/len(bs) if bs else 0.0
        seq_max = max(bs) if bs else 0
        excesso_seq = max(0.0,seq-max(1.0,seq_med))

        dados[n] = {
            "hist":hist,
            "r120":r120,
            "r60":r60,
            "r30":r30,
            "r15":r15,
            "r8":r8,
            "pond30":pond30,
            "tendencia":tendencia,
            "atraso":atr,
            "atraso_med":atr_med,
            "atraso_max":atr_max,
            "retorno":pressao_retorno,
            "seq":seq,
            "seq_med":seq_med,
            "seq_max":seq_max,
            "excesso_seq":excesso_seq,
        }

    nhist = minmax_map({n:dados[n]["hist"] for n in dados})
    n120 = minmax_map({n:dados[n]["r120"] for n in dados})
    n60 = minmax_map({n:dados[n]["r60"] for n in dados})
    n30 = minmax_map({n:dados[n]["r30"] for n in dados})
    n15 = minmax_map({n:dados[n]["r15"] for n in dados})
    npond = minmax_map({n:dados[n]["pond30"] for n in dados})
    ntend = minmax_map({n:dados[n]["tendencia"] for n in dados})
    nret = minmax_map({n:min(dados[n]["retorno"],2.5) for n in dados})
    nexcesso = minmax_map({n:dados[n]["excesso_seq"] for n in dados})

    for n in dados:
        forca = 100.0*(
            0.12*nhist[n] +
            0.12*n120[n] +
            0.10*n60[n] +
            0.16*n30[n] +
            0.12*n15[n] +
            0.13*npond[n] +
            0.10*ntend[n] +
            0.10*nret[n] +
            0.05*(1.0-nexcesso[n])
        )

        queda = 1.0-ntend[n]
        falha = 100.0*(
            0.62*(1.0-forca/100.0) +
            0.18*queda +
            0.12*nexcesso[n] +
            0.08*(1.0-nret[n])
        )

        dados[n]["forca"] = max(0.0,min(100.0,forca))
        dados[n]["falha"] = max(0.0,min(100.0,falha))

    ordem_forca = sorted(dados,key=lambda n:dados[n]["forca"],reverse=True)
    for pos,n in enumerate(ordem_forca):
        nivel = 10-int(pos*10/25)
        nivel = max(1,min(10,nivel))
        dados[n]["nivel"] = nivel
        if nivel >= 9:
            dados[n]["classe"] = "FORTE"
        elif nivel >= 7:
            dados[n]["classe"] = "BOA"
        elif nivel >= 5:
            dados[n]["classe"] = "NEUTRA"
        elif nivel >= 3:
            dados[n]["classe"] = "FRACA"
        else:
            dados[n]["classe"] = "MUITO FRACA"

    return dados


# ======================================================================
# FILTROS RÍGIDOS PEDIDOS
# ======================================================================

def metricas(jogo,ultimo):
    s = set(jogo)
    return {
        "rep":len(s & set(ultimo)),
        "pares":sum(n%2==0 for n in jogo),
        "impares":sum(n%2!=0 for n in jogo),
        "primos":sum(n in PRIMOS for n in jogo),
        "magicos":sum(n in MAGICOS for n in jogo),
        "moldura":sum(n in MOLDURA for n in jogo),
        "soma":sum(jogo),
    }


def passa_filtros(m):
    return (
        m["rep"] == REPETIDAS_EXATAS and
        m["pares"] == PARES_EXATOS and
        m["impares"] == 8 and
        PRIMOS_MIN <= m["primos"] <= PRIMOS_MAX and
        MAGICOS_MIN <= m["magicos"] <= MAGICOS_MAX and
        m["moldura"] == MOLDURA_EXATA and
        SOMA_MIN <= m["soma"] <= SOMA_MAX
    )


# ======================================================================
# PROJEÇÃO DE FALHA DO CANDIDATO
# ======================================================================

def avaliar_por_falha(jogo,mapa):
    falhas10 = tuple(sorted(UNIVERSO-set(jogo)))

    scores_falha = [mapa[n]["falha"] for n in falhas10]
    scores_forca = [mapa[n]["forca"] for n in jogo]

    falha_media = sum(scores_falha)/10.0
    falha_total = sum(scores_falha)
    falha_min = min(scores_falha)
    forca_jogo = sum(scores_forca)/15.0

    # Mesmo princípio do talo do código anterior para as 15 mantidas.
    talo = sum(
        55*mapa[n]["pond30"] +
        20*mapa[n]["r30"] +
        15*max(0.0,mapa[n]["tendencia"])
        for n in jogo
    )/15.0

    return {
        "falhas10":falhas10,
        "falha_media":falha_media,
        "falha_total":falha_total,
        "falha_min":falha_min,
        "forca_jogo":forca_jogo,
        "talo":talo,
    }


# ======================================================================
# GERAR TODAS AS C(22,15) E CLASSIFICAR
# ======================================================================

def gerar_e_classificar(grupo22,draws,mapa,pasta):
    ultimo = draws[-1]
    total = math.comb(22,15)

    classificados = []
    inicio = time.time()
    prox = 0

    caminho_classificados = os.path.join(
        pasta,
        "LOTOFACIL_230GRUPOS22_ESCOLHIDO_CLASSIFICADOS_PADRAO_V2.txt"
    )

    hr()
    print("ETAPA 1 - GERANDO TODAS AS COMBINAÇÕES")
    hr()
    print("GRUPO 22:")
    print(fmt(grupo22))
    print()
    print(f"C(22,15) = {total:,}".replace(",","."),"jogos")
    print()
    print("FILTROS RÍGIDOS:")
    print("- 9 repetidas do último")
    print("- 7 pares / 8 ímpares")
    print("- 5 a 6 primos")
    print("- 5 a 6 mágicos")
    print("- 10 moldura")
    print("- soma 201 a 204")
    print()

    for i,jogo in enumerate(itertools.combinations(grupo22,15),1):
        m = metricas(jogo,ultimo)

        if passa_filtros(m):
            falha = avaliar_por_falha(jogo,mapa)

            classificados.append((
                falha["falha_media"],
                falha["falha_min"],
                falha["forca_jogo"],
                falha["talo"],
                tuple(jogo),
                m,
                falha,
            ))

        pct = int(i*100/total)
        if pct >= prox:
            vel = i/max(.001,time.time()-inicio)
            eta = int((total-i)/max(.001,vel))
            print(
                "\r" + barra(i,total) +
                f" | {i}/{total}" +
                f" | classificados={len(classificados)}" +
                f" | ETA {eta}s",
                end="",flush=True
            )
            prox += 2

    print()

    # Regra principal pedida: MAIOR PROJEÇÃO DE FALHA PRIMEIRO.
    # Só depois vêm os desempates.
    classificados.sort(
        key=lambda x:(x[0],x[1],x[2],x[3]),
        reverse=True
    )

    with open(caminho_classificados,"w",encoding="utf-8") as f:
        f.write("LOTOFÁCIL - JOGOS CLASSIFICADOS NOS FILTROS PEDIDOS\n")
        f.write("="*100 + "\n")
        f.write(f"GRUPO22: {fmt(grupo22)}\n")
        f.write(f"ÚLTIMO: {fmt(ultimo)}\n")
        f.write(f"TOTAL C(22,15): {total}\n")
        f.write(f"TOTAL CLASSIFICADOS: {len(classificados)}\n")
        f.write("FILTROS: rep=9 | pares=7 | ímpares=8 | primos=5..6 | mágicos=5..6 | moldura=10 | soma=201..204\n")
        f.write("="*100 + "\n\n")

        for pos,item in enumerate(classificados,1):
            falha_media,falha_min,forca_jogo,talo,jogo,m,fa = item
            f.write(
                f"{pos:05d}. {fmt(jogo)}"
                f" | falhas={fmt(fa['falhas10'])}"
                f" | falha_med={falha_media:.4f}"
                f" | falha_min={falha_min:.4f}"
                f" | rep={m['rep']}"
                f" | P/I={m['pares']}/{m['impares']}"
                f" | primos={m['primos']}"
                f" | magicos={m['magicos']}"
                f" | moldura={m['moldura']}"
                f" | soma={m['soma']}"
                f" | forca15={forca_jogo:.4f}"
                f" | talo={talo:.4f}\n"
            )

    return classificados,caminho_classificados,total


# ======================================================================
# RELATÓRIO FINAL
# ======================================================================

def salvar_resultado(pasta,base,grupo22,mapa,classificados,total):
    caminho = os.path.join(
        pasta,
        "LOTOFACIL_230GRUPOS22_170544_FILTROS_FALHA_V2_RESULTADO.txt"
    )

    concursos = list(base.keys())
    draws = list(base.values())
    ultimo = draws[-1]

    melhor = classificados[0]
    falha_media,falha_min,forca_jogo,talo,jogo,m,fa = melhor

    with open(caminho,"w",encoding="utf-8") as f:
        A = lambda s="": f.write(str(s)+"\n")

        A("LOTOFÁCIL - 230 GRUPOS22 -> MAIS ATRASADO -> 170.544 -> FILTROS -> MAIOR FALHA")
        A("="*100)
        A(f"Concursos carregados: {len(draws)}")
        A(f"Último concurso: {concursos[-1]}")
        A(f"Último resultado: {fmt(ultimo)}")
        A(f"Grupo 22: {fmt(grupo22)}")
        A(f"Total gerado: {total}")
        A(f"Total que passou nos filtros: {len(classificados)}")
        A()
        A("FILTROS RÍGIDOS")
        A("- 9 repetidas")
        A("- 7 pares / 8 ímpares")
        A("- 5 a 6 primos")
        A("- 5 a 6 mágicos")
        A("- moldura 10")
        A("- soma 201 a 204")
        A()
        A("MELHOR JOGO")
        A("-"*100)
        A(fmt(jogo))
        A()
        A("10 FALHAS PROJETADAS")
        A(fmt(fa["falhas10"]))
        A()
        A(f"Projeção média de falha: {falha_media:.4f}")
        A(f"Menor score dentro das 10 falhas: {falha_min:.4f}")
        A(f"Força média das 15 mantidas: {forca_jogo:.4f}")
        A(f"Talo médio das 15 mantidas: {talo:.4f}")
        A()
        A(f"Repetidas: {m['rep']}")
        A(f"Pares: {m['pares']}")
        A(f"Ímpares: {m['impares']}")
        A(f"Primos: {m['primos']}")
        A(f"Mágicos: {m['magicos']}")
        A(f"Moldura: {m['moldura']}")
        A(f"Soma: {m['soma']}")
        A()
        A("MAPA - ORDEM DE MAIOR PROJEÇÃO DE FALHA")
        A("-"*100)
        for pos,n in enumerate(sorted(mapa,key=lambda x:mapa[x]["falha"],reverse=True),1):
            d = mapa[n]
            A(
                f"{pos:02d}. {n:02d}"
                f" | falha={d['falha']:.4f}"
                f" | força={d['forca']:.4f}"
                f" | classe={d['classe']}"
                f" | atraso={d['atraso']}"
                f" | R30={d['r30']*100:.1f}%"
                f" | R15={d['r15']*100:.1f}%"
            )
        A()
        A("TOP 20 JOGOS CLASSIFICADOS")
        A("-"*100)
        for pos,item in enumerate(classificados[:20],1):
            fm,fmin,fj,tl,g,mm,ffa = item
            A(
                f"{pos:02d}. {fmt(g)}"
                f" | falhas={fmt(ffa['falhas10'])}"
                f" | falha={fm:.4f}"
                f" | primos={mm['primos']}"
                f" | mágicos={mm['magicos']}"
                f" | soma={mm['soma']}"
            )

    return caminho


# ======================================================================
# MAIN
# ======================================================================

def main():
    os.system("clear")

    hr()
    print(APP)
    print("230 GRUPOS DE 22 -> MAIS ATRASADO EM 15 -> 170.544 -> FILTROS -> FALHA")
    hr()

    pasta = pasta_download()
    arquivo = escolher_arquivo(pasta)
    base = ler_base(arquivo)
    draws = list(base.values())
    concursos = list(base.keys())
    ultimo = draws[-1]

    print()
    hr()
    print("BASE CARREGADA")
    hr()
    print("CONCURSOS:",len(draws))
    print("ÚLTIMO CONCURSO:",concursos[-1])
    print("ÚLTIMO RESULTADO:",fmt(ultimo))
    print("TOTAL DE GRUPOS DE 22:",math.comb(25,22))
    print("CADA GRUPO GERA C(22,15):",math.comb(22,15))

    print()
    ranking_grupos, caminho_grupos = analisar_todos_grupos22(
        draws, concursos, pasta
    )

    mostrar_top_grupos22(ranking_grupos,15)

    vencedor = ranking_grupos[0]
    grupo22 = vencedor["grupo22"]

    print()
    hr()
    print("GRUPO DE 22 ESCOLHIDO")
    hr()
    print("GRUPO22:",fmt(grupo22))
    print("3 DEZENAS FORA:",fmt(vencedor["fora3"]))
    print("VEZES QUE FEZ 15 NA HISTÓRIA:",vencedor["vezes15"])
    print("ÚLTIMO CONCURSO COM 15:",vencedor["ultimo_concurso15"])
    print("ATRASO ATUAL SEM 15:",vencedor["atraso_atual"],"concurso(s)")
    print("INTERVALO MÉDIO ENTRE 15:",f"{vencedor['media_intervalo']:.2f}")
    print("MAIOR INTERVALO HISTÓRICO:",vencedor["max_intervalo"])
    print("PRESSÃO ATUAL:",f"{vencedor['pressao']:.3f}")

    print()
    print("Construindo o MAPA FORTE/FALHA com TODO o histórico...")
    mapa = construir_mapa(draws)

    print()
    classificados,caminho_classificados,total = gerar_e_classificar(
        grupo22,draws,mapa,pasta
    )

    if not classificados:
        print()
        hr("!")
        print("NENHUM JOGO PASSOU EM TODOS OS FILTROS.")
        print("O grupo mais atrasado foi encontrado corretamente, mas nenhum de seus")
        print("170.544 jogos encaixou simultaneamente em todos os filtros rígidos.")
        print("Nesse caso NÃO será trocado automaticamente para outro grupo, para não")
        print("mudar a regra principal sem você mandar.")
        hr("!")
        print("\nRANKING DOS 230 GRUPOS SALVO EM:")
        print(caminho_grupos)
        input("\nENTER para encerrar...")
        return

    melhor = classificados[0]
    falha_media,falha_min,forca_jogo,talo,jogo,m,fa = melhor

    print()
    hr()
    print("RESULTADO FINAL")
    hr()
    print("GRUPO22 ESCOLHIDO:",fmt(grupo22))
    print("FORA DO GRUPO22:",fmt(vencedor["fora3"]))
    print("ATRASO DO GRUPO SEM 15:",vencedor["atraso_atual"])
    print("TOTAL GERADO DENTRO DO GRUPO:",total)
    print("TOTAL CLASSIFICADO NOS FILTROS:",len(classificados))
    print()
    print("MELHOR JOGO DE 15:")
    print(">>>",fmt(jogo),"<<<")
    print()
    print("10 FALHAS PROJETADAS:")
    print(">>>",fmt(fa["falhas10"]),"<<<")
    print()
    print("PROJEÇÃO MÉDIA DE FALHA:",f"{falha_media:.4f}")
    print("MENOR SCORE NAS 10 FALHAS:",f"{falha_min:.4f}")
    print("FORÇA MÉDIA DAS 15:",f"{forca_jogo:.4f}")
    print("TALO MÉDIO DAS 15:",f"{talo:.4f}")
    print()
    print("REPETIDAS:",m["rep"])
    print("PARES / ÍMPARES:",m["pares"],"/",m["impares"])
    print("PRIMOS:",m["primos"])
    print("MÁGICOS:",m["magicos"])
    print("MOLDURA:",m["moldura"])
    print("SOMA:",m["soma"])

    print()
    hr()
    print("TOP 10 JOGOS DENTRO DO GRUPO VENCEDOR")
    hr()
    for pos,item in enumerate(classificados[:10],1):
        fm,fmin,fj,tl,g,mm,ffa = item
        print(
            f"{pos:02d}. {fmt(g)}"
            f" | falha={fm:.3f}"
            f" | falhas10={fmt(ffa['falhas10'])}"
            f" | P={mm['primos']}"
            f" | Mág={mm['magicos']}"
            f" | soma={mm['soma']}"
        )

    caminho_resultado = salvar_resultado(
        pasta,base,grupo22,mapa,classificados,total
    )

    # Acrescenta no relatório final os dados do grupo escolhido.
    with open(caminho_resultado,"a",encoding="utf-8") as f:
        f.write("\n\nGRUPO22 ESCOLHIDO ENTRE OS 230\n")
        f.write("="*100 + "\n")
        f.write(f"Grupo22: {fmt(grupo22)}\n")
        f.write(f"Fora3: {fmt(vencedor['fora3'])}\n")
        f.write(f"Vezes15: {vencedor['vezes15']}\n")
        f.write(f"Último15: {vencedor['ultimo_concurso15']}\n")
        f.write(f"Atraso atual: {vencedor['atraso_atual']}\n")
        f.write(f"Intervalo médio: {vencedor['media_intervalo']:.2f}\n")
        f.write(f"Maior intervalo histórico: {vencedor['max_intervalo']}\n")
        f.write(f"Pressão: {vencedor['pressao']:.3f}\n")

    print()
    print("RANKING DOS 230 GRUPOS SALVO EM:")
    print(caminho_grupos)
    print()
    print("TODOS OS JOGOS CLASSIFICADOS DO GRUPO ESCOLHIDO:")
    print(caminho_classificados)
    print()
    print("RELATÓRIO FINAL:")
    print(caminho_resultado)
    print()
    print("CONCLUÍDO.")

    input("\nENTER para encerrar...")


if __name__ == "__main__":
    main()
