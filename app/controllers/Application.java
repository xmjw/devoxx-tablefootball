package controllers;

import utils.*;
import models.*;
import play.*;
import play.libs.*;
import play.mvc.*;
import play.data.*;
import views.html.*;
import com.twilio.sdk.verbs.*;
import java.util.*;
import org.codehaus.jackson.node.ObjectNode;


public class Application extends Controller {

  // This will show the leaderboard. 
  public static Result index() {
    return ok(index.render(topFiveTeams(),Pusher.PusherKey()));
  }

  public static Result delete() {
    int items = 0;
    for (Member m : Member.all()) {
      items++;
      m.delete();
    }
    for (Team m : Team.all()) {
      items++;
      m.delete();
    }
    return ok("Deleted "+items+" items from the database.");    
  }

  private static Team find_or_create_team() {
    List<Team> teams = Team.all();
    Team team = null;
    
    //find an empty team...
    for (Team t : teams) {
      if (t.members.size() == 1) {
        team = t;
      }
    }

    //nope. Create a team.
    if (team == null) {
      team = Team.CreateTeam();
    }
    team.save();
    
    // return the team...
    return team;
  }

  private static Member create_member(Member member, String body, String from) {
    member = new Member();
    member.number = from;
    member.name = body; //obviously needs imrpoving...
    member.team = find_or_create_team();
    member.save();
    return member;    
  }

  private static void NoMember(Member member,String state, String body, String from) {
    //Is person asking for help?
    if (state.equals("join")) {
      member = create_member(member, body, from);
      //Create a member. Can we put it in a team?
      if (member.team.members.size() == 2) {
        TwilioNotifier.NewTeam(member.team);
      }
      else {
        TwilioNotifier.TeamWaiting(member);
      }
    }
    else {
      TwilioNotifier.NonMemberHelp(member);
    }
  }

  private static void MemberPlay(Member member) {
    //Find a team that can be played, and isn't this one.
    List<Team> teams = Team.all();
    Team challenged = null;
    Team team = member.team;
    for (Team t : teams) {
      if (t.seeking) {
        challenged = t;
      }
    } 
    
    if (challenged != null) {
      team.play(challenged);
      challenged.play(team);
      team.save();
      challenged.save();
      TwilioNotifier.Play(team,challenged);
      TwilioNotifier.Play(challenged,team);
    }
    else {
      TwilioNotifier.NoPlay(team);
    }
  }

  private static void GameOver(Team winner, Team loser) {
    winner.won();
    loser.lost();
    winner.save();
    loser.save();
    TwilioNotifier.Win(winner, loser);
    TwilioNotifier.Loss(loser, winner);
  }

  private static void GameDraw(Team team, Team against) {
    team.tempScore = 0;
    against.tempScore = 0;
    team.save();
    against.save();
    TwilioNotifier.Draw(team,against);
  }

  private static void MemberScore(Member member) {
    // Log a score for a game.
    Team team = member.team;
    Team against = Team.find.byId(team.playing_against);

    //Both teams have registered a score!
    if (against.tempScore != -1) {
      if (team.tempScore > against.tempScore) GameOver(team,against); //Member wins.
      else if (team.tempScore < against.tempScore) GameOver(against,team); //Against wins.
      else GameDraw(team, against); // a draw!
    }
    else TwilioNotifier.ScorePending(team);
  }

  private static void MemberAbort(Member member) {
    //Something went wrong. Cancel and reset to non-game playing.
    Team team = member.team;
    if (team.playing) {
      Team against = Team.find.byId(team.playing_against);
      team.abort();
      against.abort();
      team.save();
      against.save();
      TwilioNotifier.Abort(team);
      TwilioNotifier.Abort(against);
    }
  }

  private static void Member(Member member, String state) {
    //Already a member, and we have that in variable member.
    if (state.equals("play")) {
      MemberPlay(member);
    }
    else if (state.equals("score")) {
      MemberScore(member);
    }
    else if (state.equals("abort")) {
      MemberAbort(member);
    }
    else {
      TwilioNotifier.MemberHelp(member);
    } 
  }

  private static void GameLogic(Member member,String state, String body, String from) {
    if (member == null) {
      NoMember(member, state, body, from);
    }
    else {
      Member(member, state);
    }
  }

  // This will handle all incoming SMS...
  public static Result sms() {
    //final Map<String, String[]> values = request().body().asFormUrlEncoded();
    DynamicForm requestData = form().bindFromRequest();
    String from = requestData.get("From");
    String to   = requestData.get("To");
    String body = requestData.get("Body");

    Logger.info("We have just received some data! From: '"+from+"' To: '"+to+"' Body: '"+body+"'.");

    // Get a member and a status for the message.
    Member member = Member.findByNumber(from);
    String state = new SmsParser(body).getState();
    TwiMLResponse twiml = new TwiMLResponse();
    
    // Handle everything..
    GameLogic(member,state, body, from);
    
    //Might as well do this whenever we get something happening...
    pushTopFive();
    
    //To simplify branching we will send SMS notifications with the REST API.
    return ok("<Response></Response>").as("text/xml");
  }

  private static void pushTopFive() {
    List<Team> teams = topFiveTeams();
   
    //Put each of the teams into a Json object.   
    ObjectNode data = Json.newObject();
    int i=1;
    for (Team team : teams) {
      ObjectNode node = Json.newObject();
      node.put("name", team.name);
      node.put("score", team.wins);
      data.put(Integer.toString(i),node);
      i++;
    }
    // Put the set of teams into the data...
    ObjectNode result = Json.newObject();
    result.put("data",data);   
    Logger.info("About to push Score update: "+result.toString());
    Pusher.send(result.toString(),"update_scores");
  }

  //Work out who the top 5 teams are...
  private static List<Team> topFiveTeams() {
    List<Team> teams = Team.all();
    Collections.sort(teams, new TeamComparator());
    int max = (teams.size() > 5 ? 5 : teams.size());
    return teams.subList(0,max);
  }

  //Wrapper to handle errors we twiml.append()
  private static TwiMLResponse safelyAppendElement(TwiMLResponse twiml, Verb verb) {
    try {
      twiml.append(verb);
    } 
    catch (Exception e) {
      Logger.error("Encountered a problem adding a Verb ("+verb.toXML()+") to the Response.");
    }
    return twiml;
  }
}