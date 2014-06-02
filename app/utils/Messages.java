package utils;

// These are all the possible response messages we're using...
public class Messages {  
  public static String non_member_help() { return "Hello!/r/nIf you'd like to plat Twilio-Devoxx Table Football, SMS this number with 'join (yourname)' and you'll get further instructions."; }
  public static String member_help() { return "Text 'play <teamname>' to play, or just 'play' if you'd like to play a random team, and we'll allocate one for you."; }
  public static String mid_game(String team, String table) { return "You're currently due to be playing a game with team "+team+". To record a score SMS 'score <yourpoints>', e.g., 'score 5'. If something went wrong, SMS 'abort' and we will cancel the game."; }
  public static String abort(String team) { return "We have cancelled your fixture to team "+team+". You'll need to elect to play a new game."; }
  public static String join(String team, String name, String colleague) { return "Hi "+name+", we've paired you up with "+colleague+" as team "+team+". "+member_help(); }
  public static String pending() { return "Thanks for joining the Twilio-Devoxx football tournament! We are waiting for a partner to join to pair you up with. We'll SMS you very shorty with more information."; }
  public static String play(String team, String table) { return "Gametime! You're due to play team "+team+" on table "+table+". So head over and get going!"; }
  public static String score(int teamScore, String team) { return "Thanks. We've recorded your score of "+teamScore+" against "+team+"."; }
}