#Testes

##GUI##

####Main Activity####

Foram feitos dois testes nesta activity, os quais:

01. Para verificar, após o scroll, se o item clicado se encontra na posição 1: 

    @Test
    public void scrollNaTela(){
        onView(withId(R.id.items)).perform(pressKey(KeyEvent.KEYCODE_DPAD_DOWN),
                pressKey(KeyEvent.KEYCODE_DPAD_DOWN)).check(new SelecionarListaAssertion(1));
    }

02. Para verificar se o título do item da posição 9 da ListView corresponde ao que está armazenado no xml de strings (pasta res), quando passa para a activity EpisodeDetailActivity:

	@Test
    public void clicarElemento() {
        onData(anything())
                .inAdapterView(withId(R.id.items))
                .atPosition(9)
                .perform(click());
        //vai para activity contendo os detalhes do episódio após selecionar
        onView(withId(R.id.titulo_epi)).check(matches(withText(R.string.teste_gui_titulo))); //só funciona se for o podcast do Não Salvo
