## Memória

### Leak Canary

Usando Leak Canary, foi detectado um memory leak referente à activity com o PreferenceFragment, SettingsActivity. Os arquivos gerados pelo LeakCanary citam problemas com thread. Um possível problema que estivesse causando isso foi por ter registrado o listener do SharedPreference no onCreate do PreferenceFragment, mas sem haver o unregister para ele, e assim o leak podia acontecer em qualquer tela, inclusive fora do aplicativo (segundo plano). Com o unregister sendo chamado no método onStop do Fragment, não ouve sucesso em resolver o memory leak, e pondo no onDestroy também não foi resolvido.

### Android Profiler

Android do aparelho: 6.0

Analisando as imagens salvas na pasta "android-profiler", em relação à memória, temos os seguintes valores máximos atingidos:

+ Dando scroll na lista de episódios, baixando apenas um e colocando para tocar, o espaço máximo não foi além de 40,87mb (aos 44s, provavelmente enquanto baixava o episódio).

  - O maior uso total de memória percebido gira em torno dos 35mb depois de ter baixado o episódio, sem haver com prometimento em relação ao uso de espaço.

+ Cenário de troca de feed via SharedPreference e 4 downloads simultâneos, além de ir novamente para EpisodeDetailsActivity e dar scroll na lista

  - Com 1m32s, o uso de memória começa a crescer à medida em que os episódios vão sendo colocados para baixar, atingindo os 48mb com mais de 3m de funcionamento do aplicativo. Um cenário como esse não é problemático com celulares com quantidade razoável de memória, porém é algo a ser melhor observado em aparelhos com quantidade crítica de memória.  