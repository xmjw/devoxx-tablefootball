package utils;

import models.*;

// These are all the possible response messages we're using...
public class Messages {  
  //In use...
  public static String NonMemberHelp() { return "Hello! If you'd like to play Twilio-Devoxx Table Football, SMS this number with 'join' and you'll get further instructions."; }
  public static String MemberHelp() { return "Text 'play <teamname>' to play, or just 'play' if you'd like to play a random team, and we'll allocate one for you."; }
  public static String Win(Team against) { return "Congratulations! You won your match against "+against.name+". It certainly isn't over! Text back 'play' to find your next opponent."; }
  public static String Loss(Team against) { return "Bad luck! You lost your match against "+against.name+". Don't think it's all over? Text back 'play' and find your next opponent."; }
  public static String Draw(Team against) { return "Wow! You drew against team "+against.name+"! Oh well. Best play one more ball and see who wins that! Text back 'score 1' if you win, 'score 1' if you lose... Good luck..."; }
  public static String Play(Team against) { return "It's almost kick off! You've been fixed up to play "+against.name+"! Head to the Football Tables to get started. Text back 'help' if you're having problems."; }
  public static String NoPlay() { return "We're sorry, we couldn't find a team to match you up against. Why not play a friendly with your team mate? Text back 'play' in a few minutes to try and find another opponent."; }
  public static String ScorePending() { return "Thanks! We're just waiting for your opponent to confirm their score so we can declare a winner."; }
  public static String Abort() { return "You current match has been cancelled. Text back 'play' to find an opponent."; }
  public static String TeamWaiting() { return "Thanks for joining the Twilio-Devoxx Table Football Tournament. We're just trying to find you a partner for your team. We'll SMS you when we have someone and you can start playing!"; }
  public static String NewTeam(Team team) { return "Great news! You're now one half of team "+team.name+"! Text 'play' when you want to play a game, or we'll text you when you're randomly selected to play someone else. If you get stuck, text back with 'help'."; }
}