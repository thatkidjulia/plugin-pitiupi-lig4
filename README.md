# Lig4 — Plugin para a plataforma Pitiupi

Plugin desenvolvido como parte de um trabalho da disciplina de **Redes de Computadores**. Ele implementa o jogo Lig4 para duas pessoas conectadas pela rede usando a plataforma Pitiupi.

## Sobre a Pitiupi

A Pitiupi é uma ferramenta P2P acadêmica para práticas de Redes de Computadores. Sua base foi desenvolvida por estudantes do laboratório Alice, no contexto do projeto da UFSJ. A plataforma permite que os participantes se encontrem na rede e oferece uma arquitetura de plugins para criar atividades em grupo.

## Como jogar

1. Abra a Pitiupi nos dois computadores e entre na mesma sala, usando nomes diferentes.
2. Na lista de usuários, clique com o botão direito no nome da outra pessoa e escolha **Desafiar para Lig4**.
3. A pessoa convidada aceita ou recusa o convite.
4. Quando aceito, quem convidou começa com as peças amarelas; quem aceitou joga com as azuis. Cada participante só pode jogar na própria vez.
5. Ao terminar, o vencedor pode pedir uma revanche.

O tabuleiro tem 7 colunas e 6 linhas. Vence quem alinhar quatro peças na horizontal, vertical ou diagonal. A partida termina em vitória ou empate.

## Conceitos de redes utilizados

- **Comunicação P2P:** depois de encontrados na sala, os dois jogadores trocam dados diretamente, sem um servidor central para conduzir a partida.
- **TCP unicast:** o convite, a resposta de aceitação ou recusa e cada jogada são enviados diretamente ao endereço do oponente. O TCP mantém a ordem dos dados e verifica a entrega na conexão.
- **UDP multicast da plataforma:** a Pitiupi usa multicast para anunciar e descobrir participantes na rede. O jogo usa essa presença para permitir que uma pessoa escolha quem desafiar.
- **Protocolo da aplicação:** as mensagens do plugin identificam o tipo de ação, a partida e, no caso de uma jogada, a coluna escolhida. Cada instância aplica a jogada recebida ao seu próprio tabuleiro.

## Compilar e executar no Windows

É necessário ter o JDK e o Apache Ant instalados e disponíveis no `PATH`. Abra o Prompt de Comando na pasta raiz do projeto e execute:

```bat
ant -f plugins\lig4\build.xml jar
scripts\pitiupi.cmd run --all
```

O primeiro comando compila o plugin. O segundo compila e inicia a Pitiupi com os plugins listados no catálogo do projeto.

## Estrutura do plugin

- `src/pitiupi/lig4/game/Lig4Game.java`: regras e estado do tabuleiro.
- `src/pitiupi/lig4/ui/Lig4Panel.java`: interface gráfica e interação da partida.
- `src/pitiupi/lig4/net/Lig4Message.java`: estrutura das mensagens trocadas entre os jogadores.
- `src/pitiupi/lig4/plugin/Lig4Plugin.java`: integração com a Pitiupi, convites e coordenação das mensagens.
- `src/META-INF/services/pitiupi.models.Plugin`: registro SPI usado pelo Java para localizar o plugin.

## Contexto acadêmico

Este plugin demonstra conceitos de comunicação entre aplicações em rede e integração modular com uma plataforma P2P. Foi pensado como material de aprendizagem para um trabalho de Redes de Computadores.
