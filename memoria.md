## Memória

### Leak Canary

Usando Leak Canary, foi detectado um memory leak referente à activity com o PreferenceFragment, SettingsActivity. Os arquivos gerados pelo LeakCanary citam problemas com thread. Um possível problema que estivesse causando isso foi por ter registrado o listener do SharedPreference no onCreate do PreferenceFragment, mas sem haver o unregister para ele, e assim o leak podia acontecer em qualquer tela, inclusive fora do aplicativo (segundo plano). Com o unregister sendo chamado no método onStop do Fragment, não ouve sucesso em resolver o memory leak, e pondo no onDestroy também não foi resolvido.

### Android Profiler

Android do aparelho: 6.0

Analisando as imagens salvas na pasta "android-profiler", em relação à memória, temos os seguintes valores máximos atingidos:

+ Dando scroll na lista de episódios, baixando apenas um e colocando para tocar, o espaço máximo não foi além de 40,87mb (aos 44s, provavelmente enquanto baixava o episódio).
+ Cenário de troca de feed via SharedPreference e 4 downloads simultâneos, além de ir novamente para EpisodeDetailsActivity e dar scroll na lista