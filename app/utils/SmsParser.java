package utils;

public class SmsParser {
  
  private String _body;   //What the message said.
  private String _state;  //What we decide to do with the message.
  
  private final String _HELP  = "help";   //Player wants help with something.
  private final String _JOIN  = "join";   //Person wants to join the game.
  private final String _PLAY  = "play";   //Player wants to, or is about to play a game.
  private final String _SCORE = "score";  //Player wants to record a score.

  public SmsParser(String body) {
    this._body = body;
  }
  
  public String getState() {
    return _state;
  }
  
  private void determineState() {
    cibody = _body.toLowerCase();
    
    if (cibody.equals(_HELP)){
      _state = "help";
    }
    else if (cibody.equals(_JOIN)) {
      _state = "join";
    }
    else if (cibody.equals(_PLAY)) {
      _state = "play";
    }
    else if (cibody.equals(_SCORE)) {
      _state = "score";
    }
    else {
      // Default.. assume help?
      _state = "error";
    }
    
  }
  
}
