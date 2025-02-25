package cz.vse.jokiel.model;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

import java.util.Scanner;

/**
 * Třída implementující příkaz pro pohyb mezi herními lokacemi.
 *
 * @author Jarmila Pavlíčková
 * @author Luboš Pavlíček
 * @author Jan Říha
 * @author Matyáš Jokiel
 * @version LS 2020
 */
public class CommandMoveGraphic implements ICommand {
    private static final String NAME = "jedGraphic";

    private GamePlan plan;
    private Game game;
    public Thread taskThread;

    /**
     * Konstruktor třídy.
     *
     * @param plan odkaz na herní plán s aktuálním stavem hry
     */
    public CommandMoveGraphic(GamePlan plan, Game game) {
        this.plan = plan;
        this.game = game;
    }

    /**
     * Metoda se pokusí přesunout hráče do jiné lokace. Pokud to nejde vrátí chybové hlášení
     *
     * @param /kam se chce hráč přesunout
     * @return informace pro hráče, které se vypíšou na konzoli
     */
    @Override
    public String process(String... parameters) {
        if (parameters.length == 0) {
            return "Musíš zadat kam chceš jet.";  // Pokud hráč nezadá žádný parametr (cíl cesty)
        } else if (parameters.length > 1) {
            return "Musíš zadat jen jeden cíl.";  // Pokud hráč zadá více parametrů
        }

        // Název cílové lokace si uložíme do pomocné proměnné
        String exitName = parameters[0];

        // Zkusíme získat odkaz na cílovou lokaci
        Area exitArea = plan.getCurrentArea().getExitArea(exitName);

        if (exitArea == null || !exitArea.isAccessible()) {
            return "Tam odsud jet nemůžes!";  // Pokud hráč zadal název lokace, která nesousedí s aktuální
        }

        if (!plan.getBackpack().isInStorage("Odznak")) {
            return "Nemáš u sebe odznak. Takhle by tě nebral nikdo vážně.";
        }

        if (exitName.equals("Skrys") && !plan.getBackpack().isInStorage("Zbran")) {
            return "Možná by sis měl vzít zbraň. Nevíš co tě tam může potkat.";
        }

        if (exitName.equals("Skrys") && plan.getBackpack().isInStorage("Zbran") && !plan.getShootout()) {
            plan.setCurrentArea(exitArea);
            plan.setTrueWasInArea(exitArea);

            return shootOut();
        }


        if (exitName.equals("Policejni_stanice") && plan.getArea("Skrys").isAccessible() && plan.getNPC("Pachatel").getDialog(1).wasUsed()) {
            game.setGameOver(true);
            return "Dobrá práce. Ke všemu se přiznal.";
        }


        // Změníme aktuální lokaci (přesuneme hráče) a vrátíme popis nové lokace
        plan.setCurrentArea(exitArea);
        plan.setTrueWasInArea(exitArea);
        return exitArea.getFullDescription();
    }

    /**
     * Metoda shootOut představuje přestřelku s pachatelem v poslední lokaci.
     * Je ovlivněna obtížností hry. Hráč má od 5ti sekund až do 15ti sekund na to
     * aby napsal do konzole "pouzij Zbran", čímž zastaví odpočet a střelí pachatele.
     * Když to nestihne včas, tak hráč umírá a končí hra. Má na výběr, jestli chce poslední část zkusit znovu.
     */
    /*private String shootOut()
    {
        
        Thread thre1 = new Thread(new NewThreadGetInput());
        
        game.getController().setTextOutput("Vykopnul jsi dveře. Vidíš chlapa jak vytahuje pistol. Střílej! Nebo je po tobě.");
        
        thre1.start();
        
        for(int i = game.getDifficulty()*5; i > 0; i--)
        {
            game.getController().setTextOutput(""+i);
            
            if(!thre1.isAlive())
            {
                plan.setShootout(true);
                return "Postřelil jsi pachatele do nohy. Je na zemi.\nZpoutej ho a odvez na stanici. Kluci ho vyslechnou a dají mu náplast.\n";
            }
            try{waitSec(1000);}catch(InterruptedException e){game.getController().setTextOutput("");}
        }
        
        thre1.interrupt();
        game.getController().setTextOutput("Boooom! Cítíš bodavou bolest v hrudníku. Padáš k zemi. Tma.");
        
        String input = "";
        while(!(input.equals("Y") || input.equals("N")))
        {
            game.getController().setTextOutput("Chceš druhou šanci ? (Y/N)");
            input = readLine();
        }
        switch(input)
        {
            case "Y": shootOut();
            default: game.setGameOver(true);return "";
        }
        
    }*/
    private String shootOut() {
        game.getController().setTextOutput("Vykopnul jsi dveře. Vidíš chlapa jak vytahuje pistol. Střílej! Nebo je po tobě.");
        taskThread = new Thread(new Runnable() {
            @Override
            public void run() {
        for (int i = game.getDifficulty() * 5; i > 0; i--) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                game.getController().setTextOutput("");
            }
            final int print = i;
            if(plan.getShootout())
            {taskThread.stop();}
            Platform.runLater(() -> game.getController().setTextOutput("" + print));
        }
        };});
        taskThread.start();

        return "";
    }




    /**
     * Metoda vrací název příkazu tj.&nbsp; slovo {@value NAME}.
     *
     * @return název příkazu
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Pomocná metoda pro čtení příkazů z konzole.
     *
     * @return řádek textu z konzole
     */
    private String readLine() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("> ");
        return scanner.nextLine();
    }

}
