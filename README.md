# Loto Fácil Campeão - 230 Grupos de 22 - V2

Aplicativo Android em Java puro, sem AndroidX.

## Motor preservado

A estrutura matemática do V1 foi mantida:

1. Carrega o TXT histórico da Lotofácil.
2. Analisa os 230 grupos possíveis de 22 dezenas.
3. Escolhe o grupo com maior atraso atual sem fazer 15 pontos.
4. Constrói o mesmo mapa forte/falha do PyDroid V2.
5. Gera as 170.544 combinações de 15 do grupo escolhido.
6. Aplica o filtro selecionado.
7. Classifica primeiro pela maior projeção média de falha das 10 dezenas de fora, mantendo os mesmos desempates.
8. Gera PDF com jogo verde, falhas vermelhas e resumo.

## Dois filtros

### Filtro fixo
- 9 repetidas
- 7 pares / 8 ímpares
- 5 a 6 primos
- 5 a 6 mágicos
- moldura 10
- soma 201 a 204

### Filtro alternativo
O usuário informa:
- repetidas
- pares
- ímpares
- primos (ex.: `6` ou `5-6`)
- mágicos (ex.: `5` ou `5-6`)
- moldura
- soma mínima
- soma máxima

O exemplo inicial da tela vem preenchido com 8 repetidas, 8 pares, 7 ímpares, 6 primos, 5 mágicos, moldura 9 e soma 192 a 210.

## Interface
Dashboard roxo e branco, trevo branco ao fundo, botões separados para filtro fixo e alternativo.

## PDF
O PDF do último jogo calculado mostra:
- filtro utilizado;
- grupo 22 vencedor;
- volante 01-25;
- 15 dezenas em verde;
- 10 falhas em vermelho;
- resumo do padrão e scores.

## Validação
O filtro fixo foi comparado contra o núcleo V1 em uma base determinística de 400 concursos. Grupo 22, quantidade de classificados, melhor jogo e scores ficaram idênticos.
