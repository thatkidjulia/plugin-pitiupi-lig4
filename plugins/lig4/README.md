# Plugin Lig4

O plugin oferece partidas de Lig4 entre duas pessoas na rede pitiupi. Uma pessoa seleciona a outra na lista lateral e escolhe **Desafiar para Lig4**; a pessoa convidada aceita ou recusa. Quem desafia joga com vermelho e começa. A convidada joga com amarelo. O tabuleiro tem 7×6 casas, bloqueia colunas cheias, detecta vitória/empate e permite pedir revanche.

## Como compilar

Na raiz do repositório, execute `ant -f plugins/lig4/build.xml jar`. O JAR gerado em `plugins/lig4/dist/` é listado no catálogo da raiz e será vinculado ao iniciar o aplicativo com `--all`.

## Organização

- `game/Lig4Game.java`: regras e estado do tabuleiro, sem interface gráfica.
- `ui/Lig4Panel.java`: painel Swing e interação com o tabuleiro.
- `net/Lig4Message.java`: convite, resposta e jogadas enviadas pela rede.
- `plugin/Lig4Plugin.java`: ponto de entrada, peer action e coordenação das mensagens.
- `src/META-INF/services/pitiupi.models.Plugin`: registro SPI que permite ao Java encontrar o plugin.
