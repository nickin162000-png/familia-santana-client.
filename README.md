# Familia Santana Client 2.0 (Forge 1.8.9)

## Modulos
Marca, FPS, CPS, Ping, Coordenadas, Direcao, Relogio, Keystrokes (WASD, mouse, espaco),
Armor HUD (item na mao + armadura + durabilidade), Efeitos de pocao,
Toggle Sprint, Zoom (tecla C), Fullbright.

## Teclas (mudaveis em Controles)
- Right Shift: abre o menu para ligar/desligar modulos
- C: zoom (segure)

## Como compilar (jeito mais facil)
1. Baixe o Forge MDK 1.8.9 (1.8.9-11.15.1.2318) em files.minecraftforge.net
2. Extraia o MDK, apague a pasta src dele e copie a pasta src deste projeto
   (opcional: troque o build.gradle pelo deste projeto)
3. Requer JDK 8. Na pasta: gradlew setupDecompWorkspace  e depois  gradlew build
4. O .jar fica em build/libs. Coloque em .minecraft/mods com o Forge 1.8.9 instalado.

## Compilar pelo GitHub (sem instalar nada)
Suba este projeto num repositorio do GitHub. A aba Actions compila sozinha
e o .jar aparece para baixar em "Artifacts" (FamiliaSantanaClient-jar).
