package utils;

import play.*;

public class SmsParser {
  
  private String body;   //What the message said.
  private State state;
    
  private final String HELP  = "help";   //Player wants help with something.
  private final String JOIN  = "join";   //Person wants to join the game.
  private final String PLAY  = "play";   //Player wants to, or is about to play a game.
  private final String SCORE = "score";  //Player wants to record a score.
  private final String ABORT = "abort"; //Can't play that game...
  private final String QUIT = "quit"; //Player wants to exit the league...

  public SmsParser(String sms_body, String from) {
    body = sms_body;
    determineState(from);
  }
  
  public State getState() {
    return state;
  }
    
  private void determineState(String from) {
    String cibody = body.toLowerCase().replaceAll("\\s+","");
    
    if (cibody.contains(HELP)){
      state = new State("help",from);
    } else if (cibody.startsWith(JOIN)) {
      state = new State("join",from);
    } else if (cibody.equals(PLAY)) {
      state = new State("play",from);
    } else if (cibody.contains(SCORE)) {
      
      try { 
        int data  = Integer.parseInt(cibody.replace(SCORE,""));
        state = new State("score",from,data);
      } catch (Exception e) {
        Logger.error("An error occured passing a score!");
        state = new State("error",from);
      }
    } else if (cibody.contains(QUIT)) {
      state = new State("quit",from);
    } else if (cibody.equals(ABORT)) {
      state = new State("abort",from);
    } else {
      // Default.. assume help?
      state = new State("help",from);
    }
    
  }
  
}
