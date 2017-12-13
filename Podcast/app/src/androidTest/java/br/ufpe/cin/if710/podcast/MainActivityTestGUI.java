package br.ufpe.cin.if710.podcast;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.espresso.*;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toolbar;

import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.suppliers.TestedOn;
import org.junit.runner.RunWith;

import br.ufpe.cin.if710.podcast.ui.MainActivity;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagKey;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Milena on 13/12/2017.
 */

@RunWith(AndroidJUnit4.class)
public class MainActivityTestGUI {
    @Rule
    public final ActivityTestRule<MainActivity> main = new ActivityTestRule<MainActivity>(MainActivity.class,true);

    @Test
    public void scrollNaTela(){
        onView(withId(R.id.items)).perform(pressKey(KeyEvent.KEYCODE_DPAD_DOWN),
                pressKey(KeyEvent.KEYCODE_DPAD_DOWN)).check(new SelecionarListaAssertion(1));
    }

    @Test
    public void clicarElemento() {
        onData(anything())
                .inAdapterView(withId(R.id.items))
                .atPosition(9)
                .perform(click());
        //vai para activity contendo os detalhes do episódio após selecionar
        onView(withId(R.id.titulo_epi)).check(matches(withText(R.string.teste_gui_titulo))); //só funciona se for o podcast do Não Salvo
    }

    /*@Test
    public void trocarFeed(){
       onView(withId(R.menu.menu_main)).perform();
       onView(withTagKey(R.string.link_feed)).check(matches(withText(R.string.feed_link)));
    }*/

    static class SelecionarListaAssertion implements ViewAssertion{
        private final int position;

        SelecionarListaAssertion(int position) {
            this.position = position;
        }

        @Override
        public void check(View view, NoMatchingViewException noViewFoundException) {
            Assert.assertTrue(view instanceof ListView);
            Assert.assertEquals(position,((ListView)view).getSelectedItemPosition());
        }
    }
}
